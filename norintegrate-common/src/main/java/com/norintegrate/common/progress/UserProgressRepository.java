package com.norintegrate.common.progress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

  List<UserProgress> findByUserId(UUID userId);

  Optional<UserProgress> findByUserIdAndProcedureId(UUID userId, Long procedureId);

  void deleteByUserId(UUID userId);
}
