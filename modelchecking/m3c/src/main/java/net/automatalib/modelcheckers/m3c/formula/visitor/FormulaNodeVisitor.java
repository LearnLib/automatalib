/* Copyright (C) 2013-2021 TU Dortmund
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
package net.automatalib.modelcheckers.m3c.formula.visitor;

import net.automatalib.modelcheckers.m3c.formula.AndNode;
import net.automatalib.modelcheckers.m3c.formula.AtomicNode;
import net.automatalib.modelcheckers.m3c.formula.BoxNode;
import net.automatalib.modelcheckers.m3c.formula.DiamondNode;
import net.automatalib.modelcheckers.m3c.formula.FalseNode;
import net.automatalib.modelcheckers.m3c.formula.FormulaNode;
import net.automatalib.modelcheckers.m3c.formula.NotNode;
import net.automatalib.modelcheckers.m3c.formula.OrNode;
import net.automatalib.modelcheckers.m3c.formula.TrueNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.AWUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EFNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EGNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EUNode;
import net.automatalib.modelcheckers.m3c.formula.ctl.EWUNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.GfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.LfpNode;
import net.automatalib.modelcheckers.m3c.formula.modalmu.VariableNode;

public abstract class FormulaNodeVisitor<T, L, AP> {

    public T visit(FormulaNode<L, AP> node) {
        return node.accept(this);
    }

    public abstract T visit(AFNode<L, AP> node);

    public abstract T visit(AGNode<L, AP> node);

    public abstract T visit(AUNode<L, AP> node);

    public abstract T visit(AWUNode<L, AP> node);

    public abstract T visit(EFNode<L, AP> node);

    public abstract T visit(EGNode<L, AP> node);

    public abstract T visit(EUNode<L, AP> node);

    public abstract T visit(EWUNode<L, AP> node);

    public abstract T visit(AndNode<L, AP> node);

    public abstract T visit(AtomicNode<L, AP> node);

    public abstract T visit(BoxNode<L, AP> node);

    public abstract T visit(DiamondNode<L, AP> node);

    public abstract T visit(FalseNode<L, AP> node);

    public abstract T visit(NotNode<L, AP> node);

    public abstract T visit(OrNode<L, AP> node);

    public abstract T visit(TrueNode<L, AP> node);

    public abstract T visit(GfpNode<L, AP> node);

    public abstract T visit(LfpNode<L, AP> node);

    public abstract T visit(VariableNode<L, AP> node);

}
