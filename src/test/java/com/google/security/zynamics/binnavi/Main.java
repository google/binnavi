package com.google.security.zynamics.binnavi;

import junit.framework.JUnit4TestAdapter;

public class Main {
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite_guitar());
    junit.textui.TestRunner.run(suite_tests());
    junit.textui.TestRunner.run(suite_reil());
    junit.textui.TestRunner.run(suite_zylib());
  }

  public static junit.framework.Test suite_guitar() {
    return new JUnit4TestAdapter(com.google.security.zynamics.binnavi.SitarTests.class);
  }

  public static junit.framework.Test suite_tests() {
    return new JUnit4TestAdapter(com.google.security.zynamics.binnavi.AllTests.class);
  }

  public static junit.framework.Test suite_reil() {
    return new JUnit4TestAdapter(com.google.security.zynamics.reil.AllTests.class);
  }

  public static junit.framework.Test suite_zylib() {
    return new JUnit4TestAdapter(com.google.security.zynamics.zylib.AllTests.class);
  }
}
