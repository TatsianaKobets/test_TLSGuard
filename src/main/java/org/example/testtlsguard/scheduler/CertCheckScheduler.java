package org.example.testtlsguard.scheduler;


import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.CertificateInfo;
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
    scheduler.scheduleAtFixedRate(this::checkCertificates, 0, 1, TimeUnit.MINUTES);
  }

  private void checkCertificates() {
    websiteDao.getAllWebsites().forEach(website -> {
      try {
        X509Certificate cert = CertUtils.retrieveCertificate(website.getUrl());

        // Основная валидация
        cert.checkValidity();

        CertificateInfo certInfo = (CertificateInfo) CertUtils.parseCertificate(cert);
        certificateDao.saveCertificate(website.getId(), certInfo);

      } catch (CertificateExpiredException e) {
        System.err.println("Certificate expired for " + website.getUrl());
      } catch (CertificateNotYetValidException e) {
        System.err.println("Certificate not yet valid for " + website.getUrl());
      } catch (Exception e) {
        System.err.println("Error checking certificate for " + website.getUrl());
      }
    });
  }

  private Certificate retrieveCertificate(String url) throws Exception {
    // Реализация получения сертификата (опущена для краткости)
    return null;
  }
}
