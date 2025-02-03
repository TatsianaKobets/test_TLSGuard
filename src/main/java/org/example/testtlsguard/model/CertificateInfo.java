package org.example.testtlsguard.model;

import java.sql.Timestamp;

/**
 * Represents information about a certificate.
 * <p>
 * This class provides a data model for a certificate, including its subject, issuer, valid from and
 * to timestamps, serial number, and PEM representation.
 */
public class CertificateInfo {

  /**
   * The subject of the certificate.
   */
  private String subject;
  /**
   * The issuer of the certificate.
   */
  private String issuer;
  /**
   * The timestamp when the certificate is valid from.
   */
  private Timestamp validFrom;
  /**
   * The timestamp when the certificate is valid to.
   */
  private Timestamp validTo;
  /**
   * The serial number of the certificate.
   */
  private String serialNumber;
  /**
   * The PEM representation of the certificate.
   */
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