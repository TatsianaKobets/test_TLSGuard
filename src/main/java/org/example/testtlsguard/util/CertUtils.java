package org.example.testtlsguard.util;

import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.Base64;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import org.example.testtlsguard.model.CertificateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CertUtils {

  private static final Logger logger = LoggerFactory.getLogger(CertUtils.class);

  public static CertificateInfo parseCertificate(X509Certificate cert) {
    CertificateInfo info = new CertificateInfo();
    info.setSubject(cert.getSubjectX500Principal().getName());
    info.setIssuer(cert.getIssuerX500Principal().getName());
    info.setValidFrom(new Timestamp(cert.getNotBefore().getTime()));
    info.setValidTo(new Timestamp(cert.getNotAfter().getTime()));
    info.setSerialNumber(cert.getSerialNumber().toString(16));
    logger.debug("Parsed certificate info: {}", info);
    return info;
  }

  public static X509Certificate retrieveCertificate(String urlStr) throws Exception {
    URL url = new URL(urlStr);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    conn.setConnectTimeout(5000);
    conn.setReadTimeout(5000);
    conn.connect();

    Certificate[] certs = conn.getServerCertificates();
    if (certs.length > 0 && certs[0] instanceof X509Certificate) {
      logger.info("Successfully retrieved certificate from: {}", urlStr);
      return (X509Certificate) certs[0];
    }
    logger.error("No X509Certificate found for URL: {}", urlStr);
    throw new SSLException("No X509Certificate found");
  }

  public String convertToPem(X509Certificate certificate) throws Exception {
    Base64.Encoder encoder = Base64.getMimeEncoder(64, System.lineSeparator().getBytes());
    String encodedCert = encoder.encodeToString(certificate.getEncoded());
    return "-----BEGIN CERTIFICATE-----" + System.lineSeparator() +
        encodedCert + System.lineSeparator() +
        "-----END CERTIFICATE-----";
  }
}