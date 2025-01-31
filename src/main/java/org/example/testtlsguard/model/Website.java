package org.example.testtlsguard.model;

import java.sql.Timestamp;

public class Website {

  private int id;
  private String url;
  private String schedule;
  private Timestamp lastChecked;
  private Timestamp validTo;

  public Website() {
  }

  public Website(int id, String url, String schedule) {
    this.id = id;
    this.url = url;
    this.schedule = schedule;
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
  public Timestamp getLastChecked() {
    return lastChecked;
  }

  public void setLastChecked(Timestamp lastChecked) {
    this.lastChecked = lastChecked;
  }

  public Timestamp getValidTo() {
    return validTo;
  }

  public void setValidTo(Timestamp validTo) {
    this.validTo = validTo;
  }
}
