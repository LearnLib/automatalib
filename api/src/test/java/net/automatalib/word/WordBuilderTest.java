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
package net.automatalib.word;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WordBuilderTest {

    @Test
    public void constructorTest() {
        WordBuilder<?> wb;

        wb = new WordBuilder<>();
        Assert.assertEquals(wb.size(), 0);
        Assert.assertEquals(wb.toWord(), Word.epsilon());

        wb = new WordBuilder<>(-1);
        Assert.assertEquals(wb.size(), 0);
        Assert.assertEquals(wb.toWord(), Word.epsilon());

        final Word<Character> aaaaa = Word.fromString("aaaaa");

        wb = new WordBuilder<>('a', 5);
        Assert.assertEquals(wb.size(), 5);
        Assert.assertEquals(wb.toWord(), aaaaa);

        wb = new WordBuilder<>(null, 5);
        Assert.assertEquals(wb.size(), 5);
        Assert.assertEquals(wb.toWord(), Word.fromSymbols(null, null, null, null, null));

        wb = new WordBuilder<>(10, 'a', 5);
        Assert.assertEquals(wb.size(), 5);
        Assert.assertEquals(wb.toWord(), aaaaa);

        wb = new WordBuilder<>(-1, 'a', 5);
        Assert.assertEquals(wb.size(), 5);
        Assert.assertEquals(wb.toWord(), aaaaa);

        final Word<?> abc = Word.fromString("abc");

        wb = new WordBuilder<>(abc);
        Assert.assertEquals(wb.size(), 3);
        Assert.assertEquals(wb.toWord(), abc);

        wb = new WordBuilder<>(-1, abc);
        Assert.assertEquals(wb.size(), 3);
        Assert.assertEquals(wb.toWord(), abc);
    }

    @Test
    public void appendTest() {
        WordBuilder<Character> wb = new WordBuilder<>();
        final Word<Character> aaa = Word.fromString("aaa");
        final Word<Character> abc = Word.fromString("abc");
        final Word<Character> abcabcabc = abc.concat(abc, abc);

        wb.append('a');
        Assert.assertEquals(wb.size(), 1);
        Assert.assertEquals(wb.toWord(), Word.fromSymbols('a'));

        wb.clear();
        wb.append('a', 'b', 'c');
        Assert.assertEquals(wb.size(), 3);
        Assert.assertEquals(wb.toWord(), abc);

        wb.clear();
        wb.append(abc);
        Assert.assertEquals(wb.size(), 3);
        Assert.assertEquals(wb.toWord(), abc);

        wb.clear();
        wb.append(Arrays.asList('a', 'b', 'c'));
        Assert.assertEquals(wb.size(), 3);
        Assert.assertEquals(wb.toWord(), abc);

        wb.clear();
        wb.append(abc, abc, abc);
        Assert.assertEquals(wb.size(), 9);
        Assert.assertEquals(wb.toWord(), abcabcabc);

        wb.clear();
        wb.repeatAppend(3, 'a');
        Assert.assertEquals(wb.size(), 3);
        Assert.assertEquals(wb.toWord(), aaa);

        wb.clear();
        wb.repeatAppend(3, abc);
        Assert.assertEquals(wb.size(), 9);
        Assert.assertEquals(wb.toWord(), abcabcabc);


        final int bigChunkSize = 27;
        wb.clear();
        wb.repeatAppend(bigChunkSize, abc);
        Assert.assertEquals(wb.size(), bigChunkSize * 3);

        Word<Character> buffer = Word.epsilon();
        for (int i = 0; i < bigChunkSize; i++) {
            buffer = buffer.concat(abc);
        }
        Assert.assertEquals(wb.toWord(), buffer);
    }

    @Test
    public void reverseTest() {
        WordBuilder<Character> wb = new WordBuilder<>();
        final Word<Character> abc = Word.fromString("abc");
        final Word<Character> cba = Word.fromString("cba");

        wb.append(abc).reverse();
        Assert.assertEquals(wb.toWord(), cba);
    }

    @Test
    public void toWordTest() {
        WordBuilder<Character> wb = new WordBuilder<>();
        final Word<Character> abc = Word.fromString("abc");

        wb.repeatAppend(3, abc);
        Assert.assertEquals(wb.toWord(3, 6), abc);

        Assert.assertThrows(IndexOutOfBoundsException.class, () -> wb.toWord(-1, wb.size()));
        Assert.assertThrows(IndexOutOfBoundsException.class, () -> wb.toWord(0, wb.size() + 1));
    }

    @Test
    public void truncateTest() {
        WordBuilder<Character> wb = new WordBuilder<>();
        final Word<Character> abc = Word.fromString("abc");
        final Word<Character> abcabc = abc.concat(abc);
        final Word<Character> abcabcabc = abcabc.concat(abc);

        wb.repeatAppend(3, abc).truncate(12);
        Assert.assertEquals(wb.toWord(), abcabcabc);

        wb.clear();
        wb.repeatAppend(3, abc).truncate(6);
        Assert.assertEquals(wb.toWord(), abcabc);
    }

}
