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
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerExceptionHandlingAction;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions.DebuggerOptionsBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Parser that parses the target information string that is initially sent over by the debug client
 * when the debug client initially connects to the selected target.
 */
public final class TargetInformationParser {

  /**
   * Options which are sent from the debugger. For a detailed description of the flags refer to
   * {@link DebuggerOptions}.
   */
  private enum TargetInformationDebuggerOptions {
    CAN_DETACH("detach"),
    CAN_ATTACH("attach"),
    CAN_TERMINATE("terminate"),
    CAN_HALT("halt"),
    CAN_HALT_BEFORE_COMMUNICATING("haltBeforeCommunicating"),
    PAGE_SIZE("pageSize"),
    CAN_MEMMAP("memmap"),
    HAS_STACK("hasstack"),
    CAN_VALIDATE_MEMORY("validmemory"),
    CAN_MULTI_THREAD("multithread"),
    CAN_SOFTWARE_BREAKPOINTS("softwareBreakpoints"),
    BREAKPOINT_COUNT("breakpointCount"),
    CAN_BREAK_ON_MODULE_LOAD("canBreakOnModuleLoad"),
    CAN_BREAK_ON_MODULE_UNLOAD("canBreakOnModuleUnLoad"),
    CAN_TRACE_COUNT("canTraceCount"),
    EXCEPTION("exception");

    private final String name;

    private TargetInformationDebuggerOptions(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

    public static TargetInformationDebuggerOptions getEnum(String name) {
      for (TargetInformationDebuggerOptions option : values()) {
        if (option.toString().equalsIgnoreCase(name)) {
          return option;
        }
      }
      throw new IllegalArgumentException();
    }
  }


