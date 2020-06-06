package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"os/signal"
	"runtime"
	"sort"
	"strconv"
	"strings"
	"syscall"

	"github.com/google/uuid"
	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

// UserStatistic holds the aggregated
// data about one specific user playing
// with the Pac-Man game.
type UserStatistic struct {
	User         string `json:"USER"`
	HighestScore int    `json:"HIGHEST_SCORE"`
	HighestLevel int    `json:"HIGHEST_LEVEL"`
	TotalLosses  int    `json:"TOTAL_LOSSES"`
}

func main() {

	// Try to read if the user specified how
	// many records will be shown after the
	// scoreboard computes the order.
	var count int = -1
	if len(os.Args) > 1 {
		value, err := strconv.Atoi(os.Args[1])
		if err == nil {
			count = value
		} else {
			panic(fmt.Sprintf("Invalid parameter provided %s", err))
		}
	}

	// Create a local cache of the statistics
	// computed by the pipeline. Data coming
	// from the 'SCOREBOARD' topic will be used
	// to populate this cache.
	stats := make(map[string]*UserStatistic)

	// We need to print the statistics starting with the
	// beginning of the topic, therefore we need to use
	// a unique consumer group where we can move the offset
	// to the earliest record.
	id, err := uuid.NewUUID()
	consumerGroup := fmt.Sprintf("game-statistics-%s", id.String())

	// Create an Apache Kafka consumer that will listen
	// for changes made into the 'SCOREBOARD' topic. This
	// topic is in fact a table that holds the aggregated
	// data from users playing Pac-Man.
	props := loadProperties()
	consumer, err := kafka.NewConsumer(&kafka.ConfigMap{
		"bootstrap.servers":  props["bootstrap.servers"],
		"sasl.mechanisms":    props["sasl.mechanism"],
		"security.protocol":  props["security.protocol"],
		"sasl.username":      props["sasl.username"],
		"sasl.password":      props["sasl.password"],
		"session.timeout.ms": 6000,
		"group.id":           consumerGroup,
		"auto.offset.reset":  "earliest"})
	if err != nil {
		panic(fmt.Sprintf("Failed to create consumer %s", err))
	}
	defer consumer.Close()

	// Subscribe to the topic and update the local cache
	// as new records arrive. Then, print the statistics.
	// Note that since we're rewinding the offset to the
	// beginning of the partition, we need to wait until
	// all previous records arrive until we can start to
	// print the statistics. Otherwise, the screen would
	// keep blinking and giving an annoying UI experience.
	sigchan := make(chan os.Signal, 1)
	signal.Notify(sigchan, syscall.SIGINT, syscall.SIGTERM)
	consumer.SubscribeTopics([]string{"SCOREBOARD"}, nil)
	rewindPeriod := true
	keepRunning := true
	for keepRunning == true {
		select {
		case sig := <-sigchan:
			fmt.Printf("Caught signal %v: terminating\n", sig)
			keepRunning = false
		default:
			event := consumer.Poll(500)
			if event == nil {
				continue
			}
			switch e := event.(type) {
			case kafka.OffsetsCommitted:
				rewindPeriod = false
				printStatistics(stats, count)
			case *kafka.Message:
				userStat := new(UserStatistic)
				json.Unmarshal(e.Value, userStat)
				stats[userStat.User] = userStat
				if !rewindPeriod {
					printStatistics(stats, count)
				}
			case kafka.Error:
				fmt.Fprintf(os.Stderr, "%% Error: %v: %v\n", e.Code(), e)
			}
		}
	}

}

const propsFile string = "ccloud.properties"

func loadProperties() map[string]string {
	props := make(map[string]string)
	file, err := os.Open(propsFile)
	if err != nil {
		panic(fmt.Sprintf("Failed to load the '%s' file", propsFile))
	}
	defer file.Close()
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())
		if len(line) > 0 {
			if !strings.HasPrefix(line, "//") &&
				!strings.HasPrefix(line, "#") {
				parts := strings.Split(line, "=")
				key := strings.TrimSpace(parts[0])
				value := strings.TrimSpace(parts[1])
				props[key] = value
			}
		}
	}
	return props
}

func printStatistics(stats map[string]*UserStatistic, count int) {

	// Clear the screen and calculate who
	// from the list has the biggest name.
	clearScreen()
	var biggestName int
	for user := range stats {
		if len(user) > biggestName {
			biggestName = len(user)
		}
	}
	length := biggestName + 6

	// Print the headers of each column.
	fmt.Print("+-----+")
	for index := 0; index < length+3; index++ {
		fmt.Print("-")
	}
	fmt.Println("+---------------------+-----------------+----------------+")
	formatExp := "|  #  |     %-" + strconv.Itoa(length-2) + "v|"
	fmt.Printf(formatExp, "User")
	fmt.Println("   Highest Score     |  Highest Level  |  Total Losses  |")
	fmt.Print("+-----+")
	for index := 0; index < length+3; index++ {
		fmt.Print("-")
	}
	fmt.Println("+---------------------+-----------------+----------------+")

	// Before printing the stats, we need to sort
	// the data to highlight who is rocking in the
	// game. We will sort by:
	//   1) Whoever has the highest score;
	//   2) Then whoever has the highest Level;
	//   3) Then whoever has less total losses;
	users := make([]*UserStatistic, 0, len(stats))
	for _, userStat := range stats {
		users = append(users, userStat)
	}
	sort.SliceStable(users, func(i, j int) bool {
		if users[i].HighestScore > users[j].HighestScore {
			return true
		} else if users[i].HighestScore < users[j].HighestScore {
			return false
		} else {
			if users[i].HighestLevel > users[j].HighestLevel {
				return true
			} else if users[i].HighestLevel < users[j].HighestLevel {
				return false
			}
		}
		return users[i].TotalLosses < users[j].TotalLosses
	})

	// Effectively print the statistics...
	formatExp = "|  %-3d|  %-" + strconv.Itoa(length+1) + "v|     %-16d|       %-10d|       %-9d|\n"
	for i, user := range users {
		fmt.Printf(formatExp, i+1, user.User, user.HighestScore, user.HighestLevel, user.TotalLosses)
		if i+1 == count {
			break
		}
	}

	// Print the footer for each column.
	fmt.Print("+-----+")
	for index := 0; index < length+3; index++ {
		fmt.Print("-")
	}
	fmt.Println("+---------------------+-----------------+----------------+")

}

func clearScreen() {
	switch runtime.GOOS {
	case "linux": // Linux
		cmd := exec.Command("clear")
		cmd.Stdout = os.Stdout
		cmd.Run()
	case "darwin": // Mac O.S
		cmd := exec.Command("clear")
		cmd.Stdout = os.Stdout
		cmd.Run()
	case "windows": // Windows
		cmd := exec.Command("cmd", "/c", "cls")
		cmd.Stdout = os.Stdout
		cmd.Run()
	default:
		break
	}
}
