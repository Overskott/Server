package com.s331402;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EmailExtractor extends Thread {
    private Socket socket;
    private String ipAdress;
    private int portNumber;

    public EmailExtractor(Socket socket) {
        this.socket = socket;
        this.ipAdress = socket.getInetAddress().getHostAddress();
        this.portNumber = socket.getPort();
    }

    @Override
    public void run() {
        System.out.println("Client [" + ipAdress + "] : " + portNumber + " connected");

        try (
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        ) {
            while (true) {
                String urlInput = input.readLine();
                System.out.println("Received new request from " + ipAdress + " " + urlInput);
                output.println("Server has received your request: " + urlInput);

                if(urlInput.equalsIgnoreCase("exit")) break;
            }


        } catch (IOException e) {
            System.out.println("Oops: " + e.getMessage());

        } finally {
            try {
                socket.close();
                System.out.println(ipAdress + ", Connection closed");
            } catch (IOException e) {
                System.out.println("Socket close exception!" + e.getMessage());
            }
        }
    }
}
