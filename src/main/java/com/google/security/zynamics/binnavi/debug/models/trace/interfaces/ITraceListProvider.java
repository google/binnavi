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
package com.google.security.zynamics.binnavi.debug.models.trace.interfaces;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;

import java.util.List;


/**
 * Interface that must be implemented by all concrete trace list providers.
 */
public interface ITraceListProvider extends Iterable<TraceList> {
  /**
   * Adds a listener objects that is notified about new traces.
   *
   * @param listener The listener object to add.
   */
  void addListener(ITraceManagerListener listener);

  /**
   * Creates a new trace with the given name and description.
   *
   * @param name The name of the trace.
   * @param description The description of the trace.
   *
   * @return The trace object with the given name and description.
   *
   * @throws CouldntSaveDataException Thrown if the new trace could not be created.
   */
  TraceList createTrace(String name, String description) throws CouldntSaveDataException;

  /**
   * Generates a new unique name for a trace.
   *
   * @return A unique trace name.
   */
  String generateName();

  /**
   * Returns the trace list identified by the given index.
   *
   * @param index The index of the trace list.
   *
   * @return The trace list object identified by the index.
   */
  TraceList getList(int index);

  /**
   * Returns the number of trace lists known to the trace provider.
   *
   * @return The number of trace lists known to the trace provider.
   */
  int getNumberOfTraceLists();

  /**
   * Returns the known traces.
   *
   * @return The known traces.
   */
  List<TraceList> getTraces();

  /**
   * Returns whether the trace list provider is loaded.
   *
   * @return True, if the trace list provider is loaded. False, otherwise.
   */
  boolean isLoaded();

  /**
   * Removes a trace list from the trace provider.
   *
   * @param list The list to remove.
   *
   * @throws CouldntDeleteException
   */
  void removeList(TraceList list) throws CouldntDeleteException;

  /**
   * Removes a listener from the trace list provider.
   *
   * @param listener The listener to remove.
   */
  void removeListener(ITraceManagerListener listener);
}
