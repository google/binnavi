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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import com.google.security.zynamics.binnavi.API.debug.BreakpointManager;
import com.google.security.zynamics.binnavi.API.debug.DebugException;
import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.API.debug.DebuggerBreakpointHitReply;
import com.google.security.zynamics.binnavi.API.debug.DebuggerListenerAdapter;
import com.google.security.zynamics.binnavi.API.debug.DebuggerRequestTargetReply;
import com.google.security.zynamics.binnavi.API.debug.DebuggerSingleStepReply;
import com.google.security.zynamics.binnavi.API.debug.DebuggerTargetInformationReply;
import com.google.security.zynamics.binnavi.API.debug.IDebuggerListener;
import com.google.security.zynamics.binnavi.API.debug.MemoryModule;
import com.google.security.zynamics.binnavi.API.debug.Register;
import com.google.security.zynamics.binnavi.API.debug.raw.RegisterValues;
import com.google.security.zynamics.binnavi.API.debug.raw.ThreadRegisterValues;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.IModuleListener;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.ModuleListenerAdapter;
import com.google.security.zynamics.binnavi.API.helpers.Logger;
import com.google.security.zynamics.binnavi.API.helpers.RemoteFileBrowserLoader;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * This class contains all the code that is necessary for actually resolving indirect function
 * calls.
 */
public abstract class CallResolver {
  /**
   * Number of hits after which a breakpoint is removed from the target process.
   */
  private static final Integer HIT_THRESHOLD = 20;

  /**
   * The target whose calls are resolved.
   */
  private final ICallResolverTarget target;

  /**
   * The debugger which handles call resolving.
   */
  private final Debugger debugger;

  /**
   * Keeps track of incoming events from the debugger.
   */
  private final IDebuggerListener debuggerListener = new InternalDebuggerListener();

  /**
   * Maps addresses of indirect function calls to the functions they were resolved to.
   */
  private final Map<BigInteger, Set<ResolvedFunction>> resolvedAddresses = new HashMap<>();

  /**
   * Maps addresses of indirect function calls to an integer number that says how many times in a
   * row the function call was resolved to the same address.
   */
  private final Map<BigInteger, Integer> hitCounter = new HashMap<>();

  /**
   * Maps thread IDs to the last breakpoint hit by the thread.
   */
  private final Map<Long, BigInteger> lastHits = new HashMap<>();

  /**
   * Listens on relevant changes in the target.
   */
  private final ICallResolverTargetListener internalTargetListener = new InternalTargetListener();

  /**
   * Maps modules to a map of a functions and their relocated memory addresses.
   */
  private final Map<Module, Map<Address, Function>> resolvedFunctions = new HashMap<>();

  /**
   * Keeps track of all breakpoints that were already removed because the resolver threshold for the
   * call was hit.
   */
  private final Set<IndirectCall> removedBreakpoints = new HashSet<>();

  /**
   * Keeps track of what step of the call resolving process is active.
   */
  private int step = 0;

  /**
   * The addresses of all indirect calls in the module are stored here later.
   */
  private List<IndirectCall> indirectCallAddresses = null;

  /**
   * We are using this listener to keep all required modules loaded.
   */
  private final IModuleListener moduleKeeperListener = new ModuleListenerAdapter() {
    @Override
    public boolean closingModule(final Module module) {
      return false;
    }
  };

  /**
   * In this set we keep the modules we are considering during resolving. Storing them separately
   * allows us to ignore changes in the call resolver target (like new added modules, or removed
   * modules).
   */
  private final Set<Module> modules = new HashSet<Module>();

  /**
   * The parent frame which created the call resolver object.
   */
  private final JFrame parent;

  /**
   * Creates a new call resolver target.
   *
   * @param target The target whose calls are resolved.
   * @param parent The parent dialog which created this instance.
   */
  public CallResolver(final ICallResolverTarget target, final JFrame parent) {
    assert target != null;
    this.parent = parent;
    this.target = target;
    debugger = target.getDebugger();
    target.addListener(internalTargetListener);
  }

