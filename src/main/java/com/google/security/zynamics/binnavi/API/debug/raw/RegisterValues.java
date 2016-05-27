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
package com.google.security.zynamics.binnavi.API.debug.raw;

import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


// ! Contains the known register values of all threads of a process.
/**
 * Some replies sent by debug clients contain the current register values of all threads of the
 * debugged target process. These register values are stored in the RegisterValues class.
 */
public final class RegisterValues implements Iterable<ThreadRegisterValues> {
  // / @cond INTERNAL

  /**
   * List of all known thread register values.
   */
  private final List<ThreadRegisterValues> values;

  /**
   * Creates a new API register values object.
   *
   * @param registerValues The wrapped internal register values object.
   */
  // / @endcond
  public RegisterValues(
      final com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues registerValues) {
    
    final List<ThreadRegisterValues> values = new ArrayList<ThreadRegisterValues>();		
 		 
    for (final ThreadRegisters threadRegisterValues : registerValues) {	
      values.add(new ThreadRegisterValues(threadRegisterValues));
    }
		
    this.values = values;		
   }

  // ! Returns the register values of the individual threads.
  /**
   * Returns the register values of the individual threads.
   *
   * @return The register values of the individual threads.
   */
  public List<ThreadRegisterValues> getValues() {
    return new ArrayList<ThreadRegisterValues>(values);
  }

  @Override
  public Iterator<ThreadRegisterValues> iterator() {
    return new ArrayList<ThreadRegisterValues>(values).iterator();
  }
}
