package com.norintegrate.common.pii;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PiiMaskerTest {

  @Test
  void maskEmail_typicalAddress() {
    assertThat(PiiMasker.maskEmail("john.doe@example.com")).isEqualTo("j***@example.com");
  }

  @Test
  void maskEmail_singleCharLocal() {
    assertThat(PiiMasker.maskEmail("j@example.com")).isEqualTo("j***@example.com");
  }

  @Test
  void maskEmail_null() {
    assertThat(PiiMasker.maskEmail(null)).isEqualTo("***");
  }

  @Test
  void maskEmail_blank() {
    assertThat(PiiMasker.maskEmail("  ")).isEqualTo("***");
  }

  @Test
  void maskEmail_noAtSign() {
    assertThat(PiiMasker.maskEmail("not-an-email")).isEqualTo("***");
  }

  @Test
  void maskSubject_typicalSubject() {
    assertThat(PiiMasker.maskSubject("abc123xyz")).isEqualTo("ab***yz");
  }

  @Test
  void maskSubject_shortSubject() {
    assertThat(PiiMasker.maskSubject("abc")).isEqualTo("***");
  }

  @Test
  void maskSubject_null() {
    assertThat(PiiMasker.maskSubject(null)).isEqualTo("***");
  }

  @Test
  void maskSubject_blank() {
    assertThat(PiiMasker.maskSubject("  ")).isEqualTo("***");
  }

  @Test
  void maskSubject_exactlySixChars() {
    assertThat(PiiMasker.maskSubject("abcdef")).isEqualTo("ab***ef");
  }
}
