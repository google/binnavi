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
package com.google.security.zynamics.binnavi.debug.connection.packets.parsers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Parser that can be used to parse individual information messages about memory modules.
 *
 * Example Message: <module name="foo" address="12345" size="789" />
 */
public final class MemoryModuleParser {
  /**
   * You are not supposed to instantiate this class.
   */
  private MemoryModuleParser() {}

  /**
   * Returns the value of a node attribute.
   *
   * @param node The node whose attribute string is returned.
   * @param attribute The name of the attribute whose value is returned.
   *
   * @return The value of the given node attribute.
   *
   * @throws MessageParserException If the node attribute does not exist.
   */
  private static String getAttribute(final Node node, final String attribute)
      throws MessageParserException {
    final Node attributeNode = node.getAttributes().getNamedItem(attribute);

    if (attributeNode == null) {
      throw new MessageParserException(
          String.format("IE01038: Module message does not have a '%s' attribute", attribute));
    }

    return attributeNode.getNodeValue();
  }

  /**
   * Parses a single module information message from the raw byte stream.
   *
   * @param data The byte stream to be parsed.
   * @return The corresponding memory module object.
   * @throws IllegalArgumentException Thrown if the data argument is null.
   * @throws MessageParserException Thrown if parsing the message failed.
   */
  public static MemoryModule parseModule(final byte[] data) throws MessageParserException {
    Preconditions.checkNotNull(data, "IE01037: Data argument can not be null");

    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      final DocumentBuilder builder = factory.newDocumentBuilder();
      final Document document = builder.parse(new ByteArrayInputStream(data, 0, data.length));

      return parseModule(document.getFirstChild());
    } catch (final Exception exception) {
      throw new MessageParserException(exception.getLocalizedMessage());
    }
  }

  /**
   * Parses a single module information message.
   *
   * @param node The xml node which contains the module item.
   * @return The memory module object created from the information in the message.
   * @throws MessageParserException Thrown if parsing the message failed.
   */
  public static MemoryModule parseModule(final Node node) throws MessageParserException {
    try {
      final String name = getAttribute(node, "name");
      final String path = getAttribute(node, "path");
      final RelocatedAddress baseAddress =
          new RelocatedAddress(new CAddress(new BigInteger(getAttribute(node, "address"))));
      final long size = Long.valueOf(getAttribute(node, "size"));

      return new MemoryModule(name, path, baseAddress, size);
    } catch (final Exception exception) {
      throw new MessageParserException(exception.getLocalizedMessage());
    }
  }

  /**
   * Parses the thread id from the data contents of the CModuleReply message.
   *
   * @param data The raw data of the CModuleReply message.
   *
   * @return The parsed thread id.
   *
   * @throws MessageParserException Thrown if parsing of the message failed.
   */
  public static TargetProcessThread parseThreadId(final byte[] data) throws MessageParserException {
    Preconditions.checkNotNull(data, "IE00057: Data argument can not be null");

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      final DocumentBuilder builder = factory.newDocumentBuilder();
      final Document document = builder.parse(new ByteArrayInputStream(data, 0, data.length));

      final Node node = document.getFirstChild();

      final long threadId = Long.valueOf(getAttribute(node, "threadid"));

      return new TargetProcessThread(threadId, ThreadState.SUSPENDED);
    } catch (final Exception exception) {
      throw new MessageParserException(exception.getLocalizedMessage());
    }
  }
}
