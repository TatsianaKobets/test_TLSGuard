package org.example.testtlsguard.scheduler;

import java.util.Collections;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.Website;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CertCheckSchedulerTest {

  @Test
  void testCertificateCheckScheduling() {
    WebsiteDao websiteDao = Mockito.mock(WebsiteDao.class);
    CertificateDao certificateDao = Mockito.mock(CertificateDao.class);

    Website testWebsite = new Website(1, "https://example.com", "minutely");
    Mockito.when(websiteDao.getAllWebsites())
        .thenReturn(Collections.singletonList(testWebsite));

    CertCheckScheduler scheduler = new CertCheckScheduler(websiteDao, certificateDao);
    scheduler.checkCertificates();

    Mockito.verify(certificateDao, Mockito.atLeastOnce())
        .saveCertificate(Mockito.anyInt(), Mockito.any());
  }
}
