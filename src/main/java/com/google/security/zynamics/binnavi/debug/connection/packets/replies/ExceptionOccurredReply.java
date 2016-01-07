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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;

/**
 * Represents the replies that are sent by the debug client whenever an exception occurs in the
 * target process.
 */
public final class ExceptionOccurredReply extends DebuggerReply {
  /**
   * Thread ID which caused the exception.
   */
  private final long threadId;

  /**
   * Exception ID.
   */
  private final long exceptionCode;

  /**
   * Address where the exception happened.
   */
  private final RelocatedAddress address;

  /**
   * The string representation of the exception.
   */
  private final String exceptionName;

  /**
   * Creates a new exception occurred reply.
   *
   * @param packetId Packet ID of the reply.
   * @param errorCode Error code of the reply. If this error code is 0, the requested operation was
   *        successful.
   * @param tid Thread ID which caused the exception.
   * @param code Exception code.
   * @param address Address where the exception happened.
   */
  public ExceptionOccurredReply(final int packetId,
      final int errorCode,
      final long tid,
      final long code,
      final RelocatedAddress address,
      final String exceptionName) {
    super(packetId, errorCode);
    Preconditions.checkNotNull(address, "IE01059: Address argument can not be null");
    threadId = tid;
    exceptionCode = code;
    this.address = address;
    this.exceptionName = exceptionName;
  }

  /**
   * Returns the address where the exception happened.
   *
   * @return The address where the exception happened.
   */
  public RelocatedAddress getAddress() {
    // TODO: Exceptions should transfer the whole register set
    return address;
  }

  /**
   * Returns the exception code of the exception.
   *
   * @return The exception code of the exception.
   */
  public long getExceptionCode() {
    return exceptionCode;
  }

  public String getExceptionName() {
    return exceptionName;
  }

  /**
   * Returns the thread ID of the thread that caused the exception.
   *
   * @return The thread ID of the thread that caused the exception.
   */
  public long getThreadId() {
    return threadId;
  }
}
