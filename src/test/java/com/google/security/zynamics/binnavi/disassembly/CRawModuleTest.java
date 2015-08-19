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
package com.google.security.zynamics.binnavi.disassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.CRawModule;
import com.google.security.zynamics.binnavi.disassembly.IRawModuleListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CRawModuleTest {
  @Test
  public void testCRawModule_1() {
    final MockSqlProvider sqlProvider = new MockSqlProvider();
    final MockDatabase mockDatabase = new MockDatabase(sqlProvider);

    try {
      @SuppressWarnings("unused")
      final CRawModule rawModule = new CRawModule(0, null, 0, false, null);
      fail();
    } catch (final IllegalArgumentException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CRawModule rawModule = new CRawModule(23, null, 0, false, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CRawModule rawModule = new CRawModule(23, "rawModule", 0, false, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CRawModule rawModule = new CRawModule(23, "rawModule", 1, false, null);
      fail();
    } catch (final NullPointerException e) {
    }

    final CRawModule rawModule = new CRawModule(23, "rawModule", 1, false, sqlProvider);

    @SuppressWarnings("unused")
    final IRawModuleListener listener;

    assertEquals(1, rawModule.getFunctionCount());
    assertEquals(23, rawModule.getId());
    assertEquals("rawModule", rawModule.getName());

    assertTrue(rawModule.inSameDatabase(sqlProvider));
    assertTrue(rawModule.inSameDatabase(mockDatabase));
    assertFalse(rawModule.isComplete());
  }
}
