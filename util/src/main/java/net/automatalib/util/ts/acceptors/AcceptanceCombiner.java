/* Copyright (C) 2013 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.util.ts.acceptors;

public interface AcceptanceCombiner {
	public static AcceptanceCombiner AND = new AcceptanceCombiner() {
		@Override
		public boolean combine(boolean a1, boolean a2) {
			return a1 && a2;
		}
	};
	
	public static AcceptanceCombiner OR = new AcceptanceCombiner() {
		@Override
		public boolean combine(boolean a1, boolean a2) {
			return a1 || a2;
		}
	};
	
	public static AcceptanceCombiner XOR = new AcceptanceCombiner() {
		@Override
		public boolean combine(boolean a1, boolean a2) {
			return a1 ^ a2;
		}
	};
	
	public static AcceptanceCombiner EQUIV = new AcceptanceCombiner() {
		@Override
		public boolean combine(boolean a1, boolean a2) {
			return (a1 == a2);
		}
	};
	
	public static AcceptanceCombiner IMPL = new AcceptanceCombiner() {
		@Override
		public boolean combine(boolean a1, boolean a2) {
			return !a1 || a2;
		}
	};
	
	/**
	 * Combine two acceptance values.
	 * @param a1
	 * @param a2
	 * @return
	 */
	public boolean combine(boolean a1, boolean a2);
}
