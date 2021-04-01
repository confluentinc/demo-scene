#!/usr/bin/env bash

curl -i -X PUT -H "Accept:application/json" \
-H  "Content-Type:application/json" http://localhost:8083/connectors/source-csv-ukrail-locations/config \
-d '{
        "connector.class":"io.streamthoughts.kafka.connect.filepulse.source.FilePulseSourceConnector",
        "task.reader.class": "io.streamthoughts.kafka.connect.filepulse.reader.RowFileInputReader",
        "fs.scan.directory.path":"/data/ingest/locations/",
        "fs.scan.interval.ms":"10000",
        "file.filter.regex.pattern":"openraildata-talk-carl-partridge-ukrail_locations.csv",
        "fs.scan.filters":"io.streamthoughts.kafka.connect.filepulse.scanner.local.filter.RegexFileListFilter",
        "offset.strategy":"name",
        "skip.headers": "1",
        "topic":"ukrail-locations",
        "internal.kafka.reporter.bootstrap.servers": "broker:29092",
        "internal.kafka.reporter.topic":"connect-file-pulse-status",
        "fs.cleanup.policy.class": "io.streamthoughts.kafka.connect.filepulse.clean.LogCleanupPolicy",
        "tasks.max": 1,
        "filters":"ParseLine,setKey",
        "filters.ParseLine.type": "io.streamthoughts.kafka.connect.filepulse.filter.DelimitedRowFilter",
        "filters.ParseLine.extractColumnName": "headers",
        "filters.ParseLine.trimColumn": "true",
        "filters.ParseLine.separator": ",",
        "filters.setKey.type":"io.streamthoughts.kafka.connect.filepulse.filter.AppendFilter",
        "filters.setKey.field":"$key",
        "filters.setKey.value":"$value.location_id"
    }'