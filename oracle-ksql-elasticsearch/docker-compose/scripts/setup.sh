#!/bin/bash

echo -e "Firing up Docker Compose"
docker-compose up -d

export CONNECT_HOST=connect-debezium
echo -e "\n--\n\nWaiting for Kafka Connect to start on $CONNECT_HOST … ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)
echo -e "\n--\n+> Creating Kafka Connect MySQL Debezium source"
docker-compose exec connect-debezium bash -c '/scripts/create-ora-source.sh'

echo -e "\n--\n\nWaiting for elasticsearch to start "
grep -q "started" <(docker-compose logs -f elasticsearch)
echo -e "\n--\n+> Creating Elasticsearch dynamic mapping"
docker-compose exec elasticsearch bash -c '/scripts/create-dynamic-mapping.sh'

export CONNECT_HOST=kafka-connect-cp
echo -e "\n--\n\nWaiting for Kafka Connect to start on $CONNECT_HOST … ⏳"
grep -q "Kafka Connect started" <(docker-compose logs -f $CONNECT_HOST)

echo -e "\n--\n+> Creating Kafka Connect Elasticsearch sink"

docker-compose exec kafka-connect-cp bash -c '/scripts/create-es-sink.sh'

echo -e "\n--\n+> Setting up Elasticsearch dummy data"

curl -XPOST "http://localhost:9200/kafka-ratings-enriched-2018-08/type.name=kafkaconnect" -H 'Content-Type: application/json' -d'{
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
curl -XPOST "http://localhost:9200/lisa18/type.name=kafkaconnect" -H 'Content-Type: application/json' -d'{
          "foo": "bar"
        }'

echo -e "\n--\n+> Opt out of Kibana telemetry"
curl 'http://localhost:5601/api/kibana/settings' -H 'kbn-version: 6.3.0' -H 'content-type: application/json' -H 'accept: application/json' --data-binary '{"changes":{"telemetry:optIn":false}}' --compressed

echo -e "\n--\n+> Register Kibana indices"
#curl 'http://localhost:5601/api/saved_objects/index-pattern/ratings-enriched' -X DELETE -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' 
curl -s 'http://localhost:5601/api/saved_objects/index-pattern/ratings-enriched' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"kafka-ratings-enriched-*","timeFieldName":"EXTRACT_TS"}}' --compressed 
curl -s 'http://localhost:5601/api/saved_objects/index-pattern/unhappy_platinum_customers' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"unhappy_platinum_customers","timeFieldName":"EXTRACT_TS"}}' --compressed 
curl -s 'http://localhost:5601/api/saved_objects/index-pattern/lisa18' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"lisa18"}}' --compressed 

echo -e "\n--\n+> Set default Kibana index"
curl -s 'http://localhost:5601/api/kibana/settings' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"changes":{"defaultIndex":"ratings-enriched"}}' --compressed

echo -e "\n--\n+> Import Kibana objects"

