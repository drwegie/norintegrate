package com.norintegrate.common.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.norintegrate.common.procedure.Procedure;
import com.norintegrate.common.procedure.ProcedureRepository;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

  @Mock private AppUserRepository appUserRepository;

  @Mock private UserProgressRepository userProgressRepository;

  @Mock private ProcedureRepository procedureRepository;

  @InjectMocks private ProgressService progressService;

  // --- Helpers ---

  private AppUser buildUser(UUID id, String provider, String subject, String email)
      throws Exception {
    var user = new AppUser(provider, subject, email);
    Field f = AppUser.class.getDeclaredField("id");
    f.setAccessible(true);
    f.set(user, id);
    return user;
  }

  private Procedure buildProcedure(Long id, String title) throws Exception {
    var p = new Procedure(title, "desc", "Authority", 5);
    Field f = Procedure.class.getDeclaredField("id");
    f.setAccessible(true);
    f.set(p, id);
    return p;
  }

  // --- findOrCreateUser ---

  @Test
  @DisplayName("findOrCreateUser: user already exists → returns existing user")
  void findOrCreateUser_existingUser_returnsExistingUser() throws Exception {
    var userId = UUID.randomUUID();
    var existing = buildUser(userId, "google", "sub-123", "user@example.com");
    when(appUserRepository.findByOauthProviderAndOauthSubject("google", "sub-123"))
        .thenReturn(Optional.of(existing));

    var result = progressService.findOrCreateUser("google", "sub-123", "user@example.com");

    assertThat(result).isSameAs(existing);
    verify(appUserRepository, never()).save(any());
  }

  @Test
  @DisplayName("findOrCreateUser: user does not exist → creates, saves, and returns new user")
  void findOrCreateUser_newUser_createsAndSavesUser() throws Exception {
    var savedUser = buildUser(UUID.randomUUID(), "github", "gh-456", "new@example.com");
    when(appUserRepository.findByOauthProviderAndOauthSubject("github", "gh-456"))
        .thenReturn(Optional.empty());
    when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

    var result = progressService.findOrCreateUser("github", "gh-456", "new@example.com");

    assertThat(result).isSameAs(savedUser);
    var captor = ArgumentCaptor.forClass(AppUser.class);
    verify(appUserRepository).save(captor.capture());
    assertThat(captor.getValue().getOauthProvider()).isEqualTo("github");
    assertThat(captor.getValue().getOauthSubject()).isEqualTo("gh-456");
    assertThat(captor.getValue().getEmail()).isEqualTo("new@example.com");
  }

  // --- getProgress ---

  @Test
  @DisplayName("getProgress: user not found → EntityNotFoundException")
  void getProgress_userNotFound_throwsEntityNotFoundException() {
    var userId = UUID.randomUUID();
    when(appUserRepository.existsById(userId)).thenReturn(false);

    assertThatThrownBy(() -> progressService.getProgress(userId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining(userId.toString());

    verify(userProgressRepository, never()).findByUserId(any());
  }

  @Test
  @DisplayName("getProgress: user exists → returns list from repository")
  void getProgress_userExists_returnsProgressList() throws Exception {
    var userId = UUID.randomUUID();
    var user = buildUser(userId, "google", "sub-123", "user@example.com");
    var procedure = buildProcedure(1L, "Step 1");
    var progress = new UserProgress(user, procedure);
    progress.markCompleted();

    when(appUserRepository.existsById(userId)).thenReturn(true);
    when(userProgressRepository.findByUserId(userId)).thenReturn(List.of(progress));

    var result = progressService.getProgress(userId);

    assertThat(result).containsExactly(progress);
  }

  // --- markComplete ---

  @Test
  @DisplayName("markComplete: no existing progress → creates new UserProgress and marks completed")
  void markComplete_noExistingProgress_createsAndMarksCompleted() throws Exception {
    var userId = UUID.randomUUID();
    var user = buildUser(userId, "google", "sub-123", "user@example.com");
    var procedure = buildProcedure(1L, "Step 1");

    when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
    when(procedureRepository.findById(1L)).thenReturn(Optional.of(procedure));
    when(userProgressRepository.findByUserIdAndProcedureId(userId, 1L))
        .thenReturn(Optional.empty());
    when(userProgressRepository.save(any(UserProgress.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    var result = progressService.markComplete(userId, 1L);

    assertThat(result.isCompleted()).isTrue();
    assertThat(result.getCompletedAt()).isNotNull();
    assertThat(result.getUser()).isSameAs(user);
    assertThat(result.getProcedure()).isSameAs(procedure);

    var captor = ArgumentCaptor.forClass(UserProgress.class);
    verify(userProgressRepository).save(captor.capture());
    assertThat(captor.getValue().isCompleted()).isTrue();
  }

  @Test
  @DisplayName("markComplete: existing progress → marks completed and saves")
  void markComplete_existingProgress_marksCompleted() throws Exception {
    var userId = UUID.randomUUID();
    var user = buildUser(userId, "google", "sub-123", "user@example.com");
    var procedure = buildProcedure(1L, "Step 1");
    var existingProgress = new UserProgress(user, procedure);

    when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
    when(procedureRepository.findById(1L)).thenReturn(Optional.of(procedure));
    when(userProgressRepository.findByUserIdAndProcedureId(userId, 1L))
        .thenReturn(Optional.of(existingProgress));
    when(userProgressRepository.save(existingProgress)).thenReturn(existingProgress);

    var result = progressService.markComplete(userId, 1L);

    assertThat(result.isCompleted()).isTrue();
    verify(userProgressRepository).save(existingProgress);
  }

  // --- markIncomplete ---

  @Test
  @DisplayName("markIncomplete: progress not found → EntityNotFoundException")
  void markIncomplete_notFound_throwsEntityNotFoundException() {
    var userId = UUID.randomUUID();
    when(userProgressRepository.findByUserIdAndProcedureId(userId, 1L))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> progressService.markIncomplete(userId, 1L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining(userId.toString())
        .hasMessageContaining("1");

    verify(userProgressRepository, never()).save(any());
  }

  @Test
  @DisplayName("markIncomplete: progress exists → marks incomplete and saves")
  void markIncomplete_exists_marksIncompleteAndSaves() throws Exception {
    var userId = UUID.randomUUID();
    var user = buildUser(userId, "google", "sub-123", "user@example.com");
    var procedure = buildProcedure(1L, "Step 1");
    var existingProgress = new UserProgress(user, procedure);
    existingProgress.markCompleted();

    when(userProgressRepository.findByUserIdAndProcedureId(userId, 1L))
        .thenReturn(Optional.of(existingProgress));
    when(userProgressRepository.save(existingProgress)).thenReturn(existingProgress);

    var result = progressService.markIncomplete(userId, 1L);

    assertThat(result.isCompleted()).isFalse();
    assertThat(result.getCompletedAt()).isNull();
    verify(userProgressRepository).save(existingProgress);
  }

  // --- deleteAccount ---

  @Test
  @DisplayName("deleteAccount: user not found → EntityNotFoundException")
  void deleteAccount_notFound_throwsEntityNotFoundException() {
    var userId = UUID.randomUUID();
    when(appUserRepository.existsById(userId)).thenReturn(false);

    assertThatThrownBy(() -> progressService.deleteAccount(userId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining(userId.toString());

    verify(userProgressRepository, never()).deleteByUserId(any());
    verify(appUserRepository, never()).deleteById(any());
  }

  @Test
  @DisplayName("deleteAccount: user exists → deletes progress then deletes user")
  void deleteAccount_exists_deletesProgressThenUser() {
    var userId = UUID.randomUUID();
    when(appUserRepository.existsById(userId)).thenReturn(true);

    progressService.deleteAccount(userId);

    var inOrder = org.mockito.Mockito.inOrder(userProgressRepository, appUserRepository);
    inOrder.verify(userProgressRepository).deleteByUserId(userId);
    inOrder.verify(appUserRepository).deleteById(userId);
  }
}
