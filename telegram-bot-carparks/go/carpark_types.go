package main

import "fmt"

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
