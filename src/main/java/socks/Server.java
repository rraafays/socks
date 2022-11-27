package socks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server
{
  static ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args)
  {
    String json = "{\"brand\":\"Porsche\", \"model\":\"Macan\"}";
    try
    { 
      Car car = mapper.readValue(json, Car.class); 
      System.out.println(car.model);
    }
    catch (JsonProcessingException e) { e.printStackTrace(); }
  }
}

class Car { public String brand; public String model; }
