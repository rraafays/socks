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
import com.fasterxml.jackson.core.JsonProcessingException;

// objects to map json sent by the server into
class SuccessResponse { public String _class; }
class ErrorResponse { public String _class; public String error; }
class MessageListResponse { public String _class; public Message[] messages; }

public class Client
{
  static Scanner scanner = new Scanner(System.in);
  static ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args)
  {
    System.out.println("Enter identity: ");
    String identity = scanner.nextLine();
    String open_request_json = "{\"_class\":\"OpenRequest\", \"identity\":\"" + identity + "\"}";

    System.out.println(open_request_json);
    try 
    {
      Open_Request open_request =  mapper.readValue(open_request_json, Open_Request.class);
      System.out.println(open_request.identity);
    }
    catch (JsonProcessingException e) { e.printStackTrace(); }
  }
}
