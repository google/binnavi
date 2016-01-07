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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.util.ArrayList;
import java.util.List;



// / Target process of the debugger.
/**
 * Represents a target process that is being debugged.
 */
public final class Process {
  /**
   * The wrapped internal process manager object.
   */
  private final ProcessManager processManager;

  /**
   * Simulated threads of the process.
   */
  private final List<Thread> threads = new ArrayList<>();

  /**
   * Simulated modules loaded into the address space of the process.
   */
  private final List<MemoryModule> modules = new ArrayList<>();

  /**
   * Simulated memory map of the project.
   */
  private MemoryMap memoryMap;

  /**
   * Simulated memory of the process.
   */
  private Memory memory;

  /**
   * Information about the target process.
   */
  private TargetInformation targetInformation;

  /**
   * Keeps the API process object synchronized with the internal process object.
   */
  private final InternalProcessListener listener = new InternalProcessListener();

  /**
   * Listeners that are notified about changes in the process.
   */
  private final ListenerProvider<IProcessListener> listeners = new ListenerProvider<>();

  // / @cond INTERNAL
  /**
   * Creates a new API process object.
   *
   * @param processManager The wrapped internal process object.
   */
  // / @endcond
  public Process(
      final com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager processManager) {
    this.processManager = processManager;

    processManager.addListener(listener);

    memory = new Memory(processManager.getMemory());
    targetInformation = processManager.getTargetInformation() == null ? null
        : new TargetInformation(processManager.getTargetInformation());
    memoryMap = new MemoryMap(processManager.getMemoryMap());

    for (final TargetProcessThread thread : processManager.getThreads()) {
      threads.add(new Thread(thread));
    }

    for (final com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule
        module : processManager.getModules()) {
      modules.add(new MemoryModule(module));
    }
  }

  // ! Adds a process listener.
  /**
   * Adds an object that is notified about changes in the process.
   *
   * @param listener The listener object that is notified about changes in the process.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         process.
   */
  public void addListener(final IProcessListener listener) {
    listeners.addListener(listener);
  }

  // ! Returns the process memory.
  /**
   * Returns the simulated memory of target process. This memory object contains all the memory data
   * that was already sent from the debug client to com.google.security.zynamics.binnavi.
   *
   * @return The simulated memory of the target process.
   */
  public Memory getMemory() {
    return memory;
  }

  // ! Returns the process memory layout.
  /**
   * Returns the memory layout of the target process. The memory map object contains a list of all
   * allocated parts of the target process memory.
   *
   * @return The memory layout of the target process.
   */
  public MemoryMap getMemoryMap() {
    return memoryMap;
  }

  // ! Returns the loaded modules.
  /**
   * Returns the modules loaded into the address space of the process.
   *
   * @return The modules loaded into the address space of the process.
   */
  public List<MemoryModule> getModules() {
    return new ArrayList<MemoryModule>(modules);
  }

  // ! Returns information about the target process.
  /**
   * Returns the target information object of the target process.
   *
   * @return The target information object of the target process.
   */
  public TargetInformation getTargetInformation() {
    return targetInformation;
  }

  // ! Returns active threads in the target process.
  /**
   * Returns all threads that are active in the target process.
   *
   * @return List of threads of the target process.
   */
  public List<Thread> getThreads() {
    return new ArrayList<Thread>(threads);
  }

  // ! Removes a process listener.
  /**
   * Removes a listener object from the process.
   *
   * @param listener The listener object to remove from the process.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the process.
   */
  public void removeListener(final IProcessListener listener) {
    listeners.removeListener(listener);
  }

  // ! Printable representation of the process.
  /**
   * Returns a string representation of the target process.
   *
   * @return A string representation of the target process.
   */
  @Override
  public String toString() {
    return String.format("Target Process");
  }

  /**
   * Keeps the API process object synchronized with the internal process object.
   */
  private class InternalProcessListener implements ProcessManagerListener {
    @Override
    public void addedModule(
        final com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule module) {
      final MemoryModule newModule = new MemoryModule(module);

      modules.add(newModule);

      for (final IProcessListener listener : listeners) {
        try {
          listener.addedModule(Process.this, newModule);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void addedThread(final TargetProcessThread thread) {
      final Thread newThread = new Thread(thread);

      threads.add(newThread);

      for (final IProcessListener listener : listeners) {
        try {
          listener.addedThread(Process.this, newThread);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void attached() {
      for (final IProcessListener listener : listeners) {
        try {
          listener.attached(Process.this);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void changedActiveThread(final TargetProcessThread oldThread,
        final TargetProcessThread newThread) {
      // This should not be passed to the API
    }

    @Override
    public void changedMemoryMap() {
      memoryMap = new MemoryMap(processManager.getMemoryMap());

      for (final IProcessListener listener : listeners) {
        try {
          listener.changedMemoryMap(Process.this, memoryMap);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void changedTargetInformation(
        final com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation information) {
      for (final Thread thread : threads) {
        thread.dispose();
      }

      targetInformation = new TargetInformation(information);

      modules.clear();
      threads.clear();
      memoryMap = new MemoryMap(new com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap(new ArrayList<com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection>()));
      memory = new Memory(new com.google.security.zynamics.zylib.general.memmanager.Memory());

      for (final IProcessListener listener : listeners) {
        try {
          listener.changedTargetInformation(Process.this);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void detached() {
      targetInformation = null;
      modules.clear();
      threads.clear();
      memoryMap = new MemoryMap(new com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap(new FilledList<com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection>()));
      memory = new Memory(new com.google.security.zynamics.zylib.general.memmanager.Memory());

      for (final IProcessListener listener : listeners) {
        // ESCA-JAVA0166:
        try {
          listener.detached(Process.this);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void raisedException(final DebuggerException exception) {
      // TODO (timkornau): inform listeners about exception
    }

    @Override
    public void removedModule(
        final com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule module) {
      final MemoryModule apiModule = ObjectFinders.getObject(module, modules);

      modules.remove(apiModule);

      for (final IProcessListener listener : listeners) {
        try {
          listener.removedModule(Process.this, apiModule);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void removedNonExistingModule(
        final com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule module) {
      // We do not export this event to API listeners since it is mainly important for internal
      // protocol actions.
    }

    @Override
    public void removedThread(final TargetProcessThread thread) {
      final Thread apiThread = ObjectFinders.getObject(thread, threads);

      threads.remove(apiThread);

      for (final IProcessListener listener : listeners) {
        try {
          listener.removedThread(Process.this, apiThread);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }
  }
}
