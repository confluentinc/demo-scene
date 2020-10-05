#!/bin/bash
docker exec --interactive ibmmq /opt/mqm/samp/bin/amqsput DEV.QUEUE.1 QM1 < data/note.xml
