package itbooks

import (
	"appengine"
	"appengine/datastore"
	"appengine/urlfetch"

	"bytes"
	"fmt"
	"html/template"
	"io"
	"net/http"
	"encoding/json"
	"io/ioutil"
)

var debug bool = false
var indexTemplate = template.Must(template.New("index").Parse(pushForm))
var responseTemplate = template.Must(template.New("response").Parse(responseForm))

const (
	pushForm = `
          <html>
              <body>
                </p>
                <p>Project itbooks push-messages.</p></p>
                <form action="/response" method="post">

                    <div><textarea name="Name" rows="1" cols="160" placeholder="Name"></textarea></div>
										<div><textarea name="Author" rows="1" cols="160" placeholder="Author"></textarea></div>
										<div><textarea name="Size" rows="1" cols="160" placeholder="Size"></textarea></div>
										<div><textarea name="Pages" rows="1" cols="160" placeholder="Pages"></textarea></div>
										<div><textarea name="Link" rows="1" cols="160" placeholder="Link"></textarea></div>
										<div><textarea name="ISBN" rows="1" cols="160" placeholder="ISBN"></textarea></div>
										<div><textarea name="Year" rows="1" cols="160" placeholder="Year"></textarea></div>
										<div><textarea name="Publisher" rows="1" cols="160" placeholder="Publisher"></textarea></div>
										<div><textarea name="CoverUrl" rows="1" cols="160" placeholder="CoverUrl"></textarea></div>
                    <div><textarea name="Description" rows="60" cols="160" placeholder="Description"></textarea></div>

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
//	http.HandleFunc("/dela", handleDeleteAllUsers)
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
	datastore.DeleteMulti(cxt, keys)

	//prints all data after deleting
	keys, _ = q.GetAll(cxt, &clients)
	otherClients = nil
	fmt.Fprintf(_w, "rest:%v, %v", len(keys), len(otherClients))
}

type OtherClient struct {
	UID string `json:"uid"`
	PushID string `json:"pushId"`
}

type Error string

func (e Error) Error() string {
	return string(e)
}


func handleInsert(w http.ResponseWriter, r *http.Request) {
	cxt := appengine.NewContext(r)
	defer func() {
		if err := recover(); err != nil {
			cxt.Errorf("handleInsert: %v", err)
			w.Header().Set("Content-Type", "application/json")
			fmt.Fprintf(w, `{"status":%d}`, 300)
		}
	}()

	pOtherClient := new(OtherClient)
	if bytes, e := ioutil.ReadAll(r.Body); e == nil {
		if e := json.Unmarshal(bytes, pOtherClient); e == nil {
			q := datastore.NewQuery("OtherClient").Filter("UID =", pOtherClient.UID)
			clients := make([]OtherClient, 0)
			keys, _ := q.GetAll(cxt, &clients)
			if len(clients) > 0 {
				//Delete old one if find a existed item.
				datastore.DeleteMulti(cxt, keys)
			}

			datastore.Put(cxt, datastore.NewIncompleteKey(cxt, "OtherClient", nil), pOtherClient)
			w.Header().Set("Content-Type", "application/json")
			fmt.Fprintf(w, `{"status":%d}`, 200)
		} else {
			w.Header().Set("Content-Type", "application/json")
			fmt.Fprintf(w, `{"status":%d}`, 300)
		}
	}else {
		w.Header().Set("Content-Type", "application/json")
		fmt.Fprintf(w, `{"status":%d}`, 300)
	}
}

func handleDelete(w http.ResponseWriter, r *http.Request) {
	cxt := appengine.NewContext(r)
	defer func() {
		if err := recover(); err != nil {
			cxt.Errorf("handleDelete: %v", err)
			w.Header().Set("Content-Type", "application/json")
			fmt.Fprintf(w, `{"status":%d}`, 300)
		}
	}()

	pOtherClient := new(OtherClient)
	if bytes, e := ioutil.ReadAll(r.Body); e == nil {
		if e := json.Unmarshal(bytes, pOtherClient); e == nil {
			q := datastore.NewQuery("OtherClient").Filter("UID =", pOtherClient.UID)
			clients := make([]OtherClient, 0)
			keys, _ := q.GetAll(cxt, &clients)
			datastore.DeleteMulti(cxt, keys)
			w.Header().Set("Content-Type", "application/json")
			fmt.Fprintf(w, `{"status":%d}`, 200)
		} else {
			w.Header().Set("Content-Type", "application/json")
			fmt.Fprintf(w, `{"status":%d}`, 300)
		}
	}else {
		w.Header().Set("Content-Type", "application/json")
		fmt.Fprintf(w, `{"status":%d}`, 300)
	}
}



func loadClients(_r *http.Request) (clients []OtherClient) {
	cxt := appengine.NewContext(_r)
	q := datastore.NewQuery("OtherClient")
	clients = make([]OtherClient, 0)
	q.GetAll(cxt, &clients)
	return
}


type BreakingNews struct {
	Name string
	Author   string
	Size    string
	Pages   string
	Link string
	ISBN   string
	Year    string
	Publisher   string
	CoverUrl string
	Description   string
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
	return &Request{"https://android.googleapis.com/gcm/send", "key=" + PUSH_KEY, "application/json", _msg}
}



func (this *PushMessage) newBreakingNews(name string, author string, size string, pages string,link string, isbn string, year string, publisher string, coverurl string, description string) *BreakingNews {
	return &BreakingNews{name  , author  , size  , pages  ,link  , isbn  , year  , publisher  , coverurl  , description  }
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
	if len(_r.message.registrationIDs[0]) != 0 {
		ids += fmt.Sprintf(`"%s"`, _r.message.registrationIDs[0])
	} else {
		ids = ids[:len(ids)-1]
	}

	this.pushed = fmt.Sprintf(`{"registration_ids" : [%s],"data" : {"Name": "%s", "Author": "%s", "Size": "%s", "Pages": "%s","Link": "%s", "ISBN": "%s", "Year": "%s", "Publisher": "%s","CoverUrl": "%s","Description": "%s"}}`,
													ids,
													_r.message.data.Name,
													_r.message.data.Author,
													_r.message.data.Size,
													_r.message.data.Pages,
													_r.message.data.Link,
													_r.message.data.ISBN,
													_r.message.data.Year,
													_r.message.data.Publisher,
													_r.message.data.CoverUrl,
													_r.message.data.Description)
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
	b := this.newBreakingNews(this.request.FormValue("Name"),
													  this.request.FormValue("Author"),
														this.request.FormValue("Size"),
														this.request.FormValue("Pages"),
														this.request.FormValue("Link"),
														this.request.FormValue("ISBN"),
														this.request.FormValue("Year"),
														this.request.FormValue("Publisher"),
														this.request.FormValue("CoverUrl"),
														this.request.FormValue("Description"))
	m := this.newMessage([]string{this.request.FormValue("registerID")}, b)
	r := this.newRequest(m)
	this.send(r)
}
