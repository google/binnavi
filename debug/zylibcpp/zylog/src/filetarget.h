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

#ifndef FILETARGET_HPP
#define FILETARGET_HPP

#include <fstream>
#include <string>
#include "ilogtarget.h"

namespace zylib
{
	namespace zylog
	{
		/**
		* Logger target that logs messages to a file.
		**/
		class FileTarget : public ILogTarget
		{
			private:
				std::string filename;
				std::ofstream file;

			public:
				/**
				* \brief Creates a new file target.
				*
				* Creates a new file target.
				*
				* @param filename Name of the logfile.
				**/
				explicit FileTarget(const std::string& filename);

				/**
				* \brief Writes a message to the file.
				*
				* Writes a message to the file.
				**/
				void log(const char* msg);
		};
	}
}

#endif

