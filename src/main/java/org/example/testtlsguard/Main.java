package org.example.testtlsguard;

import java.net.URL;
import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
import org.example.testtlsguard.model.Website;
import org.example.testtlsguard.scheduler.CertCheckScheduler;
import org.example.testtlsguard.util.DatabaseUtil;

public class Main {
//ToDo добавить всплывающее окно с сообщением о том что этот урл уже есть в списке
  //ToDo добавить логирование
  //ToDo добавить валидацию url
  //ToDo убрать комментарии
  //ToDo вынести переменные с бд в константы и отдельный класс

  public static void main(String[] args) throws Exception {
    DatabaseUtil.dropDatabase();
    WebsiteDao websiteDao = new WebsiteDao();
    Website newWebsite = new Website(1, "https://www.example.com", "minutely");

    websiteDao.addWebsite(newWebsite);
    CertificateDao certificateDao = new CertificateDao(); // создание таблицы происходит в конструкторе
    Server server = new Server(8080);
    server.start();
    System.out.println("Server started on port 8080");
    CertCheckScheduler scheduler = new CertCheckScheduler(websiteDao, certificateDao);
    scheduler.start();
  }
  public static boolean isValidUrl(String url) {
    try {
      new URL(url);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
