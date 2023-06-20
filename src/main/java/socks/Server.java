package socks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

@JsonIgnoreProperties(ignoreUnknown = true)
class Mask { 
    public String _class; 
    public String identity; 
}
class Message { 
    public String _class; 
    public String from; 
    public long when; 
    public String body; 
}
class Open_Request { 
    public String _class; 
    public String identity; 
}
class Publish_Request { 
    public String _class; 
    public String identity; 
    public Message message; 
}
class Subscribe_Request { 
    public String _class; 
    public String identity; 
    public String channel; 
}
class Unsubscribe_Request { 
    public String _class; 
    public String identity; 
    public String channel; 
}
class Get_Request { 
    public String _class; 
    public String identity; 
    public long after; 
}

public class Server {
    private final static int PORT = 12345; 
    private ServerSocket server_socket; 

    public Server(ServerSocket server_socket) { 
        this.server_socket = server_socket; 
    } 

    public static void main(String[] args) throws IOException {
        ServerSocket server_socket = new ServerSocket(PORT); 
        Server server = new Server(server_socket); 
        server.Start(); 
    }

    public void Start() {
        try {
            while (!server_socket.isClosed()) {
                Socket socket = server_socket.accept(); 

                Client_Handler client_handler = new Client_Handler(socket); 
                Thread thread = new Thread(client_handler); 
                thread.start(); 
            }
        }
        catch (IOException error) { 
            Stop(); 
        } 
    }

    public void Stop() {
        try { 
            if (server_socket != null) { 
                server_socket.close(); 
            } 
        } 
        catch (IOException error) { 
            error.printStackTrace(); 
        } 
    }
}

class Client_Handler implements Runnable {
    private final static String PATH = "log"; 
    private static ObjectMapper mapper = new ObjectMapper(); 
    public static ArrayList<Client_Handler> client_handlers = new ArrayList<Client_Handler>(); 

    private Socket socket; 
    private BufferedReader reader; 
    private BufferedWriter writer;
    public String identity; 
    public ArrayList<String> subscribed_channels = new ArrayList<String>(); 
    public ArrayList<Message> message_board = new ArrayList<Message>(); 
    public boolean open; 

