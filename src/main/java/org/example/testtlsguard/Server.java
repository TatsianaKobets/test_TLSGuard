package org.example.testtlsguard;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.example.testtlsguard.handler.ApiHandler;
import org.example.testtlsguard.handler.StaticHandler;

public class Server {

  private final HttpServer httpServer;

  public Server(int port) throws Exception {
    this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);

    httpServer.createContext("/api", new ApiHandler());
    httpServer.createContext("/", new StaticHandler());
  }

  public void start() {
    httpServer.start();
    System.out.println("Server started on port " + httpServer.getAddress().getPort());
  }
}
