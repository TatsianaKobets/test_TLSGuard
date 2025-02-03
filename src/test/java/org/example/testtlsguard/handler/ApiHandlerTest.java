package org.example.testtlsguard.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.Website;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ApiHandlerTest {

  private ApiHandler apiHandler;

  @Mock
  private WebsiteDao websiteDao;
  @Mock
  private CertificateDao certificateDao;

  @Mock
  private HttpExchange exchange;

  @Mock
  private OutputStream outputStream;

  @Mock
  private Headers headers;

  private ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);

    // Инициализация ApiHandler с моками
    apiHandler = new ApiHandler(websiteDao, certificateDao);

    when(exchange.getResponseBody()).thenReturn(outputStream);
    when(exchange.getResponseHeaders()).thenReturn(headers);
  }

  @Test
  void handleGet_ShouldReturnWebsitesList() throws IOException {
    Website testWebsite = new Website(1, "https://test.com", "daily");

    when(websiteDao.getAllWebsites()).thenReturn(List.of(testWebsite));
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/websites"));

    apiHandler.handle(exchange);

    String expectedJson = objectMapper.writeValueAsString(List.of(testWebsite));

    verify(outputStream).write(expectedJson.getBytes());
    verify(websiteDao).getAllWebsites();
  }

  @Test
  void handlePostWebsite_ShouldAddValidWebsite() throws IOException {
    Website website = new Website(0, "https://new.com", "hourly");

    String json = objectMapper.writeValueAsString(website);

    when(exchange.getRequestMethod()).thenReturn("POST");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/websites"));
    when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(json.getBytes()));
    when(websiteDao.checkIfWebsiteExists(anyString())).thenReturn(false);

    apiHandler.handle(exchange);

    verify(websiteDao).addWebsite(any(Website.class));
  }

  @Test
  void handleDelete_ShouldClearAllWebsites() throws IOException {
    when(exchange.getRequestMethod()).thenReturn("DELETE");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/websites"));

    apiHandler.handle(exchange);

    verify(websiteDao).clearAllWebsites();
  }

  @Test
  void handleInvalidUrl_ShouldReturnBadRequest() throws IOException {
    Website website = new Website(0, "invalid-url", "daily");

    String json = objectMapper.writeValueAsString(website);

    when(exchange.getRequestMethod()).thenReturn("POST");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/websites"));
    when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(json.getBytes()));

    apiHandler.handle(exchange);

    String expectedResponse = "{\"error\":\"Invalid URL\"}";

    verify(exchange).sendResponseHeaders(eq(400), eq((long) expectedResponse.getBytes().length));
    verify(exchange.getResponseBody()).write(expectedResponse.getBytes());
    outputStream.close();
    exchange.close();
  }

  @Test
  void handleDatabaseError_ShouldReturnInternalError() throws IOException {
    when(exchange.getRequestMethod()).thenReturn("GET");
    when(exchange.getRequestURI()).thenReturn(URI.create("/api/websites"));
    when(websiteDao.getAllWebsites()).thenThrow(new RuntimeException("DB error"));

    apiHandler.handle(exchange);

    verify(exchange).sendResponseHeaders(eq(500), anyLong());
  }
}