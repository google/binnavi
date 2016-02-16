// Copyright 2011-2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef BASESYSTEM_HPP
#define BASESYSTEM_HPP

#include <boost/filesystem.hpp>

#include <map>
#include <set>
#include <list>
#include <string>

#include "DebugExceptionHandlingAction.hpp"
#include "DebugException.hpp"
#include "DebuggerEventSettings.hpp"
#include "defs.hpp"
#include "errors.hpp"

struct DebuggerOptions;
class InformationProvider;

// Creates an XML string that contains register information.
std::string createRegisterString(const RegisterContainer& registers);

// Searches for a substring in a larger buffer.
unsigned int search(const char* buffer, unsigned int bufferSize,
                    const char* searchString, unsigned int stringLen);

// Converts a CPU address to a std::string
std::string cpuAddressToString(CPUADDRESS address);

/**
* The base class for all system policies.
*
* All classes that should be used as system policies must inherit from this
* class.
**/
class BaseSystem : public IConditionProvider {
 private:
  /**
  * The process ID of the target process.
  **/
  unsigned int pid;

  /**
  * The thread that receives thread-specific commands.
  **/
  unsigned int activeThread;

  /**
  * The path to the target process executable.
  **/
  boost::filesystem::path path;

  /**
  * Command line arguments that are passed to the target process.
  **/
  const std::vector<const NATIVE_STRING> commands;

  /**
  * A list of debug events that haven't been sent to BinNavi yet.
  **/
  std::list<DBGEVT> dbgevts;

  /**
  * List of regular breakpoints that used to be set in the target process.
  * This is necessary because of certain race conditions during process
  * synchronization.
  **/
  std::set<CPUADDRESS> bpxlistrem;

  /**
  * List of echo breakpoints that used to be set in the target process.
  * This is necessary because of certain race conditions during process
  * synchronization.
  **/
  std::set<CPUADDRESS> ebpxlistrem;

  /**
  * List of stepping breakpoints that used to be set in the target process.
  * This is necessary because of certain race conditions during process
  * synchronization.
  **/
  std::set<CPUADDRESS> sbpxlistrem;

  /**
  * Contains the last few previously requested memory ranges. This is used
  * to automatically resend memory information during idle times.
  **/
  std::vector<std::pair<CPUADDRESS, CPUADDRESS> > cachedMemoryReads;

  /**
  * Index into the cachedMemoryReads vector that says what memory to
  * reload again on the next opportunity.
  **/
  unsigned int cachedIndex;

  /**
  * The currently hit breakpoint (one per thread)
  **/
  std::map<unsigned int, CPUADDRESS> currentBreakpoints;

  /**
  * Specifies how the debugger handles certain debugger events.
  **/
  DebuggerEventSettings debuggerEventSettings;

  // Processes setBreakpoint commands that arrived from BinNavi
  NaviError processSetBreakpoints(const Packet* p,
                                  InformationProvider& provider);

  // Processes setEchoBreakpoint commands that arrived from BinNavi
  NaviError processSetEchoBreakpoints(const Packet* p,
                                      InformationProvider& provider);

  // Processes setStepBreakpoint commands that arrived from BinNavi
  NaviError processSetSteppingBreakpoints(const Packet* p,
                                          InformationProvider& provider);

  // Processes removeBreakpoint commands that arrived from BinNavi
  NaviError processRemoveBreakpoints(const Packet* p,
                                     InformationProvider& provider);

  // Processes removeEchoBreakpoint commands that arrived from BinNavi
  NaviError processRemoveEchoBreakpoints(const Packet* p,
                                         InformationProvider& provider);

  // Processes removeSteppingBreakpoint commands that arrived from BinNavi
  NaviError processRemoveSteppingBreakpoint(const Packet* p,
                                            InformationProvider& provider);

  // Processes setBreakpointCondition commands that arrived from BinNavi
  NaviError processSetBreakpointCondition(const Packet* p);

