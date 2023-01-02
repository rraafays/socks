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
class Message { public String _class; public String from; public long when; public String body; }
class Open_Request { public String _class; public String identity; }
class Publish_Request { public String _class; public String identity; public Message message; }
class Subscribe_Request { public String _class; public String identity; public String channel; }
class Unsubscribe_Request { public String _class; public String identity; public String channel; }
class Get_Request { public String _class; public String identity; public long after; }

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
  public ArrayList<Message> message_board = new ArrayList<Message>(); // array to keep track of messages published to the client
  public boolean open; // boolean to tell if client is ready to receive responses

  public Client_Handler(Socket socket) // client handler constructor
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
    }
    catch (JsonProcessingException error) { Stop(); } // if any errors occour, gracefully stop
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
      this.open = true;
    }
    catch (IOException errors) { Stop(); } // if any errors occour, gracefully stop
  }

  void Respond_Error(String reason) // send error response
  {
    try 
    {
      if (open) // if the client is open
      {
        Error_Response error_response = new Error_Response(); // create new error response
        error_response._class = "ErrorResponse"; // set the _class
        error_response.error = reason; // set error to reason
        writer.write(mapper.writeValueAsString(error_response)); // write the error response to the client
        writer.newLine(); // write newline to the client
        writer.flush(); // manually flush the writer to make sure it is ready to be used again
      }
    }
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }
  
  void Respond_Success() // send success response
  {
    try 
    {
      if (open) // if the client is open
      {
        Success_Response success_response = new Success_Response(); // create new success response
        success_response._class = "SuccessResponse"; // set the _class
        writer.write(mapper.writeValueAsString(success_response)); // write the success response to the client
        writer.newLine(); // write newline to the client
        writer.flush(); // manually flush the writer to make sure it is ready to be used again
      }
    }
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Respond_MessageList(ArrayList<Message> messages) // send message list response
  {
    try
    {
      if (open)
      {
        Message_List_Response message_list_response = new Message_List_Response(); // create new message list response
        message_list_response._class = "MessageListResponse"; // set the _class
        message_list_response.messages = messages; // set the messages of the reponse to the provided messages
        writer.write(mapper.writeValueAsString(message_list_response)); // write the message list response to the client
        writer.newLine(); // write newline to the client
        writer.flush(); // manually flush the writer to make sure it is ready to be used again
      }
    }
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Publish(String json)
  {
    try
    {
      Publish_Request publish_request = mapper.readValue(json, Publish_Request.class); // read publish request
      boolean found = false; // boolean which is set to true once the specified channel has been found
      for (Client_Handler client_handler : client_handlers) // for each client handler in client handlers
      {
        if (client_handler.identity.equals(publish_request.identity)) // if the client handler's identity is equal to the specified publish request identity
        {
          client_handler.message_board.add(publish_request.message); // add the message specified in the publish request to their message board
          found = true; // set found to true
          Respond_Success(); // send success response
        }
      }
      if (!found) { Respond_Error("NO SUCH CHANNEL: " + publish_request.identity); } // if no matching channel is found, respond with an error
    }
    catch (JsonProcessingException error) { Respond_Error("MESSAGE TOO BIG"); } // if json fails to process, send error response with the message being too big to encode as the reason
  }

  void Subscribe(String json) // subscribe to channel
  {
    try
    {
      String channel = mapper.readValue(json, Subscribe_Request.class).channel; // make string called channel which stores the channel provided by the json string
      if (subscribed_channels.contains(channel)) { Respond_Error("ALREADY SUBSCRIBED: " + channel); return;}; // if the specified channel is already in the subscribed channels list, send error response

      boolean found = false; // boolean which is set to true once the specified channel has been found
      for (Client_Handler client_handler : client_handlers) // for each client handler in client handlers
      {
        if (client_handler.identity.equals(channel)) // if the client handler's identity matches the specified channel
        { 
          subscribed_channels.add(client_handler.identity); // subscribe to it by adding it to subscribed channels
          found = true; // set found to true
          Respond_Success(); // send success response
        }
      }
      if (!found) { Respond_Error("NO SUCH CHANNEL: " + channel); } // if no matching channel is found, respond with an error
    }
    catch (JsonProcessingException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Unsubscribe(String json) // unsubscribe to channel
  {
    try
    {
      String channel = mapper.readValue(json, Unsubscribe_Request.class).channel; // make string called channel which stores the channel provided by the json string
      if (channel.equals(this.identity)) { Respond_Error("CANNOT UNSUBSCRIBE FROM SELF"); return; } // if the specified channel is the client's own identity, send error resonse

      boolean found = false;
      for (Client_Handler client_handler : client_handlers) // for each client handler in client handlers
      {
        if (client_handler.identity.equals(channel)) // if the client handler's identity matches the specified channel
        { 
          subscribed_channels.remove(client_handler.identity); // unsubscribe from it by removing it from subscribed channels
          found = true; // set found to true
          Respond_Success(); // send success response
        }
      }
      if (!found) { Respond_Error("NO SUCH CHANNEL: " + channel); }
    }
    catch (IOException error) { Stop(); } // if any errors occour, gracefully stop
  }

  void Get(String json) // get messages
  {
    ArrayList<Message> messages = new ArrayList<Message>();
    for (Client_Handler client_handler : client_handlers) // for each client handler in client handlers
    {
      if (subscribed_channels.contains(client_handler.identity)) // if subscribed to the channel
      {
        for (Message message : client_handler.message_board) 
        { 
          try { if (message.when > mapper.readValue(json, Get_Request.class).after) { messages.add(message); } } // try to get every message after the time given
          catch (JsonProcessingException error) { Respond_Error("INVALID TIME"); } // if any errors occour, send error response
        }
      }
    }
    Respond_MessageList(messages);
  }
}
