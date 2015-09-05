/*
Copyright 2015 Google Inc. All Rights Reserved.

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
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Class that is used to parse register information replies from the debug client.
 *
 *  Example Message: <threads><thread id="123"><register name="eax" value="12345" pc="0"
 * sp="0"/></thread></threads>
 */
public final class RegisterValuesParser {
  /**
   * You are not supposed to instantiate this class.
   */
  private RegisterValuesParser() {}

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
      throw new MessageParserException(String.format(
          "IE01041: Thread node of register values message does not have a '%s' attribute",
          attribute));
    }

    return attributeNode.getNodeValue();
  }

  /**
   * Determines whether a node has an attribute with the given name.
   *
   * @param node The node to check.
   * @param name The name of the attribute to search for.
   *
   * @return True, if the node has an attribute with the given name. False, otherwise.
   */
  private static boolean hasAttribute(final Node node, final String name) {
    return node.getAttributes().getNamedItem(name) != null;
  }

  /**
   * Parses a single thread node.
   *
   * @param node The node to parse.
   *
   * @return The thread registers object created from the content of the node.
   *
   * @throws MessageParserException Thrown if parsing the thread node failed.
   */
  private static ThreadRegisters parseThreadNode(final Node node) throws MessageParserException {
    final List<RegisterValue> registerValues = new ArrayList<>();

    final long tid = Long.valueOf(node.getAttributes().getNamedItem("id").getNodeValue());

    final NodeList children = node.getChildNodes();

    for (int i = 0; i < children.getLength(); ++i) {
      final Node child = children.item(i);

      final String registerName = getAttribute(child, "name");
      final BigInteger registerValue = new BigInteger(getAttribute(child, "value"), 16);
      final byte[] memory = MemoryStringParser.parseMemoryString(getAttribute(child, "memory"));
      final boolean isPc = hasAttribute(child, "pc");
      final boolean isSp = hasAttribute(child, "sp");

      registerValues.add(new RegisterValue(registerName, registerValue, memory, isPc, isSp));
    }

    return new ThreadRegisters(tid, registerValues);
  }

  /**
   * Parses a byte array from the debug client into usable register information.
   *
   * @param data Byte array from the debug client.
   *
   * @return Usable register information.
   *
   * @throws IllegalArgumentException If the data argument is null.
   * @throws MessageParserException If parsing the message failed.
   */
  public static RegisterValues parse(final byte[] data) throws MessageParserException {
    Preconditions.checkNotNull(data, "IE01299: Data argument can not be null");

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      final DocumentBuilder builder = factory.newDocumentBuilder();
      final Document document = builder.parse(new ByteArrayInputStream(data, 0, data.length));

      final NodeList nodes = document.getFirstChild().getChildNodes();

      final List<ThreadRegisters> threads = new ArrayList<>();

      for (int i = 0; i < nodes.getLength(); ++i) {
        final Node node = nodes.item(i);

        if (node.getNodeName().equals("Thread")) {
          threads.add(parseThreadNode(node));
        } else {
          throw new MessageParserException(String.format(
              "IE01040: Invalid node '%s' found during register values message parsing",
              node.getNodeName()));
        }
      }

      return new RegisterValues(threads);
    } catch (IOException | ParserConfigurationException | SAXException exception) {
      CUtilityFunctions.logException(exception);

      throw new MessageParserException(exception.getLocalizedMessage());
    }
  }
}
