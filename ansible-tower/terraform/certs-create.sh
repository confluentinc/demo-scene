#!/bin/bash

# set -o nounset \
#     -o errexit \
#     -o verbose \
#     -o xtrace

# # Cleanup files
rm -f *.crt *.csr *_creds *.jks *.srl *.key *.pem *.der *.p12

CA_CRT=ca.crt
CA_KEY=ca.key

echo "  >>>  Generate CA cert and key"
openssl req -new -x509 \
    -keyout $CA_KEY \
    -out $CA_CRT \
    -days 365 \
    -subj '/CN=test.confluent.io/OU=TEST/O=CONFLUENT/L=MountainView/S=Ca/C=US' \
    -passin pass:capassword \
    -passout pass:capassword


echo "  >>>  Create truststore and import the CA cert"
keytool -noprompt -import \
    -keystore truststore.jks \
    -alias CARoot \
    -file $CA_CRT  \
    -storepass truststorepass \
    -keypass truststorepass


echo "  >>>  Create MDS Private Key"
openssl genrsa \
    -out /var/ssl/private/generation/tokenKeypair.pem 2048

echo "  >>>  Create MDS Public Key"
openssl rsa -in /var/ssl/private/generation/tokenKeypair.pem \
 -outform PEM -pubout -out /var/ssl/private/generation/public.pem


filename="certificate-hosts"
# remove the empty lines
for line in `sed '/^$/d' $filename`; do

    OIFS=$IFS
    IFS=':'
    read -ra split_hostnames <<< "$line"
      IFS=$OIFS
      service=${split_hostnames[0]}
      internal=${split_hostnames[1]}
      fqdn=$internal.confluent
      # external=${split_hostnames[2]}
      # echo "Service: $service hostname: $internal"

      alias=$service.$internal
      KEYSTORE_FILENAME=$internal.keystore.jks

      CSR_FILENAME=$internal.csr
      CRT_SIGNED_FILENAME=$internal-ca1-signed.crt
      KEY_FILENAME=$internal-key.pem
      # EXT="SAN=dns:$internal"
      EXT="SAN=dns:$internal,dns:$fqdn"

      FORMAT=PKCS12



      echo "  >>>  Create host keystore"
      keytool -genkeypair -noprompt \
          -keystore $KEYSTORE_FILENAME \
          -alias $alias \
          -dname "CN=$service,OU=QE IT,O=CONFLUENT,L=PaloAlto,ST=Ca,C=US" \
          -ext $EXT \
          -keyalg RSA \
          -storetype $FORMAT \
          -keysize 2048 \
          -storepass keystorepass \
          -keypass keystorepass


      if [ $FORMAT = "pkcs12" ]; then
         echo "  >>>  Get host key from Keystore"
         openssl pkcs12 \
             -in $KEYSTORE_FILENAME \
             -passin pass:keystorepass \
             -passout pass:keypass \
             -nodes -nocerts \
             -out $KEY_FILENAME
      fi

      echo "  >>>  Create the certificate signing request (CSR)"
      keytool -certreq \
          -keystore $KEYSTORE_FILENAME \
          -alias $alias \
    			-ext $EXT \
          -file $CSR_FILENAME \
          -storepass keystorepass \
          -keypass keystorepass

      echo "  >>>  Sign the host certificate with the certificate authority (CA)"
      openssl x509 -req \
          -CA $CA_CRT \
          -CAkey $CA_KEY \
          -in $CSR_FILENAME \
          -out $CRT_SIGNED_FILENAME \
          -days 9999 \
          -sha256 \
          -CAcreateserial \
          -passin pass:capassword \
    			-extensions v3_req \
    			-extfile <(cat <<EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_req
prompt = no
[req_distinguished_name]
CN = $service
[v3_req]
subjectAltName = @alt_names
[alt_names]
DNS.1 = $internal
DNS.2 = $fqdn
EOF
)

      echo "  >>>  Import the CA cert into the keystore"
      keytool -noprompt -import \
          -keystore $KEYSTORE_FILENAME \
          -alias CARoot \
          -file $CA_CRT  \
          -storepass keystorepass \
          -keypass keystorepass


      if (( $# == 2 ))
      then
        echo "  >>>  Import the CA cert twice into the keystore"
        keytool -noprompt -import \
            -keystore $KEYSTORE_FILENAME \
            -alias dumbcert \
            -file $CA_CRT  \
            -storepass keystorepass \
            -keypass keystorepass
      fi

      echo "  >>> Import the host certificate into the keystore"
      keytool -noprompt -import \
          -keystore $KEYSTORE_FILENAME \
          -alias $alias \
          -file $CRT_SIGNED_FILENAME \
          -storepass keystorepass \
          -keypass keystorepass

done
