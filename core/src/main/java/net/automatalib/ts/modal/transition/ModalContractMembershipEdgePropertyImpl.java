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
package net.automatalib.ts.modal.transition;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

public class ModalContractMembershipEdgePropertyImpl extends ModalContractEdgePropertyImpl
        implements MutableGroupMemberEdge {

    private int memberId;

    public ModalContractMembershipEdgePropertyImpl(ModalType type, boolean tau, EdgeColor color, int memberId) {
        super(type, tau, color);
        this.memberId = memberId;
    }

    @Override
    public void setMemberId(int id) {
        memberId = id;
    }

    @Override
    public int getMemberId() {
        return memberId;
    }

    @Override
    public String toString() {
        return "color={" + getColor() + "}, memberId={" + memberId + "}, type={" + getType() + '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ModalContractMembershipEdgePropertyImpl that = (ModalContractMembershipEdgePropertyImpl) o;

        return this.getType() == that.getType() && this.isTau() == that.isTau() && this.getColor() == that.getColor() &&
               memberId == that.memberId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), memberId);
    }
}
