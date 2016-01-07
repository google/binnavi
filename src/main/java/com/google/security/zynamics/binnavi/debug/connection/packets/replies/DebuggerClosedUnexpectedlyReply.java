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
 * Reply type that is dispatched whenever the connection to the debugger closed unexpectedly. This
 * reply is not a real reply sent by the debug client but a "fake" reply dispatched by BinNavi
 * whenever necessary.
 */
public final class DebuggerClosedUnexpectedlyReply extends DebuggerReply {
  /**
   * Creates a new debugger closed unexpectedly reply.
   */
  public DebuggerClosedUnexpectedlyReply() {
    super(0, 0);
  }
}
