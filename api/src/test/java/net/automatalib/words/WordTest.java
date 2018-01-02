/* Copyright (C) 2013-2018 TU Dortmund
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
package net.automatalib.words;

import java.util.Arrays;
import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for static utility methods.
 *
 * @author frohme
 */
public class WordTest {

    @Test
    public void fromTest() {
        final Word<Character> reference = Word.fromCharSequence("abc");
        final Character[] referenceAsArray = new Character[] {'a', 'b', 'c'};
        final Word<Character> a = Word.fromLetter('a');
        final Word<Character> b = Word.fromLetter('b');
        final Word<Character> c = Word.fromLetter('c');

        Assert.assertEquals(Word.epsilon(), Word.fromList(Collections.emptyList()));
        Assert.assertEquals(reference, Word.fromList(Arrays.asList('a', 'b', 'c')));

        Assert.assertEquals(Word.epsilon(), Word.fromSymbols());
        Assert.assertEquals(reference, Word.fromSymbols('a', 'b', 'c'));

        Assert.assertEquals(Word.epsilon(), Word.fromWords());
        Assert.assertEquals(reference, Word.fromWords(a, b, c));

        Assert.assertEquals(Word.epsilon(), Word.fromWords(Collections.emptyList()));
        Assert.assertEquals(reference, Word.fromWords(Arrays.asList(a, b, c)));

        Assert.assertEquals(Word.epsilon(), Word.fromArray(referenceAsArray, 0, 0));
        Assert.assertEquals(reference, Word.fromArray(referenceAsArray, 0, 2));
    }

    @Test
    public void toArrayTest() throws Exception {
        final Word<Character> reference = Word.fromCharSequence("abc");
        Character[] referenceAsArray;

        referenceAsArray = new Character[3];
        reference.writeToArray(0, referenceAsArray, 0, 3);
        Assert.assertEquals('a', referenceAsArray[0].charValue());
        Assert.assertEquals('b', referenceAsArray[1].charValue());
        Assert.assertEquals('c', referenceAsArray[2].charValue());

        referenceAsArray = new Character[1];
        reference.writeToArray(2, referenceAsArray, 0, 1);
        Assert.assertEquals('c', referenceAsArray[0].charValue());

        referenceAsArray = new Character[3];
        reference.writeToArray(1, referenceAsArray, 2, 1);
        Assert.assertNull(referenceAsArray[0]);
        Assert.assertNull(referenceAsArray[1]);
        Assert.assertEquals('b', referenceAsArray[2].charValue());

        final int[] wordAsInt = reference.toIntArray(x -> x - 'a');
        Assert.assertEquals(0, wordAsInt[0]);
        Assert.assertEquals(1, wordAsInt[1]);
        Assert.assertEquals(2, wordAsInt[2]);
    }

    @Test
    public void transformTest() throws Exception {
        final Word<Character> source = Word.fromCharSequence("abc");
        final Word<String> target = Word.fromSymbols("aa", "bb", "cc");

        final Word<String> transform = source.transform(c -> new String(new char[] {c, c}));

        Assert.assertEquals(target, transform);
    }

    @Test
    public void subwordTest() throws Exception {
        final Word<Character> b = Word.fromLetter('b');
        final Word<Character> c = Word.fromLetter('c');
        final Word<Character> bc = Word.fromSymbols('b', 'c');
        final Word<Character> abc = Word.fromCharSequence("abc");

        Assert.assertEquals(c, abc.subWord(2));
        Assert.assertEquals(bc, abc.subWord(1));
        Assert.assertEquals(abc, abc.subWord(0));
        Assert.assertEquals(b, abc.subWord(1, 2));
    }
}