  /**
   * Processes a hit breakpoint.
   *
   * @param threadId The thread ID of the thread that hit the breakpoint.
   * @param breakpointAddress The address of the hit breakpoint.
   */
  private void countHit(final long threadId, final BigInteger breakpointAddress) {
    if (!hitCounter.containsKey(breakpointAddress)) {
      hitCounter.put(breakpointAddress, 0);
    }
    hitCounter.put(breakpointAddress, hitCounter.get(breakpointAddress) + 1);
    lastHits.put(threadId, breakpointAddress);
  }

  /**
   * In this step we determine the addresses of all indirect function calls in the target.
   */
  private void findIndirectCallAddresses() {
    indirectCallAddresses = target.getIndirectCalls();
    if (!indirectCallAddresses.isEmpty()) {
      // Only continue if we actually found indirect function calls.
      step++;
    }
    foundIndirectCallAddresses(indirectCallAddresses);
  }

  /**
   * Returns the program counter value of a given thread.
   *
   * @param threadId The thread ID of the thread.
   * @param registerValues The register values of the target process.
   *
   * @return The program counter value of the given thread.
   */
  private BigInteger getProgramCounter(final long threadId, final RegisterValues registerValues) {
    for (final ThreadRegisterValues registerValue : registerValues) {
      if (registerValue.getThreadId() == threadId) {
        for (final Register register : registerValue) {
          if (register.isProgramCounter()) {
            return register.getValue();
          }
        }
      }
    }

    assert false : "It is not possible to hit a breakpoint in a non-existing thread";
    return null;
  }

  /**
   * Loads the modules that belong to the target.
   */
  private void loadTargetModules() {
    for (final Module module : target.getModules()) {
      module.addListener(moduleKeeperListener);
      modules.add(module);
      if (!module.isLoaded()) {
        try {
          module.load();
        } catch (final CouldntLoadDataException e) {
          // If we can't load any of the involved modules, we can
          // not proceed to the next step.
          errorLoadingModule(module, e);
          return;
        }
      }
    }
    step++;
  }

  /**
   * After a single step was completed successfully, we know the target of the function call and can
   * update our internal structures.
   *
   * @param threadId The thread ID of the thread that completed the single step.
   * @param resolvedAddress The address of the called function.
   */
  private void processCompleteSingleStep(final long threadId, final BigInteger resolvedAddress) {
    final BigInteger lastIndirectCallAddress = lastHits.get(threadId);
    if (lastIndirectCallAddress == null) {
      // In rare cases we complete a single step without hitting a breakpoint
      // first.
      // This can happen because of a bug in the Win32 debug client that
      // occurs because of a race condition in multi-threaded programs.
      return;
    }

    synchronized (resolvedAddresses) {
      if (!resolvedAddresses.containsKey(lastIndirectCallAddress)) {
        resolvedAddresses.put(lastIndirectCallAddress, new HashSet<ResolvedFunction>());
      }

      final ResolvedFunction resolvedFunction = resolveFunction(new Address(resolvedAddress));

      if (resolvedAddresses.get(lastIndirectCallAddress).add(resolvedFunction)) {
        hitCounter.put(lastIndirectCallAddress, 0);
      }

      if (hitCounter.get(lastIndirectCallAddress) >= HIT_THRESHOLD) {
        final IndirectCall indirectCall = IndirectCallResolver.findIndirectCall(debugger,
            indirectCallAddresses, lastIndirectCallAddress);

        if (indirectCall != null) {
          removeBreakpoint(indirectCall);
          removedBreakpoints.add(indirectCall);

          resolvedCall(lastIndirectCallAddress, resolvedFunction);
        }
      }
    }
  }

