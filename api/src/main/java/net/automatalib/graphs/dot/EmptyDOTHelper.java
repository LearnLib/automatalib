/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package net.automatalib.graphs.dot;

import java.io.IOException;
import java.util.Map;

import net.automatalib.commons.util.mappings.Mapping;

public class EmptyDOTHelper<N, E> implements GraphDOTHelper<N, E> {


	@Override
	public void writePreamble(Appendable a) throws IOException {
	}

	@Override
	public void writePostamble(Mapping<N, String> identifiers, Appendable a)
			throws IOException {
	}

	@Override
	public boolean getNodeProperties(N node, Map<String, String> properties) {
		return true;
	}

	@Override
	public boolean getEdgeProperties(N src, E edge, N tgt, Map<String, String> properties) {
		return true;
	}

	@Override
	public void getGlobalNodeProperties(Map<String, String> properties) {
	}

	@Override
	public void getGlobalEdgeProperties(Map<String, String> properties) {
	}

}
