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

    final int PORT = 12345; // constant port number
    ServerSocket ss; // server socket object
    Socket s; // socket object
    BufferedReader i; // buffered reader for socket
    PrintWriter o; // print writer for writing data into the socket
    Scanner sc = new Scanner(System.in);

    try // try to instantiate our objects
    { 
      ss = new ServerSocket(PORT); // server socket constructor requires port number
      s = ss.accept(); // server socket accept method to wait for and accept connections
      o = new PrintWriter(s.getOutputStream()); // create print writer from client output stream
      i = new BufferedReader(new InputStreamReader(s.getInputStream())); // create buffered reader from client input stream

      Thread sender = new Thread(new Runnable() 
      {
        String json; // variable that will contain data written by client
        @Override // override the run method
        public void run()
        {
          while(true)
          {
            json = sc.nextLine(); // reads data from client terminal
            o.println(json); // write data stored in msg
            o.flush(); // flushes the printwriter
          }
        }
      });

      Thread listener = new Thread(new Runnable() 
      {
        String json;
        @Override
        public void run()
        {
          try
          {
            
          }
          catch
        }
      });
      sender.start(); // starts the sender
    }
    catch (IOException e) { e.printStackTrace(); } // if any input output errors occour, print them

  }
}
