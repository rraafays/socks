package socks;

// input output library
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

// network library
import java.net.ServerSocket;
import java.net.Socket;

// jackson library
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// dummy class which only contains _class used to mask json strings
@JsonIgnoreProperties(ignoreUnknown = true)
class Mask { public String _class; }

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
  static ObjectMapper mapper = new ObjectMapper(); // json object mapper
  final static int PORT = 12345; // constant port number

  public static void main(String[] args) throws IOException
  {
    System.out.println("waiting for clients.."); // write message expecting clients
    ServerSocket server_socket = new ServerSocket(PORT); // create server socket at our PORT (12345)
    Socket socket = server_socket.accept(); // accept a single socket into our server socket
    System.out.println("connection established."); // write that a connection has been established

    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // create buffered reader from the socket's input stream
    while (true) // infinite while loop
    {
      Mask mask = mapper.readValue(reader.readLine(), Mask.class); // create mask from json received from client
      if (mask._class.equals("OpenRequest")) { System.out.println("test succeeded"); } // TODO: handle open request
    }
  }
}
