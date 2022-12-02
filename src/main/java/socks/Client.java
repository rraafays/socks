package socks;

// input output library
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

// network library
import java.net.Socket;

// utility library
import java.util.Scanner;

// jackson json library
import com.fasterxml.jackson.databind.ObjectMapper;

// objects to map json sent by the server into
class SuccessResponse { public String _class; }
class ErrorResponse { public String _class; public String error; }
class MessageListResponse { public String _class; public Message[] messages; }

public class Client
{
  private static int PORT = 12345; // constant integer containing port number
  private static String ADDR = "localhost"; 
  private Socket socket;
  private BufferedReader reader;
  private BufferedWriter writer;
  private String identity;
  private static ObjectMapper mapper;

  public Client(Socket socket, String identity)
  {
    try
    {
      this.socket = socket;
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      this.identity = identity;
    }
    catch (IOException e) { e.printStackTrace(); }
  }

  public static void main(String[] args)
  {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Enter identity: ");
    String identity = scanner.nextLine();
    try 
    { 
      Socket socket = new Socket(ADDR, PORT); 
      Client client = new Client(socket, identity);
      client.Listen();
      client.Publish();
    }
    catch (IOException e) { e.printStackTrace(); }
  }

  public void Publish()
  {
    try
    {
      writer.write(identity);
      writer.newLine();
      writer.flush();
      
      Scanner scanner = new Scanner(System.in);
      while(socket.isConnected()) 
      { 
        String json = scanner.nextLine(); 
        Publish_Request publish_request = mapper.readValue(json, Publish_Request.class);
        writer.write(publish_request.identity + ": " + publish_request.message.body);
        writer.newLine();
        writer.flush();
      }
    }
    catch (IOException e) { e.printStackTrace(); }
  }

  public void Listen()
  {
    new Thread(new Runnable() 
    {
      @Override
      public void run()
      {
        String response;
        while(!socket.isClosed())
        {
          try
          {
            response = reader.readLine();
            System.out.println(response);
          }
          catch (IOException e) { e.printStackTrace(); }
        }
      }
    }).start();
  }

  public void End()
  {
    try
    {
      if (reader != null) { reader.close(); }
      if (writer != null) { writer.close(); }
      if (socket != null) { socket.close(); }
    }
    catch (IOException e) { e.printStackTrace(); } // if any errors occour, print them
  }
}
