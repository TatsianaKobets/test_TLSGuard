package org.example.testtlsguard.util;

import org.h2.tools.Server;

public class H2Console {
  public static void main(String[] args) throws Exception {
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
    System.out.println("H2 Console started at http://localhost:8082");
  }
}
