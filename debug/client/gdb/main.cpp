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

#define NOMINMAX

#include <iostream>
#include <cassert>
#include <cstdlib>
#include <vector>
#include <string>
#include <utility>
#include <ctime>
#include <memory>

#include <zylog/src/logger.h>
#include <zylog/src/consoletarget.h>
#include <zylog/src/filetarget.h>
#include <zyline/src/zyline.h>
#include <zycon/src/zycon.h>

zylib::zylog::Logger* msglog;

#include "../includer.hpp"
#include "../DebugClient.hpp"

const unsigned int EXPIRE_YEAR = 0;
const unsigned int EXPIRE_MONTH = 10;
const unsigned int EXPIRE_DAY = 01;

/**
 * Initializes the logger object that logs important information
 *
 * @param logfile The name of the logfile where the logging messages go.
 * @param level The selected log level.
 */
void initLogger(const std::string& logfile, unsigned int level) {
  // TODO: Delete this later
  msglog = new zylib::zylog::Logger(level);

  // Always log to stdout
  // TODO: Delete this later
  zylib::zylog::ConsoleTarget* t1 = new zylib::zylog::ConsoleTarget;
  msglog->addTarget(t1);

  // Log to a file if a logfile was specified in the commandline
  if (logfile != "") {
    // TODO: Delete this later
    zylib::zylog::FileTarget* t2 = new zylib::zylog::FileTarget(logfile);
    msglog->addTarget(t2);
  }
}

