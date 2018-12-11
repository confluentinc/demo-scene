{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "kafka-connect-twitter.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "kafka-connect-twitter.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "kafka-connect-twitter.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Form the Kafka URL. If Kafka is installed as part of this chart, use k8s service discovery,
else use user-provided URL
*/}}
{{- define "kafka-connect.kafka.bootstrapServers" -}}
{{- if $.Values.kafka.tls.enabled -}}bootstrap.servers=SASL_SSL://{{- $.Values.kafka.bootstrapEndpoint }}
{{- else }}bootstrap.servers=SASL_PLAINTEXT://{{- $.Values.kafka.bootstrapEndpoint }}
{{- end }}
{{- end -}}

{{/*
Default GroupId to Release Name but allow it to be overridden
*/}}
{{- define "kafka-connect.groupId" -}}
{{- if $.Values.overrideGroupId -}}
{{- .Values.overrideGroupId -}}
{{- else -}}
{{- .Release.Name -}}
{{- end -}}
{{- end -}}

{{/*
From https://github.com/openstack/openstack-helm-infra/blob/master/helm-toolkit/templates/utils/_joinListWithComma.tpl

abstract: |
  Joins a list of values into a comma seperated string
values: |
  test:
    - foo
    - bar
usage: |
  {{ include "helm-toolkit.utils.joinListWithComma" .Values.test }}
return: |
  foo,bar

*/}}
{{- define "helm-toolkit.utils.joinListWithComma" -}}
{{- $local := dict "first" true -}}
{{- range $k, $v := . -}}{{- if not $local.first -}},{{- end -}}{{- $v -}}{{- $_ := set $local "first" false -}}{{- end -}}
{{- end -}}
