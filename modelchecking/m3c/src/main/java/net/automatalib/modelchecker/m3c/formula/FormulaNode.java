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
package net.automatalib.modelchecker.m3c.formula;

import net.automatalib.common.util.string.Printable;
import net.automatalib.modelchecker.m3c.formula.parser.M3CParser;
import net.automatalib.modelchecker.m3c.formula.visitor.FormulaNodeVisitor;
import net.automatalib.modelchecker.m3c.formula.visitor.NNFVisitor;

/**
 * Generic interface for formulas return by {@link M3CParser}s.
 *
 * @param <L>
 *         label type
 * @param <AP>
 *         atomic proposition type
 */
public interface FormulaNode<L, AP> extends Printable {

    <T> T accept(FormulaNodeVisitor<T, L, AP> visitor);

    default FormulaNode<L, AP> toNNF() {
        return new NNFVisitor<L, AP>().transformToNNF(this);
    }

    int getVarNumber();

    void setVarNumber(int varNumber);
}
