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
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

// / A single thread of a target process.
/**
 * Represents a single thread of the target process.
 */
public final class Thread implements ApiObject<TargetProcessThread> {
  /**
   * The internal thread object which basically acts as a backend for the API thread object.
   */
  private final TargetProcessThread m_thread;

  /**
   * Listener that forwards events from the internal thread object to the API thread object.
   */
  private final InternalThreadListener m_listener = new InternalThreadListener();

  /**
   * Listeners that are notified about changes in the API thread object.
   */
  private final ListenerProvider<IThreadListener> m_listeners =
      new ListenerProvider<IThreadListener>();

  // / @cond INTERNAL
  /**
   * Creates a new API thread object backed by an internal thread object.
   *
   * @param thread The internal thread object.
   */
  // / @endcond
  public Thread(final TargetProcessThread thread) {
    Preconditions.checkNotNull(thread, "Error: Thread argument can not be null");

    m_thread = thread;

    m_thread.addListener(m_listener);
  }

  @Override
  public TargetProcessThread getNative() {
    return m_thread;
  }

  // ! Adds a thread listener.
  /**
   * Adds an object that is notified about changes in the thread.
   *
   * @param listener The listener object that is notified about changes in the thread.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the thread.
   */
  public void addListener(final IThreadListener listener) {
    m_listeners.addListener(listener);
  }

  // / @cond INTERNAL
  /**
   * Disposes the thread object.
   */
  // / @endcond
  public void dispose() {
    m_thread.removeListener(m_listener);
  }

  // ! Current program counter value of the thread.
  /**
   * Returns the current program counter address of the thread. This value is only useful if the
   * thread is suspended.
   *
   * @return The current program counter value of the thread.
   */
  public Address getCurrentAddress() {
    return m_thread.getCurrentAddress() == null ? null : new Address(m_thread.getCurrentAddress()
        .getAddress().toBigInteger());
  }

  // ! Current register values of the thread.
  /**
   * Returns the current register values of the thread. These values are only useful if the thread
   * is suspended.
   *
   * @return A list of register values.
   */
  public List<Register> getRegisters() {
    final List<Register> registers = new ArrayList<Register>();

    for (final RegisterValue register : m_thread.getRegisterValues()) {
      registers.add(new Register(register));
    }

    return registers;
  }

  // ! Current state of the thread.
  /**
   * Returns the current state of the thread.
   *
   * @return The current state of the thread.
   */
  public ThreadState getState() {
    return ThreadState.convert(m_thread.getState());
  }

  // ! Thread ID of the thread.
  /**
   * Returns the thread ID of the thread.
   *
   * @return The thread ID of the thread.
   */
  public long getThreadId() {
    return m_thread.getThreadId();
  }

  // ! Removes a thread listener.
  /**
   * Removes a listener object from the thread.
   *
   * @param listener The listener object to remove from the thread.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the thread.
   */
  public void removeListener(final IThreadListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Printable representation of the thread.
  /**
   * Returns a string representation of the thread.
   *
   * @return A string representation of the thread.
   */
  @Override
  public String toString() {
    return String.format("Thread (TID: %d)", getThreadId());
  }

  /**
   * Listener that forwards events from the internal thread object to the API thread object.
   */
  private class InternalThreadListener
      implements com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ThreadListener {
    @Override
    public void instructionPointerChanged(
        final TargetProcessThread thread, final RelocatedAddress oldAddress) {
      for (final IThreadListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.changedProgramCounter(Thread.this);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void registersChanged(final TargetProcessThread thread) {
      for (final IThreadListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.changedRegisters(Thread.this);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void stateChanged(final TargetProcessThread thread) {
      for (final IThreadListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.changedState(Thread.this);
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }
  }
}
