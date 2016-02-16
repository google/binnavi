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

#ifdef NAVI_GDB_WINDOWS

#include "SerialTransport.hpp"

#include "../defs.hpp"
#include "../logger.hpp"

/**
 * Opens a connection on the specified COM port.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError SerialTransport::open() {


  // Try to open the port
  if (INVALID_HANDLE_VALUE ==
      (port = CreateFile(comport.c_str(), GENERIC_READ | GENERIC_WRITE, 0, NULL,
              OPEN_EXISTING, NULL, NULL))) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't open port %s (Code %d)",
        comport.c_str(), GetLastError());

    return NaviErrors::COULDNT_CONNECT_TO_GDBSERVER;
  }

  // Try to configure the COM port
  COMMCONFIG config;

  if (!GetCommConfig(port, &config, &(config.dwSize))) {
    msglog->log(LOG_ALWAYS,
        "Error: Couldn't get COM port configuration (Code %d)",
        GetLastError());

    return NaviErrors::COULDNT_CONNECT_TO_GDBSERVER;
  }

  COMMTIMEOUTS timeouts;  ///< Windows specific Timeout configuration

  char portconfig[1000];
#ifdef WIN32
  sprintf_s(portconfig, "baud=%d parity=N data=8 stop=1", bauds);
#else
  sprintf(portconfig, "baud=%d parity=N data=8 stop=1", bauds);
#endif

  if (!BuildCommDCBAndTimeouts(portconfig, &(config.dcb), &timeouts)) {
    msglog->log(LOG_ALWAYS,
        "Error: Couldn't build COM port connection string (Code %d)",
        GetLastError());

    return NaviErrors::COULDNT_CONNECT_TO_GDBSERVER;
  }

  // config modifications if needed
  config.dwSize = sizeof(COMMCONFIG);
  config.wVersion = 1;

  if (!SetCommConfig(port, &config, config.dwSize)) {
    msglog->log(LOG_ALWAYS,
        "Error: Couldn't set COM port configuration (Code %d)",
        GetLastError());

    return NaviErrors::COULDNT_CONNECT_TO_GDBSERVER;
  }

  if (!SetCommMask(port, EV_BREAK | EV_CTS | EV_DSR | EV_ERR | EV_RING |
          EV_RLSD | EV_RXCHAR | EV_RXFLAG | EV_TXEMPTY)) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't get COM port event mask (Code %d)",
        GetLastError());

    return NaviErrors::COULDNT_CONNECT_TO_GDBSERVER;
  }

  return NaviErrors::SUCCESS;
}

/**
 * Closes the connection to the specified COM port.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError SerialTransport::close() {

  CloseHandle(port);
  return NaviErrors::SUCCESS;
}

/**
 * Checks whether data is available on the COM port.
 *
 * @return True, if data is available. False, otherwise.
 */
bool SerialTransport::hasData() const {
  COMSTAT st;
  DWORD dwerror;
  ClearCommError(port, &dwerror, &st);
  return st.cbInQue > 0;
}

/**
 * Sends binary to the COM port.
 *
 * @param buffer The input buffer.
 * @param size Number of bytes to send from the buffer to the COM port.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError SerialTransport::send(const char* buffer, unsigned int size) const {


  unsigned int toWrite = size;

  DWORD out;

  while (toWrite > 0) {
    if (!WriteFile(port, buffer + size - toWrite, toWrite, &out, 0)) {
      msglog->log(LOG_ALWAYS, "Error: Couldn't write to COM port (Code %d)",
          GetLastError());
      return NaviErrors::SEND_ERROR;
    }

    FlushFileBuffers(port);

    toWrite -= out;
  }

  return NaviErrors::SUCCESS;
}

/**
 * Reads binary data from the COM port.
 *
 * @param buffer The data from the COM port is stored here.
 * @param size Number of bytes to read from the COM port.
 *
 * @return A NaviError code that describes whether the operation was successful
 * or not.
 */
NaviError SerialTransport::read(char* buffer, unsigned int size) const {
  COMSTAT st;
  DWORD dwerror;
  DWORD in;

  unsigned int read = 0;

  while (read < size) {
    ClearCommError(port, &dwerror, &st);

    if (st.cbInQue > 0) {
      if (!ReadFile(port, (char*)buffer + read, size, &in, NULL)) {
        msglog->log(LOG_ALWAYS, "Error: Couldn't read from COM port (Code %d)",
            GetLastError());

        return NaviErrors::SEND_ERROR;
      }
      read += in;
    }
  }

  return NaviErrors::SUCCESS;
}

#endif