    public Client_Handler(Socket socket) {
        try {
        this.socket = socket; 
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); 
        }
        catch (IOException error) { 
            Stop(); 
        } 
    }

    @Override 
    public void run()
    {
        String json; 
        while (socket.isConnected()) {
            try {
                json = reader.readLine(); 
                if (json == null) { Stop(); break; } 

                BufferedWriter log = new BufferedWriter(new FileWriter(PATH, true)); 
                String _class = mapper.readValue(json, Mask.class)._class; 

                if (_class.equals("OpenRequest")) { 
                    Open(json); 
                } 
                if (_class.equals("PublishRequest")) { 
                    Publish(json); 
                } 
                if (_class.equals("SubscribeRequest")) { 
                    Subscribe(json); 
                } 
                if (_class.equals("UnsubscribeRequest")) { 
                    Unsubscribe(json); 
                } 
                if (_class.equals("GetRequest")) { 
                    Get(json); 
                } 

                log.append(json + "\n"); 
                log.close(); 
            }
            catch (IOException error) { 
                Stop(); 
                break; 
            } 
        }
    }

    void Stop()
    {
        client_handlers.remove(this); 
        try {
            if (this.reader != null) { 
                this.reader.close(); 
            } 
            if (this.writer != null) { 
                this.writer.close(); 
            } 
            if (this.socket != null) { 
                this.socket.close(); 
            } 
        }
        catch (IOException error) { 
            error.printStackTrace(); 
        } 
    }

    void Open(String json) {
        try {
            this.identity = mapper.readValue(json, Open_Request.class).identity; 
            subscribed_channels.add(identity); 
            client_handlers.add(this); 
        }
        catch (JsonProcessingException error) { 
            Stop(); 
        } 
        try {
            BufferedReader log = new BufferedReader(new FileReader(PATH)); 
            String line; 
            while ((line = log.readLine()) != null) {
                String _class = mapper.readValue(line, Mask.class)._class; 
                String identity = mapper.readValue(line,Mask.class).identity; 
                if (this.identity.equals(identity)) {
                    if (_class.equals("PublishRequest")) { 
                        Publish(line); 
                    } 
                    if (_class.equals("SubscribeRequest")) { 
                        Subscribe(line); 
                    } 
                    if (_class.equals("UnsubscribeRequest")) { 
                        Unsubscribe(line); 
                    } 
                }
            }
            log.close(); 
            this.open = true;
        }
        catch (IOException errors) { 
            Stop(); 
        } 
    }

    void Respond_Error(String reason) {
        try {
            if (open) {
                Error_Response error_response = new Error_Response(); 
                error_response._class = "ErrorResponse"; 
                error_response.error = reason; 
                writer.write(mapper.writeValueAsString(error_response)); 
                writer.newLine(); 
                writer.flush(); 
            }
        }
        catch (IOException error) { 
            Stop(); 
        } 
    }

    void Respond_Success() {
        try {
            if (open) {
                Success_Response success_response = new Success_Response(); 
                success_response._class = "SuccessResponse"; 
                writer.write(mapper.writeValueAsString(success_response)); 
                writer.newLine(); 
                writer.flush(); 
            }
        }
        catch (IOException error) { 
            Stop(); 
        } 
    }

    void Respond_MessageList(ArrayList<Message> messages) {
        try {
            if (open) {
                Message_List_Response message_list_response = new Message_List_Response(); 
                message_list_response._class = "MessageListResponse"; 
                message_list_response.messages = messages; 
                writer.write(mapper.writeValueAsString(message_list_response)); 
                writer.newLine(); 
                writer.flush(); 
            }
        }
        catch (IOException error) { 
            Stop(); 
        } 
    }

    void Publish(String json) {
        try {
            Publish_Request publish_request = mapper.readValue(json, Publish_Request.class); 
            boolean found = false; 
            for (Client_Handler client_handler : client_handlers) {
                if (client_handler.identity.equals(publish_request.identity)) {
                    client_handler.message_board.add(publish_request.message); 
                    found = true; 
                    Respond_Success(); 
                }
            }
            if (!found) { 
                Respond_Error("NO SUCH CHANNEL: " + publish_request.identity); 
            } 
        }
        catch (JsonProcessingException error) { 
            Respond_Error("MESSAGE TOO BIG"); 
        } 
    }

    void Subscribe(String json) {
        try {
            String channel = mapper.readValue(json, Subscribe_Request.class).channel; 
            if (subscribed_channels.contains(channel)) { 
                    Respond_Error("ALREADY SUBSCRIBED: " + channel); 
                    return;
            }; 

            boolean found = false; 
            for (Client_Handler client_handler : client_handlers) {
                if (client_handler.identity.equals(channel)) { 
                    subscribed_channels.add(client_handler.identity); 
                    found = true; 
                    Respond_Success(); 
                }
            }
            if (!found) { 
                Respond_Error("NO SUCH CHANNEL: " + channel); 
            } 
        }
        catch (JsonProcessingException error) { 
            Stop(); 
        } 
    }

    void Unsubscribe(String json) {
        try {
            String channel = mapper.readValue(json, Unsubscribe_Request.class).channel; 
            if (channel.equals(this.identity)) { 
                    Respond_Error("CANNOT UNSUBSCRIBE FROM SELF"); 
                    return; 
            } 

            boolean found = false;
            for (Client_Handler client_handler : client_handlers) {
                if (client_handler.identity.equals(channel)) { 
                    subscribed_channels.remove(client_handler.identity); 
                    found = true; 
                    Respond_Success(); 
                }
            }
            if (!found) { 
                Respond_Error("NO SUCH CHANNEL: " + channel); 
            }
        }
        catch (IOException error) { 
            Stop(); 
        } 
    }

    void Get(String json) {
        ArrayList<Message> messages = new ArrayList<Message>();
        for (Client_Handler client_handler : client_handlers) {
            if (subscribed_channels.contains(client_handler.identity)) {
                for (Message message : client_handler.message_board) { 
                try { 
                        if (message.when > mapper.readValue(json, Get_Request.class).after) { 
                            messages.add(message); 
                        } 
                    } 
                catch (JsonProcessingException error) { 
                        Respond_Error("INVALID TIME"); 
                    } 
                }
            }
        }
        Respond_MessageList(messages);
    }
}
