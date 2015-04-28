package itbooks

import (
	"fmt"
	"net/http"
  "io/ioutil"
  "appengine"
  "appengine/urlfetch"
	"bytes"
	"encoding/xml"
	"encoding/json" 
)

type Error string

func (e Error) Error() string {
	return string(e)
}

func init() {
	http.HandleFunc("/download", handleDownload)
}

type SearchBody struct {
		Query string `xml:"query"`
}

func handleDownload(w http.ResponseWriter, r *http.Request) {
	cxt := appengine.NewContext(r)

	//Error-handling anyway.
	defer func() {
		if err := recover(); err != nil {
			cxt.Errorf("handleTopFeeds: %v", err)
			w.Header().Set("Content-Type", "application/json")
			fmt.Fprintf(w, `{"status":%d}`, 300)
		}
	}()

	//Get book fullname.
	pSearchBody := new(SearchBody)
	if bytes, e := ioutil.ReadAll(r.Body); e == nil {
		if e := json.Unmarshal(bytes, pSearchBody); e == nil {
			ch := make(chan []*Book)
		  go download(cxt, pSearchBody.Query, ch)
		  list := <-ch
			if list != nil {
				jstr, _ := json.Marshal(&list)
				s := fmt.Sprintf(`{"status":%d, "keyword" : "%s", "count" : %d, "result":%s}`, 200, pSearchBody.Query, len(list), jstr)
				w.Header().Set("Content-Type", "application/json")
				fmt.Fprintf(w, s)
			} else {
				w.Header().Set("Content-Type", "application/json")
				fmt.Fprintf(w, `{"status":%d}`, 300)
			}
			return
		}
	}
	w.Header().Set("Content-Type", "application/json")
	fmt.Fprintf(w, `{"status":%d}`, 300)
}

const SOAP =
`<v:Envelope xmlns:i="http://www.w3.org/2001/XMLSchema-instance" xmlns:d="http://www.w3.org/2001/XMLSchema" xmlns:c="http://schemas.xmlsoap.org/soap/encoding/" xmlns:v="http://schemas.xmlsoap.org/soap/envelope/">
	<v:Header />
	<v:Body>
		<SearchBooks xmlns="http://tempuri.org/" id="o0" c:root="1">
			<books i:type="d:string">%s</books>
		</SearchBooks>
	</v:Body>
</v:Envelope>`

const API = "http://inaaya-data-center.com/itb_webservice/Service.asmx?WSDL"


type Envelope struct {
	XMLName xml.Name `xml:"Envelope"`
	Body Body `xml:"Body"`
}

type Body struct  {
	SearchBooksResponse SearchBooksResponse `xml:"SearchBooksResponse"`
}

type SearchBooksResponse struct {
	SearchBooksResult SearchBooksResult  `xml:"SearchBooksResult"`
}

type SearchBooksResult struct {
	Diffgram Diffgram `xml:"diffgram"`
}

type Diffgram struct {
	NewDataSet NewDataSet  `xml:"NewDataSet"`
}

type NewDataSet struct {
	BookList []Book `xml:"book"`
}

type Book struct {
	Name string  `xml:"_name2"`
	Author string  `xml:"_name3"`
	Size string  `xml:"_size"`
	Pages string `xml:"_pages"`
	Link  string `xml:"_download_link"`
	ISBN string  `xml:"_ISBN"`
	Year string  `xml:"_year"`
	Publisher string  `xml:"_name"`
	CoverUrl string  `xml:"_book_img_url"`
	Description string `xml:"_desc"`
}

type TinyUrl struct {
	Status bool `json:"status"`
	Result string  `json:"result"`
}


func download(cxt appengine.Context, keyword string, ch chan []*Book) {
	client := urlfetch.Client(cxt)

	cxt.Infof("keyword: %v", keyword)
	body := fmt.Sprintf(SOAP, keyword)

	if r, e := http.NewRequest("POST", API, bytes.NewBufferString(body)); e == nil {
		cxt.Infof("body: %v", body)
		r.Header.Add("User-Agent", "ksoap2-android/2.6.0+")
		r.Header.Add("SOAPAction", "http://tempuri.org/SearchBooks")
		r.Header.Add("Content-Type", "text/xml;charset=utf-8")
		r.Header.Add("Connection", "close")
		r.Header.Add("Content-Length", fmt.Sprintf("%d",len(body)))
		r.Header.Add("Host", "inaaya-data-center.com")
		r.Header.Add("Accept-Encoding", "gzip")

		if resp, e := client.Do(r); e == nil {
			if resp != nil {
				defer resp.Body.Close()
			}
			if bytes, e := ioutil.ReadAll(resp.Body); e == nil {
				pSoapResult := new(Envelope)
				if e := xml.Unmarshal(bytes, pSoapResult); e == nil {
					books := pSoapResult.Body.SearchBooksResponse.SearchBooksResult.Diffgram.NewDataSet.BookList[:]
					cxt.Infof("books: %v", books)

					//Make tinyurl to download-links.
					if books != nil {
						sz := len(books)
						chh := make(chan *Book,  sz)
						for _, b := range books {
							go func( book Book, ch chan *Book) {
									ret  := Book{ 	book.Name,
										book.Author,
										book.Size,
										book.Pages,
										book.Link,
										book.ISBN,
										book.Year,
										book.Publisher,
										book.CoverUrl,
										book.Description }
									tinyQ := fmt.Sprintf("%s%s", "https://tinyurl-wrapper.appspot.com/?q=", ret.Link)
									if r, e := http.NewRequest("GET", tinyQ, nil); e == nil {
										if resp, e := client.Do(r); e == nil {
											if resp != nil {
												defer resp.Body.Close()
											}
											if bytes, e := ioutil.ReadAll(resp.Body); e == nil {
													pTiny := new(TinyUrl)
													if e := json.Unmarshal(bytes, pTiny); e == nil {
														if pTiny.Status   && pTiny.Result != "" {
															ret.Link = pTiny.Result
														}
													}
											}
										}
									}

									tinyQ = fmt.Sprintf("%s%s", "https://tinyurl-wrapper.appspot.com/?q=", ret.CoverUrl)
									if r, e := http.NewRequest("GET", tinyQ, nil); e == nil {
										if resp, e := client.Do(r); e == nil {
											if resp != nil {
												defer resp.Body.Close()
											}
											if bytes, e := ioutil.ReadAll(resp.Body); e == nil {
													pTiny := new(TinyUrl)
													if e := json.Unmarshal(bytes, pTiny); e == nil {
														if pTiny.Status   && pTiny.Result != "" {
															ret.CoverUrl = pTiny.Result
														}
													}
											}
										}
									}
									ch <- &ret
							}(b, chh)
						}

						booksRet := make([]*Book, sz)
						for i := 0; i < sz; i++ {
								pBook := <-chh
								booksRet[i] = pBook
						}
						ch <- booksRet[:]
					} else {
						ch <- nil
					}
				} else {
          ch <- nil
					cxt.Errorf("Error but still going: %v", e)
				}
			} else {
        ch <- nil
				panic(e)
			}
		} else {
      ch <- nil
			cxt.Errorf("Error but still going: %v", e)
		}
	} else {
    ch <- nil
		panic(e)
	}
}
