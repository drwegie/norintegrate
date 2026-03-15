package com.norintegrate.common.progress;

import com.norintegrate.common.procedure.ProcedureRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

  private final AppUserRepository appUserRepository;
  private final UserProgressRepository userProgressRepository;
  private final ProcedureRepository procedureRepository;

  public ProgressService(
      AppUserRepository appUserRepository,
      UserProgressRepository userProgressRepository,
      ProcedureRepository procedureRepository) {
    this.appUserRepository = appUserRepository;
    this.userProgressRepository = userProgressRepository;
    this.procedureRepository = procedureRepository;
  }

  @Transactional
  public AppUser findOrCreateUser(String oauthProvider, String oauthSubject, String email) {
    return appUserRepository
        .findByOauthProviderAndOauthSubject(oauthProvider, oauthSubject)
        .orElseGet(() -> appUserRepository.save(new AppUser(oauthProvider, oauthSubject, email)));
  }

  @Transactional(readOnly = true)
  public AppUser findUser(String oauthProvider, String oauthSubject) {
    return appUserRepository
        .findByOauthProviderAndOauthSubject(oauthProvider, oauthSubject)
        .orElseThrow(EntityNotFoundException::new);
  }

  @Transactional(readOnly = true)
  public List<UserProgress> getProgress(UUID userId) {
    if (!appUserRepository.existsById(userId)) {
      throw new EntityNotFoundException("AppUser not found: " + userId);
    }
    return userProgressRepository.findByUserId(userId);
  }

  @Transactional
  public UserProgress markComplete(UUID userId, Long procedureId) {
    var user =
        appUserRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("AppUser not found: " + userId));
    var procedure =
        procedureRepository
            .findById(procedureId)
            .orElseThrow(() -> new EntityNotFoundException("Procedure not found: " + procedureId));
    var userProgress =
        userProgressRepository
            .findByUserIdAndProcedureId(userId, procedureId)
            .orElseGet(() -> new UserProgress(user, procedure));
    userProgress.markCompleted();
    return userProgressRepository.save(userProgress);
  }

  @Transactional
  public UserProgress markIncomplete(UUID userId, Long procedureId) {
    var userProgress =
        userProgressRepository
            .findByUserIdAndProcedureId(userId, procedureId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "UserProgress not found for userId="
                            + userId
                            + ", procedureId="
                            + procedureId));
    userProgress.markIncomplete();
    return userProgressRepository.save(userProgress);
  }

  @Transactional
  public void deleteAccount(UUID userId) {
    if (!appUserRepository.existsById(userId)) {
      throw new EntityNotFoundException("AppUser not found: " + userId);
    }
    userProgressRepository.deleteByUserId(userId);
    appUserRepository.deleteById(userId);
  }
}
