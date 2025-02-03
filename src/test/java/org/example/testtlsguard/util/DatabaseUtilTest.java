package org.example.testtlsguard.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import org.junit.jupiter.api.Test;

class DatabaseUtilTest {

  @Test
  void testDatabaseConnection() {
    assertDoesNotThrow(() -> {
      Connection conn = DatabaseUtil.getConnection();
      assertNotNull(conn);
      assertFalse(conn.isClosed());
      conn.close();
    });
  }

  @Test
  void testDatabaseInitialization() {
    assertDoesNotThrow(DatabaseUtil::initializeDatabase);
  }
}
