package org.example.testtlsguard.model;

import java.sql.Timestamp;

public class CertificateInfo {

  private String subject;
  private String issuer;
  private Timestamp validFrom;
  private Timestamp validTo;
  private String serialNumber;
  private String pem;

  public CertificateInfo() {
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getIssuer() {
    return issuer;
  }

  public void setIssuer(String issuer) {
    this.issuer = issuer;
  }

  public Timestamp getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(Timestamp validFrom) {
    this.validFrom = validFrom;
  }

  public Timestamp getValidTo() {
    return validTo;
  }

  public void setValidTo(Timestamp validTo) {
    this.validTo = validTo;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public String getPem() {
    return pem;
  }

  public void setPem(String pem) {
    this.pem = pem;
  }
}