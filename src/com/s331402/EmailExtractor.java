package com.s331402;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailExtractor extends Thread {
    private Socket socket;
    private String ipAdress;
    private int portNumber;

    EmailExtractor(Socket socket) {
        this.socket = socket;
        this.ipAdress = socket.getInetAddress().getHostAddress();
        this.portNumber = socket.getPort();
    }

    @Override
    public void run() {
        System.out.println("Client [" + ipAdress + "] : " + portNumber + " connected");

        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            while (true) {
                String urlFromClient = input.readLine().trim();
                if (urlFromClient.equalsIgnoreCase("exit")) {
                    break;
                }

                System.out.println("Received new request from " + ipAdress + " " + urlFromClient);
                output.println("Server has received your request: " + urlFromClient);

                BufferedReader reader = connectToUrl(urlFromClient);

                if (reader != null) {
                    Set<String> emailSet = extractEmail(reader);
                    reader.close();
                    clientResponse(emailSet, output);
                } else {
                    output.println(2);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println(ipAdress + ", Connection closed");
            } catch (IOException e) {
                System.out.println("Socket close exception!" + e.getMessage());
            }
        }
    }

    private BufferedReader connectToUrl(String urlFromClient) {
        try {
            URL url = new URL(urlFromClient);
            return new BufferedReader(new InputStreamReader(url.openStream()));
        }catch (IllegalArgumentException e) {
            System.out.println("Illegal argument: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.out.println("Could not open URL: " + e.getMessage());
        } catch(IOException e) {
            System.out.println("IOException in connectToURL method: " + e.getMessage());
        }
        return null;
    }

    private Set<String> extractEmail(BufferedReader bufferedReader) throws IOException {
        String line;
        String superRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";
        // Source : www.emailregex.com
        Set<String> emailList = new HashSet<>();
        Pattern emailPattern = Pattern.compile(superRegex);
        Matcher matcher;

        while ((line = bufferedReader.readLine()) != null) {
            matcher = emailPattern.matcher(line);
            if (matcher.find()) {
                emailList.add(matcher.group(0));
            }
        }

        return emailList;
    }

    private void clientResponse(Set<String> emailSet, PrintWriter output) {
        if (emailSet.size() == 0) {
            output.println(1);
        } else {
            output.println(0);

            for (String email : emailSet) {
                output.println(email);
            }
            output.println();
        }
    }
}
