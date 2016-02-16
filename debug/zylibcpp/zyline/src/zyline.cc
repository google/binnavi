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

#include <string>
#include <vector>
#include <utility>
#include <algorithm>
#include <iostream>

namespace zylib
{
	namespace zyline
	{
		/**
		* \brief Parses commandline parameters into a list of string-pairs.
		*
		* Parses commandline parameters into a list of string-pairs. The parser recognizes three types of
		* parameters.
		*
		* -# Normal arguments which can be any kind of strings (list x86 for a cpu name).
		* -# Arguments starting with a '-' character (like -v for verbose mode).
		* -# Arguments starting with a '-' character and followed by another argument without a '-' (like -p 80 to specify port 80).
		*
		* After parsing, arguments of the first two argument types are stored in pairs of the form ("arg", "")
		* while arguments of the third type are stored in pairs of the form ("arg", "value").
		*
		* @param argc Number of arguments.
		* @param argv Argument array.
		*
		* @return The commandline arguments sorted into a list of pairs.
		**/
		std::vector<std::pair<std::string, std::string> > parseCommandLine(unsigned int argc, const char* argv[])
		{
			std::vector<std::pair<std::string, std::string> > retval;

			for (unsigned int i=0;i<argc;i++)
			{
				if (argv[i][0] == '-')
				{
					if (i < argc - 1 && argv[i+1][0] != '-')
					{
						retval.push_back(std::make_pair(argv[i], argv[i+1]));
						i++;
						continue;
					}
				}

				retval.push_back(std::make_pair(argv[i], ""));
			}

			return retval;
		}
	}
}
