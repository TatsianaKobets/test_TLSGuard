package org.example.testtlsguard.model;

import java.util.Date;

public class CertificateInfo {

  private String subject;
  private String issuer;
  private Date validFrom;
  private Date validTo;
  private String serialNumber;

  public CertificateInfo() {
  }

  public CertificateInfo(String subject, String issuer, Date validFrom, Date validTo,
      String serialNumber) {
    this.subject = subject;
    this.issuer = issuer;
    this.validFrom = validFrom;
    this.validTo = validTo;
    this.serialNumber = serialNumber;
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

  public Date getValidFrom() {
    return validFrom;
  }

  public void setValidFrom(Date validFrom) {
    this.validFrom = validFrom;
  }

  public Date getValidTo() {
    return validTo;
  }

  public void setValidTo(Date validTo) {
    this.validTo = validTo;
  }

  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }
}
