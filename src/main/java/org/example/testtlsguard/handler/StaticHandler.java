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

public class StaticHandler implements HttpHandler {

  // Базовый путь к статическим ресурсам в classpath
  private static final String STATIC_DIR = "web";
  private static final String DEFAULT_FILE = "index.html";
  private static final String CONTENT_TYPE_HEADER = "Content-Type";

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String requestPath = exchange.getRequestURI().getPath();

    // Перенаправление корня на index.html
    if (requestPath.equals("/")) {
      requestPath = "/" + DEFAULT_FILE;
    }

    // Безопасная обработка пути
    String resourcePath = STATIC_DIR + requestPath;
    URL resourceUrl = getClass().getClassLoader().getResource(resourcePath);

    if (resourceUrl == null) {
      send404(exchange);
      return;
    }

    // Определение MIME-типа
    String mimeType = determineContentType(resourcePath);

    try (InputStream is = resourceUrl.openStream();
        OutputStream os = exchange.getResponseBody()) {

      // Чтение файла
      byte[] fileContent = is.readAllBytes();

      // Отправка заголовков
      exchange.getResponseHeaders().add(CONTENT_TYPE_HEADER, mimeType);
      exchange.sendResponseHeaders(200, fileContent.length);

      // Отправка содержимого
      os.write(fileContent);
    }
  }

  private String determineContentType(String filename) {
    try {
      Path path = Paths.get(filename);
      String mimeType = Files.probeContentType(path);
      return mimeType != null ? mimeType : "application/octet-stream";
    } catch (IOException e) {
      return "text/plain";
    }
  }

  private void send404(HttpExchange exchange) throws IOException {
    String response = "Resource not found";
    exchange.sendResponseHeaders(404, response.length());
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(response.getBytes());
    }
  }
}
