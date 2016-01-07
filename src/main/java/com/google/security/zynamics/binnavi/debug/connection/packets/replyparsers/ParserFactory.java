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
package com.google.security.zynamics.binnavi.debug.connection.packets.replyparsers;

import com.google.security.zynamics.binnavi.debug.connection.DebugCommandType;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;

/**
 * Factory class that can be used to find the correct debug client reply parser depending on the
 * type of a reply that was received.
 */
public final class ParserFactory {
  /**
   * Parses Attach replies.
   */
  private final AbstractReplyParser<?> attachParser;

  /**
   * Parses Set Breakpoint Condition replies.
   */
  private final AbstractReplyParser<?> breakpointConditionSetParser;

  /**
   * Parses Breakpoint Hit replies.
   */
  private final AbstractReplyParser<?> breakpointHitParser;

  /**
   * Parses Breakpoint Removed replies.
   */
  private final AbstractReplyParser<?> breakpointRemovedParser;

  /**
   * Parses Breakpoint Set replies.
   */
  private final AbstractReplyParser<?> breakpointSetParser;

  /**
   * Parses Cancel Target Selection replies.
   */
  private final AbstractReplyParser<?> cancelTargetSelectionParser;

  /**
   * Parses Detach replies.
   */
  private final AbstractReplyParser<?> detachParser;

  /**
   * Parses Echo Breakpoint Hit replies.
   */
  private final AbstractReplyParser<?> echoBreakpointHitParser;

  /**
   * Parses Echo Breakpoint Removed replies.
   */
  private final AbstractReplyParser<?> echoBreakpointRemovedParser;

  /**
   * Parses Echo Breakpoint Set replies.
   */
  private final AbstractReplyParser<?> echoBreakpointSetParser;

  /**
   * Parses Exception Occurred replies.
   */
  private final AbstractReplyParser<?> exceptionOccurredParser;

  /**
   * Parses Halt replies.
   */
  private final AbstractReplyParser<?> haltParser;

  /**
   * Parses List Files replies.
   */
  private final AbstractReplyParser<?> listFilesParser;

  /**
   * Parses List Processes replies.
   */
  private final AbstractReplyParser<?> listProcessesParser;

  /**
   * Parses Memory Map replies.
   */
  private final AbstractReplyParser<?> memoryMapParser;

  /**
   * Parses Module Loaded replies.
   */
  private final AbstractReplyParser<?> moduleLoadedParser;

  /**
   * Parses Module Unloaded replies.
   */
  private final AbstractReplyParser<?> moduleUnloadedParser;

  /**
   * Parses Process Closed replies.
   */
  private final AbstractReplyParser<?> processClosedParser;

  /**
   * Parses Read Memory replies.
   */
  private final AbstractReplyParser<?> readMemoryParser;

  /**
   * Parses Read Registers replies.
   */
  private final AbstractReplyParser<?> registersParser;

  /**
   * Parses Request Target replies.
   */
  private final AbstractReplyParser<?> requestTargetParser;

  /**
   * Parses Resume replies.
   */
  private final AbstractReplyParser<?> resumeParser;

  /**
   * Parses Resume Thread replies.
   */
  private final AbstractReplyParser<?> resumeThreadParser;

  /**
   * Parses Search replies.
   */
  private final AbstractReplyParser<?> searchParser;

  /**
   * Parses Select Target File replies.
   */
  private final AbstractReplyParser<?> selectedFileParser;

  /**
   * Parses Select Process replies.
   */
  private final AbstractReplyParser<?> selectProcessParser;

  /**
   * Parses Set Debug Event Settings replies.
   */
  private final AbstractReplyParser<?> setDebugEventSettingsParser;

  /**
   * Parse set exception settings replies.
   */
  private final AbstractReplyParser<?> setExceptionSettingsParser;

  /**
   * Parses Set Register replies.
   */
  private final AbstractReplyParser<?> setRegisterParser;

  /**
   * Parses Single Step replies.
   */
  private final AbstractReplyParser<?> singleStepParser;

  /**
   * Parses Step Breakpoint Hit replies.
   */
  private final AbstractReplyParser<?> stepBreakpointHitParser;

  /**
   * Parses Step Breakpoint Removed replies.
   */
  private final AbstractReplyParser<?> stepBreakpointRemovedParser;

  /**
   * Parses Step Breakpoint Set replies.
   */
  private final AbstractReplyParser<?> stepBreakpointSetParser;

  /**
   * Parses Suspend Thread replies.
   */
  private final AbstractReplyParser<?> suspendThreadParser;

  /**
   * Parses Target Information replies.
   */
  private final AbstractReplyParser<?> targetInformationParser;

  /**
   * Parses Terminate replies.
   */
  private final AbstractReplyParser<?> terminateParser;

  /**
   * Parses Thread Closed replies.
   */
  private final AbstractReplyParser<?> threadClosedParser;

  /**
   * Parses Thread Created replies.
   */
  private final AbstractReplyParser<?> threadCreatedParser;

  /**
   * Parses Validated Memory replies.
   */
  private final AbstractReplyParser<?> validatedMemoryParser;

