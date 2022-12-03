package socks;

// input output library
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;

// network library
import java.net.Socket;

// objects to map json sent by the server into
class SuccessResponse { public String _class; }
class ErrorResponse { public String _class; public String error; }
class MessageListResponse { public String _class; public Message[] messages; }

public class Client
{

  final static String ADDR = "localhost"; // constant address string
  final static int PORT = 12345; // constant port number

  public static void main(String[] args) throws IOException
  {
    Socket socket = new Socket(ADDR, PORT); // create a new socket and connect to ADDR:PORT (localhost:12345)

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); // create string reader from the character reader from the byte reader
    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true); // create printwriter from the socket's output stream and set it to autoflush

    System.out.println("Enter identity: "); // prompt client for an identity
    String identity = reader.readLine(); // read the client's input
    String open_request_json = "{\"_class\":\"OpenRequest\", \"identity\":\"" + identity + "\"}"; // write open request string using client's identity
    writer.println(open_request_json); // write the open request back to the server for it to interpret
  }
}
