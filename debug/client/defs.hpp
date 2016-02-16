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

#ifndef DEFS_H
#define DEFS_H

#define BOOST_FILESYSTEM_NO_DEPRECATED
#include <boost/filesystem.hpp>

#include <functional>
#include <vector>
#include <utility>
#include <string>
#include <climits>
#include <cstdio>
#include <cstring>

#include <zylog/src/logger.h>
#include <zylog/src/consoletarget.h>
#include <zycon/src/bytebuffer.h>
#include <zycon/src/zycon.h>

#include "commands.hpp"
#include "errors.hpp"
#include "logger.hpp"

// Use this mask to format addresses in the code
extern const char* ADDRESS_FORMAT_MASK;

// Forward declarations
struct RegisterDescription;
struct DBG_PROTO_ARG_ADDRESS;
class RegisterValue;
class ProcessDescription;

typedef std::vector<char> MemoryContainer;
typedef std::vector<ProcessDescription> ProcessListContainer;

typedef zylib::zycon::ByteBuffer PacketBuffer;

#if ARCHSIZE == 32
#if UINT_MAX == 4294967295UL

// A 32bit type for 32bit platforms
typedef unsigned int CPUADDRESS;

#else
#error You need to change some typedefs. Please read the documentation.
#endif
#elif ARCHSIZE == 64
#if ULLONG_MAX == 18446744073709551615UL

// A 64bit type for 64bit platforms
typedef unsigned long long CPUADDRESS;

#else
#error You need to change some typedefs. Please read the documentation.
#endif
#else
#error Unknown architecture size
#endif

#ifdef UNICODE
// A string type for Unicode platforms
#define NATIVE_STRING wchar_t *
#else
// A string type for ASCII platforms
#define NATIVE_STRING char *
#endif

// Converts a CPUADDRESS to a packet address value
DBG_PROTO_ARG_ADDRESS catopa(const CPUADDRESS& address);

// Converts a packet address value to a CPUADDRESS
CPUADDRESS patoca(const DBG_PROTO_ARG_ADDRESS& address);

// Helper function that creates a RegisterValue object
RegisterValue makeRegisterValue(const std::string& name,
                                const std::string& value,
                                const std::vector<char>& memory, bool isPc =
                                    false,
                                bool isSp = false);

// Helper function that creates a RegisterValue object
RegisterValue makeRegisterValue(const std::string& name,
                                const std::string& value, bool isPc = false,
                                bool isSp = false);

// Helper function that creates a RegisterDescription object
RegisterDescription makeRegisterDescription(const std::string& name,
                                            unsigned int size, bool editable);

char* duplicate(const char* str);

wchar_t* duplicate(const wchar_t* str);

// Identifies the types of debug packet arguments
enum argtype_t {
  // Identifies an address argument
  arg_address,

  // Identifies an integer argument
  arg_value,

  // Identifies a byte array argument
  arg_data_buf,

  // Identifies a long integer argument
  arg_long
};

// Identifies the type of a breakpoint
enum BPXType {
  // Simple/regular breakpoint
  BPX_simple,

  // Echo breakpoint
  BPX_echo,

  // Stepping breakpoint
  BPX_stepping
};

// Identifies the state of a thread
enum ThreadState {
  // Thread is running
  RUNNING = 0,

  // Thread is suspended
  SUSPENDED = 1
};

// Identifies debug events that happened in the target process
enum dbgevt_t {
  // Simple breakpoint was hit
  dbgevt_bp_hit,

  // Echo breakpoint was hit
  dbgevt_bpe_hit,

  // Stepping breakpoint was hit
  dbgevt_bps_hit,

  dbgevt_bpe_rem,

  // A new thread was created in the target process
  dbgevt_thread_created,

  // An existing thread was closed in the target process
  dbgevt_thread_closed,

  dbgevt_module_loaded,

  dbgevt_module_unloaded,

  // The target process shut down
  dbgevt_process_closed,

  // An exception happened in the target process
  dbgevt_exception,

  // The process was just started by the debugger but is not running yet
  dbgevt_process_start
};

extern const char* debugEventToString(dbgevt_t event);

/**
 * Address structure that is used to send/receive
 * address information to/from BinNavi.
 */
struct DBG_PROTO_ARG_ADDRESS {
  // High 32 bits of the address
  unsigned int high32bits;

  // Low 32 bits of the address
  unsigned int low32bits;
};

/**
 * Describes the value of a register of the target process.
 */
struct RegisterInformation {
  // The name of the register
  std::string name;

  // The value of the register
  std::string value;
};

/**
 * Describes the layout of a register in the target process.
 */
struct RegisterDescription {
  // The name of the register
  std::string name;

  // The size of the register
  unsigned int size;

