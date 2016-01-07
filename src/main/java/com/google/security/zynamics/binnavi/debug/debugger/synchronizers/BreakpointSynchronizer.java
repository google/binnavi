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
package com.google.security.zynamics.binnavi.debug.debugger.synchronizers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointManagerListener;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class keeps the state of the breakpoint manager of a debugger synchronized with the actual
 * state of the breakpoints in the system that is debugged by that debugger.
 */
public final class BreakpointSynchronizer {
  /**
   * The debugger that is used to communicate with the target process.
   */
  private final IDebugger debugger;

  /**
   * The breakpoint manager that is kept synchronized by the breakpoint synchronizer.
   */
  private final BreakpointManager manager;

  /**
   * Synchronizes breakpoint manager events with the target process.
   */
  private final InternalBreakpointManagerListener managerListener =
      new InternalBreakpointManagerListener();

  /**
   * Creates a new breakpoint synchronizer object.
   *
   * @param debugger The debugger that is synchronized.
   */
  public BreakpointSynchronizer(final IDebugger debugger) {
    this.debugger =
        Preconditions.checkNotNull(debugger, "IE00795: Debugger argument can not be null");
    manager = Preconditions.checkNotNull(debugger.getBreakpointManager(),
        "Error: debugger.getBreakpointManager() argument can not be null");
    manager.addListener(managerListener);
  }

  private void deleteBreakPoints(final Set<BreakpointAddress> addresses,
      final BreakpointType type) {
    if (addresses.size() == 0) {
      return;
    }
    try {
      debugger.removeBreakpoints(addresses, type);
    } catch (final DebugExceptionWrapper exception) {
      manager.removeBreakpointsPassive(BreakpointType.REGULAR, Sets.newHashSet(addresses));
      NaviLogger.severe(
          "Error: Debugger could not remove " + type.toString() + " breakpoints. Exception %s",
          exception);
    }
  }

  private void echoBreakpointStateChange(
      final Entry<Breakpoint, BreakpointStatus> breakpointToOldStatus,
      final Set<BreakpointAddress> toSet) {
    if (manager.getBreakpointStatus(breakpointToOldStatus.getKey().getAddress(),
        BreakpointType.ECHO) == BreakpointStatus.BREAKPOINT_ENABLED) {
      toSet.add(breakpointToOldStatus.getKey().getAddress());
    }
  }

  private void regularBreakpointStateChange(
      final Entry<Breakpoint, BreakpointStatus> breakpointToOldStatus,
      final Set<BreakpointAddress> toSet, final Set<BreakpointAddress> toRemoveFromDebugger,
      final Set<BreakpointAddress> toRemoveFromManager) {
    final BreakpointStatus oldStatus = breakpointToOldStatus.getValue();

    if (manager.getBreakpointStatus(breakpointToOldStatus.getKey().getAddress(),
        BreakpointType.REGULAR) == BreakpointStatus.BREAKPOINT_ENABLED) {
      toSet.add(breakpointToOldStatus.getKey().getAddress());
    } else if (manager.getBreakpointStatus(breakpointToOldStatus.getKey().getAddress(),
        BreakpointType.REGULAR) == BreakpointStatus.BREAKPOINT_DELETING) {
      if ((oldStatus == BreakpointStatus.BREAKPOINT_INVALID)
          || (oldStatus == BreakpointStatus.BREAKPOINT_DISABLED)) {
        toRemoveFromManager.add(breakpointToOldStatus.getKey().getAddress());
      } else {
        toRemoveFromDebugger.add(breakpointToOldStatus.getKey().getAddress());
      }
    } else if (manager.getBreakpointStatus(breakpointToOldStatus.getKey().getAddress(),
        BreakpointType.REGULAR) == BreakpointStatus.BREAKPOINT_DISABLED) {
      if ((oldStatus != BreakpointStatus.BREAKPOINT_DELETING)
          && (oldStatus != BreakpointStatus.BREAKPOINT_INVALID)
          && (oldStatus != BreakpointStatus.BREAKPOINT_INACTIVE)) {
        toRemoveFromDebugger.add(breakpointToOldStatus.getKey().getAddress());
      }
    }
  }

  private void setBreakPoints(final Set<BreakpointAddress> addresses, final BreakpointType type) {
    if (addresses.size() != 0) {
      try {
        debugger.setBreakPoints(addresses, type);
      } catch (final DebugExceptionWrapper exception) {
        manager.setBreakpointStatus(Sets.newHashSet(addresses), type,
            BreakpointStatus.BREAKPOINT_DISABLED);
        NaviLogger.severe(
            "Error: Debugger could not set " + type.toString() + " breakpoints. Exception %s",
            exception);
      }
    }
  }

