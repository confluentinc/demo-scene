package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"strings"
	"time"
)

const (
	showStreamsCmd string = "{\"ksql\": \"SHOW STREAMS EXTENDED;\"}"
	showTablesCmd  string = "{\"ksql\": \"SHOW TABLES EXTENDED;\"}"
	contentType    string = "application/vnd.ksql.v1+json; charset=utf-8"
)

type query struct {
	QueryString string `json:"queryString"`
}

type description struct {
	Name         string  `json:"name"`
	ReadQueries  []query `json:"readQueries"`
	WriteQueries []query `json:"writeQueries"`
}

type statement struct {
	Descriptions []description `json:"sourceDescriptions"`
}

func main() {

	args := os.Args

	if len(args) == 1 {
		fmt.Println("Usage: ksqldump -s <KSQL_SERVER> -f <FILENAME>")
		fmt.Println()
		fmt.Println("-s, 	--server		Endpoint of KSQL Server")
		fmt.Println("-f, 	--file			Output file to be created")
		fmt.Println("-u, 	--username		Username for authentication (Optional)")
		fmt.Println("-p, 	--password		Password for authentication (Optional)")
		fmt.Println()
		os.Exit(0)
	}

	var ksqlServer string
	var fileName string
	var userName string
	var password string

	for i, arg := range args {
		tmp := string(arg)
		if strings.Compare(tmp, "-s") == 0 || strings.Compare(tmp, "--server") == 0 {
			ksqlServer = string(args[i+1])
		} else if strings.Compare(tmp, "-f") == 0 || strings.Compare(tmp, "--file") == 0 {
			fileName = string(args[i+1])
		} else if strings.Compare(tmp, "-u") == 0 || strings.Compare(tmp, "--username") == 0 {
			userName = string(args[i+1])
		} else if strings.Compare(tmp, "-p") == 0 || strings.Compare(tmp, "--password") == 0 {
			password = string(args[i+1])
		}
	}

	if len(ksqlServer) == 0 {
		fmt.Println("No KSQL Server endpoint was provided.")
		os.Exit(0)
	}

	if len(fileName) == 0 {
		fmt.Println("No file was provided.")
		os.Exit(0)
	}

	if !strings.HasSuffix(ksqlServer, "/ksql") {
		ksqlServer = fmt.Sprintf("%s/ksql", ksqlServer)
	}

	file, err := os.Create(fileName)
	if err != nil {
		panic(err)
	}
	defer file.Close()

	httpClient := &http.Client{Timeout: 5 * time.Second}

	// Flush Streams
	file.WriteString("/*************************************/\n")
	file.WriteString("/*              Streams              */\n")
	file.WriteString("/*************************************/\n")
	file.WriteString("\n")

	payload := strings.NewReader(showStreamsCmd)
	req, err := http.NewRequest("POST", ksqlServer, payload)
	if err != nil {
		panic(err)
	}
	if len(userName) > 0 && len(password) > 0 {
		req.SetBasicAuth(userName, password)
	}
	req.Header.Set("Content-Type", contentType)
	resp, err := httpClient.Do(req)
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	respBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		panic(err)
	}

	stmts := make([]statement, 0)
	err = json.Unmarshal(respBytes, &stmts)
	if err != nil {
		panic(err)
	}

	for _, stmt := range stmts {
		for _, desc := range stmt.Descriptions {
			file.WriteString(fmt.Sprintf("/*** %s ***/ \n", desc.Name))
			file.WriteString(fmt.Sprintf("%s \n", desc.ReadQueries[0].QueryString))
			file.WriteString("\n")
		}
	}
	file.Sync()

	// Flush Tables
	file.WriteString("/*************************************/\n")
	file.WriteString("/*              Tables               */\n")
	file.WriteString("/*************************************/\n")
	file.WriteString("\n")

	payload = strings.NewReader(showTablesCmd)
	req, err = http.NewRequest("POST", ksqlServer, payload)
	if err != nil {
		panic(err)
	}
	if len(userName) > 0 && len(password) > 0 {
		req.SetBasicAuth(userName, password)
	}
	req.Header.Set("Content-Type", contentType)
	resp, err = httpClient.Do(req)
	if err != nil {
		panic(err)
	}
	defer resp.Body.Close()
	respBytes, err = ioutil.ReadAll(resp.Body)
	if err != nil {
		panic(err)
	}

	stmts = make([]statement, 0)
	err = json.Unmarshal(respBytes, &stmts)
	if err != nil {
		panic(err)
	}

	for _, stmt := range stmts {
		for _, desc := range stmt.Descriptions {
			file.WriteString(fmt.Sprintf("/*** %s ***/ \n", desc.Name))
			file.WriteString(fmt.Sprintf("%s \n", desc.WriteQueries[0].QueryString))
			file.WriteString("\n")
		}
	}
	file.Sync()
	fmt.Println("File created successfully!")

}
