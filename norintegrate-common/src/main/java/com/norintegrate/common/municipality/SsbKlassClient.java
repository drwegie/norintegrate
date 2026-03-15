package com.norintegrate.common.municipality;

import java.time.Year;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SsbKlassClient {

  private final RestClient restClient;

  public SsbKlassClient(
      @Value("${norintegrate.ssb.base-url:https://data.ssb.no/api/klass/v1}") String baseUrl) {
    this.restClient = RestClient.builder().baseUrl(baseUrl).build();
  }

  public List<MunicipalityInfo> getMunicipalities() {
    var currentYear = Year.now().getValue();
    var from = currentYear + "-01-01";

    var response =
        restClient
            .get()
            .uri("/classifications/131/codes?from={from}", from)
            .retrieve()
            .body(SsbCodesResponse.class);

    if (response == null || response.codes() == null) {
      return List.of();
    }

    return response.codes().stream()
        .map(item -> new MunicipalityInfo(item.code(), item.name()))
        .toList();
  }

  private record SsbCodeItem(String code, String name) {}

  private record SsbCodesResponse(List<SsbCodeItem> codes) {}
}