  // Processes writeMemory commands that arrived from BinNavi
  NaviError processWriteMemory(const Packet* p);

  // Processes single step commands that arrived from BinNavi
  NaviError processSingleStep(const Packet* p, InformationProvider& provider);

  // Processes resume commands that arrived from BinNavi
  NaviError processResume(const Packet* p, InformationProvider& provider);

  // Processes halt commands that arrived from BinNavi
  NaviError processHalt(const Packet* p);

  // Processes detach commands that arrived from BinNavi
  NaviError processDetach(const Packet* p);

  // Processes terminate commands that arrived from BinNavi
  NaviError processTerminate(const Packet* p);

  // Processes registers commands that arrived from BinNavi
  NaviError processRegisters(const Packet* p, InformationProvider& provider);

  // Processes list processes commands that arrived from BinNavi
  NaviError processListProcesses(const Packet* p,
                                 InformationProvider& provider);

  // Processes Select Process commands that arrived from BinNavi
  NaviError processSelectProcess(const Packet* p);

  // Processes List Files commands that arrived from BinNavi
  NaviError processListFiles(const Packet* p, InformationProvider& provider);

  // Processes Select File commands that arrived from BinNavi
  NaviError processSelectFile(const Packet* p);

  // Processes set registers commands that arrived from BinNavi
  NaviError processSetRegister(const Packet* p);

  // Processes memory range commands that arrived from BinNavi
  NaviError processMemoryRange(const Packet* p, InformationProvider& provider);

  // Processes valid memory commands that arrived from BinNavi
  NaviError processValidMem(const Packet* p, InformationProvider& provider);

  // Processes memory map commands that arrived from BinNavi
  NaviError processMemmap(const Packet* p, InformationProvider& provider);

  // Processes search commands that arrived from BinNavi
  NaviError processSearch(const Packet* p, InformationProvider& provider);

  // Helper function to process remove breakpoint commands that arrived from
  // BinNavi
  NaviError processRemoveBreakpoints(
      const Packet* p, BPXType type,
      std::vector<std::pair<CPUADDRESS, unsigned int> >& result);

  // Processes suspendThread commands that arrived from BinNavi
  NaviError processSuspendThread(const Packet* p);

  // Processes resumeThread commands that arrived from BinNavi
  NaviError processResumeThread(const Packet* p);

  // Processes setActiveThread commands that arrived from BinNavi
  NaviError processSetActiveThread(const Packet* p);

  NaviError processSetExceptionSettings(const Packet* p);

  // Process set event settings command that arrived from BinNavi
  NaviError processSetDebuggerEventSettings(const Packet* p);

  // Sets a breakpoint in the target process
  NaviError setBreakpoint(CPUADDRESS address, const BPXType type,
                          bool moreToCome);

  // Removes a breakpoint from the internal lists of known breakpoints.
  void removeBreakpointFromList(CPUADDRESS addr, const BPXType type);

  // Handles breakpoint events when simple breakpoints were hit
  NaviError simpleBreakpointHit(const BREAKPOINT& bp, unsigned int tid,
                                bool correctPc);

  // Handles breakpoint events when echo breakpoints were hit
  NaviError echoBreakpointHit(const BREAKPOINT& bp, unsigned int tid,
                              bool correctPc = true, bool doResume = true);

  // Handles breakpoint events when stepping breakpoints were hit
  NaviError steppingBreakpointHit(const BREAKPOINT& bp, unsigned int tid,
                                  bool correctPc = true);

  // Removes all step breakpoints from the target process
  void clearSteppingBreakpoints();

  // Searches for a string in memory
  NaviError searchData(CPUADDRESS from, CPUADDRESS to, const char* searchString,
                       unsigned int stringLen, InformationProvider& provider);

  // Prevents assignment of base systems.
  BaseSystem& operator=(const BaseSystem&) { return *this; }

  std::string buildExceptionRaisedString(unsigned int threadId,
                                         CPUADDRESS address,
                                         CPUADDRESS exceptionCode) const;

