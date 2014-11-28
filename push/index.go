package pushmessager

import (
  "appengine"
  "appengine/datastore"
  "appengine/urlfetch"
//	"appengine/user"

  "bytes"
  "fmt"
  "html/template"
  "io"
  "net/http"
)

var debug bool = false
var indexTemplate = template.Must(template.New("index").Parse(pushForm))
var responseTemplate = template.Must(template.New("response").Parse(responseForm))

const (
  pushForm = `
          <html>
              <body>
                </p>
                <p>Use the project-ID: 1086395265343 for itbooks push-messages.</p></p>
                <form action="/response" method="post">
                    <div><textarea name="book_id" rows="1" cols="60" placeholder="ID">1539580363</textarea></div>
                    <div><textarea name="title" rows="1" cols="60" placeholder="ID">Developing Android Applications with Flex 4.5</textarea></div>
                    <div><textarea name="desc" rows="3" cols="60" placeholder="Message">Building Android Applications with ActionScript.</textarea></div>
                    <div><textarea name="image" rows="3" cols="60" placeholder="Message">http://s.it-ebooks-api.info/3/developing_android_applications_with_flex_4.5.jpg</textarea></div>
                    <div><input type="submit" value="PUSH" /></div>
                </form>
            <form action="/dela" method="post">
              <div><input type="submit" value="REMOVE ALL USERS"/></div>
            </form>
            </body>
          </html>
        `
  responseForm = `<html><body><form action="/" method="post">{{.}}</p><div><input type="submit" value="Repush"></div></form></body></html>`
)

//func main() {
//}

func init() {
  http.HandleFunc("/", handleRoot)
  http.HandleFunc("/insert", handleInsert)
  http.HandleFunc("/del", handleDelete)
  http.HandleFunc("/response", handleResponse)
  http.HandleFunc("/dela",handleDeleteAllUsers)
}
/*
func login(_w http.ResponseWriter, _r *http.Request) {
  c := appengine.NewContext(_r)
  u := user.Current(c)
  if u == nil {
    url, err := user.LoginURL(c, _r.URL.String())
    if err != nil {
      http.Error(_w, err.Error(), http.StatusInternalServerError)
      return
    }
    _w.Header().Set("Location", url)
    _w.WriteHeader(http.StatusFound)
  }
}
*/
func handleRoot(_w http.ResponseWriter, _r *http.Request) {
//	login(_w, _r)
  indexTemplate.Execute(_w, nil)
}

func handleResponse(_w http.ResponseWriter, _r *http.Request) {
  defer func() {
    if err := recover(); err != nil {
      fmt.Fprintf(_w, "Nobody to be pushed")
    }
  }()

  pushMessage := new(PushMessage)
  pushMessage.Push(_r)
  fmt.Fprintf(_w, "%v==========>", len(otherClients))
  if pushMessage.err != nil {
    responseTemplate.Execute(_w, fmt.Sprintf("Some error from server: %s -->", pushMessage.err.Error()))
  }
  responseTemplate.Execute(_w, pushMessage.pushed)
  if debug {
    pushMessage.response.Write(_w)
  }
}

func handleDeleteAllUsers(_w http.ResponseWriter, _r *http.Request) {
  cxt := appengine.NewContext(_r)
        q := datastore.NewQuery("OtherClient")
        clients := make([]OtherClient, 0)
        keys, _ := q.GetAll(cxt, &clients)
        datastore.DeleteMulti(cxt,keys)

        //prints all data after deleting
        keys, _ = q.GetAll(cxt, &clients)
        otherClients  = nil
        fmt.Fprintf(_w, "rest:%v, %v", len(keys), len(otherClients))
}

type OtherClient struct {
  PushID string
}

func handleInsert(_w http.ResponseWriter, _r *http.Request) {
  cookies := _r.Cookies()
  otherClient := &OtherClient{PushID:cookies[0].Value}
  cxt := appengine.NewContext(_r)
        datastore.Put(cxt, datastore.NewIncompleteKey(cxt, "OtherClient", nil), otherClient)
  fmt.Fprintf(_w, otherClient.PushID )
}

func loadClients(_r *http.Request) (clients []OtherClient) {
        cxt := appengine.NewContext(_r)
        q := datastore.NewQuery("OtherClient")
        clients = make([]OtherClient, 0)
        q.GetAll(cxt, &clients)
  return
}

func handleDelete(_w http.ResponseWriter, _r *http.Request) {
        cxt := appengine.NewContext(_r)
  cookies := _r.Cookies()
        q := datastore.NewQuery("OtherClient").Filter("PushID =", cookies[0].Value)
        clients := make([]OtherClient, 0)
        keys, _:= q.GetAll(cxt, &clients)
        datastore.DeleteMulti(cxt,keys);
}


type BreakingNews struct {
  book_id string
  title   string
  desc string
  image string
}

type Message struct {
  registrationIDs       []string
  collapseKey           string
  data                  *BreakingNews
  delayWhileIdle        string
  timeToLive            string
  restrictedPackageName string
  dryRun                string
}

type Request struct {
  API          string
  API_KEY      string
  CONTENT_TYPE string
  message      *Message
}

type PushMessage struct {
  err      error
  pushed   string
  response *http.Response
  request  *http.Request
}

func (this *PushMessage) newRequest(_msg *Message) *Request {
  return &Request{"https://android.googleapis.com/gcm/send", "key=AIzaSyDnLbS3DW6U7vaCPhzm4hCvlT_KtHb8pe4", "application/json", _msg}
}

func (this *PushMessage) newBreakingNews(_book_id string, _title string, _content string, _type string) *BreakingNews {
  return &BreakingNews{_book_id, _title, _content, _type}
}

func (this *PushMessage) newMessage(_regID []string, _breakingNews *BreakingNews) *Message {
  return &Message{registrationIDs: _regID, data: _breakingNews}
}

func (this *PushMessage) body(_r *Request) (reader io.Reader) {
  ids := ""
  if otherClients != nil {
    for _, client := range otherClients {
      ids += fmt.Sprintf(`"%s",`, client.PushID)
    }
  }
  if len(_r.message.registrationIDs[0]) != 0{
      ids += fmt.Sprintf(`"%s"`, _r.message.registrationIDs[0])
  } else {
    ids = ids[:len(ids)-1]
  }

  this.pushed = fmt.Sprintf(`{"registration_ids" : [%s],"data" : {"book_id": %s, "title": "%s", "desc": "%s", "image": "%s"}}`, ids, _r.message.data.book_id , _r.message.data.title, _r.message.data.desc, _r.message.data.image)
  reader = bytes.NewBufferString(this.pushed)
  return
}

func (this *PushMessage) send(_r *Request) {
  req, err := http.NewRequest("POST", _r.API, this.body(_r))
  if err != nil {
    this.err = err
  } else {
    req.Header.Add("Authorization", _r.API_KEY)
    req.Header.Add("Content-Type", _r.CONTENT_TYPE)
    c := appengine.NewContext(this.request)
    //client := &http.Client{}
    client := urlfetch.Client(c)
    this.response, this.err = client.Do(req)
  }
}

var otherClients []OtherClient

func (this *PushMessage) Push(_r *http.Request) {
  otherClients = loadClients(_r)
  this.request = _r
  b := this.newBreakingNews(this.request.FormValue("book_id"), this.request.FormValue("title"), this.request.FormValue("desc"), this.request.FormValue("image"))
  m := this.newMessage([]string{this.request.FormValue("registerID")}, b)
  r := this.newRequest(m)
  this.send(r)
}
