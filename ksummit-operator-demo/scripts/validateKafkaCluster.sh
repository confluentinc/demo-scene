#!/bin/bash
##
## Kafka Cluster validation from Local machine. Make sure to deploy Kafka Cluster with external access.
##
red=`tput setaf 1`
green=`tput setaf 2`
reset=`tput sgr0`


function info {
  echo "$1 ==> ${green} $2 ${reset}"
}

function fail {
  echo "$1 ==> ${red} $2 ${reset}"
}

function checkDNS {
  checkStatus=$(nslookup $1 | awk -F':' '/^Address: / { matched = 1 } matched { print $2}' | xargs)
  if ! test -z "${checkStatus}"
  then
     info "DNS/IP resolved" "${1}"
  else
     fail "DNS/IP resolve issue for" "${1}"
   exit 1
  fi
}

namespace=$1
clusterName=$2
[ $# -ne 2 ] && { echo "Usage: $0 kubernetes_namespace, kafka_cluster_name"; exit 1; }

command -v kubectl >/dev/null 2>&1 || { echo "kubectl not available on the path" >&2; exit 1; }
command -v ccloud >/dev/null 2>&1 || { echo "ccloud not available on the path" >&2; exit 1; }

# Validate if cluster is present
kubectl -n ${namespace} get kafka ${clusterName} > /dev/null 2>&1
if [[ ! $? -eq 0 ]]; then
    err="Kafka Cluster with name [${clusterName}] does not exist on the namespace [${namespace}]"
    fail "Error" "$err"
    exit 1
fi

bootstrapServer=`kubectl -n ${namespace} get kafka ${clusterName} -ojsonpath='{.status.bootstrapEndpoint}'`
bootstrapEndpoint=$(echo ${bootstrapServer}| cut -d ":" -f 1)
checkDNS ${bootstrapEndpoint}

replicas=`kubectl -n ${namespace} get kafka ${clusterName} -ojsonpath='{.status.readyReplicas}'`
count=$(($replicas-1))
for i in $(seq 0 ${count} ); do
     brokerEndpoint="kubectl -n ${namespace} get kafka ${clusterName} -ojsonpath='{.status.brokerEndpoints.${clusterName}-${i} }'"
     dnsName=$(eval "${brokerEndpoint}")
     checkDNS ${dnsName}
done