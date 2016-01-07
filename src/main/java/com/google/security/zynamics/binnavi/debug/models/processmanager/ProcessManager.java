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
package com.google.security.zynamics.binnavi.debug.models.processmanager;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.memmanager.Memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class keeps track of the status of the target process.
 */
public final class ProcessManager {
  /**
   * List of listeners that want to be notified about changes in the target process.
   */
  private final ListenerProvider<ProcessManagerListener> listeners = new ListenerProvider<>();

  /**
   * Information about the target machine.
   */
  private TargetInformation targetInformation;

  /**
   * Simulates the process memory.
   */
  private final Memory simulatedProcessMemory = new Memory();

  /**
   * Contains information about the readable memory sections of the target process.
   */
  private MemoryMap memoryMap = new MemoryMap(new ArrayList<MemorySection>());

  /**
   * Indicates whether the debugger is attached to the process or not.
   */
  private boolean isDebuggerAttached = false;

  /**
   * List of modules in the address space of the process.
   */
  private final List<MemoryModule> addressSpaceModules = new ArrayList<>();

  /**
   * Allows for efficient searching of modules by address.
   */
  private final TreeSet<IAddress> moduleAddresses = new TreeSet<>();
  private final Map<IAddress, MemoryModule> moduleByAddress = new HashMap<>();

  /**
   * Set of threads that are active in the target process.
   */
  private final HashSet<TargetProcessThread> activeProcessThreads = new HashSet<>();

  /**
   * The thread that receives debug commands.
   */
  private TargetProcessThread debuggeeActiveThread = null;

