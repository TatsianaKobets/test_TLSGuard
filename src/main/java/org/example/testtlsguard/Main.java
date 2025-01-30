package org.example.testtlsguard;

import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.scheduler.CertCheckScheduler;

public class Main {
  public static void main(String[] args) throws Exception {
    // Инициализация базы данных
    WebsiteDao websiteDao = new WebsiteDao();
    websiteDao.createTable();

    CertificateDao certificateDao = new CertificateDao();
    certificateDao.createTable();

    // Запуск сервера на порту 8080
    Server server = new Server(8080);
    server.start();

    // Инициализация планировщика проверок
    CertCheckScheduler scheduler = new CertCheckScheduler(websiteDao, certificateDao);
    scheduler.start();
  }
}
