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
package com.google.security.zynamics.binnavi.standardplugins.coverage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.API.debug.BreakpointManager;
import com.google.security.zynamics.binnavi.API.debug.DebugException;
import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.API.debug.IProcessListener;
import com.google.security.zynamics.binnavi.API.debug.IThreadListener;
import com.google.security.zynamics.binnavi.API.debug.Process;
import com.google.security.zynamics.binnavi.API.debug.ProcessListenerAdapter;
import com.google.security.zynamics.binnavi.API.debug.Thread;
import com.google.security.zynamics.binnavi.API.debug.ThreadListenerAdapter;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.CodeNode;
import com.google.security.zynamics.binnavi.API.disassembly.FunctionNode;
import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;
import com.google.security.zynamics.binnavi.API.helpers.IProgressThread;
import com.google.security.zynamics.binnavi.API.helpers.MessageBox;
import com.google.security.zynamics.binnavi.API.helpers.ProgressDialog;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;


/**
 * The main class of the Visual Coverage plugin. This class sets breakpoint on all relevant
 * addresses, watches for breakpoint hits, and paints nodes according to breakpoint hits.
 */
public final class VisualCoverage {
  /**
   * Color used to paint nodes that were hit just a few times.
   */
  private static final Color COLOR_FEW_HITS = new Color(186, 255, 170);

  /**
   * Color used to paint nodes that were hit more than a few times.
   */
  private static final Color COLOR_SEVERAL_HITS = new Color(147, 213, 255);

  /**
   * Color used to paint nodes that were hit many times.
   */
  private static final Color COLOR_MANY_HITS = new Color(255, 168, 191);

  /**
   * Color used to paint the currently hit node.
   */
  private static final Color COLOR_CURRENT_HIT = Color.YELLOW;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame parent;

  /**
   * The debugger used for debugging the target process.
   */
  private final Debugger debugger;

  /**
   * The view where the coverage is shown.
   */
  private final View2D view2d;

  /**
   * Keeps track of relevant events in the target process.
   */
  private final IProcessListener processListener = new InternalProcessListener();

  /**
   * Keeps track of relevant events in individual threads of the target process.
   */
  private final IThreadListener threadListener = new InternalThreadListener();

  /**
   * List of all breakpoints set by the plugin.
   */
  private final List<Address> myBreakpoints = new ArrayList<Address>();

  /**
   * Counts how often breakpoints at individual addresses were hit.
   */
  private final Map<Address, Integer> breakpointCounter = new HashMap<Address, Integer>();

  /**
   * Helps the plug in to quickly look up nodes by address.
   * 
   * TODO (timkornau): Does not work in graphs where more than one block has the same start address.
   */
  private final Map<Address, ViewNode> nodeMap = new HashMap<Address, ViewNode>();

  /**
   * Keeps track of the previously hit node.
   */
  private Address previousNodeAddress = null;

  /**
   * Listeners that are notified about changes in the Visual Coverage object.
   */
  private final List<IVisualCoverageListener> listeners = new ArrayList<IVisualCoverageListener>();

  /**
   * Creates a Visual Coverage object. The constructor already sets breakpoints on all relevant
   * events and paints the nodes of the graph white.
   * 
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger used for debugging the target process.
   * @param view2d The view where the coverage is shown.
   */
  public VisualCoverage(final JFrame parent, final Debugger debugger, final View2D view2d) {
    this.parent = parent;
    this.debugger = debugger;
    this.view2d = view2d;

    ProgressDialog.show(parent, "Initializing graph and breakpoints ...", new StartupThread());

    debugger.getProcess().addListener(processListener);

    if (debugger.isConnected()) {
      setupListeners();
    }
  }

  /**
   * Notifies all attached listeners that the Visual Trace is complete.
   */
  private void finish() {
    updatePreviousNode();

    for (final IVisualCoverageListener listener : new ArrayList<IVisualCoverageListener>(listeners)) {
      listener.finishedCoverage();
    }
  }

  /**
   * Returns the start address of a node.
   * 
   * @param node The node in question.
   * 
   * @return The start address of the given node.
   */
  private Address getAddress(final ViewNode node) {
    if (node instanceof CodeNode) {
      return ((CodeNode) node).getAddress();
    } else if (node instanceof FunctionNode) {
      return ((FunctionNode) node).getFunction().getAddress();
    } else {
      throw new IllegalStateException("Error: Invalid node passed to getAddress");
    }
  }

  /**
   * Removes all active listeners from the API objects.
   */
  private void removeListeners() {
    final Process process = debugger.getProcess();

    process.removeListener(processListener);

    for (final Thread thread : process.getThreads()) {
      thread.removeListener(threadListener);
    }
  }

  /**
   * Removes all breakpoints which were set by the plugin and are still active.
   */
  private void removeRemainingBreakpoints() {
    ProgressDialog.show(parent, "Removing remaining breakpoints ...", new CleanupThread());
  }

  /**
   * Sets a breakpoint on a given address.
   * 
   * @param address The address where the breakpoint is set.
   */
  private void setBreakpoint(final Address address) {
    final BreakpointManager breakpointManager = debugger.getBreakpointManager();

    // Make sure that the breakpoint does not already exist.
    if (!breakpointManager.hasBreakpoint(null, address)) {
      breakpointManager.setBreakpoint(null, address);
    }
  }

