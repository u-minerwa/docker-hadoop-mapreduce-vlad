package main

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/IBM/sarama"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"kafka-consumer-application/internal/entity"
	"log"
	"os"
	"strconv"
	"time"
)

type SkinRubPrice struct {
	Name            string
	MarketplaceName string
	QualityValue    string
	RubPrice        int64
}

func main() {

	args := getArgs()

	consumer, err := sarama.NewConsumer([]string{args[0] + ":" + args[1]}, nil)
	if err != nil {
		log.Fatalf("Failed to create consumer: %v", err)
	}
	defer consumer.Close()

	partConsumer, err := consumer.ConsumePartition("skins", 0, sarama.OffsetNewest)
	if err != nil {
		log.Fatalf("Failed to consume partition: %v", err)
	}
	defer partConsumer.Close()

	for {
		select {
		// Чтение сообщения из Kafka
		case msg, ok := <-partConsumer.Messages():
			if !ok {
				log.Println("Channel closed, exiting goroutine")
				return
			}

			fmt.Println("Received message from Skins topic:\n" + string(msg.Value))
			saveJsonToDb(msg.Value)
		}
	}

}

func saveJsonToDb(jsonSkin []byte) {
	client, err := mongo.NewClient(options.Client().ApplyURI("mongodb://root:example@mongo-1:27017"))
	if err != nil {
		log.Fatal(err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	err = client.Connect(ctx)
	if err != nil {
		log.Fatal(err)
	}
	defer func() {
		if err = client.Disconnect(ctx); err != nil {
			log.Fatal(err)
		}
	}()

	collection := client.Database("cs2db").Collection("skins")

	skin := &entity.SkinPrice{}

	err = json.Unmarshal(jsonSkin, skin)

	if err != nil {
		log.Fatalf(err.Error())
	}
	rubPrice := skin.USDPrice * 88

	rubSkin := SkinRubPrice{
		Name:            skin.Name,
		MarketplaceName: skin.MarketplaceName,
		QualityValue:    skin.QualityValue,
		RubPrice:        rubPrice,
	}

	_, err = collection.InsertOne(ctx, rubSkin)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("inserted")
}

func producer() {
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
