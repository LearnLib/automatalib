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
