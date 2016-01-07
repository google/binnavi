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
package com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers;

import com.google.security.zynamics.binnavi.debug.connection.helpers.DebugProtocolHelper;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerReply;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.io.IOException;

/**
 * Base class for all reply parsers.
 *
 * @param <T> Type of the parsed reply.
 */
public abstract class AbstractReplyParser<T extends DebuggerReply> {
  /**
   * Error ID that indicates a parser error.
   */
  public static final int PARSER_ERROR = 9999;

  /**
   * Provides the data to be parsed.
   */
  private final ClientReader workerInputStream;

  /**
   * Message type of the parsed success messages.
   */
  private final int successType;

  /**
   * Creates a new parser object.
   *
   * @param workerInputStream Provides the data to be parsed.
   * @param type Message type of the parsed success messages.
   */
  protected AbstractReplyParser(final ClientReader workerInputStream, final int type) {
    this.workerInputStream = workerInputStream;
    this.successType = type;
  }

  /**
   * Parses a single integer value from the byte stream of the message.
   *
   * @return The parsed integer value.
   *
   * @throws IOException Thrown if parsing the integer value failed.
   */
  private int parseSimpleInteger() throws IOException {
    return (int) DebugProtocolHelper.readDWord(workerInputStream);
  }

  /**
   * Parses an address argument value from the byte stream of the message.
   *
   * @return The parsed address value.
   *
   * @throws IOException Thrown if parsing the address argument failed.
   */
  protected IAddress parseAddress() throws IOException {
    return new CAddress(DebugProtocolHelper.readAddress(workerInputStream));
  }

  /**
   * Parses a data argument value from the byte stream of the message.
   *
   * @return The parsed data value.
   *
   * @throws IOException Thrown if parsing the data argument failed.
   */
  protected byte[] parseData() throws IOException {
    return DebugProtocolHelper.readData(workerInputStream);
  }

  /**
   * Parses a message from the debug client that indicates an error happened.
   *
   * @param packetId ID of the packet to parse.
   *
   * @return The parsed packet.
   *
   * @throws IOException Thrown if parsing the message failed.
   */
  protected abstract T parseError(int packetId) throws IOException;

  /**
   * Parses an integer argument value from the byte stream of the message.
   *
   * @return The parsed integer value.
   *
   * @throws IOException Thrown if parsing the integer argument failed.
   */
  protected int parseInteger() throws IOException {
    return (int) DebugProtocolHelper.readThreadId(workerInputStream);
  }

  /**
   * Parses a message from the debug client that indicates a successful operation.
   *
   * @param packetId ID of the packet to parse.
   * @param argumentCount Number of arguments the message has.
   *
   * @return The parsed packet.
   *
   * @throws IOException Thrown if parsing the message failed.
   */
  protected abstract T parseSuccess(int packetId, int argumentCount) throws IOException;

  /**
   * Parses a thread ID argument value from the byte stream of the message.
   *
   * @return The parsed thread ID value.
   *
   * @throws IOException Thrown if parsing the thread ID argument failed.
   */
  protected long parseThreadId() throws IOException {
    return DebugProtocolHelper.readThreadId(workerInputStream);
  }

  /**
   * Parses a message received from the debug client.
   *
   * @param type Identifies the type of the message.
   * @param messageId ID of the message.
   *
   * @return The parsed message.
   *
   * @throws IOException Thrown if parsing the message failed.
   */
  public T parse(final int type, final int messageId) throws IOException {
    final int argumentCount = parseSimpleInteger();
    if (type == successType) {
      return parseSuccess(messageId, argumentCount);
    } else {
      return parseError(messageId);
    }
  }
}
