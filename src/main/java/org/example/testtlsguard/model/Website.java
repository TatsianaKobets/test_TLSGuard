package org.example.testtlsguard.model;

import java.sql.Timestamp;

/**
 * Represents a website with its URL, schedule, and certificate information.
 *
 * This class provides a data model for a
 * website, including its ID, URL, schedule, last checked timestamp,
 * and valid to timestamp.
 */
public class Website {

  private int id;
  private String url;
  /**
   * The schedule for checking the website's certificate.
   */
  private String schedule;
  /**
   * The timestamp when the website was last checked.
   */
  private Timestamp lastChecked;
  /**
   * The timestamp when the website's certificate is valid to.
   */
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

  public String getUrl() {
    return url;
  }

  public String getSchedule() {
    return schedule;
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