  // Flag that determines whether the register is editable or not
  bool editable;

  /**
   * Creates a new RegisterDescription object
   *
   * @param name The name of the register
   * @param size The size of the register
   * @param editable Flag that determines whether the register is editable or not
   */
  RegisterDescription(const std::string& name, unsigned int size, bool editable)
      : name(name),
        size(size),
        editable(editable) {
  }
};

/**
 * Describes a thread of the target process
 */
struct Thread {
  //  The thread ID of the thread
  unsigned int tid;

  // The state of the thread
  ThreadState state;

  std::vector<RegisterValue> registers;

  /**
   * Creates a new Thread object
   *
   * @param tid The thread ID of the thread
   * @param state The state of the thread
   */
  Thread(unsigned int tid, ThreadState state)
      : tid(tid),
        state(state) {
  }

  bool operator==(const Thread& rhs) const {
    return tid == rhs.tid;
  }

  bool operator<(const Thread& rhs) const {
    return tid < rhs.tid;
  }

  /**
   * Returns a function which matches the thread with a given id
   * Intended for use with STL functions, e.g. std::remove_if
   */
  typedef std::function<bool(const Thread&)> ThreadComparator;

  static ThreadComparator MakeThreadIdComparator(int tid) {
    return [tid](const Thread& t)->bool {return t.tid == tid; };
  }

};

struct Module {
  std::string name;

  std::string path;

  CPUADDRESS baseAddress;

  CPUADDRESS size;

  Module() : name(""), path(""), baseAddress(0), size(0) {
  }

  Module(const std::string& name, const std::string& path,
         CPUADDRESS baseAddress, CPUADDRESS size)
      : name(name),
        path(path),
        baseAddress(baseAddress),
        size(size) {
  }
};

bool operator==(const Module& lhs, const Module& rhs);
bool operator<(const Module& lhs, const Module& rhs);

/**
 * Describes a packet header that is used by all packets
 * that are sent between BinNavi and the debug client.
 */
struct RDBG_PROTO_HDR {
  // Identifies the type of the debug command
  commandtype_t command;

  // Unique command ID that identifies the command
  unsigned int id;

  // Number of arguments in the packet
  unsigned int argument_num;

  /**
   * Creates a new packet header object with uninitialized values.
   **/
  RDBG_PROTO_HDR() {
  }

  /**
   * Creates a new packet header object.
   *
   * @param command The type of the debug command
   * @param id Unique command ID that identifies the command
   * @param argument_num Number of arguments in the packet
   */
  RDBG_PROTO_HDR(commandtype_t command, unsigned int id,
                 unsigned int argument_num)
      : command(command),
        id(id),
        argument_num(argument_num) {
  }
};

/**
 * Describes an argument header of a single argument that is sent
 * in a debug command header between BinNavi and the debug client.
 */
struct DBG_PROTO_ARG {
  /**
   * Length of the argument that follows
   */
  unsigned int length;

  /**
   * Type of the argument that follows
   */
  argtype_t type;

  /**
   * Creates a new argument header object with uninitialized values.
   */
  DBG_PROTO_ARG() {
  }

  /**
   * Creates a new argument header object.
   *
   * @param length The length of the argument that follows
   * @param type The type of the argument that follows
   */
  DBG_PROTO_ARG(unsigned int length, argtype_t type)
      : length(length),
        type(type) {
  }
};

class ConditionNode;

/**
 * Describes a breakpoint that is set in the target process.
 */
struct BREAKPOINT {
  BPXType bpx_type;
  CPUADDRESS addr;
  BREAKPOINT() {}
  BREAKPOINT(BPXType bpx_type, CPUADDRESS addr)
      : bpx_type(bpx_type),
        addr(addr) {
  }
};

/**
 * Describes a packet that is sent between BinNavi and the debug client-
 */
struct Packet {
  // The packet header
  RDBG_PROTO_HDR hdr;

  // The address arguments extracted from the packet
  std::vector<CPUADDRESS> addresses;

  // The integer arguments extracted from the packet
  std::vector<unsigned int> ints;

  // The data arguments extracted from the packet
  std::vector<char> data;

  /**
   * Creates a new packet object
   */
  Packet() {
    memset(&hdr, sizeof(RDBG_PROTO_HDR), 0);
  }
};

/**
 * Describes a debug event that happened in the target process
 */
struct DBGEVT {
  // Type of the debug event
  dbgevt_t type;

  // If the debug event was a breakpoint event, the breakpoint is described
  //here.
  BREAKPOINT bp;

  // Thread ID of the thread where the exception happened
  unsigned int tid;

  // Event specific extra information; might also be platform specific (e.g. on
  // X64 exception codes might be 64 bit)
  CPUADDRESS extra;

