package org.example.testtlsguard.scheduler;

import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
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

      if (shouldCheckNow(website)) {
        try {
          X509Certificate cert = CertUtils.retrieveCertificate(website.getUrl());
          System.out.println("Certificate retrieved for: " + website.getUrl());
          cert.checkValidity(); // Проверяем валидность сертификата

          // Преобразуем сертификат в PEM
          String pem = convertToPem(cert);
          System.out.println("PEM: " + pem);

          // Создаем объект CertificateInfo и сохраняем PEM
          CertificateInfo certInfo = CertUtils.parseCertificate(cert);
          System.out.println("CertificateInfo: " + certInfo);
          certInfo.setPem(pem);

          if (certInfo.getPem() == null) {
            System.err.println("PEM is null for website ID: " + website.getId());
            return;
          }
          // Сохраняем сертификат в базу данных
          certificateDao.saveCertificate(website.getId(), certInfo);
          System.out.println("Certificate saved for website ID: " + website.getId());

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

  private X509Certificate retrieveCertificate(String url) throws Exception {
    try {
      // Создаем URL-объект
      URL targetUrl = new URL(url);

      // Открываем HTTPS-соединение
      HttpsURLConnection connection = (HttpsURLConnection) targetUrl.openConnection();
      connection.setConnectTimeout(5000); // Таймаут соединения
      connection.setReadTimeout(5000);    // Таймаут чтения
      connection.connect();

      // Получаем сертификаты из соединения
      Certificate[] certificates = connection.getServerCertificates();
      if (certificates == null || certificates.length == 0) {
        throw new SSLException("No certificates found for URL: " + url);
      }

      // Берем первый сертификат (обычно это сертификат сервера)
      Certificate certificate = certificates[0];
      if (certificate instanceof X509Certificate) {
        return (X509Certificate) certificate;
      } else {
        throw new SSLException("The certificate is not an X509Certificate for URL: " + url);
      }
    } catch (IOException e) {
      throw new Exception("Failed to retrieve certificate for URL: " + url, e);
    }
  }
  private String convertToPem(X509Certificate certificate) throws Exception {
    Base64.Encoder encoder = Base64.getMimeEncoder(64, System.lineSeparator().getBytes());
    String encodedCert = encoder.encodeToString(certificate.getEncoded());
    return "-----BEGIN CERTIFICATE-----" + System.lineSeparator() +
        encodedCert + System.lineSeparator() +
        "-----END CERTIFICATE-----";
  }
  private void sendCertificateToBackend(CertificateInfo certInfo) {
    // Реализация отправки JSON на бэкенд
    // Пример: отправляем через HTTP POST
    String json = convertToJson(certInfo);
    // Используйте вашу библиотеку для отправки HTTP-запроса
  }

  private String convertToJson(CertificateInfo certInfo) {
    // Метод для преобразования CertificateInfo в JSON
    return "{\"pem\": \"" + certInfo.getPem().replace("\n", "\\n") + "\"}";
  }
}
