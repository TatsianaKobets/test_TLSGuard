package org.example.testtlsguard.handler;

import static org.example.testtlsguard.Main.isValidUrl;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiHandler implements HttpHandler {

  private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);

  private final WebsiteDao websiteDao = new WebsiteDao();
  private final CertificateDao certificateDao = new CertificateDao();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      String method = exchange.getRequestMethod();
      String path = exchange.getRequestURI().getPath();

      logger.debug("Handling {} request for path: {}", method, path);

      switch (method) {
        case "GET":
          handleGet(exchange);
          break;
        case "POST":
          if (path.equals("/api/websites")) {
            handlePost(exchange);
          } else if (path.equals("/api/certificates")) {
            handleCertificatePost(exchange);
          } else {
            sendResponse(exchange, "{\"error\":\"Invalid endpoint\"}", 404);
          }
          break;
        case "DELETE":
          handleDelete(exchange);
          break;
        default:
          sendResponse(exchange, "{\"error\":\"Method not allowed\"}", 405);
          break;
      }
    } catch (Exception e) {
      logger.error("Error handling request: {}", e.getMessage(), e);
      sendResponse(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 500);
    }
  }

  private void handleGet(HttpExchange exchange) throws IOException {
    logger.info("Handling GET request to fetch all websites.");
    List<Website> websites = websiteDao.getAllWebsites();
    String response = convertToJson(websites);
    logger.debug("Response: {}", response);
    sendResponse(exchange, response, 200);
  }

  private void handlePost(HttpExchange exchange) throws IOException {
    logger.info("Handling POST request to add a new website.");
    InputStream is = exchange.getRequestBody();
    String json = new String(is.readAllBytes());
    logger.debug("Received JSON: {}", json);

    Website website = parseJsonToWebsite(json);

    if (!isValidUrl(website.getUrl())) {
      logger.warn("Invalid URL provided: {}", website.getUrl());
      sendResponse(exchange, "{\"error\":\"Invalid URL\"}", 400);
      return;
    }

    if (websiteDao.checkIfWebsiteExists(website.getUrl())) {
      logger.warn("Website already exists: {}", website.getUrl());
      sendResponse(exchange, "{\"error\":\"Website already exists\"}", 400);
      return;
    }

    websiteDao.addWebsite(website);
    logger.info("Website added successfully: {}", website.getUrl());
    sendResponse(exchange, "{\"status\":\"ok\"}", 200);
  }

  private void handleCertificatePost(HttpExchange exchange) throws IOException {
    logger.info("Handling POST request to add a new certificate.");
    InputStream is = exchange.getRequestBody();
    String json = new String(is.readAllBytes());
    logger.debug("Received JSON: {}", json);

    try {
      CertificateInfo certInfo = parseJsonCertificate(json);
      certificateDao.saveCertificate(1, certInfo);

      List<Website> updatedWebsites = websiteDao.getAllWebsites();
      String response = convertToJson(updatedWebsites);

      logger.debug("Updated websites after adding certificate: {}", response);
      sendResponse(exchange, response, 200);
    } catch (Exception e) {
      logger.error("Error parsing or saving certificate: {}", e.getMessage(), e);
      sendResponse(exchange, "{\"error\":\"" + e.getMessage() + "\"}", 400);
    }
  }

  private void sendResponse(HttpExchange exchange, String response, int statusCode)
      throws IOException {
    logger.debug("Sending response with status code {}: {}", statusCode, response);
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE");
    exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    exchange.sendResponseHeaders(statusCode, response.getBytes().length);
    OutputStream os = exchange.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  private String convertToJson(List<Website> websites) throws JsonProcessingException {
    String json = objectMapper.writeValueAsString(websites);
    logger.debug("Converted websites to JSON: {}", json);
    return json;
  }

  private Website parseJsonToWebsite(String json) throws IOException {
    logger.debug("Parsing JSON to Website object: {}", json);
    return objectMapper.readValue(json, Website.class);
  }

  private void handleDelete(HttpExchange exchange) throws IOException {
    logger.info("Handling DELETE request to clear all websites.");
    websiteDao.clearAllWebsites();
    sendResponse(exchange, "{\"status\":\"ok\"}", 200);
  }

  private CertificateInfo parseJsonCertificate(String json)
      throws IOException, CertificateException {
    logger.debug("Parsing JSON to CertificateInfo object: {}", json);
    JsonNode rootNode = objectMapper.readTree(json);
    String pem = rootNode.get("pem").asText();

    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    X509Certificate certificate = (X509Certificate) factory.generateCertificate(
        new ByteArrayInputStream(pem.getBytes())
    );

    return CertUtils.parseCertificate(certificate);
  }
}