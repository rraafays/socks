package socks;

// input output library
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
// network library
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// jackson library
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

// dummy class which only contains _class used to mask json strings
@JsonIgnoreProperties(ignoreUnknown = true)
class Mask { public String _class; }

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
  final static int PORT = 12345; // constant port number
  static ObjectMapper mapper = new ObjectMapper(); // json object mapper
  static ArrayList<String> channels = new ArrayList<String>();

  public static void main(String[] args) throws IOException
  {
    System.out.println("waiting for clients.."); // write message expecting clients
    ServerSocket server_socket = new ServerSocket(PORT); // create server socket at our port (12345)

    Client_Handler client_handler = new Client_Handler(); // create client handler
    client_handler.socket = server_socket.accept(); // accept the client handlers socket into the server socket
    System.out.println("connection established."); // write that a connection has been established
    client_handler.reader = new BufferedReader(new InputStreamReader(client_handler.socket.getInputStream())); // create buffered reader from the socket's input  

    while (true) // infinite while loop
    {
      String request_json = client_handler.reader.readLine(); // recieve requests in the form of json strings from the client
      Mask mask = mapper.readValue(request_json, Mask.class); // mask the request sent by the client to determine it's _class attribute

      if (mask._class.equals("OpenRequest")) { Add_Channel(request_json, client_handler); } // if an open request is recieved then add the channel to the channels list
    }
  }

  static void Add_Channel(String open_request_json, Client_Handler client_handler) // add channel to the channels list from an open request
  {
    try 
    { 
      Open_Request open_request = mapper.readValue(open_request_json, Open_Request.class); // map the open request json string to an open request object
      channels.add(open_request.identity); // add the specified identity to the channels list from the open request
      client_handler.identity = open_request.identity;
      System.out.println(client_handler.identity + " has joined the chat");
    }
    catch (JsonProcessingException e) { e.printStackTrace(); } // if any errors occur, print them
  }
}

class Client_Handler { public String identity; public Socket socket; public BufferedReader reader; } // client handler class to set attributes for each client
