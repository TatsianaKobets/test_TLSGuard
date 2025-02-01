package org.example.testtlsguard;

import org.example.testtlsguard.dao.CertificateDao;
import org.example.testtlsguard.dao.WebsiteDao;
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
    CertificateDao certificateDao = new CertificateDao(); // создание таблицы происходит в конструкторе
    Server server = new Server(8080);
    server.start();
    System.out.println("Server started on port 8080");
    CertCheckScheduler scheduler = new CertCheckScheduler(websiteDao, certificateDao);
    scheduler.start();
  }
}
