package org.example.testtlsguard;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.example.testtlsguard.handler.ApiHandler;
import org.example.testtlsguard.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final HttpServer httpServer;

  public Server(int port) throws Exception {
    logger.info("Creating HTTP server on port: {}", port);
    this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);

    logger.debug("Registering API handler at /api");
    httpServer.createContext("/api", new ApiHandler());

    logger.debug("Registering static handler at /");
    httpServer.createContext("/", new StaticHandler());
  }

  public void start() {
    logger.info("Starting HTTP server...");
    httpServer.start();
    logger.info("Server started successfully on port {}", httpServer.getAddress().getPort());
  }
}
