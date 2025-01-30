package org.example.testtlsguard.model;

public class Website {
  private int id;
  private String url;
  private String schedule;

  public Website() {
  }
  public Website(int id, String url, String schedule) {
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getSchedule() {
    return schedule;
  }

  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }
}