  /**
   * Removes a breakpoint from an indirect call.
   *
   * @param indirectCall The indirect call from which the breakpoint is removed.
   */
  private void removeBreakpoint(final IndirectCall indirectCall) {
    final Module module = indirectCall.getModule();
    final Address address = indirectCall.getAddress();
    final BreakpointManager breakpointManager = debugger.getBreakpointManager();
    if (breakpointManager.hasBreakpoint(module, address)) {
      debugger.getBreakpointManager().removeBreakpoint(indirectCall.getModule(),
          indirectCall.getAddress());
    }
  }

  private void removeBreakpoints() {
    for (final IndirectCall indirectCall : indirectCallAddresses) {
      if (!removedBreakpoints.contains(indirectCall)) {
        try {
          removeBreakpoint(indirectCall);
        } catch (final Exception exception) {
          Logger.logException(exception);
        }
      }
    }
  }

  /**
   * Resumes the target process so that the attached listener can handle breakpoint hits.
   */
  private void resolveBreakpoints() {
    if (debugger.getProcess().getTargetInformation() == null) {
      errorNotAttached();
      step = 2;
      return;
    }

    try {
      debugger.resume();
    } catch (final DebugException e) {
      errorResuming(e);
    }

    step++;
  }

  private ResolvedFunction resolveFunction(final Address address) {
    for (final Module module : target.getModules()) {
      if (!resolvedFunctions.containsKey(module)) {
        resolveFunctions(module);
        if (!resolvedFunctions.containsKey(module)) {
          continue;
        }
      }

      final Map<Address, Function> functionMap = resolvedFunctions.get(module);

      final Function function = functionMap.get(address);

      if (function != null) {
        return new ResolvedFunction(function);
      }
    }

    for (final MemoryModule memoryModule : target.getDebugger().getProcess().getModules()) {
      if ((address.toLong() >= memoryModule.getBaseAddress().toLong()) && (address.toLong()
          < (memoryModule.getBaseAddress().toLong() + memoryModule.getSize()))) {
        return new ResolvedFunction(memoryModule, address);
      }
    }

    return new ResolvedFunction(address);
  }

  private void resolveFunctions(final Module module) {
    if (!module.isLoaded()) {
      return;
    }

    final Map<Address, Function> functionMap = new HashMap<Address, Function>();

    for (final Function function : module.getFunctions()) {
      final Address rebasedAddress =
          target.getDebugger().toImagebase(module, function.getAddress());

      functionMap.put(rebasedAddress, function);
    }

    resolvedFunctions.put(module, functionMap);
  }

  /**
   * Sets breakpoints on all previously determined indirect function call addresses.
   */
  private void setBreakpoints() {
    for (final IndirectCall indirectCall : indirectCallAddresses) {
      debugger.getBreakpointManager().setBreakpoint(indirectCall.getModule(),
          indirectCall.getAddress());
    }
    step++;
  }

  /**
   * Starts the target debugger.
   */
  private void startDebugger() {
    if (debugger == null) {
      errorNoDebugger();

      return;
    }

    debugger.addListener(debuggerListener);

    try {
      if (!debugger.isConnected()) {
        debugger.connect();
      }

      step++;
    } catch (final DebugException e) {
      debugger.removeListener(debuggerListener);

      errorConnectingDebugger(e);
    }
  }

  private void stopResolving() {
    for (final Module module : modules) {
      module.removeListener(moduleKeeperListener);
    }

    modules.clear();

    if ((debugger != null) && debugger.isConnected()) {
      try {
        debugger.terminate();
      } catch (final DebugException e) {
        e.printStackTrace();
      }
    }

    if ((step == 3) || (step == 4) || (step == 5)) {
      debugger.removeListener(debuggerListener);
      removeBreakpoints();
    }

    step++;
  }

