package com.norintegrate.api.progress;

final class JwtUtils {
  private JwtUtils() {}

  static String deriveProvider(String issuer) {
    if (issuer == null) return "unknown";
    var lower = issuer.toLowerCase();
    if (lower.contains("google")) return "google";
    if (lower.contains("github")) return "github";
    return issuer;
  }
}