void handleCommandLine(std::vector<std::pair<std::string, std::string> > params,
                       std::string& gdbhost, std::string& cpu,
                       unsigned int& clientport, bool& v, bool& vv,
                       bool& testMode, std::string& lf) {
  const unsigned int LOC_HOST = 1;
  const unsigned int LOC_CPU = 2;
  const unsigned int LOC_PORT = 3;

  if (params[LOC_HOST].second != "") {
    std::cout << "Error: Invalid GDB server location specified" << std::endl;
    std::cout << szUsage << std::endl;
    std::exit(1);
  }

  gdbhost = params[LOC_HOST].first;

  if (params[LOC_CPU].second != "") {
    std::cout << "Error: Invalid target CPU specified" << std::endl;
    std::cout << szUsage << std::endl;
    std::exit(1);
  }

  cpu = params[LOC_CPU].first;

  if (params.size() == LOC_CPU + 1) {
    return;
  }

  if (zylib::zycon::isPositiveNumber(params[LOC_PORT].first)) {
    clientport = zylib::zycon::parseString<unsigned int>(
        params[LOC_PORT].first);
  } else if (params[LOC_PORT].first[0] != '-') {
    std::cout << "Error: Invalid server port specified" << std::endl;
    std::cout << szUsage << std::endl;
    std::exit(1);
  }

  for (unsigned int i = LOC_PORT + params[LOC_PORT].first[0] != '-';
      i < params.size(); i++) {
    if (params[i].first == "-v") {
      if (params[i].second == "") {
        v = true;
      } else {
        std::cout << "Error: Invalid parameter " << params[i].second
            << std::endl;
        std::cout << szUsage << std::endl;
        std::exit(1);
      }
    } else if (params[i].first == "-vv") {
      if (params[i].second == "") {
        vv = true;
      } else {
        std::cout << "Error: Invalid parameter " << params[i].second
            << std::endl;
        std::cout << szUsage << std::endl;
        std::exit(1);
      }
    } else if (params[i].first == "-test") {
      if (params[i].second == "") {
        testMode = true;
      } else {
        std::cout << "Error: Invalid parameter " << params[i].second
            << std::endl;
        std::cout << szUsage << std::endl;
        std::exit(1);
      }
    } else if (params[i].first == "-lf") {
      if (params[i].second != "") {
        lf = params[i].second;
      } else {
        std::cout << "Error: Missing logfile filename" << std::endl;
        std::cout << szUsage << std::endl;
        std::exit(1);
      }
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

void printStartMessage() {
  std::cout << "BinNavi debug client for GDB servers";
  if (EXPIRE_YEAR) {
    std::cout << " (TRIAL VERSION - Expires " << EXPIRE_YEAR << "/"
        << EXPIRE_MONTH << "/" << EXPIRE_DAY << ")";
  }
  std::cout << std::endl;
  std::cout << "Build date: " << __TIME__ << " " << __DATE__ << std::endl;
  std::cout << std::endl;
}

void checkTrial() {
  time_t seconds = time(0);

#ifdef WIN32
  tm* timeinfo = 0;
  localtime_s(timeinfo, &seconds);
#else
  tm* timeinfo = localtime(&seconds);
#endif

  if (timeinfo->tm_year > EXPIRE_YEAR - 1900
      || timeinfo->tm_mon > EXPIRE_MONTH - 1
      || (timeinfo->tm_mon == EXPIRE_MONTH - 1 && timeinfo->tm_mday > EXPIRE_DAY)) {
    std::cout << "Your evaluation license has expired. Please contact "
        "support@zynamics.com to learn about your options.";

    std::exit(0);
  }
}

int main(int argc, const char* argv[]) {
  printStartMessage();

  if (EXPIRE_YEAR) {
    checkTrial();
  }

  std::vector < std::pair<std::string, std::string> > params =
      zylib::zyline::parseCommandLine(argc, argv);

  if (params.size() < 3) {
    std::cout << szUsage << std::endl;
    return 1;
  }

  std::string gdbhost;
  std::string targetCpu;
  unsigned int clientport = 2222;
  bool verbose = false;
  bool vverbose = false;
  bool testMode = false;
  std::string lf = "";

  handleCommandLine(params, gdbhost, targetCpu, clientport, verbose, vverbose,
                    testMode, lf);

  unsigned int loglevel = LOG_ALWAYS;

  if (verbose) {
    loglevel = LOG_VERBOSE;
  } else if (vverbose) {
    loglevel = LOG_ALL;
  }

  initLogger(lf, loglevel);

  msglog->log(LOG_ALWAYS,
              "---------------------------------------------------------");
  msglog->log(LOG_ALWAYS, "Starting new GDB client session");
  msglog->log(LOG_ALWAYS, "Location of the GDB server: %s", gdbhost.c_str());
  msglog->log(LOG_ALWAYS, "Target CPU: %s", targetCpu.c_str());
  msglog->log(LOG_ALWAYS, "Server Port %d", clientport);

  if (verbose) {
    msglog->log(LOG_ALWAYS, "Verbose mode: ON");
  }

  if (vverbose) {
    msglog->log(LOG_ALWAYS, "Very Verbose mode: ON");
  }

  if (testMode) {
    msglog->log(LOG_ALWAYS, "Test mode: ON");
  }

  if (lf != "") {
    msglog->log(LOG_ALWAYS, "Logging to file: %s", lf.c_str());
  }

  msglog->log(LOG_ALWAYS,
              "---------------------------------------------------------");

  // don't delete it after use, debugClient will do it
  GdbSystem* system = new SYSTEM_POLICY(123);

  std::unique_ptr<DebugClient> debugClient(
      new DebugClient(new CONNECTION_POLICY(clientport), system));

  msglog->log(LOG_ALWAYS, "Connecting to the GDB server ...");

  NaviError initResult = system->initTarget(gdbhost, targetCpu);

  if (initResult) {
    if (initResult == NaviErrors::INVALID_CONNECTION_STRING) {
      msglog->log(LOG_ALWAYS, "Error: Invalid connection string");
    } else if (initResult == NaviErrors::INVALID_CPU_STRING) {
      msglog->log(LOG_ALWAYS, "Error: Invalid CPU string");
    } else if (initResult == NaviErrors::COULDNT_CONNECT_TO_GDBSERVER) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't connect to the gdbserver");
    } else {
      msglog->log(LOG_ALWAYS, "Error: Unexpected error (Code %d)", initResult);
    }

    return initResult;
  }

  msglog->log(LOG_ALWAYS, "Connection to the GDB server open...");

  if (testMode) {
    system->getCpu()->testRun();

    return 0;
  }

  do {
    msglog->log(LOG_VERBOSE, "Creating server for BinNavi to connect ...");

    unsigned int init = debugClient->initializeConnection();

    if (init) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't create server (Code d)", init);
      return 1;
    }

    msglog->log(LOG_VERBOSE, "Waiting for BinNavi to connect ...");

    unsigned int connected = debugClient->waitForConnection();

    if (connected) {
      msglog->log(LOG_ALWAYS, "Error: Didn't receive a connection (Code %d)",
                  connected);
      return 1;
    }

    msglog->log(LOG_VERBOSE, "Attaching to the target process ...");

    unsigned int attached = debugClient->attachToProcess();

    if (attached) {
      msglog->log(LOG_ALWAYS,
                  "Error: Couldn't attach to the target process (Code %d)",
                  attached);
      return 1;
    }

    msglog->log(LOG_VERBOSE, "Processing communication packets ...");

    unsigned int procp = debugClient->processPackets();

    if (procp) {
      msglog->log(LOG_ALWAYS,
                  "Error: Error during packet processing. (Code %d)", procp);
    }

    msglog->log(LOG_VERBOSE, "Closing the connection to BinNavi ...");

    debugClient->closeConnection();
  } while (true);
}