  /**
   * Adds an exception event which occurred in the target process.
   *
   * @param exception The object describing the exception.
   */
  public void addExceptionEvent(final DebuggerException exception) {
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.raisedException(exception);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Adds a new listener object to the list of listeners that are notified about changes in the
   * process manager.
   *
   * @param listener The listener to add.
   */
  public void addListener(final ProcessManagerListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Adds a module to the address space of the target process.
   *
   * @param module The module to add.
   */
  public void addModule(final MemoryModule module) {
    Preconditions.checkNotNull(module, "IE00756: Module argument can not be null");
    if (addressSpaceModules.contains(module)) {
      throw new IllegalStateException("IE00757: Module can not be added twice");
    }
    addressSpaceModules.add(module);
    moduleAddresses.add(module.getBaseAddress().getAddress());
    moduleByAddress.put(module.getBaseAddress().getAddress(), module);
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.addedModule(module);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a new thread to the known threads of the target process.
   *
   * @param thread The thread to add to the target process.
   */
  public void addThread(final TargetProcessThread thread) {
    Preconditions.checkNotNull(thread, "IE00766: Thread argument can not be null");
    activeProcessThreads.add(thread);
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.addedThread(thread);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Returns the thread that receives debug commands.
   *
   * @return The thread that receives debug commands.
   */
  public TargetProcessThread getActiveThread() {
    return debuggeeActiveThread;
  }

  /**
   * Returns the simulated memory of the target process.
   *
   * @return The simulated memory of the target process.
   */
  public Memory getMemory() {
    return simulatedProcessMemory;
  }

  /**
   * Returns the memory map of the target process.
   *
   * @return The memory map of the target process.
   */
  public MemoryMap getMemoryMap() {
    return memoryMap;
  }

  /**
   * Returns the module that contains to the given address or null if no such module exists.
   *
   * @param address The address for which to find the corresponding memory module.
   * @return The module that contains the given address.
   */
  public MemoryModule getModule(final RelocatedAddress address) {
    // Note: modules are non-overlapping so the head set either contains zero or one elements.
    final SortedSet<IAddress> moduleAddressesHeadSet =
        moduleAddresses.headSet(address.getAddress(), true);
    if (moduleAddressesHeadSet.isEmpty()) {
      return null;
    } else {
      final MemoryModule module = moduleByAddress.get(moduleAddressesHeadSet.last());
      final BigInteger endAddress = module.getBaseAddress().getAddress().toBigInteger()
          .add(BigInteger.valueOf(module.getSize()));
      return address.getAddress().toBigInteger().compareTo(endAddress) <= 0 ? module : null;
    }
  }

  /**
   * Returns the modules in the address space of the target process.
   *
   * @return The modules in the address space of the target process.
   */
  public List<MemoryModule> getModules() {
    return new ArrayList<>(addressSpaceModules);
  }

  /**
   * Returns information about the target process.
   *
   * @return Information about the target process.
   */
  public TargetInformation getTargetInformation() {
    return targetInformation;
  }

  /**
   * Returns the thread with the given TID.
   *
   * @param tid The thread ID of the thread.
   *
   * @return The thread object with the given thread ID.
   *
   * @throws MaybeNullException Thrown if the thread object could not be found.
   */
  public TargetProcessThread getThread(final long tid) throws MaybeNullException {
    for (final TargetProcessThread thread : activeProcessThreads) {
      if (thread.getThreadId() == tid) {
        return thread;
      }
    }

    throw new MaybeNullException();
  }

  /**
   * Returns a collection of all managed threads.
   *
   * @return A collection of all managed threads.
   */
  public List<TargetProcessThread> getThreads() {
    return new ArrayList<>(activeProcessThreads);
  }

  /**
   * Indicates whether the process manager is synchronized with a real target process.
   *
   * @return True, if the process is synchronized. False, otherwise.
   */
  public boolean isAttached() {
    return isDebuggerAttached;
  }

  /**
   * Removes a listener from the process manager.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final ProcessManagerListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Removes a module from the address space of the target process.
   *
   * @param module The module to remove.
   */
  public void removeModule(final MemoryModule module) {
    Preconditions.checkNotNull(module, "IE00758: Module argument can not be null");
    if (!addressSpaceModules.remove(module)) {
      throw new IllegalStateException("IE00759: Module was not part of this process");
    }
    moduleAddresses.remove(module.getBaseAddress().getAddress());
    moduleByAddress.remove(module.getBaseAddress().getAddress());
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.removedModule(module);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Handles the special case where the debugger reports the removal of an unknown module from the
   * debuggee's address space. This can happen on specific platforms such as Win7 X64 (during
   * process creation). We implement a dedicated method for this case in order to not weaken the
   * assumed preconditions in the standard removeModule method.
   *
   * @param module The module that was unloaded from the debuggee.
   */
  public void removeNonExistingModule(final MemoryModule module) {
    Preconditions.checkNotNull(module, "IE02256: E00084: Module argument can not be null");
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.removedNonExistingModule(module);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a thread from the list of known threads of the target process.
   *
   * @param thread The thread to remove.
   */
  public void removeThread(final TargetProcessThread thread) {
    activeProcessThreads.remove(thread);
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.removedThread(thread);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the thread that receives debug messages.
   *
   * @param thread The thread that receives debug messages.
   */
  public void setActiveThread(final TargetProcessThread thread) {
    if (debuggeeActiveThread == thread) {
      return;
    }
    if ((thread != null) && !activeProcessThreads.contains(thread)) {
      throw new IllegalStateException("IE00369: Unknown thread");
    }
    final TargetProcessThread oldThread = debuggeeActiveThread;
    debuggeeActiveThread = thread;
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.changedActiveThread(oldThread, thread);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the synchronization state of the process manager with the real target process.
   *
   * @param value True means the process is synchronized, false means it is not.
   */
  public void setAttached(final boolean value) {
    isDebuggerAttached = value;
    for (final ProcessManagerListener listener : listeners) {
      try {
        if (value) {
          listener.attached();
        } else {
          listener.detached();
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Sets the memory map of the target process.
   *
   * @param memoryMap The new memory map.
   */
  public void setMemoryMap(final MemoryMap memoryMap) {
    this.memoryMap = Preconditions.checkNotNull(memoryMap, "IE00760: Memory map can't be null");
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.changedMemoryMap();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the target information of the target process.
   *
   * @param targetInformation The new target information.
   */
  public void setTargetInformation(final TargetInformation targetInformation) {
    Preconditions.checkNotNull(targetInformation,
        "IE00761: Target information argument can not be null");
    this.targetInformation = targetInformation;
    for (final ProcessManagerListener listener : listeners) {
      try {
        listener.changedTargetInformation(this.targetInformation);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
