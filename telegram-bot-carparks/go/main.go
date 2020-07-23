package main

import (
	"fmt"
	"log"
	"strconv"

	tgbotapi "github.com/go-telegram-bot-api/telegram-bot-api"
)

// These are split up as when reading the chunked
// response we need to be able to assert the Row only

type ksqlDBMessageRow struct {
	Row struct {
		Columns []interface{} `json:"columns"`
	} `json:"row"`
}

type ksqlDBMessageHeader struct {
	Header struct {
		QueryID string `json:"queryId"`
		Schema  string `json:"schema"`
	} `json:"header"`
}

type ksqlDBMessage []struct {
	Header struct {
		QueryID string `json:"queryId"`
		Schema  string `json:"schema"`
	} `json:"header,omitempty"`
	Row struct {
		Columns []interface{} `json:"columns"`
	} `json:"row,omitempty"`
}

func main() {

	var resp string
	var chatID int64

	// Authorise and create bot instance
	bot, err := tgbotapi.NewBotAPI(TELEGRAM_API_TOKEN)
	if err != nil {
		log.Panic(err)
	}
	log.Printf("Authorized on account %s", bot.Self.UserName)

	// TODO: Check that the bot is set up for `alert` command
	// and add it if not.
	// Currently hardcoded in setup process, but outline function
	// has been added. Need to change it to take existing commands,
	// and add the new one (rather than overwrite)

	// Subscribe to updates
	u := tgbotapi.NewUpdate(0)
	u.Timeout = 60
	updates, err := bot.GetUpdatesChan(u)

	// Process any messages that we're sent as they arrive
	for update := range updates {
		if update.Message == nil { // ignore any non-Message Updates
			continue
		}

		chatID = update.Message.Chat.ID
		t := update.Message.Text
		log.Printf("[%s] %s (command: %v)", update.Message.From.UserName, t, update.Message.IsCommand())

		if update.Message.IsCommand() {
			// Handle commands
			switch update.Message.Command() {
			case "alert":
				threshold := update.Message.CommandArguments()
				if th, e := strconv.Atoi(threshold); e == nil {
					// Use a Go Routine to invoke the population
					// of the alert channel and handling the returned
					// alerts
					go func() {
						ac := make(chan string)
						go alertSpaces(ac, th)
						msg := tgbotapi.NewMessage(chatID, fmt.Sprintf("👍 Successfully created alert to be sent whenever more than %v spaces are available", th))
						if _, e := bot.Send(msg); e != nil {
							fmt.Printf("Error sending message to telegram.\nMessage: %v\nError: %v", msg, e)
						}

						for a := range ac {
							msg := tgbotapi.NewMessage(chatID, a)
							if _, e := bot.Send(msg); e != nil {
								fmt.Printf("Error sending message to telegram.\nMessage: %v\nError: %v", msg, e)
							}
						}
					}()
				} else {
					msg := tgbotapi.NewMessage(chatID, "Non-integer value specified for `/alert`")
					if _, e := bot.Send(msg); e != nil {
						fmt.Printf("Error sending message to telegram.\nMessage: %v\nError: %v", msg, e)
					}

				}

			default:
				bot.Send(tgbotapi.NewMessage(chatID, "🤔 Command not recognised."))
			}
		} else {
			// We've got a carpark status request
			if p, f, e := checkSpaces(t); e == nil {
				resp = fmt.Sprintf("ℹ️ 🚗 Car park %v is %.2f%% full (%v spaces available)\n\n", t, f, p)
			} else {
				resp = fmt.Sprintf("⚠️ There was an error calling `checkSpaces` for %v:\n\n%v\n\n", t, e)
			}
			msg := tgbotapi.NewMessage(chatID, resp)

			if _, e := bot.Send(msg); e != nil {
				fmt.Printf("Error sending message to telegram.\nMessage: %v\nError: %v", msg, e)
			}
		}

	}
}
