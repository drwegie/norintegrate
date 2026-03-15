package com.norintegrate.common.checklist;

public class CyclicDependencyException extends RuntimeException {

  public CyclicDependencyException(String visaTypeId) {
    super("Cyclic dependency detected in procedure graph for visa type: " + visaTypeId);
  }
}
