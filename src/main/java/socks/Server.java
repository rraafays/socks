package socks;

// input output library
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

// network library
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// jackson library
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

// object used to mask json strings to determine their _class attribute
@JsonIgnoreProperties(ignoreUnknown = true)
class Mask { public String _class; public String identity; } // E.G: if (mask._class.equals("OpenRequest")) { Add_Channel(request_json, client_handler); }
// objects to map json sent by the client into
class Message { public String _class; public String from; public int when; public String body; }
class Open_Request { public String _class; public String identity; }
class Publish_Request { public String _class; public String identity; public Message message; }
class Subscribe_Request { public String _class; public String identity; public String channel; }
class Unsubscribe_Request { public String _class; public String identity; public String channel; }
class Get_Request { public String _class; public String identity; public int after; }

public class Server
{
  private final static int PORT = 12345; // constant port number
  private ServerSocket server_socket; // server socket

  public Server(ServerSocket server_socket) { this.server_socket = server_socket; } // constructor which sets server socket

  public static void main(String[] args) throws IOException
  {
    ServerSocket server_socket = new ServerSocket(PORT); // create server socket at our port number
    Server server = new Server(server_socket); // pass that server socket to our constructor
    server.Start(); // start server
  }

  public void Start() // start the server
  {
    try
    {
      while (!server_socket.isClosed()) // while the server socket is open
      {
        Socket socket = server_socket.accept(); // wait for and accept incoming sockets

        Client_Handler client_handler = new Client_Handler(socket); // create client handler instance for that socket
        Thread thread = new Thread(client_handler); // create thread for that client handler
        thread.start(); // start the thread
      }
    }
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  public void Stop() // stop the server gracefully to avoid errors
  {
    try { if (server_socket != null) { server_socket.close(); } } // if the server socket is not null, close the server socket
    catch (IOException error) { error.printStackTrace(); } // if any errors occour, print them
  }
}

class Client_Handler implements Runnable // implement runnable to allow instances of this object to run on threads
{
  private final static String PATH = "log"; // constant log path string
  private static ObjectMapper mapper = new ObjectMapper(); // static object mapper used to map json strings to objects
  public static ArrayList<Client_Handler> client_handlers = new ArrayList<Client_Handler>(); // static array of client handlers to keep track of all open channels

  private Socket socket; // private socket
  private BufferedReader reader; // private reader to read from socket
  private BufferedWriter writer; // private writer to write to socket
  public String identity; // public identity for the client
  public ArrayList<String> subscribed_channels = new ArrayList<String>(); // array to keep track of channels the client has subscribed to
  public ArrayList<String> message_board = new ArrayList<String>(); // array to keep track of messages published to the client

