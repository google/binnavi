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
package com.google.security.zynamics.binnavi.Tutorials;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.io.DirUtils;
import com.google.security.zynamics.zylib.io.IDirectoryTraversalCallback;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Loads tutorials from the tutorial directory.
 */
public final class CTutorialLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTutorialLoader() {
  }

  /**
   * Loads a single tutorial from a file.
   * 
   * @param file The tutorial file to load.
   * 
   * @return The loaded tutorial.
   * 
   * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the
   *         configuration requested.
   * @throws SAXException If any parse errors occur.
   * @throws IOException If any IO errors occur.
   */
  private static CTutorial loadTutorial(final File file) throws ParserConfigurationException,
      SAXException, IOException {
    String name = "";
    String description = "";
    final List<CTutorialStep> steps = new ArrayList<CTutorialStep>();

    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    final DocumentBuilder builder = factory.newDocumentBuilder();
    final Document document = builder.parse(file);

    final NodeList nodes = document.getFirstChild().getChildNodes();

    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node node = nodes.item(i);

      final String nodeName = node.getNodeName();

      if ("name".equals(nodeName)) {
        name = node.getTextContent();
      } else if ("description".equals(nodeName)) {
        description = node.getTextContent();
      } else if ("steps".equals(nodeName)) {
        steps.addAll(readSteps(node));
      }
    }

    return new CTutorial(name, description, steps);
  }

  /**
   * Reads the individual steps of a tutorial.
   * 
   * @param node The steps XML node that is the parent node of the individual steps.
   * 
   * @return The list of read steps.
   */
  private static List<CTutorialStep> readSteps(final Node node) {
    final List<CTutorialStep> steps = new ArrayList<CTutorialStep>();

    for (int i = 0; i < node.getChildNodes().getLength(); ++i) {
      final Node child = node.getChildNodes().item(i);

      final String childText = child.getNodeName();

      if ("step".equals(childText)) {
        final List<Long> identifiers = new ArrayList<Long>();
        final List<Long> allows = new ArrayList<Long>();
        String actionDescription = null;
        boolean next = false;

        for (int j = 0; j < child.getChildNodes().getLength(); ++j) {
          final Node child2 = child.getChildNodes().item(j);

          final String childName = child2.getNodeName();

          if ("action".equals(childName)) {
            final long index = Long.valueOf(child2.getTextContent());

            if (index == 0) {
              next = true;
              continue;
            }

            identifiers.add(index);
          } else if ("allowed".equals(childName)) {
            final long index = Long.valueOf(child2.getTextContent());

            allows.add(index);
          } else if ("description".equals(childName)) {
            actionDescription = child2.getTextContent();
          }
        }

        steps.add(new CTutorialStep(actionDescription, identifiers, allows, next));
      }
    }

    return steps;
  }

  /**
   * Loads all tutorial files from the given directory.
   * 
   * @param directory Directory from which the tutorials are loaded.
   * 
   * @return List of tutorials loaded from the tutorial files in the given directory.
   */
  public static IFilledList<CTutorial> readTutorials(final String directory) {
    final IFilledList<CTutorial> tutorials = new FilledList<CTutorial>();

    DirUtils.traverse(new File(directory), new IDirectoryTraversalCallback() {
      @Override
      public void entering(final File directory) {
        // Irrelevant
      }

      @Override
      public void leaving(final File directory) {
        // Irrelevant
      }

      @Override
      public void nextFile(final File file) {
        if (file.getAbsolutePath().endsWith("xml")) {
          try {
            tutorials.add(loadTutorial(file));
          } catch (final ParserConfigurationException e) {
            CUtilityFunctions.logException(e);
          } catch (final SAXException e) {
            CUtilityFunctions.logException(e);
          } catch (final IOException e) {
            CUtilityFunctions.logException(e);
          }
        }
      }
    });

    return tutorials;
  }
}
