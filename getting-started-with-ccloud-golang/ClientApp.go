package main

import (
	"encoding/binary"
	"fmt"
	"io/ioutil"
	"math/rand"
	"os"
	"strings"
	"time"

	"github.com/golang/protobuf/proto"
	"github.com/google/uuid"
	"github.com/riferrei/srclient"
	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

const producerMode string = "producer"
const consumerMode string = "consumer"
const schemaFile string = "SensorReading.proto"

func main() {

	clientMode := os.Args[1]
	props := LoadProperties()
	topic := TopicName

	if strings.Compare(clientMode, producerMode) == 0 {
		producer(props, topic)
	} else if strings.Compare(clientMode, consumerMode) == 0 {
		consumer(props, topic)
	} else {
		fmt.Println("Invalid option. Valid options are 'producer' and 'consumer'.")
	}

}

/**************************************************/
/******************** Producer ********************/
/**************************************************/

func producer(props map[string]string, topic string) {

	CreateTopic(props)

	schemaRegistryClient := srclient.CreateSchemaRegistryClient(props["schema.registry.url"])
	schemaRegistryClient.CodecCreationEnabled(false)
	srBasicAuthUserInfo := props["schema.registry.basic.auth.user.info"]
	credentials := strings.Split(srBasicAuthUserInfo, ":")
	schemaRegistryClient.SetCredentials(credentials[0], credentials[1])

	producer, err := kafka.NewProducer(&kafka.ConfigMap{
		"bootstrap.servers": props["bootstrap.servers"],
		"sasl.mechanisms":   "PLAIN",
		"security.protocol": "SASL_SSL",
		"sasl.username":     props["sasl.username"],
		"sasl.password":     props["sasl.password"]})
	if err != nil {
		panic(fmt.Sprintf("Failed to create producer %s", err))
	}
	defer producer.Close()

	go func() {
		for event := range producer.Events() {
			switch ev := event.(type) {
			case *kafka.Message:
				message := ev
				if ev.TopicPartition.Error != nil {
					fmt.Printf("Error delivering the order '%s'\n", message.Key)
				} else {
					fmt.Printf("Reading sent to the partition %d with offset %d. \n",
						message.TopicPartition.Partition, message.TopicPartition.Offset)
				}
			}
		}
	}()

	schema, err := schemaRegistryClient.GetLatestSchema(topic, false)
	if schema == nil {
		schemaBytes, _ := ioutil.ReadFile(schemaFile)
		schema, err = schemaRegistryClient.CreateSchema(topic, string(schemaBytes), "PROTOBUF", false)
		if err != nil {
			panic(fmt.Sprintf("Error creating the schema %s", err))
		}
	}

	devices := []*SensorReading_Device{}
	d1 := new(SensorReading_Device)
	deviceID, _ := uuid.NewUUID()
	d1.DeviceID = deviceID.String()
	d1.Enabled = true
	devices = append(devices, d1)

	d2 := new(SensorReading_Device)
	deviceID, _ = uuid.NewUUID()
	d2.DeviceID = deviceID.String()
	d2.Enabled = true
	devices = append(devices, d2)

	d3 := new(SensorReading_Device)
	deviceID, _ = uuid.NewUUID()
	d3.DeviceID = deviceID.String()
	d3.Enabled = true
	devices = append(devices, d3)

	d4 := new(SensorReading_Device)
	deviceID, _ = uuid.NewUUID()
	d4.DeviceID = deviceID.String()
	d4.Enabled = true
	devices = append(devices, d4)

	d5 := new(SensorReading_Device)
	deviceID, _ = uuid.NewUUID()
	d5.DeviceID = deviceID.String()
	d5.Enabled = true
	devices = append(devices, d5)

	for {

		choosen := rand.Intn(len(devices))
		if choosen == 0 {
			choosen = 1
		}
		deviceSelected := devices[choosen-1]

		// Create key and value
		key := deviceSelected.DeviceID
		sensorReading := SensorReading{
			Device:   deviceSelected,
			DateTime: time.Now().UnixNano(),
			Reading:  rand.Float64(),
		}
		valueBytes, _ := proto.Marshal(&sensorReading)

		recordValue := []byte{}
		recordValue = append(recordValue, byte(0))

		// Technically this is not necessary because in
		// Go consumers don't need to know the schema to
		// be able to deserialize records. However, if this
		// client wants to produce records that could be
		// deserialized using Java (KafkaProtobufDeserializer)
		// then it is important to arrange the bytes according
		// to the format expected there.
		schemaIDBytes := make([]byte, 4)
		binary.BigEndian.PutUint32(schemaIDBytes, uint32(schema.ID()))
		recordValue = append(recordValue, schemaIDBytes...)

		// [Pending] insert the message index list here
		// before the actual value since it is required
		// for the Java deserializer. Meanwhile this code
		// will produce records that can only be read by
		// Go consumers.
		recordValue = append(recordValue, valueBytes...)

		// Produce the record to the topic
		producer.Produce(&kafka.Message{
			TopicPartition: kafka.TopicPartition{
				Topic: &topic, Partition: kafka.PartitionAny},
			Key: []byte(key), Value: recordValue}, nil)

		// Sleep for one second...
		time.Sleep(1000 * time.Millisecond)

	}

}

/**************************************************/
/******************** Consumer ********************/
/**************************************************/

func consumer(props map[string]string, topic string) {

	CreateTopic(props)

	// Code below has been commented out because currently
	// Go doesn't need to read the schema in order to be
	// able to deserialize the record. But keeping the code
	// here for future use ¯\_(ツ)_/¯

	// schemaRegistryClient := srclient.CreateSchemaRegistryClient(props["schema.registry.url"])
	// schemaRegistryClient.CodecCreationEnabled(false)
	// srBasicAuthUserInfo := props["schema.registry.basic.auth.user.info"]
	// credentials := strings.Split(srBasicAuthUserInfo, ":")
	// schemaRegistryClient.SetCredentials(credentials[0], credentials[1])

	consumer, err := kafka.NewConsumer(&kafka.ConfigMap{
		"bootstrap.servers":  props["bootstrap.servers"],
		"sasl.mechanisms":    props["sasl.mechanisms"],
		"security.protocol":  props["security.protocol"],
		"sasl.username":      props["sasl.username"],
		"sasl.password":      props["sasl.password"],
		"session.timeout.ms": 6000,
		"group.id":           "golang-consumer",
		"auto.offset.reset":  "latest"})
	if err != nil {
		panic(fmt.Sprintf("Failed to create consumer %s", err))
	}
	defer consumer.Close()

	consumer.SubscribeTopics([]string{topic}, nil)

	for {
		record, err := consumer.ReadMessage(-1)
		if err == nil {
			// Deserialize the record value using Protobuf encoded bytes
			sensorReading := &SensorReading{}
			err = proto.Unmarshal(record.Value[5:], sensorReading)
			if err != nil {
				panic(fmt.Sprintf("Error deserializing the record: %s", err))
			}
			// Print the record value
			fmt.Printf("SensorReading[device=%s, dateTime=%d, reading=%f]\n",
				sensorReading.Device.GetDeviceID(),
				sensorReading.GetDateTime(),
				sensorReading.GetReading())
		} else {
			fmt.Println(err)
		}
	}

}
