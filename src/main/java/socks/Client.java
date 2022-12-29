package socks;

// input output library
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

// network library
import java.net.Socket;
import java.util.Scanner;

// jackson library
import com.fasterxml.jackson.databind.ObjectMapper;

// objects to map json sent by the server into
class Success_Response { public String _class; }
class Error_Response { public String _class; public String error; }
class Message_List_Response { public String _class; public Message[] messages; }

public class Client
{
  private static ObjectMapper mapper = new ObjectMapper(); // json object mapper
  private static Scanner scanner = new Scanner(System.in);
  private final static String ADDR = "localhost"; // constant address string
  private final static int PORT = 12345; // constant port number
  private Socket socket;
  private BufferedReader reader;
  private BufferedWriter writer;
  private String identity; // identity string

  public Client(Socket socket, String identity)
  {
    try
    {
      this.socket = socket;
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.identity = identity;
    }
    catch (IOException error) { Stop(); }
  }

  public static void main(String[] args) throws IOException
  {
    Socket socket = new Socket(ADDR, PORT); // create a new socket and connect to ADDR:PORT (localhost:12345)
    System.out.println("Enter identity: "); // prompt client for an identity
    String identity = scanner.nextLine();
    Client client = new Client(socket, identity);

    Open_Request open_request = new Open_Request(); // create new open request
    open_request._class = "OpenRequest"; open_request.identity = identity; // initialise using client identity
    client.writer.write(mapper.writeValueAsString(open_request)); // write the open request back to the server for it to interpret
    client.writer.newLine();
    client.writer.flush();

    while (true) // infinite while loop
    {
      ShowMenu(); 
      
      String option = scanner.nextLine(); // grab an argument number from the client's input
      if (option.equals("1")) { client.Publish(); client.Receive_Response(); } // if option is 1, publish then await response
      if (option.equals("2")) { client.Subscribe(); client.Receive_Response(); } // if option is 2, subscribe then await response
      if (option.equals("3")) { client.Unsubscribe(); client.Receive_Response(); } // if option is 3, unsubscribe then await response
      if (option.equals("4")) { client.Get(); client.Receive_Response(); } // if option is 4, get messages then await response
    }
  }

  static void ShowMenu() // print options menu
  {
    System.out.println("[1] \u001B[35mPublish\u001B[0m, [2] \u001B[36mSubscribe\u001B[0m, [3] \u001B[31mUnsubscribe\u001B[0m, [4] \u001B[33mGet Messages\u001B[0m");
  }

  void Stop()
  {
    try
    {
      if (this.reader != null) { this.reader.close(); } // if the reader is not null, close it
      if (this.writer != null) { this.writer.close(); } // if the writer is not null, close it
      if (this.socket != null) { this.socket.close(); } // if the socket is not null, close it
    }
    catch (IOException error) { error.printStackTrace(); } // if any errors, occour, print them
  }

  void Receive_Response()
  {
    String json;
    try
    {
      json = reader.readLine();
      System.out.println(json); /* FIXME: interpret the response as opposed to just printing it */
    }
    catch (IOException error) { Stop(); }
  }

  void Publish() // build publish request
  {
    System.out.println("\u001B[35mWho's channel would you like to publish on? \u001B[0m");
    String channel = scanner.nextLine();
    System.out.println("\u001B[35mWhat would you like to say? \u001B[0m");
    String message = scanner.nextLine();

    Publish_Request publish_request = new Publish_Request(); // create publish request object
    publish_request._class = "PublishRequest"; // set the _class 
    publish_request.identity = channel; // set the identity to specified channel
    publish_request.message = new Message(); // create new message
    publish_request.message._class = "Message"; // set the _class
    publish_request.message.from = this.identity; // set from to identity
    publish_request.message.when = 0; // FIXME: set current time as opposed to just 0
    publish_request.message.body = message; // set body

    try 
    { 
      this.writer.write(mapper.writeValueAsString(publish_request));
      this.writer.newLine();
      this.writer.flush();
    } 
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Subscribe() // build subscribe request
  {
    System.out.println("\u001B[36mWho would you like to subscribe to? \u001B[0m"); // ask user who they would like to subscribe to
    String channel = scanner.nextLine();

    Subscribe_Request subscribe_request = new Subscribe_Request(); // create subscribe request object
    subscribe_request._class = "SubscribeRequest"; // set the _class
    subscribe_request.identity = this.identity; // set identity
    subscribe_request.channel = channel; // set channel
    
    try 
    { 
      this.writer.write(mapper.writeValueAsString(subscribe_request));
      this.writer.newLine();
      this.writer.flush();
    } 
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Unsubscribe() // build unsubscribe request
  {
    System.out.println("\u001B[31mWho would you like to unsubscribe from? \u001B[0m"); // ask user who they would like to unsubscribe from
    String channel = scanner.nextLine();

    Unsubscribe_Request unsubscribe_request = new Unsubscribe_Request(); // create unsubscribe request object
    unsubscribe_request._class = "UnsubscribeRequest"; // set the _class
    unsubscribe_request.identity = this.identity;
    unsubscribe_request.channel = channel;

    try 
    { 
      this.writer.write(mapper.writeValueAsString(unsubscribe_request));
      this.writer.newLine();
      this.writer.flush();
    } 
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Get() // build get request
  {
    Get_Request get_request = new Get_Request(); // create get request object
    get_request._class = "GetRequest";
    get_request.identity = this.identity; // set identity
    get_request.after = 0; // FIXME: set current time as opposed to just 0

    try 
    { 
      this.writer.write(mapper.writeValueAsString(get_request));
      this.writer.newLine();
      this.writer.flush();
    } 
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }
}
