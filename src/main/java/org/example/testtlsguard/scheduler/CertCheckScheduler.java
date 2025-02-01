package org.example.testtlsguard.scheduler;

import java.security.cert.Certificate;
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

public class CertCheckScheduler {

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final WebsiteDao websiteDao;
  private final CertificateDao certificateDao;

  public CertCheckScheduler(WebsiteDao websiteDao, CertificateDao certificateDao) {
    this.websiteDao = websiteDao;
    this.certificateDao = certificateDao;
  }

  public void start() {
    // Запускаем проверку каждую минуту
    scheduler.scheduleAtFixedRate(this::checkCertificates, 0, 1, TimeUnit.MINUTES);
    System.out.println("Scheduler started");
  }

  private void checkCertificates() {
    System.out.println("Checking certificates for all websites");
    websiteDao.getAllWebsites().forEach(website -> {
      if (website.getUrl() == null || website.getUrl().isEmpty()) {
        System.err.println("URL is null or empty for website ID: " + website.getId());
        return;
      }

      // Проверяем, нужно ли выполнять проверку для этого сайта
      if (shouldCheckNow(website)) {
        try {
          X509Certificate cert = CertUtils.retrieveCertificate(website.getUrl());
          cert.checkValidity();

          CertificateInfo certInfo = CertUtils.parseCertificate(cert);
          certificateDao.saveCertificate(website.getId(), certInfo);

          // Обновляем время последней проверки
          website.setLastChecked(new java.sql.Timestamp(System.currentTimeMillis()));
          websiteDao.updateLastChecked(website.getId(), website.getLastChecked());

        } catch (CertificateExpiredException e) {
          System.err.println("Certificate expired for " + website.getUrl());
        } catch (CertificateNotYetValidException e) {
          System.err.println("Certificate not yet valid for " + website.getUrl());
        } catch (Exception e) {
          System.err.println("Error checking certificate for " + website.getUrl());
          e.printStackTrace();
        }
      }
    });
  }
  private boolean shouldCheckNow(Website website) {
    Timestamp lastChecked = website.getLastChecked();
    if (lastChecked == null) {
      return true; // Если проверка никогда не выполнялась, выполняем её
    }

    long currentTime = System.currentTimeMillis();
    long lastCheckedTime = lastChecked.getTime();
    long timeSinceLastCheck = currentTime - lastCheckedTime;

    switch (website.getSchedule()) {
      case "minutely":
        return timeSinceLastCheck >= TimeUnit.MINUTES.toMillis(1); // Каждую минуту
      case "hourly":
        return timeSinceLastCheck >= TimeUnit.HOURS.toMillis(1); // Каждый час
      case "daily":
        return timeSinceLastCheck >= TimeUnit.DAYS.toMillis(1); // Ежедневно
      case "weekly":
        return timeSinceLastCheck >= TimeUnit.DAYS.toMillis(7); // Еженедельно
      default:
        return false;
    }
  }

  private Certificate retrieveCertificate(String url) throws Exception {
    // Реализация получения сертификата (опущена для краткости)
    return null;
  }
}