  /**
   * Attaches listeners to all relevant debugger objects.
   */
  private void setupListeners() {
    final Process process = debugger.getProcess();

    for (final Thread thread : process.getThreads()) {
      thread.addListener(threadListener);
    }
  }

  /**
   * Updates the color of the previously hit node depending on its hit count.
   */
  private void updatePreviousNode() {
    if ((previousNodeAddress == null) || !myBreakpoints.contains(previousNodeAddress)) {
      return;
    }

    final ViewNode node = nodeMap.get(previousNodeAddress);
    final int count = breakpointCounter.get(previousNodeAddress);

    if (count < 5) {
      node.setColor(COLOR_FEW_HITS);
    } else if (count < 10) {
      node.setColor(COLOR_SEVERAL_HITS);
    } else {
      node.setColor(COLOR_MANY_HITS);

      // After a breakpoint was hit 10 times we remove it so we do not
      // hit it again.

      final BreakpointManager breakpointManager = debugger.getBreakpointManager();

      if (breakpointManager.hasBreakpoint(null, previousNodeAddress)) {
        breakpointManager.removeBreakpoint(null, previousNodeAddress);
        myBreakpoints.remove(previousNodeAddress);
      }
    }

    try {
      java.lang.Thread.sleep(100);
    } catch (final InterruptedException e) {
      // restore the interrupted status of the thread.
      // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
      java.lang.Thread.currentThread().interrupt();
    }
  }

  /**
   * Adds a listener object that is notified about changes in the visual coverage object.
   * 
   * @param listener The listener object to add.
   */
  public void addListener(final IVisualCoverageListener listener) {
    listeners.add(listener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    updatePreviousNode();

    removeRemainingBreakpoints();
    removeListeners();
  }

  /**
   * Removes a previously listening listener object.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final IVisualCoverageListener listener) {
    listeners.remove(listener);
  }

  /**
   * Thread that is used to remove the remaining active breakpoints.
   */
  private class CleanupThread implements IProgressThread {
    @Override
    public boolean close() {
      return false;
    }

    @Override
    public void run() {
      final BreakpointManager breakpointManager = debugger.getBreakpointManager();

      for (final Address address : myBreakpoints) {
        if (breakpointManager.hasBreakpoint(null, address)) {
          // The if-check there because the user or some other script
          // could have removed the breakpoints in the meantime.

          breakpointManager.removeBreakpoint(null, address);
        }
      }
    }
  }

  /**
   * Keeps track of relevant events in the target process.
   */
  private class InternalProcessListener extends ProcessListenerAdapter {
    @Override
    public void addedThread(final Process process, final Thread thread) {
      // When a thread is added, we need to keep track of that thread too.

      thread.addListener(threadListener);
    }

    @Override
    public void attached(final Process process) {
      // When the connection to the process is first established, we need
      // to make sure that event handlers are attached to the existing
      // threads.

      setupListeners();
    }

    @Override
    public void detached(final Process process) {
      // On detaching from the target process we can remove all active
      // listeners and the breakpoints we set earlier.

      removeRemainingBreakpoints();
      removeListeners();
      finish();
    }

    @Override
    public void removedThread(final Process process, final Thread thread) {
      // When a thread is shut down we do not need to keep track of
      // it anymore.

      thread.removeListener(threadListener);
    }
  }

  /**
   * Keeps track of important events in individual threads.
   */
  private class InternalThreadListener extends ThreadListenerAdapter {
    @Override
    public void changedProgramCounter(final Thread thread) {
      // Program counters of threads are only updated when some event
      // occurred that stopped the thread. There are three different
      // possibilities:
      //
      // 1. A breakpoint was hit
      // 2. A single step was executed
      // 3. An exception occurred

      final Address currentAddress = thread.getCurrentAddress();

      if (myBreakpoints.contains(currentAddress)) {
        // One of our breakpoints was hit

        // Increase the breakpoint counter
        breakpointCounter.put(currentAddress, breakpointCounter.get(currentAddress) + 1);

        // Highlight the currently hit node
        final ViewNode currentNode = nodeMap.get(currentAddress);
        currentNode.setColor(COLOR_CURRENT_HIT);

        // Change the color of the previous node to its hit-count-specific
        // color.
        updatePreviousNode();

        previousNodeAddress = currentAddress;

        if (myBreakpoints.isEmpty()) {
          removeListeners();
          finish();
        }

        // Resume the thread to continue tracing
        try {
          debugger.resume();
        } catch (final DebugException exception) {
          MessageBox.showError(parent, "Could not resume the target process");
        }
      } else if (debugger.getBreakpointManager().hasBreakpoint(null, currentAddress)) {
        // Hit a breakpoint but was not one of ours.
      } else {
        // Debugger halted for another reason (single step, simulated
        // step [step over or step to next block], debugger was halted,
        // an exception happened.
        //
        // TODO (timkornau): Distinguishing between these events must be improved
        // in BinNavi 2.2
      }
    }

  }

  /**
   * Thread that is used to set up everything necessary while a progress dialog is running.
   */
  private class StartupThread implements IProgressThread {
    @Override
    public boolean close() {
      return false;
    }

    @Override
    public void run() {
      for (final ViewNode node : view2d.getView().getGraph()) {
        if ((node instanceof CodeNode) || (node instanceof FunctionNode)) {
          node.setColor(Color.WHITE);

          final Address address = getAddress(node);

          setBreakpoint(address);

          myBreakpoints.add(address);
          breakpointCounter.put(address, 0);
          nodeMap.put(address, node);
        }
      }
    }
  }
}
