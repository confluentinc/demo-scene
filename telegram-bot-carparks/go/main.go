package main

import "fmt"

// These are split up as when reading the chunked
// response we need to be able to assert the Row only

type ksqlDBMessageRow struct {
	Row struct {
		Columns []interface{} `json:"columns"`
	} `json:"row"`
}

type ksqlDBMessageHeader struct {
	Header struct {
		QueryID string `json:"queryId"`
		Schema  string `json:"schema"`
	} `json:"header"`
}

type ksqlDBMessage []struct {
	Header struct {
		QueryID string `json:"queryId"`
		Schema  string `json:"schema"`
	} `json:"header,omitempty"`
	Row struct {
		Columns []interface{} `json:"columns"`
	} `json:"row,omitempty"`
}

func main() {

	c := "Broadway"
	if p, f, e := checkSpaces(c); e == nil {
		fmt.Printf("Car park %v is %.2f%% full (%v spaces available)\n\n", c, f, p)
	} else {
		fmt.Printf("There was an error calling `checkSpaces`.\n%v\n\n", e)
	}
	alertSpaces(100)

}
