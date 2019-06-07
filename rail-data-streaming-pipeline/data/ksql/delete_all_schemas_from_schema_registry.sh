#!/usr/bin/env bash


curl -s "http://proxmox01.moffatt.me:8081/subjects"|jq '.'|sed -s 's/[ ",]//g'|xargs -Ifoo echo curl -X DELETE "http://proxmox01.moffatt.me:8081/subjects/foo"