  boost::filesystem::path normalize(const boost::filesystem::path& p) const;

  NaviError walkDirectory(const boost::filesystem::path& parentPath,
                          std::vector<boost::filesystem::path>& files,
                          std::vector<boost::filesystem::path>& dirs) const;

  // Removes all breakpoints in the given list which fall into the module.
  void pruneByModule(std::set<CPUADDRESS>& bplist, const Module& module);

  // Checks whether a breakpoint was hit in the given thread.
  bool hasCurrentBreakpoint(unsigned int threadId) const;

 protected:
  /**
  * List of echo breakpoints set in the target process.
  **/
  std::set<CPUADDRESS> ebpxlist;

  /**
  * List of stepping breakpoints set in the target process.
  **/
  std::set<CPUADDRESS> sbpxlist;

  /**
  * List of breakpoints set in the target process.
  **/
  std::set<CPUADDRESS> bpxlist;

  // Conditions for simple breakpoints.
  std::map<CPUADDRESS, ConditionNode*> conditions;

  // Map an exception code to the exception handling action which should be
  // carried out by the debugger
  std::map<CPUADDRESS, DebugExceptionHandlingAction> exceptionSettings;

  /**
  * Attaches to the target process.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError attachToProcess() = 0;

  /**
  * Starts a new process for debugging.
  *
  * @param path The path to the executable of the process.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError startProcess(
      const NATIVE_STRING path,
      const std::vector<const NATIVE_STRING>& commands) = 0;

  /**
  * Detaches from the target process.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError detach() = 0;

  /**
  * Terminates the target process.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError terminateProcess() = 0;

  /**
  * Stores the original data that is replaced by a given breakpoint.
  *
  * @param bp The breakpoint in question.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError storeOriginalData(const BREAKPOINT& bp) = 0;

  /**
  * Sets a breakpoint in the target process.
  *
  * @param breakpoint The breakpoint to be set.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError setBreakpoint(const BREAKPOINT& breakpoint,
                                  bool moreToCome = false) = 0;

  // Removes all breakpoints from the target process
  NaviError clearBreakpoints(const std::set<CPUADDRESS>& addresses,
                             BPXType type);

  /**
  * Removes a breakpoint from the target process.
  *
  * @param bp The breakpoint to be removed.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError removeBreakpoint(const BREAKPOINT& bp,
                                     bool moreToCome = false) = 0;

  /**
  * Executes a single instruction.
  *
  * @param tid The thread ID of the thread that executes the instruction.
  * @param address The address of the instruction pointer after the single
  * step.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError doSingleStep(unsigned int& tid, CPUADDRESS& address) = 0;

  /**
  * Resumes the debugged process.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError resumeProcess() = 0;

  /**
  * Suspends the thread with the given thread ID.
  *
  * @param tid The thread ID of the thread.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError suspendThread(unsigned int tid) = 0;

  virtual NaviError resumeThread(unsigned int tid) = 0;

  /**
  * Halts the target process.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError halt() = 0;

  /**
  * Retrieves the value of the instruction pointer in a given thread.
  *
  * @param tid The thread ID of the thread.
  * @param addr The variable where the value of the instruction pointer is
  * stored.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError getInstructionPointer(unsigned int tid,
                                          CPUADDRESS& addr) = 0;

  /**
  * Sets the instruction pointer in the target process to a new value.
  *
  * @param tid Thread ID of the target thread.
  * @param address The new value of the instruction pointer.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError setInstructionPointer(unsigned int tid,
                                          CPUADDRESS address) = 0;

  /**
  * Fills a given register container structure with information about the
  * current values of the CPU registers.
  *
  * @param registers The register information structure.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError readRegisters(RegisterContainer& registers) = 0;

  /**
  * Updates the value of a given register in a given thread.
  *
  * @param tid The thread ID of the thread.
  * @param index The index of the register.
  * @param value The new value of the register.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError setRegister(unsigned int tid, unsigned int index,
                                CPUADDRESS value) = 0;

  /**
  * Given a start address, this function returns the first and last offset of
the
  * memory region the start address belongs to.
  *
  * @param start The start address.
  * @param from The first offset of the memory region.
  * @param to The last offset of the memory region.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError getValidMemory(CPUADDRESS start, CPUADDRESS& from,
                                   CPUADDRESS& to) = 0;

  /**
  * Returns a list of all memory regions that are available in the target
process.
  *
  * @param addresses The memory map is written into this list.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError getMemmap(std::vector<CPUADDRESS>& addresses) = 0;

  /**
  * Fills a buffer with memory data from the current process.
  *
  * @param buffer The buffer to fill.
  * @param address The address from where the memory is read.
  * @param size Number of bytes to read.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError readMemoryData(char* buffer, CPUADDRESS address,
                                   CPUADDRESS size) = 0;

  /**
  * Overwrites the target process memory at the given address with the given
data.
  *
  * @param address The start address of the memory write operation.
  * @param data The data to write to the target process memory.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError writeMemory(CPUADDRESS address,
                                const std::vector<char>& data) = 0;

  /**
  * Fills the process list argument with a list of currently running processes
  * on the target system.
  *
  * @param processList The process list filled by the function.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError readProcessList(ProcessListContainer& processList) = 0;

  /**
  * Fills the file list argument with file information about the root directory
  * of the target system (or the system drive on Windows).
  *
  * @param fileList The file list filled by the function.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  NaviError readFiles(FileListContainer& fileList) const;

  /**
  * Fill the given vector with the roots of all available file systems
  * (e.g. drives on Windows).
  **/
  virtual NaviError getFileSystems(
      std::vector<boost::filesystem::path>& roots) const = 0;