  protected abstract void debuggerChanged();
  protected abstract void debuggerClosed();
  protected abstract void errorConnectingDebugger(DebugException e);
  protected abstract void errorLoadingModule(Module module, CouldntLoadDataException e);
  protected abstract void errorNoDebugger();
  protected abstract void errorNotAttached();
  protected abstract void errorResuming(DebugException e);
  protected abstract void foundIndirectCallAddresses(List<IndirectCall> indirectCallAddresses2);

  protected abstract void resolvedCall(BigInteger lastIndirectCall,
      ResolvedFunction resolvedFunction);

  public void dispose() {
    target.removeListener(internalTargetListener);

    stopResolving();
  }

  public int getCurrentStep() {
    return step;
  }

  public List<IndirectCall> getIndirectAddresses() {
    return new ArrayList<IndirectCall>(indirectCallAddresses);
  }

  public Map<BigInteger, Set<ResolvedFunction>> getResolvedAddresses() {
    synchronized (resolvedAddresses) {
      return new HashMap<BigInteger, Set<ResolvedFunction>>(resolvedAddresses);
    }
  }

  public ICallResolverTarget getTarget() {
    return target;
  }

  /**
   * Executes the next step of the call resolving process.
   */
  public void next() {
    switch (step) {
      case 0:
        loadTargetModules();
        break;
      case 1:
        findIndirectCallAddresses();
        break;
      case 2:
        startDebugger();
        break;
      case 3:
        setBreakpoints();
        break;
      case 4:
        resolveBreakpoints();
        break;
      case 5:
        stopResolving();
        break;
      case 6:
        reset();
    }
  }

  /**
   * Resets the call resolver.
   */
  public void reset() {
    if (step != 6) {
      stopResolving();
    }

    resolvedAddresses.clear();
    resolvedFunctions.clear();
    hitCounter.clear();
    removedBreakpoints.clear();
    lastHits.clear();
    indirectCallAddresses = null;

    step = 0;
  }

  /**
   * Keeps track of incoming breakpoint hits.
   */
  private class InternalDebuggerListener extends DebuggerListenerAdapter {
    @Override
    public void debuggerClosed(final int errorCode) {
      step = 5;

      stopResolving();

      CallResolver.this.debuggerClosed();
    }

    @Override
    public void breakpointHit(final DebuggerBreakpointHitReply reply) {
      if (step != 5) {
        return;
      }

      final BigInteger breakpointAddress =
          getProgramCounter(reply.getThreadId(), reply.getRegisterValues());

      countHit(reply.getThreadId(), breakpointAddress);

      try {
        target.getDebugger().singleStep();
      } catch (final DebugException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void requestTarget(final DebuggerRequestTargetReply reply) {
      // The debugger is asking us for the path to the target process to be started.
      if (reply.getErrorCode() == 0) {
        SwingUtilities.invokeLater(new Thread() {
          @Override
          public void run() {
            final RemoteFileBrowserLoader loader = new RemoteFileBrowserLoader(parent, debugger);
            if (!loader.load()) {
              try {
                debugger.cancelTargetSelection();
              } catch (final DebugException e) {
                Logger.logException(e);
              }
            }
          }
        });
      }
    }

    @Override
    public void singleStep(final DebuggerSingleStepReply reply) {
      if (step != 5) {
        return;
      }

      final BigInteger resolvedAddress =
          getProgramCounter(reply.getThreadId(), reply.getRegisterValues());

      processCompleteSingleStep(reply.getThreadId(), resolvedAddress);

      try {
        debugger.resume();
      } catch (final DebugException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void targetInformation(final DebuggerTargetInformationReply reply) {
      try {
        debugger.setExceptionSettings(reply.getTargetInformation().getExceptionSettings());
      } catch (final DebugException e) {
        e.printStackTrace();
      }
    }
  }

  private class InternalTargetListener implements ICallResolverTargetListener {
    @Override
    public void changedDebugger(final ICallResolverTarget target, final Debugger debugger) {
      reset();

      debuggerChanged();
    }
  }
}
