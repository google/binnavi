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

#include "GdbFactory.hpp"

#include <zycon/src/zycon.h>

#include "../defs.hpp"
#include "../logger.hpp"
#include "Transport.hpp"
#include "TcpTransport.hpp"
#include "cpus/ARMLittleEndian.hpp"
#include "cpus/CpuX86.hpp"
#include "cpus/Cisco2600.hpp"
#include "cpus/NS5XT.hpp"
#include "cpus/PPC603e.hpp"
#include "cpus/Cisco3600.hpp"
#include "SerialTransport.hpp"

/**
 * Checks whether a connection string is a TCP/IP string.
 *
 * @param connection The connection string.
 *
 * @return True, if the string is a TCP/IP string. False, otherwise.
 */
bool isTcpConnectionString(const std::string& connection) {
  // TCP connection strings have the format HOST:PORT
  return connection.find(":") != std::string::npos;
}

/**
 * Checks whether a connection string is a COM string.
 *
 * @param connection The connection string.
 *
 * @return True, if the string is a COM string. False, otherwise.
 */
bool isCOMConnectionString(const std::string& connection) {
  // COM connection strings have the format PORT,BAUD
  return connection.find(",") != std::string::npos;
}

/**
 * Splits a string by a substring.
 *
 * @param str The string to split.
 * @param ss The substring.
 *
 * @return A pair that contains the data of the string before the substring
 *         and after the substring.
 */
std::pair<std::string, std::string> split(const std::string& str,
                                          const std::string& ss) {
  size_t colpos = str.find(ss);

  return std::make_pair(str.substr(0, colpos), str.substr(colpos + 1));
}

NaviError getConnection(const std::string& connection, Transport** transport) {
  // Find out how to connect to the target
  if (isTcpConnectionString(connection)) {
    // TCP connection strings have the format HOST:PORT
    std::pair < std::string, std::string > conn = split(connection, ":");

    if (!zylib::zycon::isPositiveNumber(conn.second)) {
      return NaviErrors::INVALID_CONNECTION_STRING;
    }

    unsigned int port = zylib::zycon::parseString<unsigned int>(
        conn.second.c_str());

    *transport = new TcpTransport(conn.first, port);

    return NaviErrors::SUCCESS;
  }
#ifdef NAVI_GDB_WINDOWS
  else if (isCOMConnectionString(connection)) {
    // COM connection strings have the format PORT,BAUD
    std::pair<std::string, std::string> conn = split(connection, ",");

    if (!zylib::zycon::isPositiveNumber(conn.second)) {
      return NaviErrors::INVALID_CONNECTION_STRING;
    }

    unsigned int bauds =
    zylib::zycon::parseString<unsigned int>(conn.second.c_str());

    *transport = new SerialTransport(conn.first, bauds);

    return NaviErrors::SUCCESS;
  }
#endif
  else {
    // Neither TCP/IP nor serial connection string? That's invalid.

    return NaviErrors::INVALID_CONNECTION_STRING;
  }
}

/**
 * Creates a concrete CPU description object from a given connection string and a
 * given
 * CPU string.
 *
 * Valid connection strings are either TCP/IP strings of the form HOST:PORT or
 * serial strings
 * of the form COMx,BAUDRATE.
 *
 * @param connection The connection string.
 * @param cpuString The CPU string.
 * @param cpu Pointer to the concrete CPU description object that is created.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError getCpu(const std::string& connection, const std::string& cpuString,
                 GdbCpu** cpu) {


  Transport* transport = 0;

  NaviError transportError = getConnection(connection, &transport);

  // Find out how to connect to the target
  if (transportError) {
    return transportError;
  }

  // Find the target CPU
  if (cpuString == "x86") {
    *cpu = new CpuX86(transport);
  } else if (cpuString == "Cisco2600") {
    *cpu = new Cisco2600(transport);
  } else if (cpuString == "NS5XT") {
    *cpu = new NS5XT(transport);
  } else if (cpuString == "PPC603e") {
    *cpu = new PPC603e(transport);
  } else if (cpuString == "Cisco3600") {
    *cpu = new MIPS(transport);
  } else if (cpuString == "ARMLittleEndian") {
    *cpu = new ARMLittleEndian(transport);
  } else {
    // No known CPU string.

    return NaviErrors::INVALID_CPU_STRING;
  }

  return NaviErrors::SUCCESS;
}