  private void stepBreakpointStateChange(
      final Entry<Breakpoint, BreakpointStatus> breakpointToOldStatus,
      final Set<BreakpointAddress> toSet, final Set<BreakpointAddress> toRemove) {
    if (manager.getBreakpointStatus(breakpointToOldStatus.getKey().getAddress(),
        BreakpointType.STEP) == BreakpointStatus.BREAKPOINT_ENABLED) {
      toSet.add(breakpointToOldStatus.getKey().getAddress());
    } else if (manager.getBreakpointStatus(breakpointToOldStatus.getKey().getAddress(),
        BreakpointType.STEP) == BreakpointStatus.BREAKPOINT_DELETING) {
      toRemove.add(breakpointToOldStatus.getKey().getAddress());
    }
  }

  /**
   * Synchronizes breakpoint manager events with the target process.
   */
  private class InternalBreakpointManagerListener implements BreakpointManagerListener {

    /**
     * Handler to take care of a list of {@link Breakpoint} to be added to the appropriate lists.
     *
     * @param breakPoints The {@link Breakpoint} to add.
     * @return The list of {@link BreakpointAddress} that can be added to the debugger.
     */
    private List<BreakpointAddress> echoBreakPointsAddedHandler(
        final List<Breakpoint> breakPoints) {
      final List<BreakpointAddress> addresses = new ArrayList<BreakpointAddress>();
      final Set<BreakpointAddress> addressesForManager = new HashSet<BreakpointAddress>();

      for (final Breakpoint breakpoint : breakPoints) {
        Preconditions.checkArgument(
            manager.getBreakpointStatus(breakpoint.getAddress(), BreakpointType.ECHO)
            == BreakpointStatus.BREAKPOINT_ENABLED,
            "Internal Error: Breakpoint with type echo has unexpected status");

        if (debugger.isConnected()) {
          if (isBreakpointInsideModules(breakpoint, debugger.getProcessManager().getModules())) {
            addresses.add(breakpoint.getAddress());
          } else {
            addressesForManager.add(breakpoint.getAddress());
          }
        } else {
          NaviLogger.severe(
              "Internal Error: It should only be possible to set echo breakpoints when the "
              + "debugger is connected");
        }
      }

      manager.setBreakpointStatus(addressesForManager, BreakpointType.ECHO,
          BreakpointStatus.BREAKPOINT_INACTIVE);
      return addresses;

    }

    /**
     * Test if given {@link Breakpoint} is within the boundaries of any of the loaded modules.
     *
     * @param breakpoint The {@link Breakpoint} to be tested.
     * @param list The list of currently loaded modules.
     * @return True if the breakpoint is contained within a module. False otherwise.
     */
    private boolean isBreakpointInsideModules(final Breakpoint breakpoint,
        final List<MemoryModule> list) {
      final RelocatedAddress bpAddress = debugger.fileToMemory(breakpoint.getAddress().getModule(),
          breakpoint.getAddress().getAddress());
      for (final MemoryModule module : list) {
        final boolean addressOk = (bpAddress.getAddress().toBigInteger()
            .compareTo(module.getBaseAddress().getAddress().toBigInteger()) >= 0) && (bpAddress
            .getAddress().toBigInteger().compareTo(module.getBaseAddress().getAddress()
                .toBigInteger().add(BigInteger.valueOf(module.getSize()))) <= 0);

        if (addressOk && (module.getName().compareToIgnoreCase(
            breakpoint.getAddress().getModule().getConfiguration().getName()) == 0)) {
          return true;
        }
      }
      return false;
    }

    private List<BreakpointAddress> regularBreakPointsAddedHandler(
        final List<Breakpoint> breakPoints) {
      final List<BreakpointAddress> addresses = new ArrayList<BreakpointAddress>();
      final Set<BreakpointAddress> addressesForManager = new HashSet<BreakpointAddress>();

      for (final Breakpoint breakpoint : breakPoints) {
        if (debugger.isConnected()) {
          if (isBreakpointInsideModules(breakpoint, debugger.getProcessManager().getModules())) {
            addresses.add(breakpoint.getAddress());
          } else {
            addressesForManager.add(breakpoint.getAddress());
          }
        }
      }
      manager.setBreakpointStatus(addressesForManager, BreakpointType.REGULAR,
          BreakpointStatus.BREAKPOINT_INACTIVE);
      return addresses;
    }

