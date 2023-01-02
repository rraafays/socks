package socks;

// input output library
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

// network library
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

// jackson library
import com.fasterxml.jackson.databind.ObjectMapper;

// objects to map json sent by the server into
class Success_Response { public String _class; }
class Error_Response { public String _class; public String error; }
class Message_List_Response { public String _class; public ArrayList<Message> messages; }

public class Client
{
  private static ObjectMapper mapper = new ObjectMapper(); // json object mapper
  private static Scanner scanner = new Scanner(System.in);
  private final static String ADDR = "localhost"; // constant address string
  private final static int PORT = 12345; // constant port number
  private Socket socket; // private socket
  private BufferedReader reader; // private reader to read from socket
  private BufferedWriter writer; // private reader to write to socket
  private String identity; // identity string

  public Client(Socket socket, String identity) // client constructor
  {
    try
    {
      this.socket = socket; // set the given socket to socket
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // get the socket's input stream, create an input reader out of that stream, then create a buffered reader out of that reader
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // get the socket's output stream, create an output writer out of that stream, then create a buffered writer out for that writer
      this.identity = identity; // set the given identity to identity
    }
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  public static void main(String[] args) throws IOException
  {
    try 
    {
      Socket socket = new Socket(ADDR, PORT); // create a new socket and connect to ADDR:PORT (localhost:12345)
      System.out.println("Enter identity: "); // prompt client for an identity
      String identity = scanner.nextLine(); // take the next line entered by the user as the client identity
      Client client = new Client(socket, identity); // create an instance of client with the specific socket and identity

      Open_Request open_request = new Open_Request(); // create new open request
      open_request._class = "OpenRequest"; open_request.identity = identity; // initialise using client identity
      client.writer.write(mapper.writeValueAsString(open_request)); // write the open request back to the server for it to interpret
      client.writer.newLine(); // send new line to the server
      client.writer.flush(); // manually flush the writer to make sure it is ready to be used again

      while (true) // infinite while loop
      {
        ShowMenu(); // print the menu
        
        String option = scanner.nextLine(); // grab an argument number from the client's input
        if (option.equals("1")) { client.Publish(); client.Receive_Response(); } // if option is 1, publish then await response
        if (option.equals("2")) { client.Subscribe(); client.Receive_Response(); } // if option is 2, subscribe then await response
        if (option.equals("3")) { client.Unsubscribe(); client.Receive_Response(); } // if option is 3, unsubscribe then await response
        if (option.equals("4")) { client.Get(); client.Receive_Response(); } // if option is 4, get messages then await response
      }
    }
    catch (IOException error) { System.out.println("\u001B[31mstart the server first!\u001B[0m"); }
  }

  static void ShowMenu() // print options menu
  {
    System.out.println("\n[1] \u001B[35mPublish\u001B[0m, [2] \u001B[34mSubscribe\u001B[0m, [3] \u001B[31mUnsubscribe\u001B[0m, [4] \u001B[33mGet Messages\u001B[0m");
  }

  void Stop() // stop the client
  {
    try
    {
      if (this.reader != null) { this.reader.close(); } // if the reader is not null, close it
      if (this.writer != null) { this.writer.close(); } // if the writer is not null, close it
      if (this.socket != null) { this.socket.close(); } // if the socket is not null, close it
    }
    catch (IOException error) { error.printStackTrace(); } // if any errors, occour, print them
  }

  void Receive_Response() // receive response from the server
  {
    String json; // empty string for json
    try
    {
      json = reader.readLine(); // try to read from the server into our json variable
      String _class = mapper.readValue(json, Mask.class)._class; // isolate the _class attribute by masking the json string using mask object
      if (_class.equals("SuccessResponse")) { System.out.println("\u001B[32mRequest succeeded!\u001B[0m"); } // if the _class is success response, tell user request succeeded
      if (_class.equals("ErrorResponse")) { System.out.println("\u001B[31m" + mapper.readValue(json, Error_Response.class).error + "\u001B[0m"); } // if the _class is error response, tell user reason
      if (_class.equals("MessageListResponse")) // if the class is message list response
      {  
        Message_List_Response message_list_response = mapper.readValue(json, Message_List_Response.class); // read respnse into message list response object
        for (Message message : message_list_response.messages) // for each message in message list
        {
          System.out.println // println
          (
            "[TIME] IDENTITY: MESSAGE" // template string for substituting into
              .replaceAll("TIME", new SimpleDateFormat("HH:mm").format(message.when)) // substitute time for when
              .replaceAll("IDENTITY", message.from) // substitute id for from
              .replaceAll("MESSAGE", message.body) // substitute message for body
          ); // print the string post substitution
        }
      }
    }
    catch (IOException error) { Stop(); } // if any errors, occour, gracefully stop
  }

  void Publish() // build publish request
  {
    System.out.println("\u001B[35mWho's channel would you like to publish on? \u001B[0m"); // prompt the user for the name of channel
    String channel = scanner.nextLine(); // take the next line entered by the user as the specified channel
    System.out.println("\u001B[35mWhat would you like to say? \u001B[0m"); // prompt the user for message
    String message = scanner.nextLine(); // take the next line entered by the user as the message

    Publish_Request publish_request = new Publish_Request(); // create publish request object
    publish_request._class = "PublishRequest"; // set the _class 
    publish_request.identity = channel; // set the identity to specified channel
    publish_request.message = new Message(); // create new message
    publish_request.message._class = "Message"; // set the _class
    publish_request.message.from = this.identity; // set from to identity
    publish_request.message.when = System.currentTimeMillis(); // set when to the current time in milliseconds
    publish_request.message.body = message; // set body

    try 
    { 
      this.writer.write(mapper.writeValueAsString(publish_request)); // write json string of the publish request to the server
      this.writer.newLine(); // write newline to the server
      this.writer.flush(); // manually flush the writer to make sure it is ready to be used again
    } 
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Subscribe() // build subscribe request
  {
    System.out.println("\u001B[34mWho would you like to subscribe to? \u001B[0m"); // prompt the user for the name of channel
    String channel = scanner.nextLine(); // take the next line entered by the user as the specified channel

    Subscribe_Request subscribe_request = new Subscribe_Request(); // create subscribe request object
    subscribe_request._class = "SubscribeRequest"; // set the _class
    subscribe_request.identity = this.identity; // set identity
    subscribe_request.channel = channel; // set channel
    
    try 
    { 
      this.writer.write(mapper.writeValueAsString(subscribe_request)); // write json string of the subscribe request to the server
      this.writer.newLine(); // write newline to the server
      this.writer.flush(); // manually flush the writer to make sure it is ready to be used again
    } 
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Unsubscribe() // build unsubscribe request
  {
    System.out.println("\u001B[31mWho would you like to unsubscribe from? \u001B[0m"); // prompt the user for the name of channel
    String channel = scanner.nextLine(); // take the next line entered by the user as the specified channel

    Unsubscribe_Request unsubscribe_request = new Unsubscribe_Request(); // create unsubscribe request object
    unsubscribe_request._class = "UnsubscribeRequest"; // set the _class
    unsubscribe_request.identity = this.identity; // set the identity
    unsubscribe_request.channel = channel; // set the specified channel

    try 
    { 
      this.writer.write(mapper.writeValueAsString(unsubscribe_request)); // write json string of the unsubscribe request to the server
      this.writer.newLine(); // write newline to the server
      this.writer.flush(); // manually flush the writer to make sure it is ready to be used again
    } 
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Get() // build get request
  {
    Get_Request get_request = new Get_Request(); // create get request object
    get_request._class = "GetRequest"; // set the _class
    get_request.identity = this.identity; // set identity
    System.out.println("\u001B[33mAfter what date would you like messages from? E.G: 01/01/2023 10:30\u001B[0m"); // prompt the user for a date
    try { get_request.after = new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(scanner.nextLine()).getTime(); } // set after to the time in milliseconds between 1970 and their date
    catch (ParseException error) { get_request.after = 0; } // if an error occours, set after to 0 to receive all messages

    try 
    { 
      this.writer.write(mapper.writeValueAsString(get_request)); // write json string of the get request to the server
      this.writer.newLine(); // write newline to the server
      this.writer.flush(); // manually flush the writer to make sure it is ready to be used again
    } 
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }
}
