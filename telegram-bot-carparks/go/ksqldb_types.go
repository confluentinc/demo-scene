package main

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
