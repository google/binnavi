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

// / Simulates the memory of a target process.
/**
 * Simulates the memory of the target process. The simulated memory of the target process contains
 * all the data that was already sent from the debug client to com.google.security.zynamics.binnavi.
 */
public final class Memory {
  /**
   * Wrapped internal memory object.
   */
  private final com.google.security.zynamics.zylib.general.memmanager.Memory m_memory;

  /**
   * Listeners that are notified about changes in the memory.
   */
  private final List<IMemoryListener> m_listeners = new ArrayList<IMemoryListener>();

  /**
   * Keeps the API memory object synchronized with the internal memory object.
   */
  private final InternalMemoryListener m_listener = new InternalMemoryListener();

  // / @cond INTERNAL
  /**
   * Creates a new memory object
   *
   * @param memory The wrapped internal memory object.
   */
  // / @endcond
  public Memory(final com.google.security.zynamics.zylib.general.memmanager.Memory memory) {
    m_memory = memory;

    memory.addMemoryListener(m_listener);
  }

  // ! Adds memory listeners.
  /**
   * Adds an object that is notified about changes in the simulated memory.
   *
   * @param listener The listener object that is notified about changes in the simulated memory.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         simulated memory.
   */
  public void addListener(final IMemoryListener listener) {
    m_listeners.add(listener);
  }

  // ! Reads data from memory.
  /**
   * Returns the data of a given memory section of the simulated memory.
   *
   * @param start The start of the memory section.
   * @param length Number of bytes in the memory section.
   *
   * @return Byte-array that contains the bytes of the target process memory.
   *
   * @throws MissingDataException Thrown if the requested memory data is not available.
   */
  public byte[] getData(final long start, final int length) throws MissingDataException {
    Preconditions.checkArgument(start >= 0, "Error: Address can't be less than 0");
    Preconditions.checkArgument(length > 0, "Error: Length must be positive");

    try {
      return m_memory.getData(start, length);
    } catch (final IllegalArgumentException exception) {
      throw new MissingDataException(String.format(
          "Memory data between %d and %d is not available", start, start + length - 1));
    }
  }

  // ! Checks for the existence of data in the memory.
  /**
   * Determines whether a given memory section is available in the simulated memory.
   *
   * @param start The start of the memory section.
   * @param length Number of bytes in the memory section.
   *
   * @return True, if the memory section is available in the simulated memory. False, if it is not.
   */
  public boolean hasData(final long start, final int length) {
    return m_memory.hasData(start, length);
  }

  // ! Removes memory listeners.
  /**
   * Removes a listener object from the database.
   *
   * @param listener The listener object to remove from the database.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the database.
   */
  public void removeListener(final IMemoryListener listener) {
    m_listeners.remove(listener);
  }

  // ! Printable representation of the memory.
  /**
   * Returns a string representation of the memory object.
   *
   * @return A string representation of the memory object.
   */
  @Override
  public String toString() {
    return String.format("Simulated Memory (Size: %d Bytes)", m_memory.getMemorySize());
  }

  /**
   * Keeps the API memory object synchronized with the internal memory object.
   */
  private class InternalMemoryListener
      implements com.google.security.zynamics.zylib.general.memmanager.IMemoryListener {
    @Override
    public void memoryChanged(final long address, final int size) {
      for (final IMemoryListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.changedMemory(Memory.this, address, size);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void memoryCleared() {
      for (final IMemoryListener listener : m_listeners) {
        // ESCA-JAVA0166:
        try {
          listener.clearedMemory(Memory.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
