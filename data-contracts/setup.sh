echo "Setup Confluent Cloud and all Gradle Builds for Demo"
echo "-------------------------------------"
echo ""

echo "Initializing Confluent Cloud Environment..."
cd cc-terraform
terraform init
terraform plan -out "tfplan"
terraform apply "tfplan"

echo "Confluent Cloud Environment Complete."

echo ""
echo "-------------------------------------"
echo "Exporting Confluent Cloud Connection Properties for later use..."
terraform output -json \
 | jq -r 'to_entries | map( {key: .key|tostring|split("_")|join("."), value: .value} ) | map("\(.key)=\(.value.value)") | .[]' \
 | while read -r line ; do echo "$line"; done > ../shared/src/main/resources/confluent.properties

cd ../

echo "-------------------------------------"
echo ""
echo "Building \`shared\` jar..."
cd shared
gradle wrapper
./gradlew clean build publishToMavenLocal --info
echo "Published \`shared\` code to local maven repository"
cd ../

echo "-------------------------------------"
echo ""
echo "Registering Schemas with Confluent Cloud Data Governance..."
cd schemas
gradle wrapper
./gradlew clean registerSchemasTask --info
echo "Schema registration complete."
cd ../

echo "-------------------------------------"
echo ""
echo "Generating Code from Schemas for \`app-schema-v1\` module..."
cd app-schema-v1
gradlew wrapper
./gradlew clean generateCode --info
echo "Code Generation Complete"
cd ../
echo "-------------------------------------"

echo "Generating Code for \`app-schema-v2\` module..."
echo ""
cd app-schema-v2
gradlew wrapper
./gradlew clean generateCode --info
echo "Code Generation Complete"
cd ../

echo "-------------------------------------"
echo ""

echo "Setup Complete!"