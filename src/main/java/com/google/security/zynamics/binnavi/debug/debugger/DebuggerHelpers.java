/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.debug.debugger;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * Contains a few helper functions for working with debuggers.
 */
public final class DebuggerHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private DebuggerHelpers() {
  }

  /**
   * Converts a memory address into a breakpoint address.
   *
   * @param debugger The debugger which handles the breakpoint.
   * @param memoryAddress The memory address to convert.
   *
   * @return The breakpoint address.
   */
  public static BreakpointAddress getBreakpointAddress(
      final IDebugger debugger, final RelocatedAddress memoryAddress) {
    Preconditions.checkNotNull(debugger, "IE00161: Debugger argument can not be null");
    Preconditions.checkNotNull(memoryAddress, "IE00163: Memory address argument can not be null");
    final INaviModule module = debugger.getModule(memoryAddress);
    return new BreakpointAddress(module, module == null ? new UnrelocatedAddress(memoryAddress
        .getAddress()) : debugger.memoryToFile(module, memoryAddress));
  }
}
