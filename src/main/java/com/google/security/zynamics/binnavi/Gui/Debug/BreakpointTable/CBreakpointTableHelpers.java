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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * Provides convenience functions that are useful in more than one class of the breakpoint table
 * package.
 */
public final class CBreakpointTableHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBreakpointTableHelpers() {
  }

  /**
   * Given a row of the breakpoint table, this function calculates to what breakpoint manager the
   * breakpoint of that row belongs to and what index the breakpoint has within the breakpoint
   * manager.
   *
   * @param debuggerProvider The debuggers that are used to fill the breakpoint table.
   * @param row A row of the breakpoint table.
   *
   * @return A pair that contains the breakpoint manager and the breakpoint index of the breakpoint
   *         identified by the given row.
   *
   * @throws IllegalArgumentException Thrown if the debugger provider argument is null or the row
   *         argument is out of bounds.
   */
  public static Pair<IDebugger, Integer> findBreakpoint(
      final BackEndDebuggerProvider debuggerProvider, final int row) {
    Preconditions.checkNotNull(
        debuggerProvider, "IE01336: Debugger provider argument can't be null");
    Preconditions.checkArgument(row >= 0, "IE01337: Row arguments can not be negative");

    int breakpoints = 0;

    for (final IDebugger debugger : debuggerProvider.getDebuggers()) {
      if ((row >= breakpoints) && (row < breakpoints
          + debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR))) {
        return new Pair<IDebugger, Integer>(debugger, row - breakpoints);
      } else {
        breakpoints +=
            debugger.getBreakpointManager().getNumberOfBreakpoints(BreakpointType.REGULAR);
      }
    }

    throw new IllegalArgumentException("IE01338: Invalid row number");
  }
}
