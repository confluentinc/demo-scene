package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"net/http"
	"sort"
	"strconv"
	"strings"
	"time"
)

type carPark struct {
	name        string
	ts          string
	emptyplaces int64
	capacity    int64
	distance_km float64
	URL         string
	lat         float64
	lon         float64
}

type carParks []carPark

func (c carParks) Len() int {
	return len(c)
}

func (c carParks) Less(i, j int) bool {
	return c[i].distance_km < c[j].distance_km
}

func (c carParks) Swap(i, j int) {
	{
		c[i], c[j] = c[j], c[i]
	}
}

// func getClosest(lat float64, lon float64) (cp carPark, distance float32, e error) {
func getClosest(lat float64, lon float64) (c carPark, e error) {
	var cp carPark
	var cps carParks

	// TODO add a channel so that user can run another
	// command to delete an alert

	// Prepare the request
	url := "http://localhost:8088/query"
	method := "POST"
	k := "SELECT NAME AS CARPARK, LATEST_TS, GEO_DISTANCE(cast(" + fmt.Sprintf("%v", lat) + " as double), cast(" + fmt.Sprintf("%v", lon) + " as double), cast(LATitude as double), cast(LONgitude as double)) AS DISTANCE_TO_CARPARK_KM, CURRENT_EMPTY_PLACES, (       (CAST(CAPACITY as double) - cast( CURRENT_EMPTY_PLACES AS DOUBLE)) / CAST(CAPACITY AS DOUBLE)) * 100 AS PCT_FULL, CAPACITY, DIRECTIONSURL, LATITUDE,LONGITUDE FROM CARPARK4 C WHERE CURRENT_EMPTY_PLACES > 10 EMIT CHANGES;"
	payload := strings.NewReader("{\"ksql\":\"" + k + "\"}")

	// Create the client
	client := &http.Client{}
	req, err := http.NewRequest(method, url, payload)
	if err != nil {
		return cp, err
	}
	req.Header.Add("Content-Type", "application/vnd.ksql.v1+json; charset=utf-8")

	// Make the request
	res, err := client.Do(req)
	if err != nil {
		return cp, err
	}
	defer res.Body.Close()

	// Parse the stream of results
	var r ksqlDBMessageRow
	var lastRow time.Time
	gotRow := false
	reader := bufio.NewReader(res.Body)
	doThis := true
	for doThis {
		// Check if it's more than 30 seconds since we received a row
		if gotRow {
			d := time.Now().Sub(lastRow)
			//fmt.Printf("It's %v since the last row was received\n", d)
			if d.Microseconds() > 500 {
				doThis = false
			}
		}
		// Read the next chunk
		lb, err := reader.ReadBytes('\n')
		if err != nil {
			doThis = false
		}

		if len(lb) > 2 {
			//fmt.Printf("\nGot some data:\n\t%v", string(lb))

			// Do a dirty hack to remove the trailing comma and \r so that the `row` can be
			// parsed as JSON
			// e.g.
			// {"row":{"columns":["Burnett St",1595373720000,122,117]}},
			//   becomes
			// {"row":{"columns":["Burnett St",1595373720000,122,117]}}
			//
			// Pretty sure instead of `ReadBytes` above I should be using
			// Scanner (https://golang.org/pkg/bufio/#Scanner) to split on ASCII 44 10 13 (,CRLF)
			lb = lb[:len(lb)-2]

			// Convert the JSON to Go object
			if strings.Contains(string(lb), "row") {
				// Store now, so that we can start a countdown to timeout
				gotRow = true
				lastRow = time.Now()
				// Looks like a Row, let's process it!
				err = json.Unmarshal(lb, &r)
				if err != nil {
					return cp, fmt.Errorf("error decoding JSON %v (%v)", string(lb), err)
				}

				if r.Row.Columns != nil {
					cp.name = r.Row.Columns[0].(string)
					cp.ts = r.Row.Columns[1].(string)
					cp.distance_km = r.Row.Columns[2].(float64)
					cp.emptyplaces = int64(r.Row.Columns[3].(float64))
					cp.capacity = int64(r.Row.Columns[5].(float64))
					cp.URL = r.Row.Columns[6].(string)
					cp.lat, _ = strconv.ParseFloat(r.Row.Columns[7].(string), 64)
					cp.lon, _ = strconv.ParseFloat(r.Row.Columns[8].(string), 64)
					cps = append(cps, cp)
				}

			} else {
				//fmt.Printf("-> Ignoring JSON as it doesn't look like a Row\n")
				continue
			}
		}

	}

	// do clever return stuff here taking the list of carparks and sorting it by distance
	if len(cps) > 0 {
		if sort.IsSorted(cps) == false {
			sort.Sort(cps)
		}
		// for _, c := range cps {
		// 	fmt.Printf("%v\t%v\n", c.distance_km, c.name)
		// }
		return cps[0], nil
	}

	return cp, fmt.Errorf("❌ No carpark found nearby with spaces available")
}
