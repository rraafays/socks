package socks;

// input output library
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;

// network library
import java.net.Socket;

// jackson library
import com.fasterxml.jackson.databind.ObjectMapper;

// objects to map json sent by the server into
class SuccessResponse { public String _class; }
class ErrorResponse { public String _class; public String error; }
class MessageListResponse { public String _class; public Message[] messages; }

public class Client
{
  static ObjectMapper mapper = new ObjectMapper(); // json object mapper
  final static String ADDR = "localhost"; // constant address string
  final static int PORT = 12345; // constant port number

  public static void main(String[] args) throws IOException
  {
    Socket socket = new Socket(ADDR, PORT); // create a new socket and connect to ADDR:PORT (localhost:12345)

    BufferedReader client = new BufferedReader(new InputStreamReader(System.in)); // create string reader from the character reader from the byte reader
    PrintWriter server = new PrintWriter(socket.getOutputStream(), true); // create printwriter from the socket's output stream and set it to autoflush

    System.out.println("Enter identity: "); // prompt client for an identity
    String identity = client.readLine(); // read the client's input

    Open_Request open_request = new Open_Request(); // create a new open request
    open_request._class = "OpenRequest"; open_request.identity = identity; // initialise using client identity
    
    server.println(mapper.writeValueAsString(open_request)); // write the open request back to the server for it to interpret
    while (true)
    {
      ShowMenu(); // main menu: 1 for publish, 2 for subscribe, 3 for getting messages
      
      String option = client.readLine(); // grab an argument number from the client's input
      if (option.equals("1")) { server.println(Publish()); }
      if (option.equals("2")) { server.println(Subscribe()); }
      if (option.equals("3")) { server.println(Get()); }
    }
  }

  static void ShowMenu()
  {
    System.out.println("[1] Publish");
    System.out.println("[2] Subscribe");
    System.out.println("[3] Get Messages");
  }

  static String Publish()
  {
    return("1 is not implemented!");
  }

  static String Subscribe()
  {
    return("2 is not implemented!");
  }

  static String Get()
  {
    return("3 is not implemented!");
  }
}
