package com.norintegrate.common.pii;

public final class PiiMasker {

  private PiiMasker() {}

  public static String maskEmail(String email) {
    if (email == null || email.isBlank()) {
      return "***";
    }
    int at = email.indexOf('@');
    if (at < 0) {
      return "***";
    }
    String local = email.substring(0, at);
    String domain = email.substring(at);
    return local.charAt(0) + "***" + domain;
  }

  public static String maskSubject(String subject) {
    if (subject == null || subject.isBlank()) {
      return "***";
    }
    if (subject.length() < 6) {
      return "***";
    }
    return subject.substring(0, 2) + "***" + subject.substring(subject.length() - 2);
  }
}
