curl -s -X "POST" "http://localhost:8088/ksql" \
           -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
           -d '{"ksql": "SHOW QUERIES;"}' | \
    jq '.[].queries[].id' | \
    xargs -Ifoo curl -X "POST" "http://localhost:8088/ksql" \
             -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
             -d '{"ksql": "TERMINATE 'foo';"}'

curl -s -X "POST" "http://localhost:8088/ksql" \
             -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
             -d '{"ksql": "SHOW STREAMS;"}' | \
      jq '.[].streams[].name' | \
      xargs -Ifoo curl -X "POST" "http://localhost:8088/ksql" \
               -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
               -d '{"ksql": "DROP STREAM 'foo';"}'

curl -s -X "POST" "http://localhost:8088/ksql" \
             -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
             -d '{"ksql": "SHOW TABLES;"}' | \
      jq '.[].tables[].name' | \
      xargs -Ifoo curl -X "POST" "http://localhost:8088/ksql" \
               -H "Content-Type: application/vnd.ksql.v1+json; charset=utf-8" \
               -d '{"ksql": "DROP TABLE 'foo';"}'