  /**
   * You are not supposed to instantiate this class.
   */
  private TargetInformationParser() {}

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
      throw new MessageParserException(String.format("%s message does not have a '%s' attribute",
          node.getNodeName(), attribute));
    }
    return attributeNode.getNodeValue();
  }

  /**
   * Parses the debugger options string that is part of the target information string.
   *
   * @param node The options node to parse.
   *
   * @return The debugger options parsed from the debugger options message.
   *
   * @throws MessageParserException Thrown if parsing failed.
   */
  private static DebuggerOptions parseOptionsInformation(final Node node)
      throws MessageParserException {
    final NodeList nodes = node.getChildNodes();
    final DebuggerOptionsBuilder builder = new DebuggerOptionsBuilder();

    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node child = nodes.item(i);
      final String name = getAttribute(child, "name");
      switch (TargetInformationDebuggerOptions.getEnum(name)) {
        case CAN_DETACH:
          builder.canDetach(parseBooleanOption(child));
          break;
        case CAN_ATTACH:
          builder.canAttach(parseBooleanOption(child));
          break;
        case CAN_TERMINATE:
          builder.canTerminate(parseBooleanOption(child));
          break;
        case HAS_STACK:
          builder.stackAvailable(parseBooleanOption(child));
          break;
        case CAN_VALIDATE_MEMORY:
          builder.canValidateMemory(parseBooleanOption(child));
          break;
        case CAN_HALT:
          builder.canHalt(parseBooleanOption(child));
          break;
        case CAN_HALT_BEFORE_COMMUNICATING:
          builder.canHaltBeforeCommunicating(parseBooleanOption(child));
          break;
        case CAN_MULTI_THREAD:
          builder.canMultithread(parseBooleanOption(child));
          break;
        case CAN_SOFTWARE_BREAKPOINTS:
          builder.canSoftwareBreakpoints(parseBooleanOption(child));
          break;
        case BREAKPOINT_COUNT:
          builder.breakpointCounter(parseIntOption(child, "value"));
          break;
        case PAGE_SIZE:
          builder.pageSize(parseIntOption(child, "value"));
          break;
        case CAN_BREAK_ON_MODULE_LOAD:
          builder.canBreakOnModuleLoad(parseBooleanOption(child));
          break;
        case CAN_BREAK_ON_MODULE_UNLOAD:
          builder.canBreakOnModuleUnload(parseBooleanOption(child));
          break;
        case EXCEPTION:
          builder.addException(parseExceptionOption(child));
          break;
        case CAN_TRACE_COUNT:
          builder.canTraceCounts(parseBooleanOption(child));
          break;
        case CAN_MEMMAP:
          builder.canMemmap(parseBooleanOption(child));
          break;
        default:
          throw new MessageParserException(
              String.format("Invalid attribute '%s' found in debugger options string", name));
      }
    }
    return builder.build();
  }

  private static boolean parseBooleanOption(Node node) {
    return Boolean.valueOf(node.getAttributes().getNamedItem("value").getNodeValue());
  }

  private static long parseLongOption(Node node, String itemName) {
    return Long.valueOf(node.getAttributes().getNamedItem(itemName).getNodeValue());
  }

  private static int parseIntOption(Node node, String itemName) {
    return Integer.valueOf(node.getAttributes().getNamedItem(itemName).getNodeValue());
  }

  private static String parseStringOption(Node node, String itemName) {
    return node.getAttributes().getNamedItem(itemName).getNodeValue();
  }

  /**
   * Parses an exception debugger option.
   *
   * @param node The node where the exception attributes are parsed from.
   *
   * @return A {@link DebuggerException exception}.
   */
  private static DebuggerException parseExceptionOption(Node node) {
    return new DebuggerException(parseStringOption(node, "exceptionName"),
        parseLongOption(node, "exceptionCode"), DebuggerExceptionHandlingAction
            .convertToHandlingAction(parseIntOption(node, "handlingAction")));
  }

  /**
   * Parses register information from the target information string.
   *
   * @param node The root node of the registers information.
   *
   * @return Parsed register description information.
   *
   * @throws MessageParserException Thrown if parsing the information failed.
   */
  private static List<RegisterDescription> parseRegisterInformation(final Node node)
      throws MessageParserException {
    final List<RegisterDescription> registers = new ArrayList<>();
    final NodeList nodes = node.getChildNodes();

    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node child = nodes.item(i);
      final String registerName = getAttribute(child, "name");
      final String registerSize = getAttribute(child, "size");
      final String registerEditable = getAttribute(child, "editable");
      registers.add(new RegisterDescription(registerName, Integer.valueOf(registerSize),
          Boolean.valueOf(registerEditable)));
    }

    return registers;
  }

  /**
   * Parses a target information message and creates Java objects from the information found in the
   * message.
   *
   * @param data The byte array that contains the target information.
   * @return The usable object that contains the target information.
   * @throws IllegalArgumentException If the data argument is null.
   * @throws MessageParserException If parsing goes wrong.
   */
  public static TargetInformation parse(final byte[] data) throws MessageParserException {
    Preconditions.checkNotNull(data, "IE01300: Data argument can not be null");

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    int addressSize = -1;
    List<RegisterDescription> registers = null;
    DebuggerOptions options = null;

    try {
      final DocumentBuilder builder = factory.newDocumentBuilder();
      final Document document = builder.parse(new ByteArrayInputStream(data, 0, data.length));
      final NodeList nodes = document.getFirstChild().getChildNodes();

      for (int i = 0; i < nodes.getLength(); ++i) {
        final Node node = nodes.item(i);
        final String nodeName = node.getNodeName();
        if ("registers".equals(nodeName)) {
          registers = parseRegisterInformation(node);
        } else if ("size".equals(nodeName)) {
          addressSize = Integer.valueOf(node.getTextContent());
        } else if ("options".equals(nodeName)) {
          options = parseOptionsInformation(node);
        } else {
          throw new MessageParserException(
              String.format("Found unknown node '%s' in target information string", nodeName));
        }
      }
    } catch (final ParserConfigurationException | SAXException | IOException exception) {
      CUtilityFunctions.logException(exception);
      throw new MessageParserException(exception.getLocalizedMessage());
    }

    if (addressSize == -1) {
      throw new MessageParserException(
          "E00070: IE01043: Received invalid target information string (missing address size information)");
    }

    Preconditions.checkNotNull(registers,
        "IE01044: Received invalid target information string (missing registers information)");
    Preconditions.checkNotNull(options,
        "IE01046: Received invalid target information string (missing options information)");
    return new TargetInformation(addressSize, registers, options);
  }
}
