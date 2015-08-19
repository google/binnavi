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
package com.google.security.zynamics.binnavi.API.debug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public final class TargetInformationTest {
  @Test
  public void testConstructor() {
    final List<RegisterDescription> registers = new ArrayList<>();
    final DebuggerOptions debuggerOptions = new DebuggerOptions(false,
        false,
        true,
        false,
        true,
        false,
        true,
        false,
        true,
        false,
        1,
        0,
        new ArrayList<DebuggerException>(),
        true,
        true,
        true);

    final com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation
        internalTargetInformation =
        new com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation(4,
            registers, debuggerOptions);

    final TargetInformation targetInformation = new TargetInformation(internalTargetInformation);

    assertFalse(targetInformation.canDetach());
    assertFalse(targetInformation.canAttach());
    assertTrue(targetInformation.canTerminate());
    assertFalse(targetInformation.canMapMemory());
    assertFalse(targetInformation.canValidateMemory());
    assertTrue(targetInformation.canHalt());
    assertTrue(targetInformation.canMultithread());
    assertFalse(targetInformation.canSoftwareBreakpoint());
    assertEquals(4, targetInformation.getAddressSize());
    assertTrue(targetInformation.canTracecount());
  }
}
