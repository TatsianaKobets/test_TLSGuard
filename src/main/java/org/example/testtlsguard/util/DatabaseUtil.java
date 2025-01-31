package org.example.testtlsguard.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {
  private static final String JDBC_URL = "jdbc:h2:~/tls_checker_db";

  public static void dropDatabase() {
    try (Connection conn = DriverManager.getConnection(JDBC_URL);
        Statement stmt = conn.createStatement()) {

      // Удаляем все объекты в базе данных
      stmt.execute("DROP ALL OBJECTS DELETE FILES");
      System.out.println("Database dropped successfully.");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
