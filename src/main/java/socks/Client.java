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
class Success_Response { public String _class; }
class Error_Response { public String _class; public String error; }
class Message_List_Response { public String _class; public Message[] messages; }

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
      if (option.equals("1")) // if option is 1 then publish
      {
        System.out.println("\u001B[35mWho's channel would you like to publish on? \u001B[0m");
        String channel = client.readLine();
        System.out.println("\u001B[35mWhat would you like to say? \u001B[0m");
        String message = client.readLine();
        server.println(Publish(channel, message));
      }
      if (option.equals("2")) // if option is 2 then subscribe
      { 
        System.out.println("\u001B[36mWho would you like to subscribe to? \u001B[0m"); // ask user who they would like to subscribe to
        server.println(Subscribe(client.readLine())); // print subscribe request to the server where whatever they type is the channel they wish to subscribe to
      } 
      if (option.equals("3")) // if option is 2 then subscribe
      { 
        System.out.println("\u001B[31mWho would you like to unsubscribe from? \u001B[0m"); // ask user who they would like to subscribe to
        server.println(Unsubscribe(client.readLine())); // print subscribe request to the server where whatever they type is the channel they wish to subscribe to
      } 
      if (option.equals("4")) { server.println(Get()); } // if option is 3 then get messages
    }
  }

  static void ShowMenu() // print options menu
  {
    System.out.println("[1] \u001B[35mPublish\u001B[0m, [2] \u001B[36mSubscribe\u001B[0m, [3] \u001B[31mUnsubscribe\u001B[0m, [4] \u001B[33mGet Messages\u001B[0m");
  }

  static String Publish(String channel, String message) // build publish request
  {
    Publish_Request publish_request = new Publish_Request(); // create publish request object
    publish_request._class = "PublishRequest"; // set the _class 
    publish_request.identity = channel; // set the identity to specified channel
    publish_request.message = new Message(); // create new message
    publish_request.message._class = "Message"; // set the _class
    publish_request.message.from = identity; // set from to identity
    publish_request.message.when = 0; // FIXME: set current time as opposed to just 0
    publish_request.message.body = message; // set body

    try { return(mapper.writeValueAsString(publish_request)); } // try to return the publish request as json string
    catch (JsonProcessingException error) { return(""); } // if any errors occour, return an empty string
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

  static String Unsubscribe(String channel) // build unsubscribe request
  {
    Unsubscribe_Request unsubscribe_request = new Unsubscribe_Request(); // create unsubscribe request object
    unsubscribe_request._class = "UnsubscribeRequest"; // set the _class
    unsubscribe_request.identity = identity;
    unsubscribe_request.channel = channel;

    try { return(mapper.writeValueAsString(unsubscribe_request)); } // try to return the unsubscribe request as json string
    catch (JsonProcessingException error) { return(""); } // if any errors occour, return an empty string
  }

  static String Get() // build get request
  {
    Get_Request get_request = new Get_Request(); // create get request object
    get_request._class = "GetRequest";
    get_request.identity = identity; // set identity
    get_request.after = 0; // FIXME: set current time as opposed to just 0

    try { return(mapper.writeValueAsString(get_request)); } // try to return the get request as json string
    catch (JsonProcessingException error) { return(""); } // if any errors occour, return an empty string
  }
}
