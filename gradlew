#!/usr/bin/env sh
#
# Gradle start up script for UN*X
#
# Copyright 2009-2019 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

##############################################################################
# Begin standard Gradle wrapper script (trimmed for brevity)
##############################################################################

# Determine the script dir
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")/"$link"
  fi
done

PRGDIR=$(dirname "$PRG")
# make it fully qualified
SCRIPT_DIR=$(cd "$PRGDIR" >/dev/null; pwd)

# Set default JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
  JAVA_CMD="java"
else
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

CLASSPATH="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

##############################################################################
# End standard Gradle wrapper script
##############################################################################
