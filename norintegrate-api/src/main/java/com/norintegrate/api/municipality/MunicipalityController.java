package com.norintegrate.api.municipality;

import com.norintegrate.common.municipality.MunicipalityService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/municipalities")
public class MunicipalityController {

  private final MunicipalityService municipalityService;

  public MunicipalityController(MunicipalityService municipalityService) {
    this.municipalityService = municipalityService;
  }

  @GetMapping
  public List<MunicipalityResponse> search(@RequestParam @NotBlank @Size(min = 2) String query) {
    return municipalityService.search(query).stream()
        .map(m -> new MunicipalityResponse(m.code(), m.name()))
        .toList();
  }

  @GetMapping("/{code}")
  public MunicipalityResponse findByCode(@PathVariable String code) {
    var m = municipalityService.findByCode(code);
    return new MunicipalityResponse(m.code(), m.name());
  }
}
