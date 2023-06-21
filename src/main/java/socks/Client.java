package socks;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.Socket;

import java.util.ArrayList;
import java.util.Scanner;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

class Success_Response { 
    public String _class; 
}
class Error_Response { 
    public String _class; 
    public String error; 
}
class Message_List_Response { 
    public String _class; 
    public ArrayList<Message> messages; 
}

public class Client {
    private static ObjectMapper mapper = new ObjectMapper(); 
    private static Scanner scanner = new Scanner(System.in);
    private final static String ADDR = "localhost"; 
    private final static int PORT = 12345; 
    private Socket socket; 
    private BufferedReader reader; 
    private BufferedWriter writer;
    private String identity; 

    public Client(Socket socket, String identity) {
        try {
            this.socket = socket; 
            this.reader = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()
                )
            ); 
            this.writer = new BufferedWriter(
                new OutputStreamWriter(
                    socket.getOutputStream()
                )
            ); 
            this.identity = identity;
        }
        catch (IOException error) { 
            Stop(); 
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            Socket socket = new Socket(ADDR, PORT); 
            System.out.println("Enter identity: ");
            String identity = scanner.nextLine(); 
            Client client = new Client(socket, identity); 

            Open_Request open_request = new Open_Request(); 
            open_request._class = "OpenRequest"; open_request.identity = identity; 
            client.writer.write(mapper.writeValueAsString(open_request)); 
            client.writer.newLine(); 
            client.writer.flush(); 

            while (true) {
                ShowMenu(); 
                
                String option = scanner.nextLine(); 
                if (option.equals("1")) { 
                    client.Publish(); 
                    client.Receive_Response(); 
                } 
                if (option.equals("2")) { 
                    client.Subscribe(); 
                    client.Receive_Response(); 
                } 
                if (option.equals("3")) { 
                    client.Unsubscribe(); 
                    client.Receive_Response(); 
                } 
                if (option.equals("4")) { 
                    client.Get(); 
                    client.Receive_Response(); 
                } 
            }
        }
        catch (IOException error) { 
            System.out.println("\u001B[31mstart the server first!\u001B[0m"); 
        }
    }

    static void ShowMenu() {
        System.out.println("\n[1] \u001B[35mPublish\u001B[0m, [2] \u001B[34mSubscribe\u001B[0m, [3] \u001B[31mUnsubscribe\u001B[0m, [4] \u001B[33mGet Messages\u001B[0m");
    }

    void Stop() {
        try {
            if (this.reader != null) { 
                this.reader.close(); 
            } 
            if (this.writer != null) { 
                this.writer.close(); 
            } 
            if (this.socket != null) { 
                this.socket.close(); 
            } 
        }
        catch (IOException error) { 
            error.printStackTrace(); 
        } 
    }

    void Receive_Response() 
    {
        String json; 
        try {
            json = reader.readLine(); 
            String _class = mapper.readValue(json, Mask.class)._class; 
            if (_class.equals("SuccessResponse")) { 
                System.out.println("\u001B[32mRequest succeeded!\u001B[0m"); 
            } 
            if (_class.equals("ErrorResponse")) { 
                System.out.println("\u001B[31m" + mapper.readValue(json, Error_Response.class).error + "\u001B[0m"); 
            } 
            if (_class.equals("MessageListResponse")) {  
                Message_List_Response message_list_response = mapper.readValue(json, Message_List_Response.class); 
                for (Message message : message_list_response.messages) {
                    System.out.println (
                        "[TIME] IDENTITY: MESSAGE" 
                            .replaceAll("TIME", new SimpleDateFormat("HH:mm").format(message.when)) 
                            .replaceAll("IDENTITY", message.from) 
                            .replaceAll("MESSAGE", message.body) 
                    ); 
                }
            }
        }
        catch (IOException error) { 
            Stop(); 
        }
    }

    void Publish() {
        System.out.println("\u001B[35mWho's channel would you like to publish on? \u001B[0m"); 
        String channel = scanner.nextLine(); 
        System.out.println("\u001B[35mWhat would you like to say? \u001B[0m"); 
        String message = scanner.nextLine(); 

        Publish_Request publish_request = new Publish_Request(); 
        publish_request._class = "PublishRequest"; 
        publish_request.identity = channel; 
        publish_request.message = new Message(); 
        publish_request.message._class = "Message"; 
        publish_request.message.from = this.identity; 
        publish_request.message.when = System.currentTimeMillis(); 
        publish_request.message.body = message; 

        try { 
            this.writer.write(mapper.writeValueAsString(publish_request)); 
            this.writer.newLine(); 
            this.writer.flush(); 
        } 
        catch (IOException error) { 
            Stop(); 
        } 
    }

    void Subscribe() {
        System.out.println("\u001B[34mWho would you like to subscribe to? \u001B[0m"); 
        String channel = scanner.nextLine(); 

        Subscribe_Request subscribe_request = new Subscribe_Request(); 
        subscribe_request._class = "SubscribeRequest"; 
        subscribe_request.identity = this.identity; 
        subscribe_request.channel = channel; 
            
        try { 
            this.writer.write(mapper.writeValueAsString(subscribe_request)); 
            this.writer.newLine(); 
            this.writer.flush();
        } 
        catch (IOException error) { 
            Stop(); 
        }
    }

    void Unsubscribe() 
    {
        System.out.println("\u001B[31mWho would you like to unsubscribe from? \u001B[0m"); 
        String channel = scanner.nextLine(); 

        Unsubscribe_Request unsubscribe_request = new Unsubscribe_Request(); 
        unsubscribe_request._class = "UnsubscribeRequest"; 
        unsubscribe_request.identity = this.identity; 
        unsubscribe_request.channel = channel; 

        try { 
            this.writer.write(mapper.writeValueAsString(unsubscribe_request)); 
            this.writer.newLine(); 
            this.writer.flush(); 
        } 
        catch (IOException error) { 
            Stop(); 
        } 
    }

    void Get() {
        Get_Request get_request = new Get_Request(); 
        get_request._class = "GetRequest"; 
        get_request.identity = this.identity; 
        System.out.println("\u001B[33mAfter what date would you like messages from? E.G: 01/01/2023 10:30\u001B[0m"); 
        try { 
            get_request.after = new SimpleDateFormat("dd/MM/yyyy HH:mm")
                .parse(scanner.nextLine())
                .getTime(); 
        } 
        catch (ParseException error) { 
            get_request.after = 0; 
        } 

        try { 
            this.writer.write(mapper.writeValueAsString(get_request)); 
            this.writer.newLine(); 
            this.writer.flush(); 
        } 
        catch (IOException error) { 
            Stop(); 
        } 
    }
}