curl -s 'http://localhost:5601/api/saved_objects/search/2f3d2290-6ff0-11e8-8fa0-279444e59a8f?overwrite=true' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"Unhappy Platinum Customers","description":"","hits":0,"columns":["EMAIL","MESSAGE","STARS"],"sort":["EXTRACT_TS","desc"],"version":1,"kibanaSavedObjectMeta":{"searchSourceJSON":"{\"index\":\"unhappy_platinum_customers\",\"highlightAll\":true,\"version\":true,\"query\":{\"language\":\"lucene\",\"query\":\"\"},\"filter\":[]}"}}}' --compressed 
curl -s 'http://localhost:5601/api/saved_objects/search/11a6f6b0-31d5-11e8-a6be-09f3e3eb4b97?overwrite=true' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"Ratings detail","description":"","hits":0,"columns":["FULL_NAME","EMAIL","CLUB_STATUS","STARS","MESSAGE","CHANNEL"],"sort":["_score","desc"],"version":1,"kibanaSavedObjectMeta":{"searchSourceJSON":"{\"index\":\"ratings-enriched\",\"highlightAll\":true,\"version\":true,\"query\":{\"language\":\"lucene\",\"query\":\"\"},\"filter\":[]}"}}}' --compressed 
curl -s 'http://localhost:5601/api/saved_objects/visualization/5ef922e0-6ff0-11e8-8fa0-279444e59a8f?overwrite=true' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"Unhappy Platinum Customers","visState":"{\"title\":\"Unhappy Platinum Customers\",\"type\":\"metric\",\"params\":{\"addTooltip\":true,\"addLegend\":false,\"type\":\"metric\",\"metric\":{\"percentageMode\":false,\"useRanges\":false,\"colorSchema\":\"Green to Red\",\"metricColorMode\":\"None\",\"colorsRange\":[{\"from\":0,\"to\":10000}],\"labels\":{\"show\":false},\"invertColors\":false,\"style\":{\"bgFill\":\"#000\",\"bgColor\":false,\"labelColor\":false,\"subText\":\"\",\"fontSize\":60}}},\"aggs\":[{\"id\":\"1\",\"enabled\":true,\"type\":\"count\",\"schema\":\"metric\",\"params\":{}}]}","uiStateJSON":"{\"spy\":null}","description":"","version":1,"kibanaSavedObjectMeta":{"searchSourceJSON":"{\"index\":\"unhappy_platinum_customers\",\"filter\":[],\"query\":{\"query\":\"\",\"language\":\"lucene\"}}"}}}' --compressed 
curl -s 'http://localhost:5601/api/saved_objects/visualization/c6344a70-6ff0-11e8-8fa0-279444e59a8f?overwrite=true' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"Median Rating, by Club Status","visState":"{\"title\":\"Median Rating, by Club Status\",\"type\":\"area\",\"params\":{\"type\":\"area\",\"grid\":{\"categoryLines\":false,\"style\":{\"color\":\"#eee\"}},\"categoryAxes\":[{\"id\":\"CategoryAxis-1\",\"type\":\"category\",\"position\":\"bottom\",\"show\":true,\"style\":{},\"scale\":{\"type\":\"linear\"},\"labels\":{\"show\":true,\"truncate\":100},\"title\":{}}],\"valueAxes\":[{\"id\":\"ValueAxis-1\",\"name\":\"LeftAxis-1\",\"type\":\"value\",\"position\":\"left\",\"show\":true,\"style\":{},\"scale\":{\"type\":\"linear\",\"mode\":\"normal\"},\"labels\":{\"show\":true,\"rotate\":0,\"filter\":false,\"truncate\":100},\"title\":{\"text\":\"Median STARS\"}}],\"seriesParams\":[{\"show\":\"true\",\"type\":\"area\",\"mode\":\"stacked\",\"data\":{\"label\":\"Median STARS\",\"id\":\"1\"},\"drawLinesBetweenPoints\":true,\"showCircles\":true,\"interpolate\":\"linear\",\"valueAxis\":\"ValueAxis-1\"}],\"addTooltip\":true,\"addLegend\":true,\"legendPosition\":\"right\",\"times\":[],\"addTimeMarker\":false},\"aggs\":[{\"id\":\"1\",\"enabled\":true,\"type\":\"median\",\"schema\":\"metric\",\"params\":{\"field\":\"STARS\",\"percents\":[50]}},{\"id\":\"2\",\"enabled\":true,\"type\":\"date_histogram\",\"schema\":\"segment\",\"params\":{\"field\":\"EXTRACT_TS\",\"interval\":\"auto\",\"customInterval\":\"2h\",\"min_doc_count\":1,\"extended_bounds\":{}}},{\"id\":\"3\",\"enabled\":true,\"type\":\"terms\",\"schema\":\"group\",\"params\":{\"field\":\"CLUB_STATUS\",\"otherBucket\":false,\"otherBucketLabel\":\"Other\",\"missingBucket\":false,\"missingBucketLabel\":\"Missing\",\"size\":5,\"order\":\"desc\",\"orderBy\":\"_term\"}}]}","uiStateJSON":"{\"vis\":{\"colors\":{\"gold\":\"#E5AC0E\",\"bronze\":\"#99440A\",\"silver\":\"#806EB7\",\"platinum\":\"#DEDAF7\"}}}","description":"","version":1,"kibanaSavedObjectMeta":{"searchSourceJSON":"{\"index\":\"ratings-enriched\",\"filter\":[],\"query\":{\"query\":\"\",\"language\":\"lucene\"}}"}}}' --compressed 
curl -s 'http://localhost:5601/api/saved_objects/visualization/0c118530-31d5-11e8-a6be-09f3e3eb4b97?overwrite=true' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"Ratings by Channel","visState":"{\"title\":\"Ratings by Channel\",\"type\":\"histogram\",\"params\":{\"type\":\"histogram\",\"grid\":{\"categoryLines\":false,\"style\":{\"color\":\"#eee\"}},\"categoryAxes\":[{\"id\":\"CategoryAxis-1\",\"type\":\"category\",\"position\":\"bottom\",\"show\":true,\"style\":{},\"scale\":{\"type\":\"linear\"},\"labels\":{\"show\":true,\"truncate\":100},\"title\":{}}],\"valueAxes\":[{\"id\":\"ValueAxis-1\",\"name\":\"LeftAxis-1\",\"type\":\"value\",\"position\":\"left\",\"show\":true,\"style\":{},\"scale\":{\"type\":\"linear\",\"mode\":\"normal\"},\"labels\":{\"show\":true,\"rotate\":0,\"filter\":false,\"truncate\":100},\"title\":{\"text\":\"Count\"}}],\"seriesParams\":[{\"show\":\"true\",\"type\":\"histogram\",\"mode\":\"stacked\",\"data\":{\"label\":\"Count\",\"id\":\"1\"},\"valueAxis\":\"ValueAxis-1\",\"drawLinesBetweenPoints\":true,\"showCircles\":true}],\"addTooltip\":true,\"addLegend\":true,\"legendPosition\":\"right\",\"times\":[],\"addTimeMarker\":false},\"aggs\":[{\"id\":\"1\",\"enabled\":true,\"type\":\"count\",\"schema\":\"metric\",\"params\":{}},{\"id\":\"2\",\"enabled\":true,\"type\":\"date_histogram\",\"schema\":\"segment\",\"params\":{\"field\":\"EXTRACT_TS\",\"interval\":\"auto\",\"customInterval\":\"2h\",\"min_doc_count\":1,\"extended_bounds\":{}}},{\"id\":\"3\",\"enabled\":true,\"type\":\"terms\",\"schema\":\"group\",\"params\":{\"field\":\"CHANNEL\",\"otherBucket\":false,\"otherBucketLabel\":\"Other\",\"missingBucket\":false,\"missingBucketLabel\":\"Missing\",\"size\":5,\"order\":\"desc\",\"orderBy\":\"1\"}}]}","uiStateJSON":"{}","description":"","version":1,"kibanaSavedObjectMeta":{"searchSourceJSON":"{\"index\":\"ratings-enriched\",\"filter\":[],\"query\":{\"query\":\"\",\"language\":\"lucene\"}}"}}}' --compressed 
curl -s 'http://localhost:5601/api/saved_objects/visualization/39803a20-31d5-11e8-a6be-09f3e3eb4b97?overwrite=true' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"Ratings per Person","visState":"{\"title\":\"Ratings per Person\",\"type\":\"metric\",\"params\":{\"addTooltip\":true,\"addLegend\":false,\"type\":\"metric\",\"metric\":{\"percentageMode\":false,\"useRanges\":false,\"colorSchema\":\"Green to Red\",\"metricColorMode\":\"None\",\"colorsRange\":[{\"from\":0,\"to\":10000}],\"labels\":{\"show\":true},\"invertColors\":false,\"style\":{\"bgFill\":\"#000\",\"bgColor\":false,\"labelColor\":false,\"subText\":\"\",\"fontSize\":60}}},\"aggs\":[{\"id\":\"1\",\"enabled\":true,\"type\":\"count\",\"schema\":\"metric\",\"params\":{}},{\"id\":\"2\",\"enabled\":true,\"type\":\"terms\",\"schema\":\"group\",\"params\":{\"field\":\"FULL_NAME\",\"otherBucket\":false,\"otherBucketLabel\":\"Other\",\"missingBucket\":false,\"missingBucketLabel\":\"Missing\",\"size\":5,\"order\":\"desc\",\"orderBy\":\"1\"}}]}","uiStateJSON":"{}","description":"","version":1,"kibanaSavedObjectMeta":{"searchSourceJSON":"{\"index\":\"ratings-enriched\",\"filter\":[],\"query\":{\"query\":\"\",\"language\":\"lucene\"}}"}}}' --compressed 
curl -s 'http://localhost:5601/api/saved_objects/dashboard/mysql-ksql-kafka-es?overwrite=true' -H 'kbn-version: 6.3.0' -H 'Content-Type: application/json;charset=UTF-8' -H 'Accept: application/json, text/plain, */*' --data-binary '{"attributes":{"title":"Ratings Data","hits":0,"description":"","panelsJSON":"[{\"gridData\":{\"h\":15,\"i\":\"1\",\"w\":24,\"x\":0,\"y\":10},\"id\":\"0c118530-31d5-11e8-a6be-09f3e3eb4b97\",\"panelIndex\":\"1\",\"type\":\"visualization\",\"version\":\"6.3.0\"},{\"gridData\":{\"h\":10,\"i\":\"2\",\"w\":48,\"x\":0,\"y\":35},\"id\":\"39803a20-31d5-11e8-a6be-09f3e3eb4b97\",\"panelIndex\":\"2\",\"type\":\"visualization\",\"version\":\"6.3.0\"},{\"gridData\":{\"h\":10,\"i\":\"4\",\"w\":8,\"x\":0,\"y\":0},\"id\":\"5ef922e0-6ff0-11e8-8fa0-279444e59a8f\",\"panelIndex\":\"4\",\"type\":\"visualization\",\"version\":\"6.3.0\"},{\"gridData\":{\"h\":10,\"i\":\"5\",\"w\":40,\"x\":8,\"y\":0},\"id\":\"2f3d2290-6ff0-11e8-8fa0-279444e59a8f\",\"panelIndex\":\"5\",\"type\":\"search\",\"version\":\"6.3.0\"},{\"gridData\":{\"h\":15,\"i\":\"6\",\"w\":24,\"x\":24,\"y\":10},\"id\":\"c6344a70-6ff0-11e8-8fa0-279444e59a8f\",\"panelIndex\":\"6\",\"type\":\"visualization\",\"version\":\"6.3.0\"},{\"embeddableConfig\":{},\"gridData\":{\"h\":10,\"i\":\"7\",\"w\":48,\"x\":0,\"y\":25},\"id\":\"11a6f6b0-31d5-11e8-a6be-09f3e3eb4b97\",\"panelIndex\":\"7\",\"type\":\"search\",\"version\":\"6.3.0\",\"sort\":[\"EXTRACT_TS\",\"desc\"]}]","optionsJSON":"{\"darkTheme\":false,\"hidePanelTitles\":false,\"useMargins\":true}","version":1,"timeRestore":false,"kibanaSavedObjectMeta":{"searchSourceJSON":"{\"query\":{\"language\":\"lucene\",\"query\":\"\"},\"filter\":[],\"highlightAll\":true,\"version\":true}"}}}' --compressed 
