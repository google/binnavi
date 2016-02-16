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
	echo ###$(CXXFLAGS)###
	make -C zycon
	make -C zyline
	make -C zylog
	make -C hash
	make -C utility
	make -C xml

macosx: 
	make -C zycon
	make -C zyline
	make -C zylog

client:
	make -C zycon
	make -C zyline
	make -C zylog

install-simple:
	make -C zycon install
	make -C zyline install
	make -C zylog install

install:
	make -C zycon install
	make -C zyline install
	make -C zylog install
	make -C hash install
	make -C utility install
	make -C xml install
	
clean:
	make -C zycon clean
	make -C zyline clean
	make -C zylog clean
	make -C hash clean
	make -C utility clean
	make -C xml clean
	rm -f lib/*.a
