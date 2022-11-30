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
    ServerSocket server_socket; // server socket object
    Socket socket; // socket object
    BufferedReader in; // buffered reader for socket
    PrintWriter out; // print writer for writing data into the socket
    Scanner scanner = new Scanner(System.in);

    try // try to instantiate our objects
    { 
      server_socket = new ServerSocket(PORT); // server socket constructor requires port number
      socket = server_socket.accept(); // server socket accept method to wait for and accept connections
      out = new PrintWriter(socket.getOutputStream()); // create print writer from client output stream
      in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // create buffered reader from client input stream

      Thread sender = new Thread(new Runnable() // thread to interpret send json to clients
      {
        String json; // variable that will contain data written by client
        @Override // override the run method
        public void run()
        {
          while(true) // infinite while loop
          {
            json = scanner.nextLine(); // reads data from client terminal
            out.println(json); // write data stored in msg
            out.flush(); // flushes the printwriter
          }
        }
      });

      Thread listener = new Thread(new Runnable() // thread to listen for new json from clients
      {
        String json; // variable that will contain data written by client
        @Override // override the run method
        public void run() 
        {
          try // try to read json from the client
          {
            json = in.readLine();
            while(json != null) // while the client is active write any json they write
            {
              System.out.println(json);
              json = in.readLine();
            }
            System.out.println("Client disconnected"); // if the loop breaks then disconnect the client gracefully
            /* close resources */
            out.close();
            socket.close();
            server_socket.close();
          }
          catch (IOException e) { e.printStackTrace(); } // if any input output errors occour, print them
        }
      });
      sender.start(); // starts the sender
      listener.start(); // starts the listener
    }
    catch (IOException e) { e.printStackTrace(); } // if any input output errors occour, print them

  }
}