  /**
   * Parses Write Memory replies.
   */
  private final AbstractReplyParser<?> writeMemoryParser;

  /**
   * Parses Debugger Event Settings queries.
   */
  private final AbstractReplyParser<?> queryDebuggerEventSettingsParser;

  /**
   * Parses Process Start replies.
   */
  private final AbstractReplyParser<?> processStartParser;

  /**
   * Creates a new parser factory object for a given connection stream to a debug client.
   *
   * @param clientReader Used to read incoming data from a debug client.
   */
  public ParserFactory(final ClientReader clientReader) {
    attachParser = new AttachParser(clientReader);
    breakpointHitParser = new BreakpointHitParser(clientReader);
    breakpointRemovedParser = new BreakpointsRemovedParser(clientReader);
    breakpointSetParser = new BreakpointSetParser(clientReader);
    cancelTargetSelectionParser = new CancelTargetSelectionParser(clientReader);
    detachParser = new DetachParser(clientReader);
    echoBreakpointHitParser = new EchoBreakpointHitParser(clientReader);
    echoBreakpointRemovedParser = new EchoBreakpointRemovedParser(clientReader);
    echoBreakpointSetParser = new EchoBreakpointSetParser(clientReader);
    exceptionOccurredParser = new ExceptionOccurredParser(clientReader);
    listFilesParser = new ListFilesParser(clientReader);
    listProcessesParser = new ListProcessesParser(clientReader);
    memoryMapParser = new MemoryMapParser(clientReader);
    moduleLoadedParser = new ModuleLoadedParser(clientReader);
    moduleUnloadedParser = new ModuleUnloadedParser(clientReader);
    processClosedParser = new ProcessClosedParser(clientReader);
    readMemoryParser = new ReadMemoryParser(clientReader);
    registersParser = new RegistersParser(clientReader);
    requestTargetParser = new RequestTargetParser(clientReader);
    resumeParser = new ResumeParser(clientReader);
    searchParser = new SearchParser(clientReader);
    selectProcessParser = new SelectProcessParser(clientReader);
    setRegisterParser = new SetRegisterParser(clientReader);
    singleStepParser = new SingleStepParser(clientReader);
    stepBreakpointHitParser = new StepBreakpointHitParser(clientReader);
    stepBreakpointRemovedParser = new StepBreakpointRemovedParser(clientReader);
    stepBreakpointSetParser = new StepBreakpointSetParser(clientReader);
    targetInformationParser = new TargetInformationReplyParser(clientReader);
    terminateParser = new TerminateParser(clientReader);
    threadClosedParser = new ThreadClosedParser(clientReader);
    threadCreatedParser = new ThreadCreatedParser(clientReader);
    validatedMemoryParser = new ValidateMemoryParser(clientReader);
    selectedFileParser = new SelectFileParser(clientReader);
    haltParser = new HaltParser(clientReader);
    resumeThreadParser = new ResumeThreadParser(clientReader);
    suspendThreadParser = new SuspendThreadParser(clientReader);
    breakpointConditionSetParser = new BreakpointConditionSetParser(clientReader);
    writeMemoryParser = new WriteMemoryParser(clientReader);
    setExceptionSettingsParser = new SetExceptionSettingsParser(clientReader);
    setDebugEventSettingsParser = new SetDebuggerEventSettingsParser(clientReader);
    queryDebuggerEventSettingsParser = new QueryDebuggerEventSettingsParser(clientReader);
    processStartParser = new ProcessStartReplyParser(clientReader);
  }

