package src;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        //  Defaults Configuration can be overridden by Env Vars
        int port = 8080;
        String version = System.getenv("APP_VERSION") != null ? System.getenv("APP_VERSION") : "v1.0 (Stable)";
        String color = System.getenv("BG_COLOR") != null ? System.getenv("BG_COLOR") : "#ADD8E6"; // Default Blue

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Context Handler
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange t) throws IOException {
                String response = "<html>" +
                        "<head><title>FinDash Bank</title></head>" +
                        "<body style='background-color:" + color + "; text-align:center; padding-top:50px; font-family: sans-serif;'>" +
                        "<div style='background: white; width: 50%; margin: auto; padding: 20px; border-radius: 10px; box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);'>" +
                        "<h1>FinDash Financial Systems By Manisha</h1>" +
                        "<h2>Current Version: " + version + "</h2>" +
                        "<p>Status: <b style='color:green'>SYSTEM ONLINE</b></p>" +
                        "<p><i>Serving from Container</i></p>" +
                        "</div>" +
                        "</body></html>";
                
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        server.setExecutor(null);
        System.out.println("FinDash Server Started on Port " + port);
        server.start();
    }
}