  /**
  * Retrieves the (system dependent) system root or system drive.
  **/
  virtual NaviError getSystemRoot(boost::filesystem::path& root) const = 0;

  /**
  * Fills the file list argument with file information about the given
  * directory of the target system.
  *
  * @param fileList The file list filled by the function.
  * @param path The path for which file information is generated.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  NaviError readFiles(FileListContainer& fileList,
                      const boost::filesystem::path& path) const;

  // Determines whether a breakpoint of a given type is set at a given address.
  bool hasBreakpoint(CPUADDRESS address, const BPXType type) const;

  // Returns the breakpoint of a given type that is set at a given address,
  BREAKPOINT getBreakpoint(CPUADDRESS addr, const BPXType type) const;

  std::vector<BREAKPOINT> getBreakpoints(CPUADDRESS address) const;

  // Event handler that is used to handle breakpoint hits.
  NaviError breakpointHit(CPUADDRESS addr, unsigned int tid,
                          bool resume_on_echo);

  // Deprecated
  NaviError breakpointHit(const std::string& addressString, unsigned int tid);

  // Resumes the target process
  NaviError resume(unsigned int tid);

  // Event handler that is used to handle the creation of new threads
  NaviError threadCreated(unsigned int tid, ThreadState state);

  // Event handler that is used to handle terminating threads
  NaviError threadExit(unsigned int tid);

  // Event handler that is used to signal newly loaded modules.
  NaviError moduleLoaded(const Module& module, unsigned int threadId);

  // Event handler that is used to signal unloaded modules.
  NaviError moduleUnloaded(const Module& module);

  // Event handler that is used to handle terminating target processes
  void processExit();

  // Event handler that is used to handle exceptions in the target process
  NaviError exceptionRaised(unsigned int tid, CPUADDRESS address,
                            CPUADDRESS exc_code);

  // Sets behaviour for given exception (e.g. whether we should pause debugging
  // or not)
  virtual NaviError SetExceptionAction(CPUADDRESS exc_code,
                                       DebugExceptionHandlingAction action);

  virtual DebugExceptionHandlingAction GetExceptionAction(
      CPUADDRESS exc_code) const;

  // Adds a debug event to the list of debug events to be sent to BinNavi
  void addDebugEvent(const DBGEVT& evt);

  // Sets the process ID of the target process
  void setPID(unsigned int pid) { this->pid = pid; }

  /**
  * Changes the thread that is receiving input.
  *
  * @param activeThread The new active thread.
  **/
  void setActiveThread(unsigned int activeThread) {
    this->activeThread = activeThread;
  }

