package com.norintegrate.common.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "procedure")
public class Procedure {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column private String authority;

  @Column(name = "estimated_days")
  private Integer estimatedDays;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private OffsetDateTime updatedAt;

  protected Procedure() {}

  public Procedure(String title, String description, String authority, Integer estimatedDays) {
    this.title = title;
    this.description = description;
    this.authority = authority;
    this.estimatedDays = estimatedDays;
  }

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAuthority() {
    return authority;
  }

  public void setAuthority(String authority) {
    this.authority = authority;
  }

  public Integer getEstimatedDays() {
    return estimatedDays;
  }

  public void setEstimatedDays(Integer estimatedDays) {
    this.estimatedDays = estimatedDays;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
