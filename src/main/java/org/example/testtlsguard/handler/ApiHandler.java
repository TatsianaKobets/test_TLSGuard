package org.example.testtlsguard.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.Website;

public class ApiHandler implements HttpHandler {

  private final WebsiteDao websiteDao = new WebsiteDao();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if ("GET".equals(exchange.getRequestMethod())) {
      handleGet(exchange);
    } else if ("POST".equals(exchange.getRequestMethod())) {
      handlePost(exchange);
    } else if ("DELETE".equals(exchange.getRequestMethod())) {
      handleDelete(exchange);
    }
  }

  private void handleGet(HttpExchange exchange) throws IOException {
    // Получение списка сайтов
    List<Website> websites = websiteDao.getAllWebsites();
    String response = convertToJson(websites);
    sendResponse(exchange, response);
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    // Добавление нового сайта
    InputStream is = exchange.getRequestBody();
    String json = new String(is.readAllBytes());
    Website website = parseJsonToWebsite(json);
    websiteDao.addWebsite(website);
    sendResponse(exchange, "{\"status\":\"ok\"}");
  }

  private void sendResponse(HttpExchange exchange, String response) throws IOException {
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(200, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }


  private String convertToJson(List<Website> websites)
      throws JsonProcessingException, JsonProcessingException {
    return objectMapper.writeValueAsString(websites);
  }

  private Website parseJsonToWebsite(String json) throws IOException {
    return objectMapper.readValue(json, Website.class);
  }

  private void handleDelete(HttpExchange exchange) throws IOException {
    websiteDao.clearAllWebsites();
    sendResponse(exchange, "{\"status\":\"ok\"}");
  }
}