  /**
   * Returns the correct parser for a given message type.
   *
   * @param type The message type.
   *
   * @return The parser for messages of the given type.
   */
  public AbstractReplyParser<?> getParser(final int type) {
    switch (type) {
      case DebugCommandType.RESP_ATTACH_SUCCESS:
      case DebugCommandType.RESP_ATTACH_ERROR:
        return attachParser;
      case DebugCommandType.RESP_BP_HIT:
        return breakpointHitParser;
      case DebugCommandType.RESP_BP_REM_SUCCESS:
      case DebugCommandType.RESP_BP_REM_ERROR:
        return breakpointRemovedParser;
      case DebugCommandType.RESP_BP_SET_SUCCESS:
      case DebugCommandType.RESP_BP_SET_ERROR:
        return breakpointSetParser;
      case DebugCommandType.RESP_CANCEL_TARGET_SELECTION_SUCCESS:
        return cancelTargetSelectionParser;
      case DebugCommandType.RESP_DETACH_SUCCESS:
      case DebugCommandType.RESP_DETACH_ERROR:
        return detachParser;
      case DebugCommandType.RESP_BPE_HIT:
        return echoBreakpointHitParser;
      case DebugCommandType.RESP_BPE_REM_SUCCESS:
      case DebugCommandType.RESP_BPE_REM_ERROR:
        return echoBreakpointRemovedParser;
      case DebugCommandType.RESP_BPE_SET_SUCCESS:
      case DebugCommandType.RESP_BPE_SET_ERROR:
        return echoBreakpointSetParser;
      case DebugCommandType.RESP_EXCEPTION_OCCURED:
        return exceptionOccurredParser;
      case DebugCommandType.RESP_LIST_FILES_SUCCESS:
      case DebugCommandType.RESP_LIST_FILES_ERROR:
        return listFilesParser;
      case DebugCommandType.RESP_LIST_PROCESSES_SUCCESS:
        return listProcessesParser;
      case DebugCommandType.RESP_MEMMAP_SUCCESS:
      case DebugCommandType.RESP_MEMMAP_ERROR:
        return memoryMapParser;
      case DebugCommandType.RESP_MODULE_LOADED:
        return moduleLoadedParser;
      case DebugCommandType.RESP_MODULE_UNLOADED:
        return moduleUnloadedParser;
      case DebugCommandType.RESP_PROCESS_CLOSED:
        return processClosedParser;
      case DebugCommandType.RESP_READ_MEMORY_SUCCESS:
      case DebugCommandType.RESP_READ_MEMORY_ERROR:
        return readMemoryParser;
      case DebugCommandType.RESP_REGISTERS_SUCCESS:
      case DebugCommandType.RESP_REGISTERS_ERROR:
        return registersParser;
      case DebugCommandType.RESP_REQUEST_TARGET:
        return requestTargetParser;
      case DebugCommandType.RESP_RESUME_SUCCESS:
      case DebugCommandType.RESP_RESUME_ERROR:
        return resumeParser;
      case DebugCommandType.RESP_SEARCH_SUCCESS:
      case DebugCommandType.RESP_SEARCH_ERROR:
        return searchParser;
      case DebugCommandType.RESP_SELECT_PROCESS_SUCCESS:
      case DebugCommandType.RESP_SELECT_PROCESS_ERROR:
        return selectProcessParser;
      case DebugCommandType.RESP_SET_REGISTER_SUCCESS:
      case DebugCommandType.RESP_SET_REGISTER_ERROR:
        return setRegisterParser;
      case DebugCommandType.RESP_SINGLE_STEP_SUCCESS:
      case DebugCommandType.RESP_SINGLE_STEP_ERROR:
        return singleStepParser;
      case DebugCommandType.RESP_BPS_HIT:
        return stepBreakpointHitParser;
      case DebugCommandType.RESP_BPS_REM_SUCCESS:
      case DebugCommandType.RESP_BPS_REM_ERROR:
        return stepBreakpointRemovedParser;
      case DebugCommandType.RESP_BPS_SET_SUCCESS:
      case DebugCommandType.RESP_BPS_SET_ERROR:
        return stepBreakpointSetParser;
      case DebugCommandType.RESP_INFO:
        return targetInformationParser;
      case DebugCommandType.RESP_TERMINATE_SUCCESS:
      case DebugCommandType.RESP_TERMINATE_ERROR:
        return terminateParser;
      case DebugCommandType.RESP_THREAD_CLOSED:
        return threadClosedParser;
      case DebugCommandType.RESP_THREAD_CREATED:
        return threadCreatedParser;
      case DebugCommandType.RESP_VALID_MEMORY_SUCCESS:
      case DebugCommandType.RESP_VALID_MEMORY_ERROR:
        return validatedMemoryParser;
      case DebugCommandType.RESP_SELECT_FILE_SUCC:
      case DebugCommandType.RESP_SELECT_FILE_ERR:
        return selectedFileParser;
      case DebugCommandType.RESP_HALTED_SUCCESS:
      case DebugCommandType.RESP_HALTED_ERROR:
        return haltParser;
      case DebugCommandType.RESP_RESUME_THREAD_SUCC:
      case DebugCommandType.RESP_RESUME_THREAD_ERR:
        return resumeThreadParser;
      case DebugCommandType.RESP_SUSPEND_THREAD_SUCC:
      case DebugCommandType.RESP_SUSPEND_THREAD_ERR:
        return suspendThreadParser;
      case DebugCommandType.RESP_SET_BREAKPOINT_CONDITION_SUCC:
      case DebugCommandType.RESP_SET_BREAKPOINT_CONDITION_ERR:
        return breakpointConditionSetParser;
      case DebugCommandType.RESP_WRITE_MEMORY_SUCC:
      case DebugCommandType.RESP_WRITE_MEMORY_ERR:
        return writeMemoryParser;
      case DebugCommandType.RESP_SET_EXCEPTIONS_SUCC:
      case DebugCommandType.RESP_SET_EXCEPTIONS_ERR:
        return setExceptionSettingsParser;
      case DebugCommandType.RESP_SET_DEBUGGER_EVENT_SETTINGS_SUCC:
      case DebugCommandType.RESP_SET_DEBUG_EVENT_SETTINGS_ERR:
        return setDebugEventSettingsParser;
      case DebugCommandType.RESP_QUERY_DEBUGGER_EVENT_SETTINGS:
        return queryDebuggerEventSettingsParser;
      case DebugCommandType.RESP_PROCESS_START:
        return processStartParser;

      default:
        throw new IllegalStateException(
            String.format("IE01085: Received unknown message %d", type));
    }
  }
}
