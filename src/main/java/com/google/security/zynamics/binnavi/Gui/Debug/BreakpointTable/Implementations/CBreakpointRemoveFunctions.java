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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.CBreakpointTableHelpers;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.views.CViewHelpers;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * Contains methods for removing breakpoints.
 */
public final class CBreakpointRemoveFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CBreakpointRemoveFunctions() {
  }

  /**
   * Removes all breakpoints of a given breakpoint manager.
   * 
   * @param manager The breakpoints manager whose breakpoints are removed.
   */
  private static void removeAll(final BreakpointManager manager) {
    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    for (final Breakpoint breakpoint : manager.getBreakpoints(BreakpointType.REGULAR)) {
      addresses.add(breakpoint.getAddress());
    }
    CGraphDebugger.removeBreakpoints(addresses, manager);
  }

  /**
   * Removes all breakpoints of a given breakpoint manager that belong to a given view.
   * 
   * @param manager The breakpoints manager whose breakpoints are removed.
   * @param view The view that decides what breakpoints are removed.
   */
  private static void removeAllView(final BreakpointManager manager, final INaviView view) {
    final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

    for (int i = 0; i < manager.getNumberOfBreakpoints(BreakpointType.REGULAR); i++) {
      final BreakpointAddress address =
          manager.getBreakpoint(BreakpointType.REGULAR, i).getAddress();

      if (CViewHelpers.containsAddress(view, address.getAddress())) {
        addresses.add(address);
      }
    }

    CGraphDebugger.removeBreakpoints(addresses, manager);
  }

  /**
   * Deletes the breakpoints specified by the rows argument.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param rows Rows that identify the breakpoints.
   */
  public static void deleteBreakpoints(final BackEndDebuggerProvider debuggerProvider,
      final int[] rows) {
    Preconditions.checkNotNull(debuggerProvider,
        "IE01886: Debugger provider argument can not be null");
    Preconditions.checkNotNull(rows, "IE02253: Rows argument can't be null");

    final ArrayList<Pair<IDebugger, BreakpointAddress>> addresses =
        new ArrayList<Pair<IDebugger, BreakpointAddress>>();

    for (final int row : rows) {
      final Pair<IDebugger, Integer> breakpoint =
          CBreakpointTableHelpers.findBreakpoint(debuggerProvider, row);

      final BreakpointManager manager = breakpoint.first().getBreakpointManager();
      final int breakpointIndex = breakpoint.second();

      addresses.add(new Pair<IDebugger, BreakpointAddress>(breakpoint.first(), manager
          .getBreakpoint(BreakpointType.REGULAR, breakpointIndex).getAddress()));
    }

    for (final Pair<IDebugger, BreakpointAddress> p : addresses) {
      final BreakpointManager manager = p.first().getBreakpointManager();
      final BreakpointAddress address = p.second();

      manager.setBreakpointStatus(Sets.newHashSet(address), BreakpointType.REGULAR,
          BreakpointStatus.BREAKPOINT_DELETING);
    }
  }

  /**
   * Disables all breakpoints.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   */
  public static void disableAll(final BackEndDebuggerProvider debuggerProvider) {
    Preconditions.checkNotNull(debuggerProvider,
        "IE01887: Debugger provider argument can not be null");

    for (final IDebugger debugger : debuggerProvider) {
      final BreakpointManager manager = debugger.getBreakpointManager();

      for (int i = 0; i < manager.getNumberOfBreakpoints(BreakpointType.REGULAR); i++) {
        manager
            .setBreakpointStatus(BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED, i);
      }
    }
  }

  /**
   * Disables all breakpoints which are part of a view.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param view The view to consider when disabling the breakpoints.
   */
  public static void disableAllView(final BackEndDebuggerProvider debuggerProvider,
      final INaviView view) {
    Preconditions.checkNotNull(debuggerProvider,
        "IE01889: Debugger provider argument can not be null");
    Preconditions.checkNotNull(view, "IE02009: View argument can't be null");

    for (final IDebugger debugger : debuggerProvider) {
      final BreakpointManager manager = debugger.getBreakpointManager();
      final Set<BreakpointAddress> addressesToDisable = new HashSet<BreakpointAddress>();

      for (int i = 0; i < manager.getNumberOfBreakpoints(BreakpointType.REGULAR); i++) {
        final BreakpointAddress address =
            manager.getBreakpoint(BreakpointType.REGULAR, i).getAddress();

        if (CViewHelpers.containsAddress(view, address.getAddress())) {
          addressesToDisable.add(address);
        }
      }
      manager.setBreakpointStatus(addressesToDisable, BreakpointType.REGULAR,
          BreakpointStatus.BREAKPOINT_DISABLED);
    }
  }

  /**
   * Disables the breakpoints identified by the rows argument.
   * 
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param rows Rows that identify the breakpoints.
   */
  public static void disableBreakpoints(final BackEndDebuggerProvider debuggerProvider,
      final int[] rows) {
    Preconditions.checkNotNull(debuggerProvider,
        "IE01919: Debugger provider argument can not be null");
    Preconditions.checkNotNull(rows, "IE02254: Rows argument can't be null");

    for (final int row : rows) {
      final Pair<IDebugger, Integer> breakpoint =
          CBreakpointTableHelpers.findBreakpoint(debuggerProvider, row);

      final BreakpointManager manager = breakpoint.first().getBreakpointManager();
      final int breakpointIndex = breakpoint.second();

      manager.setBreakpointStatus(BreakpointType.REGULAR, BreakpointStatus.BREAKPOINT_DISABLED,
          breakpointIndex);
    }
  }

  /**
   * Removes all breakpoints.
   * 
   * @param parent Parent window used for dialogs.
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   */
  public static void removeAll(final JFrame parent, final BackEndDebuggerProvider debuggerProvider) {
    Preconditions.checkNotNull(parent, "IE01360: Parent argument can't be null");
    Preconditions.checkNotNull(debuggerProvider,
        "IE01921: Debugger provider argument can not be null");

    if (JOptionPane.YES_OPTION == CMessageBox.showYesNoCancelQuestion(parent,
        "Do you really want to remove all breakpoints?")) {
      for (final IDebugger debugger : debuggerProvider) {
        removeAll(debugger.getBreakpointManager());
      }
    }
  }

  /**
   * Removes all breakpoints that are part of a given view.
   * 
   * @param parent Parent window used for dialogs.
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param view The view to consider when removing breakpoints.
   */
  public static void removeAllView(final JFrame parent,
      final BackEndDebuggerProvider debuggerProvider, final INaviView view) {
    Preconditions.checkNotNull(parent, "IE01933: Parent argument can't be null");
    Preconditions.checkNotNull(debuggerProvider,
        "IE02251: Debugger provider argument can not be null");
    Preconditions.checkNotNull(view, "IE01956: View argument can't be null");

    if (JOptionPane.YES_OPTION == CMessageBox.showYesNoCancelQuestion(parent,
        "Do you really want to remove all breakpoints from this view?")) {
      for (final IDebugger debugger : debuggerProvider) {
        removeAllView(debugger.getBreakpointManager(), view);
      }
    }
  }

  /**
   * Removes breakpoints from all the functions in a given list.
   * 
   * @param targets List of debugger/function pairs from which the breakpoints are removed.
   */
  public static void removeBreakpoints(final IFilledList<Pair<IDebugger, INaviFunction>> targets) {
    Preconditions.checkNotNull(targets, "IE01260: Targets argument can not be null");

    for (final Pair<IDebugger, INaviFunction> target : targets) {
      CGraphDebugger.removeBreakpoint(target.first().getBreakpointManager(), target.second()
          .getModule(), new UnrelocatedAddress(target.second().getAddress()));
    }
  }
}