  virtual DebugExceptionContainer getPlatformExceptions() const = 0;

  std::string getExceptionName(CPUADDRESS exceptionCode) const;

  // Returns the settings which control the debugger behavior of the debugger
  // when certain debug events occur (e.g. break on dll)
  DebuggerEventSettings getDebuggerEventSettings() const {
    return debuggerEventSettings;
  }

  // Returns the path of the debuggee
  boost::filesystem::path getTargetApplicationPath() const { return path; }

  // Must be called when the target process has been started but is not yet
  // running
  NaviError processStart(const Module& module, const Thread& thread);

  // Update the breakpoint lists after a module has been unloaded.
  void pruneBreakpointsByModule(const Module& unloadedModule);

  // Resume the process after stepping away from a breakpoint.
  virtual NaviError resumeAfterStepping(unsigned int threadId,
                                        CPUADDRESS address);

 public:
  unsigned int getActiveThread() const { return activeThread; }

  /**
  * Finds out whether debug events occurred in the target process.
  *
  * @return A NaviError code that describes whether the operation was
  * successful or not.
  **/
  virtual NaviError readDebugEvents() = 0;

  /**
   * Returns a list of the names of the registers of the underlying platform.
   *
   * @return A list of register names.
   **/
  virtual std::vector<RegisterDescription> getRegisterNames() const = 0;

  /**
  * Returns the maximum size of a memory address of the target machine.
  *
  * @return The maximum size of a memory address of the target machine.
  **/
  virtual unsigned int getAddressSize() const = 0;

  /**
  * Returns the debugger options that are supported by the debug client.
  *
  * @return The debugger options that are supported by the debug client.
  **/
  virtual DebuggerOptions getDebuggerOptions() const = 0;

  BaseSystem() : pid(0), cachedIndex(0) {}

  /**
  * Creates a new BaseSystem object.
  *
  * @param pid The process ID of the target process.
  **/
  BaseSystem(unsigned int pid) : pid(pid), cachedIndex(0) {}

  /**
  * Creates a new BaseSystem object.
  *
  * @param path The path to the target executable.
  **/
  BaseSystem(const NATIVE_STRING path,
             const std::vector<const NATIVE_STRING>& commands)
      : pid(0),
        path(boost::filesystem::path(path).make_preferred()),
        commands(commands),
        cachedIndex(0) {}

  virtual ~BaseSystem() {}

  // Starts debugging the target process
  NaviError start();

  // Processes an incoming packet from BinNavi
  NaviError processPacket(const Packet* p, InformationProvider& provider);

  // Determines whether a debug event is ready to be processed.
  bool isDebugEventAvailable() const;

  // Returns the next unprocessed debug event.
  unsigned int getDebugEvent(DBGEVT* event) const;

  // Removes the next unprocessed debug event.
  unsigned int popDebugEvent();

  // Reloads memory data.
  NaviError reloadMemory(Packet* p, InformationProvider& provider);

  /**
   * Checks whether the debugger is attached to either a file or a process.
   *
   * @return True, if the debugger is attached. False, otherwise.
   **/
  bool hasTarget() const { return !path.empty() || pid; }

  /**
   * Returns the process ID of the target process.
   *
   * @return The process ID of the target process.
   **/
  unsigned int getPID() const { return pid; }

  /**
   * Fills the given vector with success error codes for each breakpoint
   * address. Used by the gdb agent since echo breakpoints are not re-added
   * after they were hit and thus do not need to be removed later on.
   **/
  NaviError processFakeRemoveBreakpoints(
      const Packet* p,
      std::vector<std::pair<CPUADDRESS, unsigned int> >& result);
};

#endif
