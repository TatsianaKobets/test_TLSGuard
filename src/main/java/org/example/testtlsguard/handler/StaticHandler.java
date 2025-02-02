package org.example.testtlsguard.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticHandler implements HttpHandler {

  private static final Logger logger = LoggerFactory.getLogger(StaticHandler.class);

  private static final String STATIC_DIR = "web";
  private static final String DEFAULT_FILE = "index.html";
  private static final String CONTENT_TYPE_HEADER = "Content-Type";

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String requestPath = exchange.getRequestURI().getPath();
    logger.debug("Handling request for path: {}", requestPath);

    if (requestPath.equals("/")) {
      requestPath = "/" + DEFAULT_FILE;
      logger.debug("Redirecting root path to default file: {}", requestPath);
    }

    String resourcePath = STATIC_DIR + requestPath;
    logger.debug("Looking for resource at path: {}", resourcePath);

    URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);

    if (resourceUrl == null) {
      logger.warn("Resource not found: {}", resourcePath);
      send404(exchange);
      return;
    }

    String mimeType = determineContentType(resourcePath);
    logger.debug("Determined MIME type for {}: {}", resourcePath, mimeType);

    try (InputStream is = resourceUrl.openStream();
        OutputStream os = exchange.getResponseBody()) {

      byte[] fileContent = is.readAllBytes();
      logger.debug("Read {} bytes from resource: {}", fileContent.length, resourcePath);

      exchange.getResponseHeaders().add(CONTENT_TYPE_HEADER, mimeType);
      exchange.sendResponseHeaders(200, fileContent.length);

      os.write(fileContent);
      logger.info("Successfully served resource: {}", resourcePath);
    } catch (IOException e) {
      logger.error("Error serving resource {}: {}", resourcePath, e.getMessage(), e);
      send500(exchange);
    }
  }

  private String determineContentType(String filename) {
    try {
      Path path = Paths.get(filename);
      String mimeType = Files.probeContentType(path);
      return mimeType != null ? mimeType : "application/octet-stream";
    } catch (IOException e) {
      logger.warn("Could not determine MIME type for {}: {}", filename, e.getMessage());
      return "text/plain";
    }
  }

  private void send404(HttpExchange exchange) throws IOException {
    String response = "Resource not found";
    logger.warn("Sending 404 response: {}", response);
    exchange.sendResponseHeaders(404, response.length());
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(response.getBytes());
    }
  }

  private void send500(HttpExchange exchange) throws IOException {
    String response = "Internal server error";
    logger.error("Sending 500 response: {}", response);
    exchange.sendResponseHeaders(500, response.length());
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(response.getBytes());
    }
  }
}