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

#include <sys/stat.h>

#include <cstdlib>
#include <iostream>

#include <zylog/src/logger.h>
#include <zylog/src/consoletarget.h>
#include <zylog/src/filetarget.h>
#include <zyline/src/zyline.h>
#include <zycon/src/zycon.h>

#include "../includer.hpp"
#include "../logger.hpp"
#include "../DebugClient.hpp"
#include "WinDynamoRioSystem.hpp"

// !!!
// The whole code here is crappy and will be rewritten with gflags soon
// !!!

std::string client_dll_path;
std::string drrun_path;

zylib::zylog::Logger* msglog;

void PrintStartMessage() {
  std::cout << "BinNavi DynamoRIO debug client for Win32 applications"
            << std::endl;
  std::cout << "Build date: " << __TIME__ << " " << __DATE__ << std::endl;
  std::cout << std::endl;
}

void usage() {
  std::cout << "=============================================================================\n"
               " TODO \n"
               "=============================================================================\n"
            << std::endl;
  exit(1);
}

void InitLogger(const std::string& logfile, unsigned int level) {
  // TODO(jannewger): Delete this later
  msglog = new zylib::zylog::Logger(level);

  // TODO: Delete this later
  zylib::zylog::ConsoleTarget* t1 = new zylib::zylog::ConsoleTarget;
  msglog->addTarget(t1);

  if (logfile != "") {
    // TODO: Delete this later
    zylib::zylog::FileTarget* t2 = new zylib::zylog::FileTarget(logfile);
    msglog->addTarget(t2);
  }
}

void HandleCommandLine(int argc, const char* argv[], std::string& target,
                       unsigned int& port, bool& v, bool& vv, std::string& lf,
                       std::vector<const char*>& commandline) {
  bool targetFound = false;

  for (int i = 1; i < argc; i++) {
    if (targetFound) {
      commandline.push_back(argv[i]);
      continue;
    }
    if (!strcmp(argv[i], "-h")) {
      std::cout << "Command line help" << std::endl;
      usage();
    }
    if (!strcmp(argv[i], "-v")) {
      v = true;
    } else if (!strcmp(argv[i], "-vv")) {
      vv = true;
    } else if (!strcmp(argv[i], "-p")) {
      if (i == argc - 1) {
        std::cout << "Error: Missing port number" << std::endl;
        usage();
      } else if (!zylib::zycon::isPositiveNumber(argv[i + 1])) {
        std::cout << "Error: Invalid server port specified" << std::endl;
        usage();
      }
      port = zylib::zycon::parseString<unsigned int>(argv[i + 1]);
      i++;
    } else if (!strcmp(argv[i], "--client")) {
      if (i == argc - 1) {
        std::cout << "Error: --client: Missing client dll path" << std::endl;
        usage();
      }
      client_dll_path = argv[i + 1];
      i++;
    } else if (!strcmp(argv[i], "--drrun")) {
      if (i == argc - 1) {
        std::cout << "Error: --drrun: Missing drrun.exe path" << std::endl;
        usage();
      }
      drrun_path = argv[i + 1];
      i++;
    } else if (!strcmp(argv[i], "-lf")) {
      if (i == argc - 1) {
        std::cout << "Error: Missing log file name" << std::endl;
        usage();
      }
      lf = argv[i + 1];
      i++;
    } else if (argv[i][0] != '-') {
      target = argv[i];
      targetFound = true;
    }
  }

  if (v && vv) {
    std::cout
        << "Error: Verbose mode and Very Verbose mode are mutually exclusive"
        << std::endl;
    usage();
  }

  if (client_dll_path == "" || drrun_path == "") {
    std::cout << "Error: Please specify both client dll path and drrun.exe path"
              << std::endl;
    usage();
  }
}

bool FileExists(const char* filename) {
  struct stat results;

  if (stat(filename, &results)) {
    return false;
  }

  // Do not use the macro; looks like it is not in VC++
  return (results.st_mode & S_IFMT) == S_IFREG;
}

int main(int argc, const char* argv[]) {
  PrintStartMessage();

  std::string target;

  unsigned int port = 2222;
  bool verbose = false;
  bool vverbose = false;
  std::string lf = "";
  std::vector<const char*> commands;

  HandleCommandLine(argc, argv, target, port, verbose, vverbose, lf, commands);

  unsigned int loglevel = LOG_ALWAYS;

  if (verbose)
    loglevel = LOG_VERBOSE;
  else if (vverbose)
    loglevel = LOG_ALL;

  InitLogger(lf, loglevel);

  msglog->log(LOG_ALWAYS,
              "---------------------------------------------------------");
  msglog->log(LOG_ALWAYS, "Starting new Debugging session");
  msglog->log(LOG_ALWAYS, "Server Port %d", port);
  if (verbose) msglog->log(LOG_ALWAYS, "Verbose mode: ON");
  if (vverbose) msglog->log(LOG_ALWAYS, "Very Verbose mode: ON");
  if (lf != "") {
    msglog->log(LOG_ALWAYS, "Logging to file: %s", lf.c_str());
  }

  for (;;) {
    DebugClient* debugClient;

    if (target == "") {
      msglog->log(LOG_ALWAYS,
                  "No target specified. Target will be chosen later.");

      debugClient =
          new DebugClient(new CONNECTION_POLICY(port),
                          new WinDynamoRioSystem(client_dll_path, drrun_path));
    } else {
      msglog->log(LOG_ALWAYS, "Target executable: %s", target.c_str());

      if (!FileExists(target.c_str())) {
        msglog->log(LOG_ALWAYS, "Error: Target executable '%s' does not exist",
                    target.c_str());

        return 1;
      }

      // TODO: Delete this later
      debugClient =
          new DebugClient(new CONNECTION_POLICY(port),
                          new WinDynamoRioSystem(client_dll_path, drrun_path,
                                                 target.c_str(), commands));
    }

    std::string arguments = "";

    if (commands.size() == 0) {
      arguments = "-";
    }

    for (const char* cmd : commands) {
      arguments += cmd;
      arguments += " ";
    }

    msglog->log(LOG_ALWAYS, "Commandline arguments: %s", arguments.c_str());
    msglog->log(LOG_ALWAYS,
                "---------------------------------------------------------");
    msglog->log(LOG_ALWAYS, "Waiting for connection from BinNavi...");

    unsigned int init = debugClient->initializeConnection();

    if (init) {
      msglog->log(LOG_ALWAYS,
                  "Error: Couldn't initialize connection (Code: %d)", init);
      return 1;
    }

    unsigned int connect_res = debugClient->waitForConnection();

    if (connect_res) {
      msglog->log(LOG_ALWAYS, "Error: Didn't receive a connection (Code %d)",
                  connect_res);
      return 1;
    }

    std::cout << "BinNavi has connected!" << std::endl;

    debugClient->requestTarget();

    unsigned int procp = debugClient->processPackets();

    if (procp) {
      msglog->log(LOG_ALWAYS,
                  "Error: Error during packet processing. (Code %d)", procp);
    }

    debugClient->closeConnection();

    delete debugClient;
  }
}
