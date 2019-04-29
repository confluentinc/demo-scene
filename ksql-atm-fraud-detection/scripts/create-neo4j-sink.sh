curl -s \
     -X "POST" "http://localhost:18083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
          "name": "sink_neo4j_raw_txns_01",
          "config": {
            "value.converter":"org.apache.kafka.connect.json.JsonConverter",
            "value.converter.schemas.enable": false,
            "key.converter":"org.apache.kafka.connect.storage.StringConverter",
            "connector.class": "streams.kafka.connect.sink.Neo4jSinkConnector",
            "errors.tolerance": "all",
            "errors.log.enable": true,
            "errors.log.include.messages": true,
            "neo4j.server.uri": "bolt://neo4j:7687",
            "neo4j.authentication.basic.username": "neo4j",
            "neo4j.authentication.basic.password": "connect",
            "topics": "atm_txns_gess",
            "neo4j.topic.cypher.atm_txns_gess": "MERGE (ac:account{account_id: event.account_id}) merge (atm:atm{name: event.atm, long: event.location.lon, lat: event.location.lat}) merge (ac)-[:WITHDREW_FROM{amount_sterling:event.amount,txn_id:event.transaction_id}]->(atm)"
          }
        } '

curl -s \
     -X "POST" "http://localhost:18083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
          "name": "sink_neo4j_enriched_txns_01",
          "config": {
            "value.converter":"org.apache.kafka.connect.json.JsonConverter",
            "value.converter.schemas.enable": false,
            "key.converter":"org.apache.kafka.connect.storage.StringConverter",
            "connector.class": "streams.kafka.connect.sink.Neo4jSinkConnector",
            "errors.tolerance": "all",
            "errors.log.enable": true,
            "errors.log.include.messages": true,
            "neo4j.server.uri": "bolt://neo4j:7687",
            "neo4j.authentication.basic.username": "neo4j",
            "neo4j.authentication.basic.password": "connect",
            "topics": "ATM_TXNS_GESS_ENRICHED",
            "neo4j.topic.cypher.ATM_TXNS_GESS_ENRICHED": "MERGE (ac:account{account_id: event.ACCOUNT_ID, customer_name: event.CUSTOMER_NAME, customer_email: event.CUSTOMER_EMAIL, customer_phone: event.CUSTOMER_PHONE, customer_address: event.CUSTOMER_ADDRESS, customer_country: event.CUSTOMER_COUNTRY}) MERGE (atm:atm{name: event.ATM, long: event.LOCATION.LON, lat: event.LOCATION.LAT}) MERGE (ac)-[:WITHDREW_FROM{amount_sterling:event.AMOUNT,txn_id:event.TRANSACTION_ID}]->(atm)"
          }
        } '