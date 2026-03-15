package com.norintegrate.common.progress;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "app_user",
    uniqueConstraints = @UniqueConstraint(columnNames = {"oauth_provider", "oauth_subject"}))
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "oauth_provider", nullable = false)
  private String oauthProvider;

  @Column(name = "oauth_subject", nullable = false)
  private String oauthSubject;

  @Column(nullable = false)
  private String email;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected AppUser() {}

  public AppUser(String oauthProvider, String oauthSubject, String email) {
    this.oauthProvider = oauthProvider;
    this.oauthSubject = oauthSubject;
    this.email = email;
  }

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public String getOauthProvider() {
    return oauthProvider;
  }

  public String getOauthSubject() {
    return oauthSubject;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
