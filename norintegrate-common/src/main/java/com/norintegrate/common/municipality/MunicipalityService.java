package com.norintegrate.common.municipality;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MunicipalityService {

  private final SsbKlassClient ssbKlassClient;

  public MunicipalityService(SsbKlassClient ssbKlassClient) {
    this.ssbKlassClient = ssbKlassClient;
  }

  public List<MunicipalityInfo> search(String query) {
    var lowerQuery = query.toLowerCase();
    return ssbKlassClient.getMunicipalities().stream()
        .filter(m -> m.name().toLowerCase().contains(lowerQuery))
        .toList();
  }

  public MunicipalityInfo findByCode(String code) {
    return ssbKlassClient.getMunicipalities().stream()
        .filter(m -> m.code().equals(code))
        .findFirst()
        .orElseThrow(
            () -> new EntityNotFoundException("Municipality not found with code: " + code));
  }
}
