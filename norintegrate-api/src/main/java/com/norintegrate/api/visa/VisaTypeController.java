package com.norintegrate.api.visa;

import com.norintegrate.common.visa.VisaTypeService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visa-types")
public class VisaTypeController {

  private final VisaTypeService visaTypeService;

  public VisaTypeController(VisaTypeService visaTypeService) {
    this.visaTypeService = visaTypeService;
  }

  @GetMapping
  public List<VisaTypeResponse> findAll() {
    return visaTypeService.findAll().stream()
        .map(v -> new VisaTypeResponse(v.getId(), v.getName(), v.getDescription()))
        .toList();
  }

  @GetMapping("/{id}")
  public VisaTypeResponse findById(@PathVariable String id) {
    var v = visaTypeService.findById(id);
    return new VisaTypeResponse(v.getId(), v.getName(), v.getDescription());
  }
}
