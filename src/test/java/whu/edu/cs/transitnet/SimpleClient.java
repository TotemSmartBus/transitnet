package whu.edu.cs.transitnet;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SimpleClient {
    public static void main(String[] args) {
        //String host = "172.24.12.191";
        String host = "127.0.0.1";
        int port = 9201;

        try (Socket socket = new Socket(host, port)) {
            System.out.println("Connected to the server!");
            // 这里可以根据需要添加更多的逻辑来与服务器交互
        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}