  public Client_Handler(Socket socket)
  {
    try
    {
      this.socket = socket; // set the given socket to socket
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // get the socket's input stream, create an input reader out of that stream, then create a buffered reader out of that reader 
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // get the socket's output stream, create an output writer out of that stream, then create a buffered writer out of that writer

    }
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  @Override // override run method of client handler class since it is runnable
  public void run()
  {
    String json; // empty string to store json received from the client
    while (socket.isConnected()) // while the socket is connected
    {
      try
      {
        json = reader.readLine(); // json is read from the client
        if (json == null) { Stop(); break; } // if json is null, gracefully stop

        BufferedWriter log = new BufferedWriter(new FileWriter(PATH, true)); // open filewriter for a file specified by path and set it to autoflush
        String _class = mapper.readValue(json, Mask.class)._class; // isolate the _class attribute by masking the json string using mask object

        if (_class.equals("OpenRequest")) { Open(json); } // if open request, open
        if (_class.equals("PublishRequest")) { Publish(json); } // if publish request, publish 
        if (_class.equals("SubscribeRequest")) { Subscribe(json); } // if subscribe request, subscribe
        if (_class.equals("UnsubscribeRequest")) { Unsubscribe(json); } // if unsubscribe request, unsubscribe
        if (_class.equals("GetRequest")) { Get(json); } // if get request, get

        log.append(json + "\n"); // append the json and add line break to the json
        log.close(); // close the file
      }
      catch (IOException error) { Stop(); break; } // if any errors occour, gracefully stop
    }
  }

  void Stop()
  {
    client_handlers.remove(this); // remove this client handler from the client handlers array
    try
    {
      if (this.reader != null) { this.reader.close(); } // if the reader is not null, close it
      if (this.writer != null) { this.writer.close(); } // if the writer is not null, close it
      if (this.socket != null) { this.socket.close(); } // if the socket is not null, close it
    }
    catch (IOException error) { error.printStackTrace(); } // if any errors, occour, print them
  }

  void Open(String json) // open channel
  {
    try
    {
      this.identity = mapper.readValue(json, Open_Request.class).identity; // set the identity of the client handler to specified identity
      subscribed_channels.add(identity); // subscribe to itself
      client_handlers.add(this); // track this client handler
      /* TODO: send success response to client */
    }
    catch (JsonProcessingException error) { /* TODO: send error response to client */ }
    try
    {
      BufferedReader log = new BufferedReader(new FileReader(PATH)); // create buffered reader from file reader of specified file
      String line; // null string to contain lines
      while ((line = log.readLine()) != null) // start the reader, while current line is not null
      {
        String _class = mapper.readValue(line, Mask.class)._class; // isolate the _class attribute by masking the json string
        String identity = mapper.readValue(line,Mask.class).identity; // isolate the identity attribute by masking the json string
        if (this.identity.equals(identity)) // if the identity contained in the json string matches client handler's identity
        {
          if (_class.equals("PublishRequest")) { Publish(line); } // if publish request, publish
          if (_class.equals("SubscribeRequest")) { Subscribe(line); } // if subscribe request, subscribe
          if (_class.equals("UnsubscribeRequest")) { Unsubscribe(line); } // if unsubscribe request, unsubscribe
        }
      }
      log.close(); // close the log
    }
    catch (IOException errors) { Stop(); } // if any errors occour, gracefully stop
  }

  void Publish(String json)
  {
    try
    {
      Publish_Request publish_request = mapper.readValue(json, Publish_Request.class); // read publish request
      for (Client_Handler client_handler : client_handlers) // for each client handler in client handlers
      {
        if (client_handler.identity.equals(publish_request.identity)) // if the client handler's identity is equal to the specified publish request identity
        {
          client_handler.message_board.add(mapper.writeValueAsString(publish_request.message)); // add the message specified in the publish request to their message board
        }
      }
      try 
      {
        writer.write("publish");
        writer.newLine();
        writer.flush();
      }
      catch (IOException error) { error.printStackTrace(); }
      /* TODO: send success response to client */
    }
    catch (JsonProcessingException error) { /* TODO: send error response to client */ }
  }

  void Subscribe(String json) // subscribe to channel
  {
    try
    {
      String channel = mapper.readValue(json, Subscribe_Request.class).channel; // make string called channel which stores the channel provided by the json string

      boolean found = false; // boolean which is set to true once the specified channel has been found
      for (Client_Handler client_handler : client_handlers) // for each client handler in client handlers
      {
        if (client_handler.identity.equals(channel)) // if the client handler's identity matches the specified channel
        { 
          subscribed_channels.add(client_handler.identity); // subscribe to it by adding it to subscribed channels
          found = true; // set found to true
          /* TODO: send success response to client */
        }
      }
      if (!found) // if the channel has not been found
      {
        Error_Response  error_response = new Error_Response(); // create new error response
        error_response._class = "ErrorResponse"; // set _class to error response
        error_response.error = "NO SUCH CHANNEL:" + ' ' + channel; // report that there's no such channel
        System.out.println(mapper.writeValueAsString(error_response)); // FIXME: send the response as opposed to write it
      }
      try 
      {
        writer.write("subscribe");
        writer.newLine();
        writer.flush();
      }
      catch (IOException error) { error.printStackTrace(); }
    }
    catch (JsonProcessingException error) { /* TODO: send error response to client */ }
  }

  void Unsubscribe(String json) // subscribe to channel
  {
    try
    {
      String channel = mapper.readValue(json, Unsubscribe_Request.class).channel; // make string called channel which stores the channel provided by the json string
      subscribed_channels.remove(channel);
      try 
      {
        writer.write("unsubscribe");
        writer.newLine();
        writer.flush();
      }
    catch (IOException error) { error.printStackTrace(); }
    }
    catch (JsonProcessingException error) { /* TODO: send error response to client */ }
  }

  void Get(String json) // get messages
  {
    for (Client_Handler client_handler : client_handlers) // for each client handler in client handlers
    {
      if (subscribed_channels.contains(client_handler.identity)) // if subscribed to the channel
      {
        for (String string : client_handler.message_board) { System.out.println(string); } /* TODO: add to send message list response as opposed to just writing each message on the server */
      }
    }
    try 
    {
      writer.write("get");
      writer.newLine();
      writer.flush();
    }
    catch (IOException error) { error.printStackTrace(); }
    /* TODO: send message list response to client */
  }
}
