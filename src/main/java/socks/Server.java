package socks;

// input output library
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
// network library
import java.net.ServerSocket;
import java.net.Socket;

// utility library
import java.util.Scanner;

// jackson json library
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

// objects to map json sent by the client into
class Message { public String _class; public String from; public int when; public String body; }
class OpenRequest { public String _class; public String identity; }
class PublishRequest { public String _class; public String identity; public Message message; }
class SubscribeRequest { public String _class; public String identity; public String channel; }
class GetRequest { public String _class; public String identity; public int after; }

public class Server
{
  static ObjectMapper om = new ObjectMapper();

  public static void main(String[] args)
  {
    // 1 - client sends openrequest identifying the channel to publish on
    // 2 - server responds with success if it succeedes
    // 3 - client sends either publish, subscribe, unsubscribe or get requests
    // 4 - in case of get, server responds with messagelist otherwise server responds with success or error
    // 5 - loop 3


    final int PORT = 12345;
    ServerSocket ss;
    Socket s;
    BufferedReader i;
    PrintWriter o;
    Scanner sc = new Scanner(System.in);

    try 
    { 
      ss = new ServerSocket(PORT); 
      s = ss.accept();
      o = new PrintWriter(s.getOutputStream());
      i = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }
    catch (IOException e) { e.printStackTrace(); }

    Thread sender = new Thread(new Runnable()
    {
      while(true)
      {
        
      }
    });
  }
}
