package main

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"strings"
)

func checkSpaces(c string) (emptyPlaces float64, pctFull float64, err error) {

	// Prepare the request
	url := "http://localhost:8088/query"
	method := "POST"
	k := "SELECT CURRENT_EMPTY_PLACES, PCT_FULL FROM CARPARK4 WHERE NAME='" + c + "';"
	payload := strings.NewReader("{\"ksql\":\"" + k + "\"}")

	// Create the client, make the request
	client := &http.Client{}
	req, err := http.NewRequest(method, url, payload)

	if err != nil {
		return 0, 0, err
	}
	req.Header.Add("Content-Type", "application/vnd.ksql.v1+json; charset=utf-8")

	res, err := client.Do(req)
	if err != nil {
		return 0, 0, err
	}
	defer res.Body.Close()

	body, err := ioutil.ReadAll(res.Body)
	if err != nil {
		return 0, 0, err
	}

	// Parse the output
	var m ksqlDBMessage
	var CURRENT_EMPTY_PLACES float64
	var PCT_FULL float64
	err = json.Unmarshal(body, &m)

	CURRENT_EMPTY_PLACES = m[1].Row.Columns[0].(float64)
	PCT_FULL = m[1].Row.Columns[1].(float64)

	return CURRENT_EMPTY_PLACES, PCT_FULL, nil
}
