package org.example.testtlsguard;

import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.scheduler.CertCheckScheduler;
import org.example.testtlsguard.util.DatabaseUtil;

public class Main {

  public static void main(String[] args)
      throws Exception {
    // Удаляем всю базу данных перед созданием таблиц
    DatabaseUtil.dropDatabase();

    WebsiteDao websiteDao = new WebsiteDao(); // создание таблицы происходит в конструкторе
    CertificateDao certificateDao = new CertificateDao(); // создание таблицы происходит в конструкторе

// Запуск сервера на порту 8080
    Server server = new Server(8080);
    server.start();

// Инициализация планировщика проверок
    CertCheckScheduler scheduler = new CertCheckScheduler(websiteDao, certificateDao);
    scheduler.start();
  }
}