  // Address where the exception happened
  CPUADDRESS address;

  // Register string to be sent to BinNavi
  std::string registerString;
};

class ProcessDescription {
 private:
  unsigned long pid;

  std::string name;

 public:
  ProcessDescription(unsigned long pid, const std::string& name)
      : pid(pid),
        name(name) {
  }

  unsigned long getPid() const {
    return pid;
  }

  std::string getName() const {
    return name;
  }
};

class FileListContainer {
 private:

  boost::filesystem::path directory;

  std::vector<boost::filesystem::path> drives;

  std::vector<boost::filesystem::path> directories;

  std::vector<boost::filesystem::path> files;

 public:

  boost::filesystem::path getDirectory() const {
    return directory;
  }

  void setDirectory(const boost::filesystem::path& directory) {
    this->directory = directory;
  }

  std::vector<boost::filesystem::path> getDrives() const {
    return drives;
  }

  void setDrives(const std::vector<boost::filesystem::path>& drives) {
    this->drives = drives;
  }

  std::vector<boost::filesystem::path> getDirectories() const {
    return directories;
  }

  void setDirectories(const std::vector<boost::filesystem::path>& directories) {
    this->directories = directories;
  }

  std::vector<boost::filesystem::path> getFiles() const {
    return files;
  }

  void setFiles(const std::vector<boost::filesystem::path>& files) {
    this->files = files;
  }
};

/**
 * Describes a register and its value in the target process.
 */
class RegisterValue {
 private:
  // Name of the register
  std::string name;

  // Value of the register
  std::string value;

  std::vector<char> memory;

  // Flag that signals whether the register is the PC register of the target
  //platform
  bool isPc_;

  // Flag that signals whether the register is the stack pointer register of
  //the target platform
  bool isSp_;

 public:
  /**
   * Creates a new register value object.
   *
   * @param name Name of the register
   * @param value Value of the register
   * @param isPC Flag that signals whether the register is the PC register
   * of the target platform
   * @param isSP Flag that signals whether the register is the stack pointer
   * register of the target platform
   */
  RegisterValue(const std::string& name, const std::string& value,
                const std::vector<char>& memory, bool isPc = false, bool isSp =
                    false)
      : name(name),
        value(value),
        memory(memory),
        isPc_(isPc),
        isSp_(isSp) {
  }

  /**
   * Returns the name of the register.
   *
   * @return The name of the register.
   */
  std::string getName() const {
    return name;
  }

  /**
   * Returns the value of the register.
   *
   * @return The value of the register.
   */
  std::string getValue() const {
    return value;
  }

  /**
   * Returns the memory at the location the register is pointing to.
   *
   * @return The pointed memory values.
   */
  std::vector<char> getMemory() const {
    return memory;
  }

  /**
   * Signals whether the register is the PC register of the target platform.
   *
   * @return True, if the register is the PC register. False, otherwise.
   */
  bool isPc() const {
    return isPc_;
  }

  /**
   * Signals whether the register is the stack pointer register of the target
   * platform.
   *
   * @return True, if the register is the stack pointer register.
   */
  bool isSp() const {
    return isSp_;
  }
};

class RegisterContainer {
 private:
  std::vector<Thread> threads;

 public:
  void addThread(const Thread& thread) {
    threads.push_back(thread);
  }

  std::vector<Thread> getThreads() const {
    return threads;
  }
};

class IConditionProvider {
 public:
  virtual NaviError readMemoryData(char* buffer, CPUADDRESS address,
                                   CPUADDRESS size) = 0;
};

class ConditionNode {
 private:
  std::vector<ConditionNode*> children;

 public:
  void addChild(ConditionNode* child) {
    children.push_back(child);
  }

  std::vector<ConditionNode*> getChildren() const {
    return children;
  }

  virtual unsigned int evaluate(unsigned int tid,
                                const RegisterContainer& registers,
                                IConditionProvider* system) const = 0;
};

class ExpressionNode : public ConditionNode {
 private:
  std::string op;

 public:
  ExpressionNode(const std::string& op)
      : op(op) {
  }

  unsigned int evaluate(unsigned int tid, const RegisterContainer& registers,
                        IConditionProvider* system) const {


    std::vector<ConditionNode*> children = getChildren();

    bool result = op == "&&";

    for (std::vector<ConditionNode*>::iterator Iter = children.begin();
        Iter != children.end(); ++Iter) {
      result =
          op == "&&" ?
              result && (*Iter)->evaluate(tid, registers, system) :
              result || (*Iter)->evaluate(tid, registers, system);
    }

    return result;
  }
};

class FormulaNode : public ConditionNode {
 private:
  std::string op;

 public:
  FormulaNode(const std::string& op)
      : op(op) {
  }

