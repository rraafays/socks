package socks;

// input output library
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

// network library
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

// utility library
import java.util.Scanner;

// jackson json library
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

// objects to map json sent by the client into
class Message { public String _class; public String from; public int when; public String body; }
class Open_Request { public String _class; public String identity; }
class Publish_Request { public String _class; public String identity; public Message message; }
class Subscribe_Request { public String _class; public String identity; public String channel; }
class Get_Request { public String _class; public String identity; public int after; }

// 1 - client sends openrequest identifying the channel to publish on
// 2 - server responds with success if it succeedes
// 3 - client sends either publish, subscribe, unsubscribe or get requests
// 4 - in case of get, server responds with messagelist otherwise server responds with success or error
// 5 - loop 3

public class Server
{
  private static int PORT = 12345; // constant integer containing port number
  private ServerSocket server_socket; // server socket used to accept client sockets

  public Server(ServerSocket server_socket) { this.server_socket = server_socket; } // constructor which sets the server socket of the server

  public static void main(String[] args) throws IOException
  {
    ServerSocket server_socket = new ServerSocket(PORT);
    Server server = new Server(server_socket);
    server.Start();
  }

  public void Start() // method to start the server
  {
    try
    {
      while (!server_socket.isClosed()) // while the socket server socket is open
      {
        Socket socket = server_socket.accept(); // accept new sockets into the server socket
        System.out.println("Client connected."); // output that a new client has connected
        Handler handler = new Handler(socket); // create a client handler for this socket
        Thread thread = new Thread(handler);
      }
    }
    catch (IOException e) { e.printStackTrace(); } // if any errors occour, print them
  }
  
  public void Stop() // method to stop server
  {
    try
    {
      if (server_socket != null) // if the server socket exists
      {
        server_socket.close(); // close it
      }
    }
    catch (IOException e) { e.printStackTrace(); } // if any errors occour, print them
  }
}

class Handler implements Runnable // by implementing this class as runnable we can execute it on multiple threads
{
  private Socket socket; // the client socket which needs handling
  private BufferedReader reader;
  private BufferedWriter writer;
  private String identity;
  public static ArrayList<Handler> handlers = new ArrayList<>(); // static as all handlers must access the same array instead of having their own instances

  public Handler(Socket socket) // constructor which sets the client socket
  { 
    try
    {
      this.socket = socket;  // assigns socket to the handler
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // creates buffered reader from the input stream reader
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // creates buffered writer from the output stream reader
      this.identity = reader.readLine(); // gets the client's identity from their input
      handlers.add(this); // adds the current handler to an array of handlers
      Open_Request open_request = new Open_Request(); // create open request
      open_request._class = "OpenRequest"; open_request.identity = identity + " has entered the chat"; // sets the _class and identity
      // TODO: interpret the request
    }
    catch (IOException e) { e.printStackTrace(); /* TODO: method to close everything */ } // if any errors occour, print them
  } 

  @Override
  public void run() // because our class is runnable we must override the run method
  {
    String json;
    while (socket.isConnected()) // while the client is connected
    {
      try { json = reader.readLine(); /* TODO interpret the request */ } // read json written manually by the client
      catch (IOException e) { e.printStackTrace(); /* TODO: method to close everything */ break; } // if any errors occour, print them
    }
  }
}
