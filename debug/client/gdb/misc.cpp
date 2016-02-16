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

char szUsage[] = {
  "============================================================================"
  "=\n"
  " Usage: \n"
  "    clientgdb [GDBLOC] [CPU] [PORT] [OPTIONS]\n"
  "\n"
  "    [ GDBLOC  ] Location of the GDB server (either HOST:PORT or COMx,BAUD)\n"
  "    [ CPU     ] CPU string of the target CPU; one of ...:\n"
  "                   x86\n"
  "                   Cisco2600 (PowerPC)\n"
  "                   Cisco3600 (MIPS)\n"
  "                   NS5XT\n"
  "                   PPC603e\n"
  "    [ PORT    ] TCP Port to bind to for communication (default is 2222)\n"
  "    [ OPTIONS ] One of ...\n"
  "                   -v              Verbose mode \n"
  "                   -vv             Very verbose mode \n"
  "                   -test           Test mode \n"
  "                   -lf filename    Filename of the logfile\n"
  "============================================================================"
  "=\n"
};
