package org.example.testtlsguard;

import java.net.URL;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.Website;
import org.example.testtlsguard.scheduler.CertCheckScheduler;
import org.example.testtlsguard.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point of the application.
 * <p>
 * This class is responsible for starting the application, dropping the existing database, adding a
 * new website, starting the server, and scheduling certificate checks.
 */
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  /**
   * The main entry point of the application.
   * <p>
   * Starts the application, drops the existing database, adds a new website, starts the server, and
   * schedules certificate checks.
   *
   * @param args command-line arguments (not used)
   */
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
   * Checks if a given string is a valid URL.
   *
   * @param url the string to check
   * @return true if the string is a valid URL, false otherwise
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