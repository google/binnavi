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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

/**
 * This class is used to display a progress dialog until a certain number of breakpoints is set.
 */
public final class CStartTraceListener extends CEndlessHelperThread {
  /**
   * Logger which is monitored by the trace listener.
   */
  private final TraceLogger m_logger;

  /**
   * Trace the events are added to.
   */
  private final TraceList m_trace;

  /**
   * Addresses where echo breakpoints are set by the logger.
   */
  private final Set<BreakpointAddress> m_addresses;

  /**
   * Maximum number of hits before an echo breakpoint is removed.
   */
  private final int m_maximumHits;

  /**
   * Creates a new trace listener object.
   * @param logger Logger which is monitored by the trace listener.
   * @param trace Trace the events are added to.
   * @param addresses Addresses where echo breakpoints are set by the logger.
   * @param maximumHits Maximum number of hits before an echo breakpoint is removed.
   */
  public CStartTraceListener(final TraceLogger logger, final TraceList trace,
      final Set<BreakpointAddress> addresses, final int maximumHits) {
    m_logger = Preconditions.checkNotNull(logger, "IE01562: Logger argument can not be null");
    m_addresses =
        Preconditions.checkNotNull(addresses, "IE01563: Addresses argument can not be null");
    m_trace = Preconditions.checkNotNull(trace, "IE01107: trace argument can not be null");
    m_maximumHits = maximumHits;
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    m_logger.start(m_trace, m_addresses, m_maximumHits);
  }
}
