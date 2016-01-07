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
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;

/**
 * Represents a single module loaded into the address space of the target process.
 */
public final class MemoryModule {
  /**
   * Name of the module.
   */
  private final String name;

  /**
   * Full file path of the module
   */
  private final String path;

  /**
   * Base address of the module.
   */
  private final RelocatedAddress relocatedAddress;

  /**
   * Size of the module in bytes.
   */
  private final long byteSize;

  /**
   * Creates a new module object.
   *
   * @param name Name of the module.
   * @param path Full path to the module.
   * @param baseAddress Base address of the module.
   * @param size Size of the module in bytes.
   */
  public MemoryModule(final String name, final String path, final RelocatedAddress baseAddress,
      final long size) {
    this.name = Preconditions.checkNotNull(name, "IE00751: Name argument can not be null");
    this.path = Preconditions.checkNotNull(path, "IE00180: Path argument can not be null");
    relocatedAddress =
        Preconditions.checkNotNull(baseAddress, "IE00752: Base address argument can not be null");
    byteSize = size;
  }

  /**
   * Returns the base address of the module.
   *
   * @return The base address of the module.
   */
  public RelocatedAddress getBaseAddress() {
    return relocatedAddress;
  }

  /**
   * Returns the name of the module.
   *
   * @return The name of the module.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the path of the module
   *
   * @return The path of the module
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the size of the module.
   *
   * @return The size of the module.
   */
  public long getSize() {
    return byteSize;
  }

  @Override
  public String toString() {
    return getName() + ", " + getPath() + ": " + getBaseAddress().getAddress().toHexString();
  }
}
