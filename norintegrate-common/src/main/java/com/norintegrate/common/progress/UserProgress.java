package com.norintegrate.common.progress;

import com.norintegrate.common.procedure.Procedure;
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
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "user_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "procedure_id"}))
public class UserProgress {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "procedure_id", nullable = false)
  private Procedure procedure;

  @Column(nullable = false)
  private boolean completed;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private OffsetDateTime updatedAt;

  protected UserProgress() {}

  public UserProgress(AppUser user, Procedure procedure) {
    this.user = user;
    this.procedure = procedure;
    this.completed = false;
  }

  @PrePersist
  void prePersist() {
    createdAt = OffsetDateTime.now();
  }

  public void markCompleted() {
    this.completed = true;
    this.completedAt = OffsetDateTime.now();
  }

  public void markIncomplete() {
    this.completed = false;
    this.completedAt = null;
  }

  public Long getId() {
    return id;
  }

  public AppUser getUser() {
    return user;
  }

  public Procedure getProcedure() {
    return procedure;
  }

  public boolean isCompleted() {
    return completed;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
