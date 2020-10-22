package main

import (
	"context"
	"fmt"
	"sort"
	"strconv"
	"time"

	ksqldb "github.com/rmoff/ksqldb-go"
)

// Takes the name of a carpark and returns the number
// of empty places, the percentage occupied, and
// the timestamp of the data reported
func checkSpaces(c string) (latestTS string, emptyPlaces float64, pctFull float64, err error) {

	ctx, cancel := context.WithTimeout(context.TODO(), 10*time.Second)
	defer cancel()

	client := ksqldb.NewClient(KSQLDB_ENDPOINT, KSQLDB_API_KEY, KSQLDB_API_SECRET).Debug()

	k := "SELECT LATEST_TS, CURRENT_EMPTY_PLACES, CURRENT_PCT_FULL FROM CARPARK WHERE NAME='" + c + "';"
	_, r, e := client.Pull(ctx, k)

	if e != nil {
		// handle the error better here, e.g. check for no rows returned
		return latestTS, emptyPlaces, pctFull, fmt.Errorf("Error running Pull request against ksqlDB:\n%v", e)
	}

	// Parse the output; we're expecting just one row.
	for _, row := range r {
		if row != nil {
			latestTS = row[0].(string)
			emptyPlaces = row[1].(float64)
			pctFull = row[2].(float64)
		}
	}
	return latestTS, emptyPlaces, pctFull, nil

}

// Returns a channel, to which a carpark's details are
// written each time data is received in which more
// than the specified number of places are available
func alertSpaces(a chan<- string, c int) (e error) {
	// Whilst this is implemented using ksqlDB, perhaps it could be done
	// using a normal Kafka consumer and use Golang to apply the filter
	//
	// TODO01 add a channel so that user can run another
	// command to delete an alert
	//
	// TODO02 return a chan of carPark type, not a hardcoded string
	//
	defer close(a)

	// Run this for five minutes and then exit
	// TODO: does this actually do this?
	const queryResultTimeoutSeconds = 300

	// Prepare the request
	k := "SELECT NAME, TS, CAPACITY, EMPTY_PLACES"
	k += "  FROM CARPARK_EVENTS"
	k += " WHERE  EMPTY_PLACES > " + strconv.Itoa(c)
	k += " EMIT CHANGES;"

	// This Go routine will handle rows as and when they
	// are sent to the channel
	rc := make(chan ksqldb.Row)
	hc := make(chan ksqldb.Header, 1)

	var CARPARK string
	var DATA_TS float64
	var CURRENT_EMPTY_PLACES float64
	var CAPACITY float64
	go func() {

		for row := range rc {
			if row != nil {

				// Store the row values in the carPark object

				CARPARK = row[0].(string)
				DATA_TS = row[1].(float64)
				CURRENT_EMPTY_PLACES = row[2].(float64)
				CAPACITY = row[3].(float64)
				// Handle the timestamp
				t := int64(DATA_TS)
				ts := time.Unix(t/1000, 0).Format(time.RFC822)
				a <- fmt.Sprintf("✨ 🎉  🚗 The %v carpark has %v spaces available (capacity %v)\n(data as of %v)", CARPARK, CURRENT_EMPTY_PLACES, CAPACITY, ts)
			}
		}
	}()

	// Do the request
	ctx, cancel := context.WithTimeout(context.TODO(), queryResultTimeoutSeconds*time.Second)
	defer cancel()

	client := ksqldb.NewClient(KSQLDB_ENDPOINT, KSQLDB_API_KEY, KSQLDB_API_SECRET).Debug()

	e = client.Push(ctx, k, "latest", rc, hc)

	if e != nil {
		return fmt.Errorf("Error running Push request against ksqlDB:\n%v", e)
	}

	return nil
}

// Returns the details of the carpark that is nearest
// to the provided location (lat/long), with more
// than 10 spaces available.
func getClosest(lat float64, lon float64) (c carPark, e error) {
	const availableThreshold = 10
	const queryResultTimeoutSeconds = 20

	var cps carParks

	// Prepare the request
	k := "SELECT NAME AS CARPARK, TIMESTAMPTOSTRING(TS,'yyyy-MM-dd HH:mm:ss','Europe/London'), GEO_DISTANCE(CAST(" + fmt.Sprintf("%v", lat) + " AS DOUBLE), "
	k += "       CAST(" + fmt.Sprintf("%v", lon) + " AS DOUBLE), LATITUDE, LONGITUDE) AS DISTANCE_TO_CARPARK_KM, "
	k += "       EMPTY_PLACES, ((CAST(CAPACITY as DOUBLE) - CAST(EMPTY_PLACES AS DOUBLE)) / CAST(CAPACITY AS DOUBLE)) * 100 AS PCT_FULL, "
	k += "       CAPACITY, DIRECTIONSURL, LATITUDE,LONGITUDE"
	k += "  FROM CARPARK_LATEST C "
	k += " WHERE EMPTY_PLACES > " + fmt.Sprintf("%v", availableThreshold)
	k += " AND ROWTIME > UNIX_TIMESTAMP()-(1000 * 60 * 5)"
	k += " EMIT CHANGES;"

	// This Go routine will handle rows as and when they
	// are sent to the channel
	rc := make(chan ksqldb.Row)
	hc := make(chan ksqldb.Header, 1)

	go func() {

		for row := range rc {
			if row != nil {

				// Store the row values in the carPark object
				c.name = row[0].(string)
				c.ts = row[1].(string)
				c.distanceKm = row[2].(float64)
				c.emptyplaces = int64(row[3].(float64))
				c.capacity = int64(row[5].(float64))
				c.URL = row[6].(string)
				c.lat, _ = row[7].(float64)
				c.lon, _ = row[8].(float64)
				// Add the carPark to the slice
				cps = append(cps, c)
			}
		}
	}()

	// Do the request
	ctx, cancel := context.WithTimeout(context.TODO(), queryResultTimeoutSeconds*time.Second)
	defer cancel()

	client := ksqldb.NewClient(KSQLDB_ENDPOINT, KSQLDB_API_KEY, KSQLDB_API_SECRET).Debug()

	e = client.Push(ctx, k, "earliest", rc, hc)

	if e != nil {
		// handle the error better here, e.g. check for no rows returned
		return c, fmt.Errorf("Error running Push request against ksqlDB:\n%v", e)
	}

	// do clever return stuff here taking the list of carparks and sorting it by distance
	if len(cps) > 0 {
		if sort.IsSorted(cps) == false {
			sort.Sort(cps)
		}
		fmt.Println(cps)
		return cps[0], nil
	}

	return c, fmt.Errorf("❌ No carpark found nearby with spaces available")
}
