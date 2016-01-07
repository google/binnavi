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
package com.google.security.zynamics.binnavi.API.debug;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.CodeNode;
import com.google.security.zynamics.binnavi.API.disassembly.FunctionNode;
import com.google.security.zynamics.binnavi.API.disassembly.Instruction;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

// ! Offers convenience functions for working with breakpoints.
/**
 * Offers convenience functions for working with breakpoints. Please note that many convenience
 * functions are just straight-forward implementations of commonly used algorithms and therefore can
 * have significant runtime costs.
 */
public final class BreakpointHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private BreakpointHelpers() {}

  /**
   * Returns the addresses of a code node where breakpoints of a given type are set.
   *
   * @param debugger The debugger that set the breakpoints.
   * @param node The code node to check.
   * @param type The type of the breakpoints to search for.
   *
   * @return The addresses of the code node where breakpoints are set.
   */
  private static List<Address> getBreakpoints(final Debugger debugger, final CodeNode node,
      final BreakpointType type) {
    Preconditions.checkNotNull(debugger, "Error: Debugger argument can not be null");
    Preconditions.checkNotNull(node, "Error: Node argument can not be null");

    final BreakpointManager manager = debugger.getBreakpointManager();

    final List<Address> breakpoints = new ArrayList<Address>();

    for (final Instruction instruction : node.getInstructions()) {
      final BreakpointAddress address = new BreakpointAddress(instruction.getNative().getModule(),
          new UnrelocatedAddress(new CAddress(instruction.getAddress().toLong())));

      if (manager.getNative().hasBreakpoint(type, address)) {
        breakpoints.add(new Address(address.getAddress().getAddress().toBigInteger()));
      }
    }

    return breakpoints;
  }

  /**
   * Returns the addresses of a view where breakpoints are set.
   *
   * @param debugger The debugger that set the breakpoint.
   * @param view The view to search through.
   * @param type Type of the breakpoints to search for.
   *
   * @return The addresses of the view where breakpoints of a given type are set.
   */
  private static List<Address> getBreakpoints(final Debugger debugger, final View view,
      final BreakpointType type) {
    Preconditions.checkNotNull(debugger, "Error: Debugger argument can not be null");
    Preconditions.checkNotNull(view, "Error: View argument can not be null");

    final BreakpointManager manager = debugger.getBreakpointManager();

    final List<Address> breakpoints = new ArrayList<Address>();

    for (final ViewNode node : view.getGraph().getNodes()) {
      if (node instanceof CodeNode) {
        breakpoints.addAll(getBreakpoints(debugger, (CodeNode) node, type));
      } else if (node instanceof FunctionNode) {
        final FunctionNode fnode = (FunctionNode) node;

        final BreakpointAddress address = new BreakpointAddress(
            fnode.getFunction().getNative().getModule(),
            new UnrelocatedAddress(fnode.getFunction().getNative().getAddress()));

        if (manager.getNative().hasBreakpoint(type, address)) {
          breakpoints.add(new Address(address.getAddress().getAddress().toBigInteger()));
        }
      }
    }

    return breakpoints;
  }

  // ! Finds breakpoints set in a code node.
  /**
   * Returns the addresses of all breakpoints set in a given code node.
   *
   *  This function is guaranteed to run in O(n) where n is the number of instructions in the
   * breakpoint.
   *
   * @param debugger Debugger that is checked for set breakpoints.
   * @param node The code node that is checked for set breakpoints.
   *
   * @return A list of addresses where breakpoints are set inside the code node.
   */
  public static List<Address> getBreakpoints(final Debugger debugger, final CodeNode node) {
    return getBreakpoints(debugger, node, BreakpointType.REGULAR);
  }

  // ! Finds breakpoints set in a view.
  /**
   * Returns the addresses of all breakpoints set in a given view. This includes breakpoints set in
   * code nodes and breakpoints set in function nodes.
   *
   *  This function is guaranteed to run in O(m + n) where m is the number of function nodes in the
   * view and n is the number of instructions in the view.
   *
   * @param debugger Debugger that is checked for set breakpoints.
   * @param view The view that is checked for set breakpoints.
   *
   * @return A list of addresses where breakpoints are set inside the view.
   */
  public static List<Address> getBreakpoints(final Debugger debugger, final View view) {
    return getBreakpoints(debugger, view, BreakpointType.REGULAR);
  }

  // ! Finds echo breakpoints set in a code node.
  /**
   * Returns the addresses of all echo breakpoints set in a given code node.
   *
   *  This function is guaranteed to run in O(n) where n is the number of instructions in the
   * breakpoint.
   *
   * @param debugger Debugger that is checked for set echo breakpoints.
   * @param node The code node that is checked for set echo breakpoints.
   *
   * @return A list of addresses echo where breakpoints are set inside the code node.
   */
  public static List<Address> getEchoBreakpoints(final Debugger debugger, final CodeNode node) {
    return getBreakpoints(debugger, node, BreakpointType.ECHO);
  }

  // ! Finds echo breakpoints set in a view.
  /**
   * Returns the addresses of all echo breakpoints set in a given view. This includes echo
   * breakpoints set in code nodes and echo breakpoints set in function nodes.
   *
   *  This function is guaranteed to run in O(m + n) where m is the number of function nodes in the
   * view and n is the number of instructions in the view.
   *
   * @param debugger Debugger that is checked for set echo breakpoints.
   * @param view The view that is checked for set echo breakpoints.
   *
   * @return A list of addresses where echo breakpoints are set inside the view.
   */
  public static List<Address> getEchoBreakpoints(final Debugger debugger, final View view) {
    return getBreakpoints(debugger, view, BreakpointType.ECHO);
  }
}
