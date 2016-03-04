/*
Copyright 2014 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi.Common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test the equals() contract of arbitrary types, i.e. test whether equals is reflexive, symmetric
 * and transitive. Also test whether equivalence implies identical hashCode() values.
 *
 * @author jannewger@google.com (Jan Newger)
 *
 */
@RunWith(JUnit4.class)
@Ignore
public class GenericEquivalenceRelationTest<T> {
  private final GenericEquivalenceRelationFactory<T> factory;

  public GenericEquivalenceRelationTest(final GenericEquivalenceRelationFactory<T> factory) {
    this.factory = factory;
  }

  @Test
  public void testEqualsNull() {
    assertFalse(factory.createFirstInstance().equals(null));
    assertFalse(factory.createSecondInstance().equals(null));
    assertFalse(factory.createThirdInstance().equals(null));
  }

  @Test
  public void testHashCode() {
    final T instance1a = factory.createFirstInstance();
    final T instance1b = factory.createFirstInstance();
    assertTrue(instance1a.hashCode() == instance1b.hashCode());

    final T instance2a = factory.createSecondInstance();
    final T instance2b = factory.createSecondInstance();
    assertTrue(instance2a.hashCode() == instance2b.hashCode());

    final T instance3a = factory.createThirdInstance();
    final T instance3b = factory.createThirdInstance();
    assertTrue(instance3a.hashCode() == instance3b.hashCode());
  }

  @Test
  public void testReflexivity() {
    final T instance1 = factory.createFirstInstance();
    final T instance2 = factory.createSecondInstance();
    final T instance3 = factory.createThirdInstance();
    assertTrue(instance1.equals(instance1));
    assertTrue(instance2.equals(instance2));
    assertTrue(instance3.equals(instance3));
  }

  @Test
  public void testSymmetry() {
    final T instance1a = factory.createFirstInstance();
    final T instance1b = factory.createFirstInstance();
    assertTrue(instance1a.equals(instance1b));
    assertTrue(instance1b.equals(instance1a));

    final T instance2a = factory.createSecondInstance();
    final T instance2b = factory.createSecondInstance();
    assertTrue(instance2a.equals(instance2b));
    assertTrue(instance2b.equals(instance2a));

    final T instance3a = factory.createThirdInstance();
    final T instance3b = factory.createThirdInstance();
    assertTrue(instance3a.equals(instance3b));
    assertTrue(instance3b.equals(instance3a));
  }

  @Test
  public void testTransitivity() {
    final T instance1a = factory.createFirstInstance();
    final T instance1b = factory.createFirstInstance();
    final T instance1c = factory.createFirstInstance();
    assertTrue(instance1a.equals(instance1b));
    assertTrue(instance1b.equals(instance1c));
    assertTrue(instance1a.equals(instance1c));

    final T instance2a = factory.createSecondInstance();
    final T instance2b = factory.createSecondInstance();
    final T instance2c = factory.createSecondInstance();
    assertTrue(instance2a.equals(instance2b));
    assertTrue(instance2b.equals(instance2c));
    assertTrue(instance2a.equals(instance2c));

    final T instance3a = factory.createThirdInstance();
    final T instance3b = factory.createThirdInstance();
    final T instance3c = factory.createThirdInstance();
    assertTrue(instance3a.equals(instance3b));
    assertTrue(instance3b.equals(instance3c));
    assertTrue(instance3a.equals(instance3c));
  }
}