package socks;

// input output library
import java.io.IOException;

// network library
import java.net.ServerSocket;
import java.net.Socket;

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
  final static int PORT = 12345;

  public static void main(String[] args) throws IOException
  {
    System.out.println("waiting for clients..");
    ServerSocket server_socket = new ServerSocket(PORT);
    Socket socket = server_socket.accept();
    System.out.println("connection established.");
  }
}
