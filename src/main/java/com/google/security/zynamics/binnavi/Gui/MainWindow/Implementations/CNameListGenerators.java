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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;



import java.util.List;

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Contains functions for creating lists of the names of database objects.
 */
public final class CNameListGenerators {
  /**
   * Maximum number of item names in the list.
   */
  private static final int MAX_LIST_LENGTH = 10;

  /**
   * You are not supposed to instantiate this class.
   */
  private CNameListGenerators() {
  }

  /**
   * Generates a name list from the names of the given debuggers.
   * 
   * @param debuggers The debuggers that provide the names.
   * 
   * @return The generated name list.
   */
  public static String getNameList(final DebuggerTemplate[] debuggers) {
    int count = 0;

    final StringBuilder list = new StringBuilder();

    for (final DebuggerTemplate debugger : debuggers) {
      list.append("- ");
      list.append(debugger.getName());
      list.append('\n');

      count++;

      if ((count == MAX_LIST_LENGTH) && (debuggers.length != MAX_LIST_LENGTH)) {
        list.append("\n... ");
        list.append(String.format("%d others ...", debuggers.length - count));

        break;
      }
    }

    return list.toString();
  }

  /**
   * Generates a name list from the names of the given address spaces.
   * 
   * @param addressSpaces The address spaces that provide the names.
   * 
   * @return The generated name list.
   */
  public static String getNameList(final INaviAddressSpace[] addressSpaces) {
    int count = 0;

    final StringBuilder list = new StringBuilder();

    for (final INaviAddressSpace addressSpace : addressSpaces) {
      list.append("- ");
      list.append(addressSpace.getConfiguration().getName());
      list.append('\n');

      count++;

      if ((count == MAX_LIST_LENGTH) && (addressSpaces.length != MAX_LIST_LENGTH)) {
        list.append("\n... ");
        list.append(String.format("%d others ...", addressSpaces.length - count));

        break;
      }
    }

    return list.toString();
  }

  /**
   * Generates a name list from the names of the given modules.
   * 
   * @param modules The modules that provide the names.
   * 
   * @return The generated name list.
   */
  public static String getNameList(final INaviModule[] modules) {
    int count = 0;

    final StringBuilder list = new StringBuilder();

    for (final INaviModule module : modules) {
      list.append("- ");
      list.append(module.getConfiguration().getName());
      list.append('\n');

      count++;

      if ((count == MAX_LIST_LENGTH) && (modules.length != MAX_LIST_LENGTH)) {
        list.append("\n... ");
        list.append(String.format("%d others ...", modules.length - count));

        break;
      }
    }

    return list.toString();
  }

  /**
   * Generates a name list from the names of the given projects.
   * 
   * @param projects The projects that provide the names.
   * 
   * @return The generated name list.
   */
  public static String getNameList(final INaviProject[] projects) {
    int count = 0;

    final StringBuilder list = new StringBuilder();

    for (final INaviProject project : projects) {
      list.append("- ");
      list.append(project.getConfiguration().getName());
      list.append('\n');

      count++;

      if ((count == MAX_LIST_LENGTH) && (projects.length != MAX_LIST_LENGTH)) {
        list.append("\n... ");
        list.append(String.format("%d others ...", projects.length - count));

        break;
      }
    }

    return list.toString();
  }

  /**
   * Generates a name list from the names of the given views.
   * 
   * @param views The views that provide the names.
   * 
   * @return The generated name list.
   */
  public static String getNameList(final INaviView[] views) {
    int count = 0;

    final StringBuilder list = new StringBuilder();

    for (final INaviView view : views) {
      list.append("- ");
      list.append(view.getName());
      list.append('\n');

      count++;

      if ((count == MAX_LIST_LENGTH) && (views.length != MAX_LIST_LENGTH)) {
        list.append("\n... ");
        list.append(String.format("%d others ...", views.length - count));

        break;
      }
    }

    return list.toString();
  }

  /**
   * Generates a name list from the names of the given traces.
   * 
   * @param traces The traces that provide the names.
   * 
   * @return The generated name list.
   */
  public static String getNameList(final List<TraceList> traces) {
    int count = 0;

    final StringBuilder list = new StringBuilder();

    for (final TraceList trace : traces) {
      list.append("- ");
      list.append(trace.getName());
      list.append('\n');

      count++;

      if ((count == MAX_LIST_LENGTH) && (traces.size() != MAX_LIST_LENGTH)) {
        list.append("\n... ");
        list.append(String.format("%d others ...", traces.size() - count));

        break;
      }
    }

    return list.toString();
  }
}
