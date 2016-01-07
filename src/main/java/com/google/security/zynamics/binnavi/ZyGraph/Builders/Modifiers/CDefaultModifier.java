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
package com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

/**
 * This class is used to calculate the address string that is shown in code nodes and function nodes
 * depending on what relocation options are currently active.
 */
public final class CDefaultModifier implements INodeModifier {
  /**
   * Provides the relocation settings that are considered during address string calculation.
   */
  private final ZyGraphViewSettings m_settings;

  /**
   * Provides the debuggers used to relocate addresses.
   */
  private final BackEndDebuggerProvider m_provider;

  /**
   * Creates a new default modifier.
   *
   * @param settings Provides the relocation settings that are considered during address string
   *        calculation.
   * @param provider Provides the debuggers used to relocate addresses.
   */
  public CDefaultModifier(
      final ZyGraphViewSettings settings, final BackEndDebuggerProvider provider) {
    m_settings = Preconditions.checkNotNull(settings, "IE02109: Settings argument can not be null");
    m_provider = Preconditions.checkNotNull(provider, "IE02110: Provider argument can not be null");
  }

  @Override
  public String getAddress(final INaviInstruction instruction) {
    return getAddress(
        instruction.getModule(), new UnrelocatedAddress(instruction.getAddress()), true);
  }

  @Override
  public String getAddress(final INaviFunctionNode node) {
    return getAddress(node.getFunction().getModule(),
        new UnrelocatedAddress(node.getFunction().getAddress()), true);
  }

  @Override
  public String getAddress(
      final INaviModule module, final UnrelocatedAddress address, final boolean pad) {
    final IDebugger debugger = m_provider.getDebugger(module);

    if ((debugger != null) && m_settings.getDisplaySettings().getShowMemoryAddresses(debugger)) {
      return pad ? debugger.fileToMemory(module, address).getAddress().toHexString()
          : debugger.fileToMemory(module, address)
              .getAddress()
              .toBigInteger()
              .toString(16)
              .toUpperCase();
    } else {
      return pad ? address.getAddress().toHexString() : address.getAddress()
          .toBigInteger().toString(16).toUpperCase();
    }
  }
}
