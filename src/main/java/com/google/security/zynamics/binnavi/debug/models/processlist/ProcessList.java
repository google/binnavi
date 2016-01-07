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
package com.google.security.zynamics.binnavi.debug.models.processlist;

import com.google.security.zynamics.binnavi.Log.NaviLogger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Used to store information about a list of processes.
 */
public final class ProcessList implements Iterable<ProcessDescription> {
  /**
   * Processes in the list.
   */
  private final List<ProcessDescription> processes;

  /**
   * Creates a new process list.
   *
   * @param processes Processes in the list.
   */
  private ProcessList(final List<ProcessDescription> processes) {
    this.processes = processes;
  }

  /**
   * Parses a process list in binary form.
   *
   * @param data The binary data.
   *
   * @return The parsed process list.
   *
   * @throws ParserConfigurationException Thrown if there was a problem with the parser
   *         configuration.
   * @throws SAXException Thrown if the process list could not be parsed.
   * @throws IOException Thrown if the process list could not be read.
   */
  public static ProcessList parse(final byte[] data) throws ParserConfigurationException,
      SAXException, IOException {
    final List<ProcessDescription> processes = new ArrayList<>();
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document document = builder.parse(new ByteArrayInputStream(data, 0, data.length));
    final NodeList nodes = document.getFirstChild().getChildNodes();
    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node node = nodes.item(i);
      final String nodeName = node.getNodeName();
      if ("Process".equals(nodeName)) {
        final String pid = node.getAttributes().getNamedItem("pid").getNodeValue();
        final String name = node.getAttributes().getNamedItem("name").getNodeValue();
        processes.add(new ProcessDescription(Integer.valueOf(pid), name));
      } else {
        NaviLogger.severe("Error: Unknown node name " + nodeName);
      }
    }
    return new ProcessList(processes);
  }

  /**
   * Returns the process description with the given index.
   *
   * @param index The index of the process description.
   *
   * @return The process description at the given index.
   */
  public ProcessDescription get(final int index) {
    return processes.get(index);
  }

  @Override
  public Iterator<ProcessDescription> iterator() {
    return processes.iterator();
  }

  /**
   * Returns the number of known processes in the process list.
   *
   * @return The number of known processes in the process list.
   */
  public int size() {
    return processes.size();
  }
}
