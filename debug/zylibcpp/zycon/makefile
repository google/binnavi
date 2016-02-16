# Copyright 2011-2016 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

linux:
	$(CXX) $(CXXFLAGS) -m32 -c -ansi -pedantic -Wall src/*.cc
	ar q lib/zycon.a *.o

install:
	cp lib/zycon.a ../lib
	
clean:
	rm -f *.o
	rm -f lib/*.a
	
