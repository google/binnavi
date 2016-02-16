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

#include <iostream>
#include <cstdlib>
#include <sys/stat.h>
#include <memory>

#include <zylog/src/logger.h>
#include <zylog/src/consoletarget.h>
#include <zylog/src/filetarget.h>
#include <zyline/src/zyline.h>
#include <zycon/src/zycon.h>

zylib::zylog::Logger* msglog;

#include "includer.hpp"
#include "logger.hpp"
#include "DebugClient.hpp"

void initLogger(const std::string& logfile, unsigned int level) {
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

void handleCommandLine(int argc, const char* argv[], std::string& target,
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
      std::cout << szUsage << std::endl;
      std::exit(0);
    }
    if (!strcmp(argv[i], "-v")) {
      v = true;
    } else if (!strcmp(argv[i], "-vv")) {
      vv = true;
    } else if (!strcmp(argv[i], "-p")) {
      if (i == argc - 1) {
        std::cout << "Error: Missing port number" << std::endl;
        std::cout << szUsage << std::endl;
        std::exit(1);
      } else if (!zylib::zycon::isPositiveNumber(argv[i + 1])) {
        std::cout << "Error: Invalid server port specified" << std::endl;
        std::cout << szUsage << std::endl;
        std::exit(1);
      }

      port = zylib::zycon::parseString<unsigned int>(argv[i + 1]);

      i++;
    } else if (!strcmp(argv[i], "-lf")) {
      if (i == argc - 1) {
        std::cout << "Error: Missing log file name" << std::endl;
        std::cout << szUsage << std::endl;
        std::exit(1);
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
    std::cout << szUsage << std::endl;
    std::exit(1);
  }
}

bool fexists(const char* filename) {
  struct stat results;

  if (stat(filename, &results)) {
    return false;
  }

  // Do not use the macro; looks like it is not in VC++
  return (results.st_mode & S_IFMT) == S_IFREG;
}

int main(int argc, const char* argv[]) {
  printStartMessage();

  std::string target;

  unsigned int port = 2222;
  bool verbose = false;
  bool vverbose = false;
  std::string lf = "";
  std::vector<const char*> commands;

  handleCommandLine(argc, argv, target, port, verbose, vverbose, lf, commands);

  unsigned int loglevel = LOG_ALWAYS;

  if (verbose) {
    loglevel = LOG_VERBOSE;
  } else if (vverbose) {
    loglevel = LOG_ALL;
  }

  initLogger(lf, loglevel);

  msglog->log(LOG_ALWAYS,
              "---------------------------------------------------------");
  msglog->log(LOG_ALWAYS, "Starting new Debugging session");
  msglog->log(LOG_ALWAYS, "Server Port %d", port);

  if (verbose) {
    msglog->log(LOG_ALWAYS, "Verbose mode: ON");
  }

  if (vverbose) {
    msglog->log(LOG_ALWAYS, "Very Verbose mode: ON");
  }

  if (lf != "") {
    msglog->log(LOG_ALWAYS, "Logging to file: %s", lf.c_str());
  }

  for (;;) {
    std::unique_ptr<DebugClient> debugClient;

    if (target == "") {
      msglog->log(LOG_ALWAYS,
                  "No target specified. Target will be chosen later.");

      debugClient = std::unique_ptr<DebugClient>(
          new DebugClient(new CONNECTION_POLICY(port), new SYSTEM_POLICY()));

    } else if (zylib::zycon::isPositiveNumber(target)) {
      msglog->log(LOG_ALWAYS, "Target PID: %s", target.c_str());

      unsigned int processId = zylib::zycon::parseString<unsigned int>(target);

      debugClient = std::unique_ptr<DebugClient>(
          new DebugClient(new CONNECTION_POLICY(port),
          new SYSTEM_POLICY(processId)));
    } else {
      msglog->log(LOG_ALWAYS, "Target executable: %s", target.c_str());

      if (!fexists(target.c_str())) {
        msglog->log(LOG_ALWAYS, "Error: Target executable '%s' does not exist",
                    target.c_str());

        return 1;
      }

      debugClient = std::unique_ptr<DebugClient>(new DebugClient(
          new CONNECTION_POLICY(port),
          new SYSTEM_POLICY(target.c_str(), commands)));
    }

    std::string arguments = "";

    if (commands.size() == 0) {
      arguments = "-";
    }

    for (std::vector<const char*>::iterator Iter = commands.begin();
        Iter != commands.end(); ++Iter) {
      arguments += *Iter;
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

    unsigned int connected = debugClient->waitForConnection();

    if (connected) {
      msglog->log(LOG_ALWAYS, "Error: Didn't receive a connection (Code %d)",
                  connected);
      return 1;
    }

    if (debugClient->getSystemPolicy()->hasTarget()) {
      // We do have a system policy at this point. That means that a target
      // process was
      // in some way selected.

      unsigned int attached = debugClient->attachToProcess();

      if (attached) {
        msglog->log(LOG_ALWAYS,
                    "Error: Couldn't attach to the target process (Code %d)",
                    attached);

        debugClient->closeConnection();

        target = "";

        continue;
      } else {
        // Once the debug client attached to the target for the first time, it
        // is necessary
        // to use the PID instead of the target path from now on to support
        // re-attach operations.
        target = zylib::zycon::toString(
            debugClient->getSystemPolicy()->getPID());
      }
    } else {
      debugClient->requestTarget();
    }

    unsigned int procp = debugClient->processPackets();

    if (procp) {
      msglog->log(LOG_ALWAYS,
                  "Error: Error during packet processing. (Code %d)", procp);
    }

    debugClient->closeConnection();
  }
}
