package com.s331402;

import java.io.IOException;
import java.net.ServerSocket;

public class Main {

    public static void main(String[] args) {
	    try(ServerSocket serverSocket = new ServerSocket(6666)) {
	        while(true){

	            new EmailExtractor(serverSocket.accept()).start();


            }
        } catch(IOException e) {
            System.out.println("Server exception: " + e.getMessage());
        }
    }

}
