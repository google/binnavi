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

/**
 * Represents the reply to a Set Debug Event Settings command.
 */
public class SetDebuggerEventSettingsReply extends DebuggerReply {
  /**
   * Create a new instance of the Set Debug Event Settings message.
   *
   * @param packetId The id of the packet which was received from the debug client.
   * @param errorCode The error code specifying if the command was successful.
   */
  public SetDebuggerEventSettingsReply(final int packetId, final int errorCode) {
    super(packetId, errorCode);
  }
}
