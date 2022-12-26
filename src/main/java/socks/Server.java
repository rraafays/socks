package socks;

// input output library
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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
  private static ArrayList<String> channels = new ArrayList<String>();
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
        System.out.println("new socket has connected!"); // output that a new socket has connected
        System.out.println("waiting for open request.."); // output that the server is waiting for an open request
        /* TODO: handle open request and create channel */

        Client_Handler client_handler = new Client_Handler();
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

  static void Add_Channel(String open_request_json, Client_Handler client_handler) // add channel to the channels list from an open request
  {
    try 
    { 
      Open_Request open_request = mapper.readValue(open_request_json, Open_Request.class); // map the open request json string to an open request object
      channels.add(open_request.identity); // add the specified identity to the channels list from the open request
      client_handler.identity = open_request.identity; // set the identity of the client handler using the open request
      System.out.println(client_handler.identity + " has joined the chat"); // display that the client has connected referencing their name
    }
    catch (JsonProcessingException e) { e.printStackTrace(); } // if any errors occur, print them
  }
}
