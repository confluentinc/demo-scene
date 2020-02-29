package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"os/signal"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/gomodule/redigo/redis"
	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

const (
	bootstrapServerVar   = "BOOTSTRAP_SERVER"
	clusterAPIKeyVar     = "CLUSTER_API_KEY"
	clusterAPISecretVar  = "CLUSTER_API_SECRET"
	groupIDVar           = "GROUP_ID"
	autoCreateTopicVar   = "AUTO_CREATE_TOPIC"
	numPartitionsVar     = "NUM_PARTITIONS"
	replicationFactorVar = "REPLICATION_FACTOR"
	topicNameVar         = "TOPIC_NAME"
	redisHostVar         = "REDIS_HOST"
	redisPortVar         = "REDIS_PORT"
	printVarsVar         = "PRINT_VARS"
	retryCountVar        = "RETRY_COUNT"
)

var (
	bootstrapServer   string = os.Getenv(bootstrapServerVar)
	clusterAPIKey     string = os.Getenv(clusterAPIKeyVar)
	clusterAPISecret  string = os.Getenv(clusterAPISecretVar)
	autoCreateTopic   string = os.Getenv(autoCreateTopicVar)
	numPartitions     string = os.Getenv(numPartitionsVar)
	replicationFactor string = os.Getenv(replicationFactorVar)
	topicName         string = os.Getenv(topicNameVar)
	groupID           string = os.Getenv(groupIDVar)
	redisHost         string = os.Getenv(redisHostVar)
	redisPort         string = os.Getenv(redisPortVar)
	retryCount        int    = 5
)

func main() {

	// Check if there is a custom retry count
	_retryCount, valueSet := os.LookupEnv(retryCountVar)
	if valueSet {
		value, _ := strconv.Atoi(_retryCount)
		retryCount = value
	}

	// Check if the user wants to print
	// the environment variables maybe
	// for debugging purposes.
	_printVars, valueSet := os.LookupEnv(printVarsVar)
	if valueSet {
		printVars, _ := strconv.ParseBool(_printVars)
		if printVars {
			log.Printf("%s = %s", bootstrapServerVar, bootstrapServer)
			log.Printf("%s = %s", clusterAPIKeyVar, clusterAPIKey)
			log.Printf("%s = %s", clusterAPISecretVar, clusterAPISecret)
			log.Printf("%s = %s", autoCreateTopicVar, autoCreateTopic)
			log.Printf("%s = %s", numPartitionsVar, numPartitions)
			log.Printf("%s = %s", replicationFactorVar, replicationFactor)
			log.Printf("%s = %s", topicNameVar, topicName)
			log.Printf("%s = %s", groupIDVar, groupID)
			log.Printf("%s = %s", redisHostVar, redisHost)
			log.Printf("%s = %s", redisPortVar, redisPort)
			log.Printf("%s = %s", retryCountVar, retryCount)
		}
	}

	// Programatically create the topic
	// if that was requested via config.
	if autoCreateTopic == "true" {
		adminClient, err := kafka.NewAdminClient(&kafka.ConfigMap{
			"bootstrap.servers": bootstrapServer,
			"sasl.mechanisms":   "PLAIN",
			"security.protocol": "SASL_SSL",
			"sasl.username":     clusterAPIKey,
			"sasl.password":     clusterAPISecret,
		})
		if err != nil {
			log.Printf("Failed to create Admin client: %s\n", err)
		}
		ctx, cancel := context.WithCancel(context.Background())
		defer cancel()
		maxDuration, _ := time.ParseDuration("60s")
		_numPartitions, err := strconv.Atoi(numPartitions)
		if err != nil {
			log.Printf("Invalid number of partitions provided: %s", numPartitions)
		}
		_replicationFactor, err := strconv.Atoi(replicationFactor)
		if err != nil {
			log.Printf("Invalid replication factor provided: %s", replicationFactor)
		}
		adminClient.CreateTopics(ctx, []kafka.TopicSpecification{{
			Topic:             string(topicName),
			NumPartitions:     _numPartitions,
			ReplicationFactor: _replicationFactor}},
			kafka.SetAdminOperationTimeout(maxDuration))
		adminClient.Close()
	}

	// Create a Kafka consumer
	consumer, err := kafka.NewConsumer(&kafka.ConfigMap{
		"bootstrap.servers":  bootstrapServer,
		"sasl.mechanisms":    "PLAIN",
		"security.protocol":  "SASL_SSL",
		"sasl.username":      clusterAPIKey,
		"sasl.password":      clusterAPISecret,
		"session.timeout.ms": 6000,
		"group.id":           groupID,
		"auto.offset.reset":  "earliest"})
	if err != nil {
		panic(fmt.Sprintf("Failed to create consumer %s", err))
	}
	defer consumer.Close()

	// Establish connection with Redis
	var cacheServer redis.Conn
	url := "redis://" + redisHost + ":" + redisPort
	var retries int = 0
	for {
		conn, err := redis.DialURL(url)
		if err == nil {
			cacheServer = conn
			defer cacheServer.Close()
			break
		} else {
			retries++
			log.Printf("Error during the #%d attempt to connect to Redis: %s", retries, err)
			if retries >= retryCount {
				break
			}
		}
	}
	if cacheServer == nil {
		panic("No connection with Redis was established. Exiting...")
	}

	// Fetch all records from the Kafka
	// topic and write them into Redis.
	// Then, keep updating the keys as
	// new records are written.
	sigchan := make(chan os.Signal, 1)
	signal.Notify(sigchan, syscall.SIGINT, syscall.SIGTERM)
	consumer.SubscribeTopics([]string{topicName}, nil)
	keepRunning := true
	for keepRunning == true {
		select {
		case sig := <-sigchan:
			log.Printf("Caught signal %v: terminating\n", sig)
			keepRunning = false
		default:
			event := consumer.Poll(0)
			if event == nil {
				continue
			}
			switch e := event.(type) {
			case *kafka.Message:
				var retries int = 0
				for {
					_, err := cacheServer.Do("SET",
						strings.ToLower(string(e.Key)),
						string(e.Value))
					retries++
					if err == nil || retries >= retryCount {
						break
					}
				}
			case kafka.Error:
				fmt.Fprintf(os.Stderr, "%% Error: %v: %v\n", e.Code(), e)
			}
		}
	}

}
