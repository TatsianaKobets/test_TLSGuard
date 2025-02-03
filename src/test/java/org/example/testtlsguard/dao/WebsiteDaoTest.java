package org.example.testtlsguard.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.List;
import org.example.testtlsguard.model.Website;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class WebsiteDaoTest {

  private WebsiteDao websiteDao;
  private static final String TEST_URL = "https://test.example.com";

  @BeforeEach
  void setUp() {
    websiteDao = new WebsiteDao();
    websiteDao.clearAllWebsites();
  }

  @Test
  void testAddAndGetWebsite() {
    Website website = new Website(0, TEST_URL, "hourly");
    websiteDao.addWebsite(website);

    List<Website> websites = websiteDao.getAllWebsites();
    assertEquals(1, websites.size());
    assertEquals(TEST_URL, websites.get(0).getUrl());
  }

  @Test
  void testCheckIfWebsiteExists() {
    Website website = new Website(0, TEST_URL, "daily");
    websiteDao.addWebsite(website);

    assertTrue(websiteDao.checkIfWebsiteExists(TEST_URL));
    assertFalse(websiteDao.checkIfWebsiteExists("https://invalid.url"));
  }

  @Test
  void testUpdateLastCheckedWithMockito() {
    WebsiteDao websiteDao = Mockito.mock(WebsiteDao.class);

    Website website = new Website(1, TEST_URL, "minutely");
    Timestamp now = new Timestamp(System.currentTimeMillis());

    Mockito.doNothing().when(websiteDao).updateLastChecked(1, now);
    Mockito.when(websiteDao.getAllWebsites()).thenReturn(List.of(website));

    websiteDao.updateLastChecked(1, now);

    Mockito.verify(websiteDao).updateLastChecked(1, now);

    List<Website> websites = websiteDao.getAllWebsites();
    assertEquals(1, websites.size());
    assertEquals(TEST_URL, websites.get(0).getUrl());
  }
}
