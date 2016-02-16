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

void printStartMessage()
{
	std::cout << "BinNavi debug client for Win32 applications" << std::endl;
	std::cout << "Build date: " << __TIME__ << " " << __DATE__ << std::endl;
	std::cout << std::endl;
}

char    szUsage[] = {
"=============================================================================\n"
" Usage: For regular, user-mode Win32 debugging type:\n"
"    client32 [OPTIONS] [PID|PATH] [ARGUMENTS]\n"
"    [ OPTIONS ]   One of ...\n"
"                     -p port         Debug client TCP Port (Default: 2222)\n"
"                     -v              Verbose mode \n"
"                     -vv             Very verbose mode \n"
"                     -lf filename    Filename of the logfile\n"
"    [  PID ]      Process ID or name of the process to attach to\n"
"    [ PATH ]      Path to the executable\n"
"    [ ARGUMENTS ] Command line arguments passed to the target process\n"
"=============================================================================\n"
};
