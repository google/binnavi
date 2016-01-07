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
package com.google.security.zynamics.binnavi.debug.models.breakpoints;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointManagerListener;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointStorage;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.IndexedBreakpointStorage;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Breakpoint manager class that can keep track of a breakpoints of all types.
 *
 *  Details of breakpoint management like the priority levels of breakpoint types that must be
 * considered when setting breakpoints or the initial breakpoint status for the different breakpoint
 * types are automatically handled by this class.
 */
public final class BreakpointManager {
  private final BreakpointStorage echoBreakpointStorage = new DefaultBreakpointStorage();
  private final BreakpointStorage stepBreakpointStorage = new DefaultBreakpointStorage();
  private final IndexedBreakpointStorage indexedBreakpointStorage =
      new RegularBreakpointStorage();

  private final ListenerProvider<BreakpointManagerListener> listeners = new ListenerProvider<>();

  /**
   * Adds a list of breakpoints to the breakpoint manager.
   *
   * @param addresses Addresses of the breakpoints to add.
   * @param status Initial status of the new breakpoints.
   */
  private void addBreakpoints(final Set<BreakpointAddress> addresses,
      final BreakpointStatus status, final BreakpointStorage storage, final BreakpointType type) {
    Preconditions.checkNotNull(addresses, "IE00718: addresses argument can not be null");
    Preconditions.checkNotNull(status, "IE00719: status argument can not be null");
    Preconditions.checkNotNull(storage, "IE00720: storage argument can not be null");
    Preconditions.checkNotNull(type, "IE00721: type argument can not be null");

    if (addresses.size() == 0) {
      return;
    }

    final List<Breakpoint> breakpoints = new ArrayList<>();

    for (final BreakpointAddress address : addresses) {
      final Breakpoint breakpoint = new Breakpoint(type, address);
      storage.add(breakpoint, status);
      breakpoints.add(breakpoint);
    }

    for (final BreakpointManagerListener listener : listeners) {
      try {
        listener.breakpointsAdded(breakpoints);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Checks the validity of the passed arguments.
   *
   * @param type A breakpoint type to check.
   * @param address An address to check.
   *
   * @throws IllegalArgumentException Thrown if any of the input arguments are invalid.
   */
  private void checkArguments(final BreakpointType type, final BreakpointAddress address) {
    Preconditions.checkNotNull(type, "IE01012: Type argument can not be null");
    Preconditions.checkNotNull(address, "IE01022: Address argument can not be null");
  }

  /**
   * This function enforces the type hierarchy of breakpoints.
   *
   * @param addresses The set of addresses for the breakpoints to be added.
   * @param type The type of the breakpoints to be added.
   *
   * @return The Set of breakpoints which has been set.
   */
  private Set<BreakpointAddress> enforceBreakpointHierarchy(
      final Set<BreakpointAddress> addresses, final BreakpointType type) {
    final SetView<BreakpointAddress> alreadyRegularBreakpoints =
        Sets.intersection(addresses, indexedBreakpointStorage.getBreakPointAddresses());
    final SetView<BreakpointAddress> alreadySteppingBreakpoints =
        Sets.intersection(addresses, stepBreakpointStorage.getBreakPointAddresses());
    final SetView<BreakpointAddress> alreadyEchoBreakpoints =
        Sets.intersection(addresses, echoBreakpointStorage.getBreakPointAddresses());

    Set<BreakpointAddress> addressesSet = null;

    switch (type) {
      case REGULAR:
        final SetView<BreakpointAddress> notInRegularBreakpoints =
            Sets.difference(addresses, indexedBreakpointStorage.getBreakPointAddresses());
        removeBreakpoints(alreadySteppingBreakpoints, stepBreakpointStorage);
        removeBreakpoints(alreadyEchoBreakpoints, echoBreakpointStorage);
        addressesSet = notInRegularBreakpoints;
        break;

      case STEP:
        final SetView<BreakpointAddress> notInSteppingBreakpoints =
            Sets.difference(addresses, stepBreakpointStorage.getBreakPointAddresses());
        removeBreakpoints(alreadyEchoBreakpoints, echoBreakpointStorage);
        addressesSet = Sets.difference(notInSteppingBreakpoints, alreadyRegularBreakpoints);
        break;

      case ECHO:
        final SetView<BreakpointAddress> notInEchoBreakPoints =
            Sets.difference(addresses, echoBreakpointStorage.getBreakPointAddresses());
        addressesSet = Sets.difference(notInEchoBreakPoints,
            Sets.union(alreadySteppingBreakpoints, alreadyRegularBreakpoints));
        break;
      default:
        throw new IllegalStateException("IE00722: Breakpoint of invalid type");

    }
    return addressesSet;
  }

  /**
   * Remove the given set of breakpoints.
   *
   * @param breakpointAddressSet The breakpoints to be removed.
   */
  private void removeBreakpoints(final Set<BreakpointAddress> breakpointAddressSet,
      final BreakpointStorage storage) {
    if (breakpointAddressSet.size() != 0) {
      final Set<Breakpoint> breakpoints = storage.getBreakPointsByAddress(breakpointAddressSet);
      storage.removeBreakpoints(breakpointAddressSet);

      for (final BreakpointManagerListener listener : listeners) {
        try {
          listener.breakpointsRemoved(breakpoints);
        } catch (final Exception e) {
          // This should never happen. Breakpoint listeners are not allowed
          // to throw. Nevertheless, if we don't catch unexpected exceptions,
          // it is possible that we forget to release the semaphore.
          CUtilityFunctions.logException(e);
        }
      }
    }
  }

  private void setBreakpointsStatus(final Set<BreakpointAddress> addresses,
      final BreakpointStatus newStatus, final BreakpointStorage storage) {
    if (storage.getBreakPointsByAddress(addresses).isEmpty()) {
      return;
    }

    final Map<Breakpoint, BreakpointStatus> breakpointToStatus = Maps.newHashMap();

    for (final BreakpointAddress breakpointAddress : addresses) {
      breakpointToStatus.put(storage.get(breakpointAddress),
          storage.getBreakpointStatus(breakpointAddress));
      storage.setBreakpointStatus(breakpointAddress, newStatus);
    }

    for (final BreakpointManagerListener listener : listeners) {
      try {
        listener.breakpointsStatusChanged(breakpointToStatus, newStatus);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  public void setBreakpointCondition(final int breakpointIndex, final String formula) {
    Preconditions.checkNotNull(formula, "Error: formula argument can not be null.");

    final Breakpoint breakpoint = getBreakpoint(BreakpointType.REGULAR, breakpointIndex);
    final Set<Breakpoint> conditinalBreakPoints = Sets.newHashSet();

    Condition condition = null;
    try {
      condition = BreakpointConditionParser.evaluate(formula);
    } catch (final InvalidFormulaException exception) {
      CUtilityFunctions.logException(exception);
    }

    breakpoint.setCondition(condition);
    conditinalBreakPoints.add(breakpoint);

    for (final BreakpointManagerListener listener : listeners) {
      try {
        listener.breakpointsConditionChanged(conditinalBreakPoints);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a list of breakpoints to the breakpoint manager.
   *
   * @param type The type of the breakpoints.
   * @param addresses Addresses of the breakpoints to add.
   */
  public void addBreakpoints(final BreakpointType type, final Set<BreakpointAddress> addresses) {
    if (addresses.size() == 0) {
      return;
    }

    switch (type) {
      case REGULAR:
        addBreakpoints(enforceBreakpointHierarchy(addresses, type),
            BreakpointStatus.BREAKPOINT_INACTIVE, indexedBreakpointStorage, type);
        break;
      case ECHO:
        addBreakpoints(enforceBreakpointHierarchy(addresses, type),
            BreakpointStatus.BREAKPOINT_ENABLED, echoBreakpointStorage, type);
        break;
      case STEP:
        addBreakpoints(enforceBreakpointHierarchy(addresses, type),
            BreakpointStatus.BREAKPOINT_INACTIVE, stepBreakpointStorage, type);
        break;
      default:
        throw new IllegalStateException(String.format("Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * Adds a breakpoint listener that is notified about relevant changes in the breakpoint manager.
   *
   * @param listener The listener to add.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   */
  public void addListener(final BreakpointManagerListener listener) {
    listeners.addListener(
        Preconditions.checkNotNull(listener, "IE00723: listener argument can not be null"));
  }

  /**
   * Clears all echo breakpoints without notifying the listeners about the deleted breakpoints.
   *
   * @param type The type of the breakpoints to clear. Note that REGULAR breakpoints can not be
   *        cleared passively
   *
   * @throws IllegalArgumentException Thrown if the type argument is null or REGULAR.
   */
  public void clearBreakpointsPassive(final BreakpointType type) {
    Preconditions.checkNotNull(type, "IE01011: Type argument can not be null");
    NaviLogger.info("Clearing all breakpoints of type '%s' passively", type);

    switch (type) {
      case REGULAR:
        throw new IllegalStateException(
            "IE01018: Regular breakpoints can not be cleared passively");
      case ECHO:
        echoBreakpointStorage.clear();
        return;
      case STEP:
        stepBreakpointStorage.clear();
        return;
      default:
        throw new IllegalStateException(
            String.format("IE01017: Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * Returns the breakpoint of a given type at a given address.
   *
   * @param type The type of the breakpoint.
   * @param address The address of the breakpoint.
   *
   * @return The breakpoint with the given attributes.
   *
   * @throws IllegalArgumentException Thrown if any of the input arguments are null or if there is
   *         no breakpoint of the given type at the given address.
   */
  public Breakpoint getBreakpoint(final BreakpointType type, final BreakpointAddress address) {
    checkArguments(type, address);

    switch (type) {
      case REGULAR:
        return indexedBreakpointStorage.get(address);
      case ECHO:
        return echoBreakpointStorage.get(address);
      case STEP:
        return stepBreakpointStorage.get(address);
      default:
        throw new IllegalStateException(
            String.format("IE01008: Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * Returns the breakpoint of a given type and a given index.
   *
   * @param type The type of the breakpoint.
   * @param index The index of the breakpoint (0 <= index < getNumberOfBreakpoints(type))
   * @return The breakpoint with the given attributes.
   * @throws IllegalArgumentException Thrown if the type arguments is null or the index is out of
   *         bounds.
   */
  public Breakpoint getBreakpoint(final BreakpointType type, final int index) {
    switch (type) {
      case REGULAR:
        return indexedBreakpointStorage.get(index);
      default:
        throw new IllegalStateException(String.format(
            "IE01019: Invalid breakpoint type '%s' for indexed breakpoint access", type));
    }
  }

  /**
   * Returns all breakpoints of a given type.
   *
   * @param type The type of the breakpoints to return.
   *
   * @return A list of all managed breakpoints of the given type.
   */
  public Iterable<Breakpoint> getBreakpoints(final BreakpointType type) {
    switch (type) {
      case REGULAR:
        return indexedBreakpointStorage.getBreakpoints();
      case ECHO:
        return echoBreakpointStorage.getBreakpoints();
      case STEP:
        return stepBreakpointStorage.getBreakpoints();
      default:
        throw new IllegalStateException(String.format("Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * Returns all breakpoints of a given type in the given module.
   *
   * @param type The type of the breakpoint.
   * @param module The module where to which the breakpoints are associated.
   *
   * @return A {@link Set} of {@link Breakpoint} which qualify for the above.
   */
  public Set<Breakpoint> getBreakpointsByModule(final BreakpointType type,
      final MemoryModule module) {
    switch (type) {
      case REGULAR:
        return indexedBreakpointStorage.getByModule(module);
      case ECHO:
        return echoBreakpointStorage.getByModule(module);
      case STEP:
        return stepBreakpointStorage.getByModule(module);
      default:
        throw new IllegalStateException(
            String.format("IE00725: Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * Returns the {@link BreakpointStatus} of a given index.
   *
   * @param type can only be {@link BreakpointType}.REGULAR.
   * @param index the index of the regular breakpoint.
   *
   * @return The {@link BreakpointStatus} of the breakpoint.
   */
  public BreakpointStatus getBreakpointStatus(final BreakpointType type, final int index) {
    switch (type) {
      case REGULAR:
        return getBreakpointStatus(indexedBreakpointStorage.get(index).getAddress(), type);
      default:
        throw new IllegalStateException("IE00724: Invalid breakpoint type " + type.toString()
            + " for indexed breakpoint access");
    }
  }

  /**
   * Returns the {@link BreakpointStatus} of a breakpoint with the given {@link BreakpointAddress}
   * and {@link BreakpointType}.
   *
   * @param address The {@link BreakpointAddress} of the breakpoint.
   * @param type The {@link BreakpointType} of the breakpoint.
   *
   * @return The {@link BreakpointStatus} of the breakpoint.
   */
  public BreakpointStatus getBreakpointStatus(final BreakpointAddress address,
      final BreakpointType type) {
    switch (type) {
      case REGULAR:
        return indexedBreakpointStorage.getBreakpointStatus(address);
      case ECHO:
        return echoBreakpointStorage.getBreakpointStatus(address);
      case STEP:
        return stepBreakpointStorage.getBreakpointStatus(address);
      default:
        throw new IllegalStateException("IE00726: Invalid Breakpoint Type");
    }
  }

  /**
   * Returns the number of managed breakpoints.
   *
   * @param type The type of the breakpoints.
   *
   * @return The number of breakpoints of the given type used in the project.
   *
   * @throws IllegalArgumentException Thrown if the type argument is null.
   */
  public int getNumberOfBreakpoints(final BreakpointType type) {
    Preconditions.checkNotNull(type, "IE01013: Type argument can not be null");

    switch (type) {
      case REGULAR:
        return indexedBreakpointStorage.size();
      case ECHO:
        return echoBreakpointStorage.size();
      case STEP:
        return stepBreakpointStorage.size();
      default:
        throw new IllegalStateException(
            String.format("IE01014: Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * For the specified breakpoint type retrieve the number of breakpoints belonging to any of the
   * given modules.
   *
   * @param type The breakpoint type for which to determine the number of breakpoints.
   * @param memoryModules The list of modules for which to determine the number of breakpoints.
   * @return The number of breakpoints with the given type which belong to any of the modules.
   */
  public int getNumberOfBreakpoints(final BreakpointType type,
      final IFilledList<MemoryModule> memoryModules) {
    Preconditions.checkNotNull(type, "IE00175: Type argument can not be null");
    Preconditions.checkNotNull(memoryModules, "IE00177: Memory Modules argument can not be null");

    switch (type) {
      case REGULAR:
        return indexedBreakpointStorage.sizeByModuleFilter(memoryModules);
      case ECHO:
        return echoBreakpointStorage.sizeByModuleFilter(memoryModules);
      case STEP:
        return stepBreakpointStorage.sizeByModuleFilter(memoryModules);
      default:
        throw new IllegalArgumentException(String.format("Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * Tests whether a breakpoint of the given type exists at the specified address.
   *
   * @param type The type of the breakpoint.
   * @param address The address of the breakpoint.
   *
   * @return True, if a breakpoint exists at the specified address. False, otherwise.
   *
   * @throws IllegalArgumentException Thrown if any of the input arguments are null.
   */
  public boolean hasBreakpoint(final BreakpointType type, final BreakpointAddress address) {
    checkArguments(type, address);

    switch (type) {
      case REGULAR:
        return indexedBreakpointStorage.get(address) != null;
      case ECHO:
        return echoBreakpointStorage.get(address) != null;
      case STEP:
        return stepBreakpointStorage.get(address) != null;
      default:
        throw new IllegalStateException(
            String.format("IE01007: Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * Remove a set of breakpoints given as breakpoint addresses.
   *
   * @param type The type of breakpoints which should be removed.
   * @param breakpoints The set of breakpoints which should be removed.
   */
  public void removeBreakpoints(final BreakpointType type,
      final Set<BreakpointAddress> breakpoints) {
    Preconditions.checkNotNull(type, "IE00178: Type argument can not be null");
    Preconditions.checkNotNull(breakpoints, "IE00179: breakpoints argument can not be null");

    if (breakpoints.size() == 0) {
      return;
    }

    switch (type) {
      case REGULAR:
        removeBreakpoints(breakpoints, indexedBreakpointStorage);
        break;
      case ECHO:
        removeBreakpoints(breakpoints, echoBreakpointStorage);
        break;
      case STEP:
        removeBreakpoints(breakpoints, stepBreakpointStorage);
        break;
      default:
        throw new IllegalStateException(String.format("Invalid breakpoint type '%s'", type));
    }
  }

  public void removeBreakpointsPassive(final BreakpointType type,
      final Set<BreakpointAddress> addresses) {
    if (addresses.size() == 0) {
      return;
    }
    switch (type) {
      case REGULAR:
        indexedBreakpointStorage.removeBreakpoints(addresses);
        break;
      case ECHO:
        echoBreakpointStorage.removeBreakpoints(addresses);
        break;
      case STEP:
        stepBreakpointStorage.removeBreakpoints(addresses);
        break;
      default:
        throw new IllegalStateException(
            String.format("IE01008: Invalid breakpoint type '%s'", type));
    }
  }

  /**
   * Remove a breakpoint listener from the list of listeners that are notified about changes in the
   * breakpoint manager.
   *
   * @param listener The listener to remove from the list.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   */
  public void removeListener(final BreakpointManagerListener listener) {
    listeners.removeListener(listener);
  }

  public void setBreakpointStatus(final BreakpointType type, final BreakpointStatus newStatus,
      final int index) {
    switch (type) {
      case REGULAR:
        setBreakpointsStatus(Sets.newHashSet(indexedBreakpointStorage.get(index).getAddress()),
            newStatus, indexedBreakpointStorage);
        break;
      default:
        throw new IllegalStateException(
            "IE00727: Invalid breakpoint type " + type + " for indexed breakpoint access");
    }
  }

  /**
   * Sets the status of a {@link Set} of {@link BreakpointAddress} with the given
   * {@link BreakpointType} to a new {@link BreakpointStatus}.
   *
   * @param addresses The {@link Set} of {@link BreakpointAddress} where to set the
   *        {@link BreakpointStatus}.
   * @param type The {@link BreakpointType} of the breakpoints.
   * @param newStatus The {@link BreakpointStatus} to set.
   */
  public void setBreakpointStatus(final Set<BreakpointAddress> addresses,
      final BreakpointType type, final BreakpointStatus newStatus) {
    if (addresses.size() == 0) {
      return;
    }

    switch (type) {
      case REGULAR:
        setBreakpointsStatus(addresses, newStatus, indexedBreakpointStorage);
        break;
      case ECHO:
        setBreakpointsStatus(addresses, newStatus, echoBreakpointStorage);
        break;
      case STEP:
        setBreakpointsStatus(addresses, newStatus, stepBreakpointStorage);
        break;
      default:
        throw new IllegalStateException("IE00728: Invalid Breakpoint Type");
    }
  }

  public static Color getBreakpointColor(final BreakpointStatus breakpointStatus) {
    switch (breakpointStatus) {
      case BREAKPOINT_ACTIVE:
        return ConfigManager.instance().getDebuggerColorSettings().getBreakpointActive();
      case BREAKPOINT_INACTIVE:
        return ConfigManager.instance().getDebuggerColorSettings().getBreakpointInactive();
      case BREAKPOINT_ENABLED:
        return ConfigManager.instance().getDebuggerColorSettings().getBreakpointEnabled();
      case BREAKPOINT_DISABLED:
        return ConfigManager.instance().getDebuggerColorSettings().getBreakpointDisabled();
      case BREAKPOINT_HIT:
        return ConfigManager.instance().getDebuggerColorSettings().getBreakpointHit();
      case BREAKPOINT_INVALID:
        return ConfigManager.instance().getDebuggerColorSettings().getBreakpointInvalid();
      case BREAKPOINT_DELETING:
        return ConfigManager.instance().getDebuggerColorSettings().getBreakpointDeleting();
      default:
        throw new IllegalStateException("IE01182: Unknown breakpoint status");
    }
  }
}
