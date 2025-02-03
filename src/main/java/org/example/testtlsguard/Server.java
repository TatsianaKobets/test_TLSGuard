package org.example.testtlsguard;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.handler.ApiHandler;
import org.example.testtlsguard.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple HTTP server that serves API requests and static files.
 * <p>
 * This class creates an HTTP server on a specified port and registers two handlers: one for API
 * requests and one for static files.
 */
public class Server {
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final HttpServer httpServer;
  private final WebsiteDao websiteDao;
  private final CertificateDao certificateDao;

  /**
   * Creates a new HTTP server on the specified port.
   *
   * @param port the port to listen on
   * @throws Exception if an error occurs while creating the server
   */
  public Server(int port) throws Exception {
    logger.info("Creating HTTP server on port: {}", port);
    this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);

    this.websiteDao = new WebsiteDao();
    this.certificateDao = new CertificateDao();

    logger.debug("Registering API handler at /api");
    httpServer.createContext("/api", new ApiHandler(websiteDao, certificateDao));

    logger.debug("Registering static handler at /");
    httpServer.createContext("/", new StaticHandler());
  }

  /**
   * Starts the HTTP server.
   */
  public void start() {
    logger.info("Starting HTTP server...");
    httpServer.start();
    logger.info("Server started successfully on port {}", httpServer.getAddress().getPort());
  }
}