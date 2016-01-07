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
package com.google.security.zynamics.binnavi.debug.connection.packets.replies;

import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;

/**
 * Represents the reply sent by the debug client when a module was unloaded from the process space
 * of the debugged target process.
 */
public final class ModuleUnloadedReply extends DebuggerReply {
  /**
   * Information about the unloaded memory module.
   */
  private final MemoryModule module;

  /**
   * Creates a new Module Unloaded reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param module Information about the loaded memory module. This argument must be null in case of
   *        errors.
   */
  public ModuleUnloadedReply(final int packetId, final int errorCode, final MemoryModule module) {
    super(packetId, errorCode);
    this.module = module;
  }

  /**
   * Returns the module information of the unloaded module. This method returns null in case of
   * errors.
   *
   * @return Module information of the unloaded module or null.
   */
  public MemoryModule getModule() {
    return module;
  }
}