    private void removeBreakpoints(final Set<BreakpointAddress> addresses,
        final BreakpointType type) {
      if (addresses.size() != 0) {
        try {
          debugger.removeBreakpoints(addresses, type);
        } catch (final DebugExceptionWrapper exception) {
          NaviLogger.severe(
              "Error: Debugger could not remove " + type.toString() + " breakpoints. Exception %s",
              exception);
        }
      }
    }

    private List<BreakpointAddress> stepBreakPointsAddedHandler(
        final List<Breakpoint> breakpoints) {
      final List<BreakpointAddress> addresses = new ArrayList<BreakpointAddress>();
      final Set<BreakpointAddress> addressesForManager = new HashSet<BreakpointAddress>();

      for (final Breakpoint breakpoint : breakpoints) {
        // When a step breakpoint is added, its state is changed from Inactive to Enabled.
        // The breakpointStatusChanged method then tries to set the breakpoint in the target
        // process.

        Preconditions.checkArgument(
            manager.getBreakpointStatus(breakpoint.getAddress(), BreakpointType.STEP)
            == BreakpointStatus.BREAKPOINT_INACTIVE,
            "Internal Error: Breakpoint with type STEP has unexpected status");
        Preconditions.checkArgument(debugger.isConnected(),
            "Internal Error: It should only be possible to set step breakpoints when the "
            + "debugger is connected");

        if (isBreakpointInsideModules(breakpoint, debugger.getProcessManager().getModules())) {
          addresses.add(breakpoint.getAddress());
        } else {
          addressesForManager.add(breakpoint.getAddress());
        }
      }
      manager.setBreakpointStatus(addressesForManager, BreakpointType.STEP,
          BreakpointStatus.BREAKPOINT_INACTIVE);
      return addresses;
    }

    @Override
    public void breakpointsAdded(final List<Breakpoint> breakpoints) {
      final BreakpointType breakpointType = breakpoints.get(0).getType();
      for (final Breakpoint currentBreakPoint : breakpoints) {
        Preconditions.checkArgument(currentBreakPoint.getType() == breakpointType,
            "Error: breakpoint types are not equal for all breakpoints");
      }

      final Set<BreakpointAddress> addresses = new HashSet<BreakpointAddress>();

      switch (breakpointType) {
        case ECHO:
          addresses.addAll(echoBreakPointsAddedHandler(breakpoints));
          break;
        case REGULAR:
          addresses.addAll(regularBreakPointsAddedHandler(breakpoints));
          break;
        case STEP:
          addresses.addAll(stepBreakPointsAddedHandler(breakpoints));
          break;
      }

      if (addresses.size() > 0) {
        setBreakPoints(addresses, breakpointType);
        manager.setBreakpointStatus(addresses, breakpointType, BreakpointStatus.BREAKPOINT_ACTIVE);
      }
    }

