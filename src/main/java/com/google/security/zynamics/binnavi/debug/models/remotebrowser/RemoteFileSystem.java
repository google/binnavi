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
package com.google.security.zynamics.binnavi.debug.models.remotebrowser;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;

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
 * Contains information about a directory of the remote system.
 */
public final class RemoteFileSystem {
  /**
   * Base directory all other information is relative to.
   */
  private final RemoteDirectory directory;

  /**
   * Drives available on the remote system.
   */
  private final List<RemoteDrive> drives;

  /**
   * Sub-directories of the base directory.
   */
  private final List<RemoteDirectory> directories;

  /**
   * Files in the base directory.
   */
  private final List<RemoteFile> files;

  /**
   * Creates a new remote file system object.
   *
   * @param directory Base directory all other information is relative to.
   * @param drives Drives available on the remote system.
   * @param directories Sub-directories of the base directory.
   * @param files Files in the base directory.
   */
  private RemoteFileSystem(final RemoteDirectory directory, final List<RemoteDrive> drives,
      final List<RemoteDirectory> directories, final List<RemoteFile> files) {
    this.directory = directory;
    this.drives = new ArrayList<>(drives);
    this.directories = new ArrayList<>(directories);
    this.files = new ArrayList<>(files);
  }

  /**
   * Parses XML nodes that describe the remote directories.
   *
   * @param registersNode The parent node to parse.
   * @param files List where the parsed directory information is shown.
   */
  private static void parseDirectoriesInformation(final Node registersNode,
      final List<RemoteDirectory> files) {
    final NodeList nodes = registersNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node node = nodes.item(i);
      final String nodeName = node.getNodeName();
      if ("Directory".equals(nodeName)) {
        files.add(new RemoteDirectory(node.getAttributes().getNamedItem("name").getNodeValue()));
      }
    }
  }

  /**
   * Parses XML nodes that describe the remote drives.
   *
   * @param registersNode The parent node to parse.
   * @param drives List where the parsed drive information is shown.
   */
  private static void parseDrivesInformation(final Node registersNode,
      final List<RemoteDrive> drives) {
    final NodeList nodes = registersNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node node = nodes.item(i);
      final String nodeName = node.getNodeName();
      if ("Drive".equals(nodeName)) {
        drives.add(new RemoteDrive(node.getAttributes().getNamedItem("name").getNodeValue()));
      }
    }
  }

  /**
   * Parses XML nodes that describe the remote files.
   *
   * @param registersNode The parent node to parse.
   * @param files List where the parsed file information is shown.
   */
  private static void parseFilesInformation(final Node registersNode,
      final List<RemoteFile> files) {
    final NodeList nodes = registersNode.getChildNodes();
    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node node = nodes.item(i);
      final String nodeName = node.getNodeName();
      if ("File".equals(nodeName)) {
        files.add(new RemoteFile(node.getAttributes().getNamedItem("name").getNodeValue()));
      }
    }
  }

  /**
   * Parses a byte array that contains XML formatted information about the file system.
   *
   * @param data The data to parse.
   *
   * @return The parsed file system information.
   *
   * @throws ParserConfigurationException Thrown if there is a problem with the parser
   *         configuration.
   * @throws SAXException Thrown if the data could not be parsed.
   * @throws IOException Thrown if the data could not be read.
   */
  public static RemoteFileSystem parse(final byte[] data) throws ParserConfigurationException,
      SAXException, IOException {
    Preconditions.checkNotNull(data, "IE00771: Data argument can not be null");
    RemoteDirectory directory = null;
    final List<RemoteDrive> drives = new ArrayList<>();
    final List<RemoteDirectory> directories = new ArrayList<>();
    final List<RemoteFile> files = new ArrayList<>();
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document document = builder.parse(new ByteArrayInputStream(data, 0, data.length));
    final NodeList nodes = document.getFirstChild().getChildNodes();
    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node node = nodes.item(i);
      final String nodeName = node.getNodeName();
      if ("Drives".equals(nodeName)) {
        parseDrivesInformation(node, drives);
      } else if ("Directories".equals(nodeName)) {
        parseDirectoriesInformation(node, directories);
      } else if ("Files".equals(nodeName)) {
        parseFilesInformation(node, files);
      } else if ("Directory".equals(nodeName)) {
        directory = new RemoteDirectory(node.getAttributes().getNamedItem("name").getNodeValue());
      } else {
        NaviLogger.severe("Error: Unknown node name " + nodeName);
      }
    }
    return new RemoteFileSystem(directory, drives, directories, files);
  }

  /**
   * Returns the sub-directories of the base directory.
   *
   * @return List of sub-directories.
   */
  public List<RemoteDirectory> getDirectories() {
    return new ArrayList<>(directories);
  }

  /**
   * Returns the base directory.
   *
   * @return The base directory.
   */
  public RemoteDirectory getDirectory() {
    return directory;
  }

  /**
   * Returns the drives that are available on the remote system.
   *
   * @return Drives available on the remote system.
   */
  public List<RemoteDrive> getDrives() {
    return new ArrayList<>(drives);
  }

  /**
   * Returns the files inside the base directory.
   *
   * @return Files inside the base directory.
   */
  public List<RemoteFile> getFiles() {
    return new ArrayList<>(files);
  }
}
