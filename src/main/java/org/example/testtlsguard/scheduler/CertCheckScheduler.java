package org.example.testtlsguard.scheduler;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.CertificateInfo;
import org.example.testtlsguard.model.Website;
import org.example.testtlsguard.util.CertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedules certificate checks for websites at regular intervals.
 *
 * This class provides a scheduler that checks the certificates of websites at regular intervals, based on their schedule.
 */
public class CertCheckScheduler {

  private static final Logger logger = LoggerFactory.getLogger(CertCheckScheduler.class);

  /**
   * The scheduled executor service for scheduling tasks.
   */
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final WebsiteDao websiteDao;
  private final CertificateDao certificateDao;
  private final CertUtils certUtils = new CertUtils();

  /**
   * Creates a new certificate check scheduler with the given website and certificate DAOs.
   *
   * @param websiteDao the DAO for accessing website data
   * @param certificateDao the DAO for accessing certificate data
   */
  public CertCheckScheduler(WebsiteDao websiteDao, CertificateDao certificateDao) {
    this.websiteDao = websiteDao;
    this.certificateDao = certificateDao;
  }

  /**
   * Starts the scheduler.
   */
  public void start() {
    scheduler.scheduleAtFixedRate(this::checkCertificates, 0, 1, TimeUnit.MINUTES);
    logger.info("Scheduler started successfully.");
  }

  /**
   * Checks the certificates of all websites.
   */
  private void checkCertificates() {
    logger.info("Starting certificate check for all websites.");
    websiteDao.getAllWebsites().forEach(website -> {
      if (website.getUrl() == null || website.getUrl().isEmpty()) {
        logger.warn("URL is null or empty for website ID: {}", website.getId());
        return;
      }

      if (shouldCheckNow(website)) {
        try {
          logger.debug("Checking certificate for website: {}", website.getUrl());
          X509Certificate cert = CertUtils.retrieveCertificate(website.getUrl());
          logger.info("Certificate retrieved successfully for: {}", website.getUrl());

          cert.checkValidity();
          logger.debug("Certificate is valid for: {}", website.getUrl());

          String pem = certUtils.convertToPem(cert);
          logger.debug("PEM generated for: {}", website.getUrl());

          CertificateInfo certInfo = CertUtils.parseCertificate(cert);
          certInfo.setPem(pem);

          if (certInfo.getPem() == null) {
            logger.error("PEM is null for website ID: {}", website.getId());
            return;
          }

          certificateDao.saveCertificate(website.getId(), certInfo);
          logger.info("Certificate saved successfully for website ID: {}", website.getId());

          website.setLastChecked(new Timestamp(System.currentTimeMillis()));
          website.setValidTo(certInfo.getValidTo());
          websiteDao.updateLastChecked(website.getId(), website.getLastChecked());

          if (website.getValidTo() != null) {
            logger.debug("Updated validTo in database: {}", website.getValidTo());
          }

        } catch (CertificateExpiredException e) {
          logger.error("Certificate expired for {}: {}", website.getUrl(), e.getMessage());
        } catch (CertificateNotYetValidException e) {
          logger.error("Certificate not yet valid for {}: {}", website.getUrl(), e.getMessage());
        } catch (Exception e) {
          logger.error("Error checking certificate for {}: {}", website.getUrl(), e.getMessage(),
              e);
        }
      } else {
        logger.debug("Skipping check for website ID: {} (not yet due)", website.getId());
      }
    });
    logger.info("Certificate check completed.");
  }

  /**
   * Determines whether a certificate check should be performed for the given website.
   *
   * @param website the website to check
   * @return true if a certificate check should be performed, false otherwise
   */
  private boolean shouldCheckNow(Website website) {
    Timestamp lastChecked = website.getLastChecked();
    if (lastChecked == null) {
      logger.debug("First check for website ID: {}", website.getId());
      return true;
    }

    long currentTime = System.currentTimeMillis();
    long lastCheckedTime = lastChecked.getTime();
    long timeSinceLastCheck = currentTime - lastCheckedTime;

    switch (website.getSchedule()) {
      case "minutely":
        return timeSinceLastCheck >= TimeUnit.MINUTES.toMillis(1);
      case "hourly":
        return timeSinceLastCheck >= TimeUnit.HOURS.toMillis(1);
      case "daily":
        return timeSinceLastCheck >= TimeUnit.DAYS.toMillis(1);
      case "weekly":
        return timeSinceLastCheck >= TimeUnit.DAYS.toMillis(7);
      default:
        logger.warn("Invalid schedule for website ID: {}", website.getId());
        return false;
    }
  }
}