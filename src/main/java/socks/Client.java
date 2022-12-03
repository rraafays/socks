package socks;

// input output library
import java.io.IOException;
import java.io.BufferedReader;

// network library
import java.net.Socket;

// utility library
import java.util.Scanner;

// objects to map json sent by the server into
class SuccessResponse { public String _class; }
class ErrorResponse { public String _class; public String error; }
class MessageListResponse { public String _class; public Message[] messages; }

public class Client
{
  static Scanner scanner = new Scanner(System.in); // scanner object to get input from the user

  final static String ADDR = "localhost"; // constant address string
  final static int PORT = 12345; // constant port number

  public static void main(String[] args) throws IOException
  {
    System.out.println("Enter identity: "); // prompt client for an identity
    String identity = scanner.nextLine(); // read the client's input using scanner
    String open_request_json = "{\"_class\":\"OpenRequest\", \"identity\":\"" + identity + "\"}"; // write open request string using client's identity

    Socket socket = new Socket(ADDR, PORT); // create a new socket and connect to ADDR:PORT (localhost:12345)
  }
}
