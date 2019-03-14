echo '{
  "name": "Neo4jSinkConnector_01",
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
    "neo4j.topic.cypher.atm_txns_gess": "MERGE (ac:account{account_id: event.account_id}) MERGE (atm:atm{name: event.atm, long: event.location.lon, lat: event.location.lat}) MERGE (txn:transaction{id: event.transaction_id, amount: event.amount}) MERGE (ac)-[:WITHDREW_FROM]->(atm) MERGE (ac)-[:MADE_TRANSACTION]->(txn) MERGE (txn)-[:OCCURED_AT]->(atm)"
  }
} ' | http POST localhost:18083/connectors
