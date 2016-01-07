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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Implementations;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.CBreakpointTableHelpers;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.views.CViewHelpers;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * Contains methods for setting breakpoints.
 */
public final class CBreakpointSetFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBreakpointSetFunctions() {
  }

  /**
   * Checks whether any of the input arguments is null and throws an exception if it is.
   * 
   * @param debuggerProvider The debugger provider object to check for null.
   */
  private static void checkArguments(final BackEndDebuggerProvider debuggerProvider) {
    Preconditions.checkNotNull(debuggerProvider,
        "IE01357: Debugger provider argument can not be null");
  }

  /**
   * Checks whether any of the input arguments is null and throws an exception if it is.
   * 
   * @param debuggerProvider The debugger provider object to check for null.
   * @param view The view argument to check for null.
   */
  private static void checkArguments(final BackEndDebuggerProvider debuggerProvider,
      final INaviView view) {
    checkArguments(debuggerProvider);
    checkArguments(view);
  }

  /**
   * Checks whether any of the input arguments is null and throws an exception if it is.
   * 
   * @param debuggerProvider The debugger provider object to check for null.
   * @param rows The rows argument to check.
   */
  private static void checkArguments(final BackEndDebuggerProvider debuggerProvider,
      final int[] rows) {
    checkArguments(debuggerProvider);
    Preconditions.checkNotNull(rows, "IE01358: Rows argument can't be null");
  }

  /**
   * Checks whether any of the input arguments is null and throws an exception if it is.
   * 
   * @param view The view provider object to check for null.
   */
  private static void checkArguments(final INaviView view) {
    Preconditions.checkNotNull(view, "IE01359: View argument can't be null");
  }

  /**
   * Enables all breakpoints of a given breakpoint manager.
   * 
   * @param manager The breakpoints manager whose breakpoints are enabled.
   */
  private static void enableAll(final BreakpointManager manager) {
    for (int i = 0; i < manager.getNumberOfBreakpoints(BreakpointType.REGULAR); i++) {
      manager.setBreakpointStatus(BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_ENABLED, i);
    }
  }

  /**
   * Enables all breakpoints of a given breakpoint manager that belong to a given view.
   * 
   * @param manager The breakpoints manager whose breakpoints are enabled.
   * @param view The view that decides what breakpoints are enabled.
   */
  private static void enableAllView(final BreakpointManager manager, final INaviView view) {
    final Set<BreakpointAddress> addressesToEnable = new HashSet<BreakpointAddress>();
    for (int i = 0; i < manager.getNumberOfBreakpoints(BreakpointType.REGULAR); i++) {
      final BreakpointAddress address =
          manager.getBreakpoint(BreakpointType.REGULAR, i).getAddress();

      if (CViewHelpers.containsAddress(view, address.getAddress())) {
        addressesToEnable.add(address);
      }
    }
    manager.setBreakpointStatus(addressesToEnable, BreakpointType.REGULAR,
        BreakpointStatus.BREAKPOINT_ENABLED);
  }

  /**
   * Enables all breakpoints.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   */
  public static void enableAll(final BackEndDebuggerProvider debuggerProvider) {
    checkArguments(debuggerProvider);

    for (final IDebugger debugger : debuggerProvider) {
      enableAll(debugger.getBreakpointManager());
    }
  }

  /**
   * Enables all breakpoints that are part of a given view.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param view The view to consider when enabling the breakpoints.
   */
  public static void enableAllView(final BackEndDebuggerProvider debuggerProvider,
      final INaviView view) {
    checkArguments(debuggerProvider, view);

    for (final IDebugger debugger : debuggerProvider) {
      enableAllView(debugger.getBreakpointManager(), view);
    }
  }

  /**
   * Enables all breakpoints that are identified by the rows argument.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param rows Rows that identify the breakpoints.
   */
  public static void enableBreakpoints(final BackEndDebuggerProvider debuggerProvider,
      final int[] rows) {
    checkArguments(debuggerProvider, rows);

    for (final int row : rows) {
      final Pair<IDebugger, Integer> breakpoint =
          CBreakpointTableHelpers.findBreakpoint(debuggerProvider, row);

      final BreakpointManager manager = breakpoint.first().getBreakpointManager();
      final int breakpointIndex = breakpoint.second();

      manager.setBreakpointStatus(BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_ENABLED,
          breakpointIndex);
    }
  }

  /**
   * Sets breakpoints on all the functions in a given list.
   * 
   * @param targets List of debugger/function pairs where the breakpoints are set.
   */
  public static void setBreakpoints(final IFilledList<Pair<IDebugger, INaviFunction>> targets) {
    Preconditions.checkNotNull(targets, "IE01261: Targets argument can not be null");

    for (final Pair<IDebugger, INaviFunction> target : targets) {
      if (target.second().getType() == FunctionType.IMPORT) {
        continue;
      }

      CGraphDebugger.setBreakpoint(target.first().getBreakpointManager(), target.second()
          .getModule(), new UnrelocatedAddress(target.second().getAddress()));
    }
  }
}
