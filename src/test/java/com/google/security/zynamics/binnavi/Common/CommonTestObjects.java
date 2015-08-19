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

import java.util.Set;

import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Gui.Users.CUser;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;

/**
 * Some common test objects which can be shared across many test cases.
 */
public final class CommonTestObjects {
  public static final long THREAD_ID = 123;
  public static final TargetProcessThread THREAD = new TargetProcessThread(THREAD_ID, ThreadState.RUNNING);

  public static final MockModule MODULE = new MockModule();
  public static final RelocatedAddress MODULE_IMAGE_BASE = new RelocatedAddress(new CAddress(
      0x1000));
  public static final CAddress MODULE_FILE_BASE = new CAddress(0);
  public static final long MODULE_SIZE = 0x10000;
  public static final MemoryModule MEMORY_MODULE = new MemoryModule("Mock Module",
      "C:\\mockmodule.exe", MODULE_IMAGE_BASE, MODULE_SIZE);

  public static final BreakpointAddress BP_ADDRESS_123 = new BreakpointAddress(MODULE,
      new UnrelocatedAddress(new CAddress(0x123)));
  public static final Set<BreakpointAddress> BP_ADDRESS_123_SET = Sets.newHashSet(BP_ADDRESS_123);
  public static final RelocatedAddress BP_ADDRESS_123_RELOC = new RelocatedAddress(new CAddress(
      0x1123));

  public static final BreakpointAddress BP_ADDRESS_0 = new BreakpointAddress(MODULE,
      new UnrelocatedAddress(new CAddress(0)));
  public static final Set<BreakpointAddress> BP_ADDRESS_0_SET = Sets.newHashSet(BP_ADDRESS_0);

  public static final BreakpointAddress BP_ADDRESS_333 = new BreakpointAddress(MODULE,
      new UnrelocatedAddress(new CAddress(0x333)));
  public static final Set<BreakpointAddress> BP_ADDRESS_333_SET = Sets.newHashSet(BP_ADDRESS_333);

  public static final BreakpointAddress BP_ADDRESS_456 = new BreakpointAddress(MODULE,
      new UnrelocatedAddress(new CAddress(0x456)));
  public static final Set<BreakpointAddress> BP_ADDRESS_456_SET = Sets.newHashSet(BP_ADDRESS_456);
  public static final RelocatedAddress BP_ADDRESS_456_RELOC = new RelocatedAddress(new CAddress(
      0x1456));

  public static final String MD5 = "12345678123456781234567812345678";
  public static final String SHA1 = "1234567812345678123456781234567812345678";

  public static final CUser TEST_USER_1 = new CUser(123456, "TEST USER 1");
  public static final CUser TEST_USER_2 = new CUser(654321, "TEST USER 2");
  public static final CUser TEST_USER_3 = new CUser(2871937, "TEST USER 2");
}
