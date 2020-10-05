#!/bin/bash
curl --show-error --silent https://tfl.gov.uk/tfl/syndication/feeds/cycle-hire/livecyclehireupdates.xml | \
    xq -rc --arg sep $'\x1c' '.stations.station[] + { lastUpdate: .stations."@lastUpdate"} |  [ .id + $sep, tostring] |  join("")' | \
    kafkacat -b localhost:9092 -t livecyclehireupdates_02 -P -K$'\x1c'
