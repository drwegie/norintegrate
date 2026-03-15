package com.norintegrate.common.visa;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisaTypeService {

  private final VisaTypeRepository visaTypeRepository;

  public VisaTypeService(VisaTypeRepository visaTypeRepository) {
    this.visaTypeRepository = visaTypeRepository;
  }

  @Transactional(readOnly = true)
  public List<VisaType> findAll() {
    return visaTypeRepository.findAll();
  }

  @Transactional(readOnly = true)
  public VisaType findById(String id) {
    return visaTypeRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("VisaType not found: " + id));
  }
}
