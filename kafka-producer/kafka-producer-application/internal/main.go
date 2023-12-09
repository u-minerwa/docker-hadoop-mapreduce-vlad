package main

import (
	"encoding/json"
	"fmt"
	"github.com/IBM/sarama"
	"kafka-producer-application/internal/entity"
	"log"
	"os"
	"strconv"
	"time"
)

func main() {

	fmt.Println(getArgs())

	args := getArgs()

	var err error
	var producer sarama.SyncProducer

	for {
		producer, err = sarama.NewSyncProducer([]string{args[0] + ":" + args[1]}, nil)
		if err == nil {
			break
		}
		log.Printf("Failed to create producer: %v\nTrying again in 5 seconds", err)
		time.Sleep(5 * time.Second)
	}

	defer producer.Close()

	err, generator := entity.NewGenerator()

	if err != nil {
		panic(err)
	}

	timeout, err := strconv.Atoi(args[3])

	if err != nil {
		panic(err)
	}

	for i := 1; ; i++ {

		err, skin := generator.GenerateSkinPrice()

		if err != nil {
			log.Fatalf(err.Error())
		}

		skinJson, err := json.Marshal(skin)

		if err != nil {
			log.Fatalf(err.Error())
		}

		msg := &sarama.ProducerMessage{
			Topic: args[2],
			//	Key:   sarama.StringEncoder(requestID),
			Value: sarama.ByteEncoder(skinJson),
		}

		_, _, err = producer.SendMessage(msg)
		if err != nil {
			log.Printf("Failed to send message to Kafka: %v", err)
			//c.JSON(500, gin.H{"error": "failed to send message to Kafka"})
			return
		}

		fmt.Println("JSON " + strconv.Itoa(i) + "sent")

		time.Sleep(time.Duration(int64(timeout)) * time.Second)
	}

}

func getArgs() []string {
	args := make([]string, 4)
	osArgs := os.Args

	if len(osArgs) > 1 {
		args[0] = osArgs[1]
	} else {
		args[0] = "localhost"
	}

	if len(osArgs) > 2 {
		args[1] = osArgs[2]
	} else {
		args[1] = "9092"
	}

	if len(osArgs) > 3 {
		args[2] = osArgs[3]
	} else {
		args[2] = "skins"
	}

	if len(osArgs) > 4 {
		args[3] = osArgs[4]
	} else {
		args[3] = "5"
	}

	return args
}
