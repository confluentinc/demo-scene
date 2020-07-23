package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"strings"
)

func setCommand(a string) {

	// curl --location --request GET 'https://api.telegram.org/botMY_TOKEN/setMyCommands' \
	// --header 'Content-Type: application/json' \
	// --data-raw '{
	// 	"commands": [
	// 		{
	// 			"command": "Alert",
	// 			"description": "Define an alert to be sent if a carpark becomes available with greater than the defined number of spaces"
	// 		}
	// 	]
	// }'

	url := "https://api.telegram.org/bot" + a + "/setMyCommands"
	method := "GET"

	payload := strings.NewReader("{\"commands\": [    {        \"command\": \"Alert\",        \"description\": \"Define an alert to be sent if a carpark becomes available with greater than the defined number of spaces\"    }]}")

	client := &http.Client{}
	req, err := http.NewRequest(method, url, payload)

	if err != nil {
		fmt.Println(err)
	}
	req.Header.Add("Content-Type", "application/json")

	res, err := client.Do(req)
	defer res.Body.Close()
	body, err := ioutil.ReadAll(res.Body)

	fmt.Println(string(body))
}
