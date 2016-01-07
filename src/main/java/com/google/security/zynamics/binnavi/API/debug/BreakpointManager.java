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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// / Used to set and remove breakpoints.
/**
 * Keeps track of all breakpoints set by a debugger.
 */
public final class BreakpointManager implements
    ApiObject<com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager> {
  /**
   * The wrapped internal breakpoint manager object.
   */
  private final com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager
      breakpointManager;

  /**
   * Listeners that are notified about changes in the breakpoint manager.
   */
  private final ListenerProvider<IBreakpointManagerListener> listeners = new ListenerProvider<>();

  /**
   * Keeps the API breakpoint manager object synchronized with the internal breakpoint manager
   * object.
   */
  private final InternalBreakpointManagerListener internalListener =
      new InternalBreakpointManagerListener();

  /**
   * Maps between native breakpoint objects and their API counterparts.
   */
  private final
      Map<com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint, Breakpoint>
      breakpointMap = new HashMap<>();

  /**
   * Maps between native echo breakpoi[nt objects and their API counterparts.
   */
  private final
      Map<com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint, Breakpoint>
      echoBreakpointMap = new HashMap<>();

  // / @cond INTERNAL
  /**
   * Creates a new API breakpoint manager object.
   *
   * @param breakpointManager The wrapped internal breakpoint manager object.
   */
  // / @endcond
  public BreakpointManager(
      final com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager breakpointManager) {
    this.breakpointManager = breakpointManager;

    breakpointManager.addListener(internalListener);

    for (final com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint breakpoint :
        breakpointManager.getBreakpoints(BreakpointType.REGULAR)) {
      breakpointMap.put(breakpoint, new Breakpoint(breakpoint));
    }

    for (final com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint breakpoint :
        breakpointManager.getBreakpoints(BreakpointType.ECHO)) {
      echoBreakpointMap.put(breakpoint, new Breakpoint(breakpoint));
    }
  }

  @Override
  public com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager getNative() {
    return breakpointManager;
  }

  // ! Adds a breakpoint manager listener.
  /**
   * Adds an object that is notified about changes in the breakpoint manager.
   *
   * @param listener The listener object that is notified about changes in the breakpoint manager.
   *
   */
  public void addListener(final IBreakpointManagerListener listener) {
    listeners.addListener(listener);
  }

  // ! Returns a regular breakpoint from a given address.
  /**
   * Returns the regular breakpoint at the given address.
   *
   * @param module The module the breakpoint belongs to. This argument can be null.
   * @param address The address of the breakpoint to return.
   *
   * @return The breakpoint at the given address.
   */
  public Breakpoint getBreakpoint(final Module module, final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    final com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint
        internalBreakpoint = breakpointManager.getBreakpoint(
        BreakpointType.REGULAR, new BreakpointAddress(module == null ? null : module.getNative(),
            new UnrelocatedAddress(new CAddress(address.toLong()))));

    return breakpointMap.get(internalBreakpoint);
  }

  // ! Returns a list of all managed breakpoints.
  /**
   * Returns a list of all managed breakpoints.
   *
   * @return A list of all managed breakpoints.
   */
  public List<Breakpoint> getBreakpoints() {
    return new ArrayList<Breakpoint>(breakpointMap.values());
  }

  // ! Returns an echo breakpoint from a given address.
  /**
   * Returns the echo breakpoint at the given address.
   *
   * @param module The module the echo breakpoint belongs to.
   * @param address The address of the breakpoint to return.
   *
   * @return The breakpoint at the given address.
   */
  public Breakpoint getEchoBreakpoint(final Module module, final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    final com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint
        internalBreakpoint = breakpointManager.getBreakpoint(
        BreakpointType.ECHO, new BreakpointAddress(module == null ? null : module.getNative(),
            new UnrelocatedAddress(new CAddress(address.toLong()))));

    return echoBreakpointMap.get(internalBreakpoint);
  }

  // ! Returns a list of all managed echo breakpoints.
  /**
   * Returns a list of all managed echo breakpoints.
   *
   * @return A list of all managed echo breakpoints.
   */
  public List<Breakpoint> getEchoBreakpoints() {
    return new ArrayList<Breakpoint>(echoBreakpointMap.values());
  }

  // ! Checks for the existence of a breakpoint.
  /**
   * Checks whether a regular breakpoint exists at a given address.
   *
   * @param module The module the breakpoint is tied to. This argument can be null.
   * @param address The address to check.
   *
   * @return True, if a regular breakpoint exists at the given address. False, otherwise.
   */
  public boolean hasBreakpoint(final Module module, final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    return breakpointManager.hasBreakpoint(BreakpointType.REGULAR, new BreakpointAddress(
        module == null ? null : module.getNative(),
        new UnrelocatedAddress(new CAddress(address.toLong()))));
  }

  // ! Checks for the existence of an echo breakpoint.
  /**
   * Checks whether an echo breakpoint exists at a given address.
   *
   * @param module The module the breakpoint is tied to. This argument can be null.
   * @param address The address to check.
   *
   * @return True, if an echo breakpoint exists at the given address. False, otherwise.
   */
  public boolean hasEchoBreakpoint(final Module module, final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    return breakpointManager.hasBreakpoint(BreakpointType.ECHO, new BreakpointAddress(
        module == null ? null : module.getNative(),
        new UnrelocatedAddress(new CAddress(address.toLong()))));
  }

  // ! Removes a regular breakpoint.
  /**
   * Removes a regular breakpoint from a given address.
   *
   * @param module The module the breakpoint is tied to. This argument can be null.
   * @param address The address of the breakpoint.
   */
  public void removeBreakpoint(final Module module, final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    final BreakpointStatus currentStatus =
        com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus.BREAKPOINT_DELETING;
    final BreakpointAddress breakpointAddress = new BreakpointAddress(
        module == null ? null : module.getNative(),
        new UnrelocatedAddress(new CAddress(address.toLong())));

    breakpointManager.setBreakpointStatus(Sets.newHashSet(breakpointAddress),
        BreakpointType.REGULAR, currentStatus);
  }

  // ! Removes an echo breakpoint.
  /**
   * Removes an echo breakpoint from a given address.
   *
   * @param module The module the breakpoint is tied to. This argument can be null.
   * @param address The address of the breakpoint.
   */
  public void removeEchoBreakpoint(final Module module, final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    breakpointManager.removeBreakpoints(BreakpointType.ECHO, Sets.newHashSet(new BreakpointAddress(
        module == null ? null : module.getNative(),
        new UnrelocatedAddress(new CAddress(address.toLong())))));
  }

  // ! Removes a breakpoint manager listener.
  /**
   * Removes a listener object from the breakpoint manager.
   *
   * @param listener The listener object to remove from the breakpoint manager.
   */
  public void removeListener(final IBreakpointManagerListener listener) {
    listeners.removeListener(listener);
  }

  // ! Sets a regular breakpoint.
  /**
   * Sets a regular breakpoint at the given address.
   *
   * @param module The module the breakpoint is tied to. This argument can be null.
   * @param address The address of the breakpoint.
   *
   * @return The set breakpoint. Null is returned if no breakpoint was set.
   */
  public Breakpoint setBreakpoint(final Module module, final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");
    final INaviModule realModule = module == null ? null : module.getNative();
    final BreakpointAddress breakpointAddress =
        new BreakpointAddress(realModule, new UnrelocatedAddress(new CAddress(address.toLong())));
    final Set<BreakpointAddress> breakpoints = Sets.newHashSet(breakpointAddress);
    breakpointManager.addBreakpoints(BreakpointType.REGULAR, breakpoints);
    return echoBreakpointMap.get(
        breakpointManager.getBreakpoint(BreakpointType.REGULAR, breakpointAddress));
  }

  // ! Sets an echo breakpoint.
  /**
   * Sets an echo breakpoint at the given address.
   *
   * @param module The module the breakpoint is tied to. This argument can be null.
   * @param address The address of the breakpoint.
   *
   * @return The set breakpoint. Null is returned if no breakpoint was set.
   */
  public Breakpoint setEchoBreakpoint(final Module module, final Address address) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");
    final INaviModule realModule = module == null ? null : module.getNative();
    final BreakpointAddress breakpointAddress =
        new BreakpointAddress(realModule, new UnrelocatedAddress(new CAddress(address.toLong())));
    final Set<BreakpointAddress> breakpoints = Sets.newHashSet(breakpointAddress);
    breakpointManager.addBreakpoints(BreakpointType.ECHO, breakpoints);
    return echoBreakpointMap.get(
        breakpointManager.getBreakpoint(BreakpointType.ECHO, breakpointAddress));
  }

  // ! Printable representation of the breakpoint manager.
  /**
   * Returns a string representation of the breakpoint manager.
   *
   * @return A string representation of the breakpoint manager.
   */
  @Override
  public String toString() {
    return String.format("BreakpointManager (Managing %d Breakpoints)",
        breakpointManager.getNumberOfBreakpoints(BreakpointType.REGULAR));
  }

  /**
   * Keeps the API breakpoint manager object synchronized with the internal breakpoint manager
   * object.
   */
  private class InternalBreakpointManagerListener implements
      com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointManagerListener {
    @Override
    public void breakpointsAdded(final List<
        com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint> breakpoints) {
      for (final com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint
          breakpoint : breakpoints) {
        final Breakpoint newBreakpoint = new Breakpoint(breakpoint);

        if (breakpoint.getType() == BreakpointType.REGULAR) {
          breakpointMap.put(breakpoint, newBreakpoint);
          for (final IBreakpointManagerListener listener : listeners) {
            try {
              listener.addedBreakpoint(BreakpointManager.this, newBreakpoint);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        } else if (breakpoint.getType() == BreakpointType.ECHO) {
          echoBreakpointMap.put(breakpoint, newBreakpoint);
          for (final IBreakpointManagerListener listener : listeners) {
            try {
              listener.addedEchoBreakpoint(BreakpointManager.this, newBreakpoint);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void breakpointsConditionChanged(final Set<
        com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint> breakpoints) {}

    @Override
    public void breakpointsDescriptionChanged(final Set<
        com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint> breakpoints) {}

    @Override
    public void breakpointsRemoved(final Set<
        com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint> breakpoints) {
      Preconditions.checkNotNull(breakpoints, "Error: breakpoints argument can not be null");
      if (breakpoints.size() == 0) {
        return;
      }
      for (final com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint
          breakpoint : breakpoints) {
        if (breakpoint.getType() == BreakpointType.REGULAR) {
          final Breakpoint removedBreakpoint = breakpointMap.get(breakpoint);
          breakpointMap.remove(breakpoint);
          for (final IBreakpointManagerListener listener : listeners) {
            try {
              listener.removedBreakpoint(BreakpointManager.this, removedBreakpoint);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        } else if (breakpoint.getType() == BreakpointType.ECHO) {
          final Breakpoint removedBreakpoint = echoBreakpointMap.get(breakpoint);
          echoBreakpointMap.remove(breakpoint);
          for (final IBreakpointManagerListener listener : listeners) {
            try {
              listener.removedEchoBreakpoint(BreakpointManager.this, removedBreakpoint);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void breakpointsStatusChanged(final Map<
        com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint,
        BreakpointStatus> breakpointsToOldStatus, final BreakpointStatus newStatus) {}
  }
}
