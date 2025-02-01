package org.example.testtlsguard.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.CertificateInfo;
import org.example.testtlsguard.model.Website;
import org.example.testtlsguard.util.CertUtils;

public class ApiHandler implements HttpHandler {

  private final WebsiteDao websiteDao = new WebsiteDao();
  private final CertificateDao certificateDao = new CertificateDao(); // Добавлен CertificateDao
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      if ("GET".equals(exchange.getRequestMethod())) {
        handleGet(exchange); // Обработка GET-запросов
      } else if ("POST".equals(exchange.getRequestMethod())) {
        // Определяем, какой тип POST-запроса
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/api/websites")) {
          handlePost(exchange); // Обработка добавления сайта
        } else if (path.equals("/api/certificates")) {
          handleCertificatePost(exchange); // Обработка сохранения сертификата
        } else {
          sendResponse(exchange, "{\"error\":\"Invalid endpoint\"}", 404);
        }
      } else if ("DELETE".equals(exchange.getRequestMethod())) {
        handleDelete(exchange); // Обработка DELETE-запросов
      } else {
        sendResponse(exchange, "{\"error\":\"Method not allowed\"}", 405);
      }
    } catch (Exception e) {
      sendResponse(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 500);
    }
  }

  private void handleGet(HttpExchange exchange) throws IOException {
    // Получение списка сайтов
    List<Website> websites = websiteDao.getAllWebsites();
    String response = convertToJson(websites);
    sendResponse(exchange, response, 200);
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    InputStream is = exchange.getRequestBody();
    String json = new String(is.readAllBytes());
    System.out.println("Received JSON: " + json); // Логирование полученного JSON
    Website website = parseJsonToWebsite(json);
    websiteDao.addWebsite(website);
    sendResponse(exchange, "{\"status\":\"ok\"}", 200);
  }

  private void handleCertificatePost(HttpExchange exchange) throws IOException {
    InputStream is = exchange.getRequestBody();
    String json = new String(is.readAllBytes());
    try {
      CertificateInfo certInfo = parseJsonCertificate(json);
      // Сохраняем информацию о сертификате в базе данных
      certificateDao.saveCertificate(1, certInfo); // Используйте ваш DAO (websiteId = 1 для примера)
      sendResponse(exchange, "{\"status\":\"ok\"}", 200);
    } catch (Exception e) {
      sendResponse(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 400);
    }
  }

  private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(statusCode, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  private String convertToJson(List<Website> websites) throws JsonProcessingException {
    return objectMapper.writeValueAsString(websites);
  }

  private Website parseJsonToWebsite(String json) throws IOException {
    return objectMapper.readValue(json, Website.class);
  }

  private void handleDelete(HttpExchange exchange) throws IOException {
    websiteDao.clearAllWebsites();
    sendResponse(exchange, "{\"status\":\"ok\"}", 200);
  }

  private CertificateInfo parseJsonCertificate(String json)
      throws IOException, CertificateException {
    JsonNode rootNode = objectMapper.readTree(json);
    String pem = rootNode.get("pem").asText();

    // Преобразуем PEM обратно в X509Certificate
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    X509Certificate certificate = (X509Certificate) factory.generateCertificate(
        new ByteArrayInputStream(pem.getBytes())
    );

    return CertUtils.parseCertificate(certificate);
  }
}