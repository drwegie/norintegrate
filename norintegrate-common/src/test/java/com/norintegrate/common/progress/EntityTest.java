package com.norintegrate.common.progress;

import static org.assertj.core.api.Assertions.assertThat;

import com.norintegrate.common.procedure.Procedure;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EntityTest {

  @Nested
  @DisplayName("AppUser entity")
  class AppUserEntityTest {

    @Test
    @DisplayName("constructor and getters return correct values")
    void constructorAndGetters_returnCorrectValues() {
      var user = new AppUser("google", "sub-123", "user@example.com");

      assertThat(user.getOauthProvider()).isEqualTo("google");
      assertThat(user.getOauthSubject()).isEqualTo("sub-123");
      assertThat(user.getEmail()).isEqualTo("user@example.com");
      assertThat(user.getId()).isNull();
      assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("setEmail updates email")
    void setEmail_updatesEmail() {
      var user = new AppUser("google", "sub-123", "old@example.com");

      user.setEmail("new@example.com");

      assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("prePersist sets createdAt")
    void prePersist_setsCreatedAt() {
      var user = new AppUser("google", "sub-123", "user@example.com");

      user.prePersist();

      assertThat(user.getCreatedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("UserProgress entity")
  class UserProgressEntityTest {

    private Procedure buildProcedure(Long id) throws Exception {
      var p = new Procedure("Test", "desc", "Auth", 5);
      Field f = Procedure.class.getDeclaredField("id");
      f.setAccessible(true);
      f.set(p, id);
      return p;
    }

    @Test
    @DisplayName("constructor sets user, procedure, and completed=false")
    void constructor_setsFieldsCorrectly() throws Exception {
      var user = new AppUser("google", "sub-123", "user@example.com");
      var proc = buildProcedure(1L);

      var progress = new UserProgress(user, proc);

      assertThat(progress.getUser()).isSameAs(user);
      assertThat(progress.getProcedure()).isSameAs(proc);
      assertThat(progress.isCompleted()).isFalse();
      assertThat(progress.getCompletedAt()).isNull();
      assertThat(progress.getId()).isNull();
      assertThat(progress.getCreatedAt()).isNull();
      assertThat(progress.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("markCompleted sets completed=true and completedAt")
    void markCompleted_setsCompletedAndTimestamp() throws Exception {
      var user = new AppUser("google", "sub-123", "user@example.com");
      var proc = buildProcedure(1L);
      var progress = new UserProgress(user, proc);

      progress.markCompleted();

      assertThat(progress.isCompleted()).isTrue();
      assertThat(progress.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("markIncomplete resets completed=false and completedAt=null")
    void markIncomplete_resetsCompletedAndTimestamp() throws Exception {
      var user = new AppUser("google", "sub-123", "user@example.com");
      var proc = buildProcedure(1L);
      var progress = new UserProgress(user, proc);
      progress.markCompleted();

      progress.markIncomplete();

      assertThat(progress.isCompleted()).isFalse();
      assertThat(progress.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("prePersist sets createdAt")
    void prePersist_setsCreatedAt() throws Exception {
      var user = new AppUser("google", "sub-123", "user@example.com");
      var proc = buildProcedure(1L);
      var progress = new UserProgress(user, proc);

      progress.prePersist();

      assertThat(progress.getCreatedAt()).isNotNull();
    }
  }
}
