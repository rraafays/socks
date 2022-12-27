package socks;

// input output library
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
class Mask { public String _class; } // E.G: if (mask._class.equals("OpenRequest")) { Add_Channel(request_json, client_handler); }
// objects to map json sent by the client into
class Message { public String _class; public String from; public int when; public String body; }
class Open_Request { public String _class; public String identity; }
class Publish_Request { public String _class; public String identity; public Message message; }
class Subscribe_Request { public String _class; public String identity; public String channel; }
class Get_Request { public String _class; public String identity; public int after; }

// 1 - client sends open request identifying the channel to publish on
// 2 - server responds with success if it succeedes
// 3 - client sends either publish, subscribe, unsubscribe or get requests
// 4 - in case of get, server responds with messagelist otherwise server responds with success or error
// 5 - loop 3

public class Server
{
  private final static int PORT = 12345; // constant port number
  private static ObjectMapper mapper = new ObjectMapper(); // json object mapper
  private ServerSocket server_socket;

  public Server(ServerSocket server_socket) { this.server_socket = server_socket; } // constructor which sets server socket

  public static void main(String[] args) throws IOException
  {
    ServerSocket server_socket = new ServerSocket(PORT); // create server socket at our port number
    Server server = new Server(server_socket); // pass that server socket to our constructor
    server.Start(); // start server
  }

  public void Start() // start the server by initialising it and connecting new clients
  {
    try
    {
      while (!server_socket.isClosed()) // while the server socket is open
      {
        Socket socket = server_socket.accept(); // wait for and accept incoming sockets

        Client_Handler client_handler = new Client_Handler(socket);
        Thread thread = new Thread(client_handler);
        thread.start();
      }
    }
    catch (IOException error) { Stop(); }
  }

  public void Stop() // stop the server gracefully to avoid errors
  {
    try
    {
      if (server_socket != null) // avoid null pointer exceptions
      {
        server_socket.close(); // close the socket
      }
    }
    catch (IOException error) { error.printStackTrace(); }
  }

  static void Add_Channel()
  {
  }
}

class Client_Handler implements Runnable
{
  private final static String PATH = "log"; // constant log path string
  private static ObjectMapper mapper = new ObjectMapper(); // static object mapper used to map json strings to objects
  public static ArrayList<Client_Handler> open_channels = new ArrayList<Client_Handler>(); // static array of client handlers to keep track of all open channels

  private Socket socket; // private socket
  private BufferedReader reader; // private reader to read from socket
  private BufferedWriter writer; // private writer to write to socket
  public String identity; // public identity for the client
  public ArrayList<Client_Handler> subscribed_channels = new ArrayList<Client_Handler>(); // array of client handlers which the user has subscribed to

  public Client_Handler(Socket socket)
  {
    try
    {
      this.socket = socket; // set the given socket to socket
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // get the socket's input stream, create an input reader out of that stream, then create a buffered reader out of that reader 
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // get the socket's output stream, create an output writer out of that stream, then create a buffered writer out of that writer

      String json = reader.readLine(); // read json string from the client
      if (mapper.readValue(json, Mask.class)._class.equals("OpenRequest")) { this.identity = mapper.readValue(json, Open_Request.class).identity; } // mask json to determine if it's an open request, if it is, set the identity to the identity stored in the open request
      open_channels.add(this);
      /* TODO: method to send success response */
    }
    catch (IOException error) { /* TODO: method to stop client handler */ } // if any errors occour, gracefully close
  }

  @Override
  public void run()
  {
    String json;

    while (socket.isConnected())
    {
      try
      {
        json = reader.readLine();
        System.out.println(json);
        BufferedWriter log = new BufferedWriter(new FileWriter(PATH, true));
        log.append(json + "\n");
        log.close();
        /* TODO: save the message to chat log and use method to send success response */
      }
      catch (IOException error) { /* TODO: method to stop client handler */ break; }
    }
  }
}
