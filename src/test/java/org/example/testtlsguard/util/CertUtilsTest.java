package org.example.testtlsguard.util;

import org.junit.jupiter.api.Test;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;

class CertUtilsTest {

  @Test
  void testRetrieveValidCertificate() {
    try {
      X509Certificate cert = CertUtils.retrieveCertificate("https://example.com");
      assertNotNull(cert);
      assertTrue(cert.getSubjectDN().getName().contains("example.com"));
    } catch (Exception e) {
      fail("Exception thrown: " + e.getMessage());
    }
  }

  @Test
  void testInvalidUrlHandling() {
    assertThrows(Exception.class, () ->
        CertUtils.retrieveCertificate("invalid.url")
    );
  }
}
