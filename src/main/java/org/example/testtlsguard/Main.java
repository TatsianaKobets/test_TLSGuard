package org.example.testtlsguard;

import java.net.URL;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.Website;
import org.example.testtlsguard.scheduler.CertCheckScheduler;
import org.example.testtlsguard.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    try {
      logger.info("Starting application...");

      logger.info("Dropping existing database...");
      DatabaseUtil.dropDatabase();

      WebsiteDao websiteDao = new WebsiteDao();
      CertificateDao certificateDao = new CertificateDao();

      Website newWebsite = new Website(1, "https://www.example.com", "minutely");
      logger.info("Adding new website: {}", newWebsite.getUrl());
      websiteDao.addWebsite(newWebsite);

      Server server = new Server(8080);
      server.start();
      logger.info("Server started successfully on port 8080");

      CertCheckScheduler scheduler = new CertCheckScheduler(websiteDao, certificateDao);
      scheduler.start();
      logger.info("Certificate check scheduler started successfully.");

    } catch (Exception e) {
      logger.error("Error during application startup: {}", e.getMessage(), e);
    }
  }

  /**
   * Проверяет, является ли строка допустимым URL.
   *
   * @param url строка для проверки.
   * @return true, если строка является допустимым URL, иначе false.
   */
  public static boolean isValidUrl(String url) {
    try {
      new URL(url);
      logger.debug("URL is valid: {}", url);
      return true;
    } catch (Exception e) {
      logger.warn("Invalid URL: {}", url);
      return false;
    }
  }
}