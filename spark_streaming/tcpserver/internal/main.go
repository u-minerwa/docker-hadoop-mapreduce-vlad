package main

import (
	"TCPServer/internal/entity"
	"encoding/json"
	"fmt"
	"net"
	"os"
	"strconv"
	"time"
)

func main() {
	//	fmt.Print("Hello!")

	err, skinGen := entity.NewGenerator()

	if err != nil {
		panic(err)
	}

	if err != nil {
		panic(err)
	}

	args := os.Args

	var timeout int
	if len(args) < 3 {
		timeout = 5
	} else {
		timeout, err = strconv.Atoi(args[2])
	}

	if err != nil {
		panic(err)
	}

	err, listener := getListener()
	if err != nil {
		fmt.Println(err)
	}

	for {
		conn, err := listener.Accept()
		if err != nil {
			fmt.Println(err)
		}
		go handleClient(skinGen, timeout, conn)

	}

}

func handleClient(skinGen *entity.SkinPriceGenerator, timeout int, conn net.Conn) {

	remoteAddr := conn.RemoteAddr()

	var ip string

	switch addr := remoteAddr.(type) {
	case *net.UDPAddr:
		return
	case *net.TCPAddr:
		ip = addr.IP.String()
	}

	fmt.Println("Starting generating JSONs with interval " + strconv.Itoa(timeout) + " seconds\nClient IP: " + ip)

	var err error = nil

	for err == nil {

		err, skin := skinGen.GenerateSkinPrice()

		if err != nil {
			return
		}

		jsonSkin, err := json.Marshal(skin)

		jsonSkinI, err := json.MarshalIndent(skin, "", "\t")

		fmt.Println(string(jsonSkinI))

		var endString byte = '\n'

		_, err = conn.Write(append(jsonSkin, endString))

		if err != nil {
			fmt.Println("Closed connection on IP " + ip)
			return
		}

		time.Sleep(time.Duration(int64(timeout)) * time.Second)
	}
}

func getListener() (error, net.Listener) {
	args := os.Args

	var port string

	if len(args) < 2 {
		port = "9999"
	} else {
		port = args[1]
	}

	fmt.Println("TCP Server configuration" + ", port: " + port)

	listener, err := net.Listen("tcp", ":"+port)
	if err != nil {
		return err, nil
	}

	return nil, listener
}
