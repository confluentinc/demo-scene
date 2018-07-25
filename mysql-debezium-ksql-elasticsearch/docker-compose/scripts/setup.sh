#!/bin/bash

export CONNECT_HOST=connect-debezium
echo -e "\n--\n\nWaiting for Kafka Connect to start on $CONNECT_HOST … ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)
docker-compose exec connect-debezium bash -c '/scripts/create-mysql-source.sh'

echo -e "\n--\n\nWaiting for elasticsearch to start "
grep -q "started" <(docker-compose logs -f elasticsearch)
docker-compose exec elasticsearch bash -c '/scripts/create-dynamic-mapping.sh'

export CONNECT_HOST=kafka-connect-cp
echo -e "\n--\n\nWaiting for Kafka Connect to start on $CONNECT_HOST … ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)

docker-compose exec kafka-connect-cp bash -c '/scripts/create-es-sink.sh'


curl -XPOST "http://localhost:9200/ratings-with-customer-data/type.name=kafkaconnect" -H 'Content-Type: application/json' -d'{
          "RATING_ID": 15486,
          "MESSAGE": "thank you for the most friendly, helpful experience today at your new lounge",
          "STARS": 4,
          "CHANNEL": "iOS",
          "ID": 8,
          "FULL_NAME": "Patti Rosten",
          "CLUB_STATUS": "silver",
          "EMAIL": "prosten7@ihg.com",
          "EXTRACT_TS": 1532512096460
        }'
curl -XPOST "http://localhost:9200/unhappy_platinum_customers/type.name=kafkaconnect" -H 'Content-Type: application/json' -d'{
          "FULL_NAME": "Laney Toopin",
          "MESSAGE": "more peanuts please",
          "STARS": 1,
          "EXTRACT_TS": 1532507457763,
          "CLUB_STATUS": "platinum",
          "EMAIL": "ltoopinc@icio.us"
        }'
curl 'http://localhost:5601/api/saved_objects/index-pattern' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"ratings-with-customer-data","timeFieldName":"EXTRACT_TS"}}' --compressed
curl 'http://localhost:5601/api/saved_objects/index-pattern' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"unhappy_platinum_customers","timeFieldName":"EXTRACT_TS"}}' --compressed
