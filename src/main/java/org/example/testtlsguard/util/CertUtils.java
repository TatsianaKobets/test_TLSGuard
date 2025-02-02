package org.example.testtlsguard.util;

import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import org.example.testtlsguard.model.CertificateInfo;

public class CertUtils {

  public static CertificateInfo parseCertificate(X509Certificate cert) {
    CertificateInfo info = new CertificateInfo();
    info.setSubject(cert.getSubjectX500Principal().getName());
    info.setIssuer(cert.getIssuerX500Principal().getName());
    info.setValidFrom(new Timestamp(cert.getNotBefore().getTime()));
    info.setValidTo(new Timestamp(cert.getNotAfter().getTime()));
    info.setSerialNumber(cert.getSerialNumber().toString(16));
    return info;
  }

  private static String formatDate(Timestamp date) {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
  }

  public static X509Certificate retrieveCertificate(String urlStr) throws Exception {
    URL url = new URL(urlStr);
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    conn.setConnectTimeout(5000);
    conn.setReadTimeout(5000);
    conn.connect();

    Certificate[] certs = conn.getServerCertificates();
    if (certs.length > 0 && certs[0] instanceof X509Certificate) {
      return (X509Certificate) certs[0];
    }
    throw new SSLException("No X509Certificate found");
  }
}
