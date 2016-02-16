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

#include "logger.h"

#include <cstdarg>


#include "consoletarget.h"
#include "filetarget.h"


namespace zylib
{
	namespace zylog
	{
#ifdef WINCE
		enum { MAX_MSG= 100 };
		#define vsnprintf _vsnprintf
#else
		enum { MAX_MSG= 4096 };
#endif

		void Logger::addTarget(ILogTarget* target)
		{
			targets.push_back(target);
		}

		void Logger::log(unsigned int level, const char* message, ...)
		{
			if (level > logLevel)
			{
				return;
			}

			va_list arglist;
			char text[MAX_MSG];
			va_start( arglist, message );
#ifdef WIN32
			vsnprintf_s(text, MAX_MSG, _TRUNCATE, message, arglist);
#else
			vsnprintf( text, MAX_MSG, message, arglist );
#endif
			va_end( arglist );

			for (std::vector<ILogTarget*>::iterator Iter = targets.begin(); Iter != targets.end(); ++Iter)
			{
				(*Iter)->log(text);
			}
		}
	}
}
