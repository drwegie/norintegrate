package com.norintegrate.common.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "document_requirement")
public class DocumentRequirement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "procedure_id", nullable = false)
  private Procedure procedure;

  @Column(name = "document_name", nullable = false)
  private String documentName;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "is_mandatory", nullable = false)
  private boolean mandatory;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected DocumentRequirement() {}

  public DocumentRequirement(
      Procedure procedure, String documentName, String description, boolean mandatory) {
    this.procedure = procedure;
    this.documentName = documentName;
    this.description = description;
    this.mandatory = mandatory;
  }

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Procedure getProcedure() {
    return procedure;
  }

  public String getDocumentName() {
    return documentName;
  }

  public void setDocumentName(String documentName) {
    this.documentName = documentName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
