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
	distanceKm  float64
	URL         string
	lat         float64
	lon         float64
}

func (c carPark) String() string {
	return fmt.Sprintf("🚗 %v\n\tTimestamp: %v\n\tSpaces free: %v out of %v\n\tLocation: %v,%v (%v)\n\tDistance from user: %.1f km\n\n", c.name,
		c.ts,
		c.emptyplaces,
		c.capacity,
		c.lat,
		c.lon,
		c.URL,
		c.distanceKm)
}

type carParks []carPark

func (c carParks) Len() int {
	return len(c)
}

func (c carParks) Less(i, j int) bool {
	return c[i].distanceKm < c[j].distanceKm
}

func (c carParks) Swap(i, j int) {
	{
		c[i], c[j] = c[j], c[i]
	}
}

func getClosest(lat float64, lon float64) (c carPark, e error) {
	var cp carPark
	var cps carParks
	const availableThreshold = 10
	const queryResultsTimeoutMs = 500
	// Prepare the request
	url := "http://localhost:8088/query"
	method := "POST"
	k := "SELECT NAME AS CARPARK, LATEST_TS, GEO_DISTANCE(CAST(" + fmt.Sprintf("%v", lat) + " AS DOUBLE), "
	k += "       CAST(" + fmt.Sprintf("%v", lon) + " AS DOUBLE), CAST(LATITUDE AS DOUBLE), CAST(LONGITUDE AS DOUBLE)) AS DISTANCE_TO_CARPARK_KM, "
	k += "       CURRENT_EMPTY_PLACES, ((CAST(CAPACITY as DOUBLE) - CAST(CURRENT_EMPTY_PLACES AS DOUBLE)) / CAST(CAPACITY AS DOUBLE)) * 100 AS PCT_FULL, "
	k += "       CAPACITY, DIRECTIONSURL, LATITUDE,LONGITUDE"
	k += "  FROM CARPARK4 C "
	k += " WHERE CURRENT_EMPTY_PLACES > " + fmt.Sprintf("%v", availableThreshold)
	k += " EMIT CHANGES;"
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
	doThis := true
	reader := bufio.NewReader(res.Body)
	for doThis {
		// Check if it's more than 30 seconds since we received a row
		if gotRow {
			d := time.Now().Sub(lastRow)
			//fmt.Printf("It's %v since the last row was received\n", d)
			if d.Microseconds() > queryResultsTimeoutMs {
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
					// Store the row values in the carPark object
					cp.name = r.Row.Columns[0].(string)
					cp.ts = r.Row.Columns[1].(string)
					cp.distanceKm = r.Row.Columns[2].(float64)
					cp.emptyplaces = int64(r.Row.Columns[3].(float64))
					cp.capacity = int64(r.Row.Columns[5].(float64))
					cp.URL = r.Row.Columns[6].(string)
					cp.lat, _ = strconv.ParseFloat(r.Row.Columns[7].(string), 64)
					cp.lon, _ = strconv.ParseFloat(r.Row.Columns[8].(string), 64)
					// Add the carPark to the slice
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
		fmt.Println(cps)
		return cps[0], nil
	}

	return cp, fmt.Errorf("❌ No carpark found nearby with spaces available")
}
