#!/bin/sh

##   Licensed to the Apache Software Foundation (ASF) under one or more
##   contributor license agreements.  See the NOTICE file distributed with
##   this work for additional information regarding copyright ownership.
##   The ASF licenses this file to You under the Apache License, Version 2.0
##   (the "License"); you may not use this file except in compliance with
##   the License.  You may obtain a copy of the License at
##
##       http://www.apache.org/licenses/LICENSE-2.0
##
##   Unless required by applicable law or agreed to in writing, software
##   distributed under the License is distributed on an "AS IS" BASIS,
##   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##   See the License for the specific language governing permissions and
##   limitations under the License.

##   Run HAR Convert tool in CLI

##    run script shell with parameters : <JMETER_HOME>/bin/har-convertor-to-jmeter.sh -har_in "myhar.har" -jmx_out "script_out.jmx" -record_out "record.xml" -add_pause true -new_tc_pause 5000
##    run script shell with parameters : <JMETER_HOME>/bin/har-convertor-to-jmeter.sh -har_in "myhar.har" -jmx_out "script_out.jmx" -filter_include "https://mysite.com/.*" -filter_exclude "https://notmysite.com/.*" -

cd `dirname $0`

CP=../lib/ext/har-convertor-jmeter-plugin-${version}-jar-with-dependencies.jar
CP=${CP}:../lib/*

java -cp $CP io.github.vdaburon.jmeter.har.HarForJMeter $*
