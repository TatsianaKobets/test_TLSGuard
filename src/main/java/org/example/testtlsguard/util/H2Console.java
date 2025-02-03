package org.example.testtlsguard.util;

import org.h2.tools.Server;

/**
 * Starts the H2 Console web server.
 *
 * This class provides a simple way to start the H2 Console web server, which allows for web-based access to the H2 database.
 */
public class H2Console {
  public static void main(String[] args) throws Exception {
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
    System.out.println("H2 Console started at http://localhost:8082");
  }
}
