#!/bin/bash
set -e

pem=$1
path=$2
[[ $# -ne 2 ]] && {
	echo "Usage: $0 CA_pem_file_to_add_in_truststore or Fullchain_pem_file";
	echo -e "\tCA_pem_file_to_add_in_truststore: CA certificate";
        echo -e "\tFullchain_pem_file: Includes CA & Certificate of server or client to trust";
        exit 1;
}

if [[ -e "/tmp/${path}/truststore.jks" ]];then
  rm /tmp/${path}/truststore.jks
  rm -rf /tmp/trustCAs
fi

mkdir -p /tmp/trustCAs
mkdir -p /tmp/${path}/trustCAs
cat ${pem} | awk 'split_after==1{n++;split_after=0} /-----END CERTIFICATE-----/ {split_after=1} {print > ("/tmp/trustCAs/ca" n ".pem")}'
for file in /tmp/trustCAs/*; do
  fileName="${file##*/}"
  keytool -import \
        -trustcacerts \
        -alias ${fileName} \
        -file ${file} \
	-keystore /tmp/${path}/truststore.jks \
	-deststorepass mystorepassword \
	-noprompt
done
echo "validate CA certs"
keytool -list -keystore /tmp/${path}/truststore.jks -storepass mystorepassword
echo -e "\n"
echo "ssl.truststore.location=/tmp/${path}/truststore.jks"
echo "ssl.truststore.password=mystorepassword"