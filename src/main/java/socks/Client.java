package socks;

// input output library
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;

// network library
import java.net.Socket;

import com.fasterxml.jackson.core.JsonProcessingException;
// jackson library
import com.fasterxml.jackson.databind.ObjectMapper;

// objects to map json sent by the server into
class SuccessResponse { public String _class; }
class ErrorResponse { public String _class; public String error; }
class MessageListResponse { public String _class; public Message[] messages; }

public class Client
{
  static ObjectMapper mapper = new ObjectMapper(); // json object mapper
  final static String ADDR = "localhost"; // constant address string
  final static int PORT = 12345; // constant port number
  static String identity; // identity string

  public static void main(String[] args) throws IOException
  {
    Socket socket = new Socket(ADDR, PORT); // create a new socket and connect to ADDR:PORT (localhost:12345)

    BufferedReader client = new BufferedReader(new InputStreamReader(System.in)); // create string reader from the character reader from the byte reader
    PrintWriter server = new PrintWriter(socket.getOutputStream(), true); // create printwriter from the socket's output stream and set it to autoflush

    System.out.println("Enter identity: "); // prompt client for an identity
    identity = client.readLine(); // read the client's input

    Open_Request open_request = new Open_Request(); // create new open request
    open_request._class = "OpenRequest"; open_request.identity = identity; // initialise using client identity
    server.println(mapper.writeValueAsString(open_request)); // write the open request back to the server for it to interpret

    while (true) // infinite while loop
    {
      ShowMenu(); // main menu: 1 for publish, 2 for subscribe, 3 for getting messages
      
      String option = client.readLine(); // grab an argument number from the client's input
      if (option.equals("1")) { server.println(Publish()); } // if option is 1 then publish
      if (option.equals("2")) // if option is 2 then subscribe
      { 
        System.out.println("Who would you like to subscribe to? "); // ask user who they would like to subscribe to
        server.println(Subscribe(client.readLine())); // print subscribe request to the server where whatever they type is the channel they wish to subscribe to
      } 
      if (option.equals("3")) { server.println(Get()); } // if option is 3 then get messages
    }
  }

  static void ShowMenu() // print options menu
  {
    System.out.println("[1] Publish");
    System.out.println("[2] Subscribe");
    System.out.println("[3] Get Messages");
  }

  static String Publish() // build publish request
  {
    return("1 is not implemented!");
  }

  static String Subscribe(String channel) // build subscribe request
  {
    Subscribe_Request subscribe_request = new Subscribe_Request(); // create subscribe request object
    subscribe_request._class = "SubscribeRequest"; // set the _class
    subscribe_request.identity = identity; // set identity
    subscribe_request.channel = channel; // set channel
    
    try { return(mapper.writeValueAsString(subscribe_request)); } // try to return the subscribe request as json string
    catch (JsonProcessingException error) { return(""); } // if any errors occour, return an empty string
  }

  static String Get() // build get request
  {
    return("3 is not implemented!");
  }
}
