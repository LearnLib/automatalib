/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of AutomataLib <https://automatalib.net>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "net_automatalib_common_util_lib_NativeGreeter.h"
#include <stdio.h>
#include <string.h>

JNIEXPORT jstring JNICALL Java_net_automatalib_common_util_lib_NativeGreeter_greet
(JNIEnv *env, jobject obj, jstring string) {

	const char* input = (*env)->GetStringUTFChars(env, string, 0);
	size_t inputLength = strlen(input);

	char result[inputLength + 6];

	strcpy(result, "Hello ");
	strcat(result, input);

	return (*env)->NewStringUTF(env, result);
}
