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
package net.automatalib.commons.util.combinatorics;

public class DisjointSetForestInt {
	
	
	private int[] parent;
	private int[] rank;

	public DisjointSetForestInt(int initSize) {
		this.parent = new int[initSize];
		for(int i = 0; i < this.parent.length; i++)
			this.parent[i] = -1;
		this.rank = new int[initSize];
	}
	
	
	public int find(int a) {
		int p = parent[a];
		if(p == -1)
			return a;
		p = find(p);
		parent[a] = p;
		return p;
	}
	
	public int union(int a, int b) {
		return directUnion(find(a), find(b));
	}
	
	public int directUnion(int a, int b) {		
		assert parent[a] == -1 && parent[b] == -1;
		
		if(a == b)
			return a;
		
		int ra = rank[a], rb = rank[b];
		if(ra < rb) {
			parent[a] = b;
			return b;
		}
		if(ra == rb) {
			rank[a]++;
		}
		parent[b] = a;
		return a;	
	}
	
	public int size() {
		return parent.length;
	}
	
	
}
