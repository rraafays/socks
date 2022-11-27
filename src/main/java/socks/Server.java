package socks;

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
    
    String json = "{\"_class\":\"PublishRequest\", \"identity\":\"Alice\", \"message\":{\"_class\":\"Message\", \"from\":\"Bob\", \"when\":0, \"body\":\"Hello again!\"}}";
    try 
    {
      PublishRequest pr = om.readValue(json, PublishRequest.class);
      System.out.println(pr.message.body);
    }
    catch (JsonProcessingException e) { e.printStackTrace(); }
  }
}
