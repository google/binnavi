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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import com.google.security.zynamics.binnavi.disassembly.types.Section;

/**
 * TODO(jannewger): how to let "data providers" know that the current section was changed? We either
 * would need to force at least two "data providers" (static section data and section data while
 * debugging) to implement a "setCurrentSection" method, or let them be listeners. Pro: only one
 * class would change its interface (namely the listener provider: add/removeListener).
 *
 * @author jannewger (Jan Newger)
 *
 */
public interface SectionChangedListener {

  public void sectionChanged(Section newSection);
}
