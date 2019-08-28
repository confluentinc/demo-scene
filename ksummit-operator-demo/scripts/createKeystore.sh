#!/bin/bash
set -e

server=$1
serverKey=$2
path=$3
[ $# -ne 3 ] && { echo "Usage: $0 fullchain_pem_file private_key"; exit 1; }

if [ -e /tmp/${path}/keystore.jks ];then
  rm /tmp/${path}/keystore.jks
fi
rm -rf /tmp/${path}/trustCAs
mkdir -p /tmp/${path}/trustCAs


echo "Check $server certificate"
openssl x509 -in $server -text -noout


openssl pkcs12 -export \
	-in ${server} \
	-inkey ${serverKey} \
	-out /tmp/${path}/pkcs.p12 \
	-name testService \
	-passout pass:mykeypassword

keytool -importkeystore \
	-deststorepass mystorepassword \
	-destkeypass mykeypassword \
	-destkeystore /tmp/${path}/keystore.jks \
	-srckeystore /tmp/${path}/pkcs.p12 \
	-srcstoretype PKCS12 \
	-srcstorepass mykeypassword

echo "Validate Server Certificate from Keytool"
keytool -list -keystore /tmp/${path}/keystore.jks -storepass mystorepassword
echo -e "\n"
echo "ssl.keystore.location=/tmp/${path}/keystore.jks"
echo "ssl.keystore.password=mystorepassword"
echo "ssl.key.password=mykeypassword"