  unsigned int evaluate(unsigned int tid, const RegisterContainer& registers,
                        IConditionProvider* system) const {


    std::vector<ConditionNode*> children = getChildren();

    unsigned int result = 0;

    for (std::vector<ConditionNode*>::iterator Iter = children.begin();
        Iter != children.end(); ++Iter) {
      if (Iter == children.begin()) {
        result = (*Iter)->evaluate(tid, registers, system);
      } else {
        if (op == "+") {
          result += (*Iter)->evaluate(tid, registers, system);
        } else if (op == "-") {
          result -= (*Iter)->evaluate(tid, registers, system);
        } else if (op == "*") {
          result *= (*Iter)->evaluate(tid, registers, system);
        } else if (op == "/") {
          result /= (*Iter)->evaluate(tid, registers, system);
        } else if (op == "%") {
          result %= (*Iter)->evaluate(tid, registers, system);
        } else if (op == "<<") {
          result <<= (*Iter)->evaluate(tid, registers, system);
        } else if (op == ">>") {
          result >>= (*Iter)->evaluate(tid, registers, system);
        } else if (op == "&") {
          result &= (*Iter)->evaluate(tid, registers, system);
        } else if (op == "|") {
          result |= (*Iter)->evaluate(tid, registers, system);
        } else if (op == "^") {
          result ^= (*Iter)->evaluate(tid, registers, system);
        }
      }
    }

    return result;
  }
};

class IdentifierNode : public ConditionNode {
 private:
  std::string identifier;

 public:
  IdentifierNode(const std::string& identifier)
      : identifier(identifier) {
  }

  unsigned int evaluate(unsigned int tid, const RegisterContainer& registers,
                        IConditionProvider*) const {


    if (zylib::zycon::toLower(identifier) == "tid") {
      return tid;
    } else {
      std::vector<Thread> threads = registers.getThreads();

      for (std::vector<Thread>::iterator Iter = threads.begin();
          Iter != threads.end(); ++Iter) {
        Thread t = *Iter;

        if (t.tid == tid) {
          for (std::vector<RegisterValue>::iterator rIter = t.registers.begin();
              rIter != t.registers.end(); ++rIter) {
            if (zylib::zycon::toLower(rIter->getName())
                == zylib::zycon::toLower(identifier)) {
              return zylib::zycon::parseHexString<unsigned int>(
                  rIter->getValue());
            }
          }
        }
      }

      return 0xDEADBEEA;
    }
  }
};

class MemoryNode : public ConditionNode {
 public:
  unsigned int evaluate(unsigned int tid, const RegisterContainer& registers,
                        IConditionProvider* system) const {


    unsigned int address = getChildren()[0]->evaluate(tid, registers, system);

    char buffer[4];

    if (system->readMemoryData((char*) &buffer[0], (CPUADDRESS) address,
                               (CPUADDRESS) 4)) {
      return 0xDEADBEEA;
    } else {
      return *(unsigned int*) &buffer[0];
    }
  }
};

class NumberNode : public ConditionNode {
 private:
  unsigned int value;

 public:
  NumberNode(unsigned int value)
      : value(value) {
  }

  unsigned int evaluate(unsigned int, const RegisterContainer&,
                        IConditionProvider*) const {


    return value;
  }
};

class RelationNode : public ConditionNode {
 private:
  std::string op;

 public:
  RelationNode(const std::string& op)
      : op(op) {
  }

  unsigned int evaluate(unsigned int tid, const RegisterContainer& registers,
                        IConditionProvider* system) const {


    std::vector<ConditionNode*> children = getChildren();

    if (op == "==") {
      return children[0]->evaluate(tid, registers, system)
          == children[1]->evaluate(tid, registers, system);
    } else if (op == "!=" || op == "<>") {
      return children[0]->evaluate(tid, registers, system)
          != children[1]->evaluate(tid, registers, system);
    } else if (op == "<") {
      return children[0]->evaluate(tid, registers, system)
          < children[1]->evaluate(tid, registers, system);
    } else if (op == ">") {
      return children[0]->evaluate(tid, registers, system)
          > children[1]->evaluate(tid, registers, system);
    } else if (op == "<=") {
      return children[0]->evaluate(tid, registers, system)
          <= children[1]->evaluate(tid, registers, system);
    } else if (op == ">=") {
      return children[0]->evaluate(tid, registers, system)
          >= children[1]->evaluate(tid, registers, system);
    } else {
      return false;
    }
  }
};

class SubNode : public ConditionNode {
 public:
  unsigned int evaluate(unsigned int tid, const RegisterContainer& registers,
                        IConditionProvider* system) const {


    return getChildren()[0]->evaluate(tid, registers, system);
  }
};

#endif
