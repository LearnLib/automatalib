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
package net.automatalib.serialization.mata.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.StateIDs;
import net.automatalib.automaton.fsa.NFA;
import net.automatalib.common.util.IOUtil;
import net.automatalib.serialization.InputModelSerializer;

public class MataNFAWriter<I> implements InputModelSerializer<I, NFA<?, I>> {

    @Override
    public void writeModel(OutputStream os, NFA<?, I> model, Alphabet<I> alphabet) throws IOException {
        try (Writer w = IOUtil.asNonClosingUTF8Writer(os)) {
            write(w, model, alphabet);
        }
    }

    private <S> void write(Writer w, NFA<S, I> model, Alphabet<I> alphabet) throws IOException {

        w.write("@NFA-explicit\n");
        w.write("%Alphabet");
        for (int i = 0; i < alphabet.size(); i++) {
            w.write(' ');
            w.write(Integer.toString(i));
        }
        w.write('\n');

        final StateIDs<S> stateIDs = model.stateIDs();
        final List<Integer> finals = new ArrayList<>(model.size());

        w.write("%States");
        for (S s : model) {
            int id = stateIDs.getStateId(s);
            w.write(' ');
            w.write('q');
            w.write(Integer.toString(id));
            if (model.isAccepting(s)) {
                finals.add(id);
            }
        }
        w.write('\n');

        w.write("%Initial");
        for (S init : model.getInitialStates()) {
            w.write(' ');
            w.write('q');
            w.write(Integer.toString(stateIDs.getStateId(init)));
        }
        w.write('\n');

        w.write("%Final");
        for (Integer s : finals) {
            w.write(' ');
            w.write('q');
            w.write(Integer.toString(s));
        }
        w.write('\n');

        for (S s : model) {
            for (int i = 0; i < alphabet.size(); i++) {
                for (S t : model.getSuccessors(s, alphabet.getSymbol(i))) {
                    w.write('q');
                    w.write(Integer.toString(stateIDs.getStateId(s)));
                    w.write(' ');
                    w.write(Integer.toString(i));
                    w.write(' ');
                    w.write('q');
                    w.write(Integer.toString(stateIDs.getStateId(t)));
                    w.write('\n');
                }
            }
        }
    }
}
