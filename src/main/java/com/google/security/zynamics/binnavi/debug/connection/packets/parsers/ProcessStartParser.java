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
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessStart;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Can be used to parse a CThread object from an XML stream.
 */
public final class ProcessStartParser {
  /**
   * Your are not supposed to instantiate this class.
   */
  private ProcessStartParser() {}

  /**
   * Converts the numerical value of a thread state into the proper enumeration value.
   *
   * @param value The numerical value of the thread state.
   *
   * @return The enumeration value of the thread state.
   */
  private static ThreadState convertThreadState(final int value) {
    switch (value) {
      case 0:
        return ThreadState.RUNNING;
      case 1:
        return ThreadState.SUSPENDED;
      default:
        throw new IllegalArgumentException(
            String.format("Received invalid thread state %d", value));
    }
  }

  /**
   * Returns the value of a node attribute.
   *
   * @param node The node whose attribute string is returned.
   * @param attribute The name of the attribute whose value is returned.
   * @return The value of the given node attribute.
   * @throws MessageParserException If the node attribute does not exist.
   */
  private static String getAttribute(final Node node, final String attribute)
      throws MessageParserException {
    final Node attributeNode = node.getAttributes().getNamedItem(attribute);

    if (attributeNode == null) {
      throw new MessageParserException(String.format("%s message does not have a '%s' attribute",
          node.getNodeName(), attribute));
    }

    return attributeNode.getNodeValue();
  }

  /**
   * Parses the thread xml node.
   *
   * @param node The node containing the thread item.
   * @return The parsed thread object.
   * @throws MessageParserException Thrown if an error occurred while parsing.
   */
  private static TargetProcessThread parseThreadInformation(final Node node) throws MessageParserException {
    final long id = Long.valueOf(getAttribute(node, "threadId"));
    final ThreadState state =
        convertThreadState(Integer.valueOf(getAttribute(node, "threadState")));

    return new TargetProcessThread(id, state);
  }

  /**
   * Parses the byte stream containing the process start packet.
   *
   * @param data The raw data to be parsed.
   * @return The parsed process start object.
   * @throws MessageParserException Thrown if an error occurred while parsing.
   */
  public static ProcessStart parse(final byte[] data) throws MessageParserException {
    Preconditions.checkNotNull(data, "IE00066: Data argument can not be null");

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    TargetProcessThread thread = null;
    MemoryModule module = null;

    try {
      final DocumentBuilder builder = factory.newDocumentBuilder();
      final Document document = builder.parse(new ByteArrayInputStream(data, 0, data.length));

      final NodeList nodes = document.getFirstChild().getChildNodes();

      for (int i = 0; i < nodes.getLength(); ++i) {
        final Node node = nodes.item(i);

        final String nodeName = node.getNodeName();

        if ("thread".equals(nodeName)) {
          thread = parseThreadInformation(node);
        } else if ("module".equals(nodeName)) {
          module = MemoryModuleParser.parseModule(node);
        } else {
          throw new MessageParserException(
              String.format("Found unknown node '%s' in process start string", nodeName));
        }
      }
    } catch (final Exception exception) {
      CUtilityFunctions.logException(exception);

      throw new MessageParserException(exception.getLocalizedMessage());
    }

    Preconditions.checkNotNull(thread,
        "IE01665: E00068: Received invalid process start string (missing thread information)");

    Preconditions.checkNotNull(module,
        "IE01668: E00069: Received invalid target process start string (missing module information)");

    return new ProcessStart(thread, module);
  }
}