    private void enableSingleBPCondition(final Breakpoint breakpoint) {
      final BreakpointStatus status =
          manager.getBreakpointStatus(breakpoint.getAddress(), BreakpointType.REGULAR);
      if ((status.equals(BreakpointStatus.BREAKPOINT_ENABLED)
          || status.equals(BreakpointStatus.BREAKPOINT_ACTIVE))
          && breakpoint.getCondition() != null) {
        try {
          debugger.setBreakPointCondition(breakpoint.getAddress(), breakpoint.getCondition());
        } catch (final DebugExceptionWrapper exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }


    @Override
    public void breakpointsConditionChanged(final Set<Breakpoint> breakpoints) {
      for (final Breakpoint breakpoint : breakpoints) {
        enableSingleBPCondition(breakpoint);
      }
    }

    @Override
    public void breakpointsDescriptionChanged(final Set<Breakpoint> breakpoints) {}

    @Override
    public void breakpointsRemoved(final Set<Breakpoint> breakpoints) {
      if (breakpoints.size() != 0) {
        final Set<BreakpointAddress> echoAddresses = Sets.newHashSet();
        final Set<BreakpointAddress> stepAddresses = Sets.newHashSet();
        final Set<BreakpointAddress> regularAddresses = Sets.newHashSet();

        for (final Breakpoint breakpoint : breakpoints) {
          switch (breakpoint.getType()) {
            case ECHO:
              echoAddresses.add(breakpoint.getAddress());
              break;
            case STEP:
              stepAddresses.add(breakpoint.getAddress());
              break;
            case REGULAR:
              regularAddresses.add(breakpoint.getAddress());
              break;
          }
        }

        removeBreakpoints(echoAddresses, BreakpointType.ECHO);
        removeBreakpoints(stepAddresses, BreakpointType.STEP);
        removeBreakpoints(regularAddresses, BreakpointType.REGULAR);
      }
    }

    @Override
    public void breakpointsStatusChanged(
        final Map<Breakpoint, BreakpointStatus> breakpointsToOldStatus,
        final BreakpointStatus newStatus) {
      final Set<BreakpointAddress> regularBreakpointAddressesToAdd =
          new HashSet<BreakpointAddress>();
      final Set<BreakpointAddress> regularBreakpointAddressesToRemoveFromDebugger =
          new HashSet<BreakpointAddress>();
      final Set<BreakpointAddress> regularBreakpointAddressesToRemoveFromManager =
          new HashSet<BreakpointAddress>();
      final Set<BreakpointAddress> stepBreakpointAddressesToAdd =
          new HashSet<BreakpointAddress>();
      final Set<BreakpointAddress> stepBreakpointAddressesToRemove =
          new HashSet<BreakpointAddress>();
      final Set<BreakpointAddress> echoBreakPointAddressesToAdd =
          new HashSet<BreakpointAddress>();

      for (final Entry<Breakpoint, BreakpointStatus> breakpointToOldStatus :
          breakpointsToOldStatus.entrySet()) {
        final BreakpointType type = breakpointToOldStatus.getKey().getType();
        switch (type) {
          case REGULAR:
            regularBreakpointStateChange(breakpointToOldStatus, regularBreakpointAddressesToAdd,
                regularBreakpointAddressesToRemoveFromDebugger,
                regularBreakpointAddressesToRemoveFromManager);
            break;
          case ECHO:
            echoBreakpointStateChange(breakpointToOldStatus, echoBreakPointAddressesToAdd);
            break;
          case STEP:
            stepBreakpointStateChange(breakpointToOldStatus, stepBreakpointAddressesToAdd,
                stepBreakpointAddressesToRemove);
            break;
          default:
            break;
        }
      }

      if (debugger.isConnected()) {
        if (!echoBreakPointAddressesToAdd.isEmpty()) {
          setBreakPoints(echoBreakPointAddressesToAdd, BreakpointType.ECHO);
        }
        if (!stepBreakpointAddressesToAdd.isEmpty()) {
          setBreakPoints(stepBreakpointAddressesToAdd, BreakpointType.STEP);
        }
        if (!regularBreakpointAddressesToAdd.isEmpty()) {
          setBreakPoints(regularBreakpointAddressesToAdd, BreakpointType.REGULAR);
          breakpointAddressesConditionChanged(regularBreakpointAddressesToAdd);
        }
        if (!regularBreakpointAddressesToRemoveFromDebugger.isEmpty()) {
          deleteBreakPoints(regularBreakpointAddressesToRemoveFromDebugger, BreakpointType.REGULAR);
        }
        if (!regularBreakpointAddressesToRemoveFromManager.isEmpty()) {
          manager.removeBreakpointsPassive(BreakpointType.REGULAR,
              regularBreakpointAddressesToRemoveFromManager);
        }
      } else {
        if (!echoBreakPointAddressesToAdd.isEmpty()) {
          manager.setBreakpointStatus(echoBreakPointAddressesToAdd, BreakpointType.ECHO,
              BreakpointStatus.BREAKPOINT_INACTIVE);
        }

        if (!regularBreakpointAddressesToAdd.isEmpty()) {
          manager.setBreakpointStatus(regularBreakpointAddressesToAdd, BreakpointType.REGULAR,
              BreakpointStatus.BREAKPOINT_INACTIVE);
        }
        if (!stepBreakpointAddressesToAdd.isEmpty()) {
          manager.setBreakpointStatus(stepBreakpointAddressesToAdd, BreakpointType.STEP,
              BreakpointStatus.BREAKPOINT_DELETING);
        }
        if (!stepBreakpointAddressesToRemove.isEmpty()) {
          manager.removeBreakpointsPassive(BreakpointType.STEP, stepBreakpointAddressesToRemove);
        }
        if (!regularBreakpointAddressesToRemoveFromDebugger.isEmpty()) {
          manager.removeBreakpointsPassive(BreakpointType.REGULAR,
              regularBreakpointAddressesToRemoveFromDebugger);
        }
        if (!regularBreakpointAddressesToRemoveFromManager.isEmpty()) {
          manager.removeBreakpointsPassive(BreakpointType.REGULAR,
              regularBreakpointAddressesToRemoveFromManager);
        }
      }
    }

    private void breakpointAddressesConditionChanged(
        final Set<BreakpointAddress> regularBreakpointAddressesToAdd) {
      for (final BreakpointAddress address : regularBreakpointAddressesToAdd) {
        enableSingleBPCondition(manager.getBreakpoint(BreakpointType.REGULAR, address));
      }
    }
  }
}
