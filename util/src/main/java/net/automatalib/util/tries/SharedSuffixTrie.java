package net.automatalib.util.tries;

import net.automatalib.words.Alphabet;

public class SharedSuffixTrie<I> extends SuffixTrie<I> {

	private final Alphabet<I> alphabet;
	
	public SharedSuffixTrie(Alphabet<I> alphabet) {
		super(new SharedSuffixTrieNode<I>());
		this.alphabet = alphabet;
	}
	public SharedSuffixTrie(Alphabet<I> alphabet, boolean graphRepresentable) {
		super(graphRepresentable, new SharedSuffixTrieNode<I>());
		this.alphabet = alphabet;
	}

	/* (non-Javadoc)
	 * @see net.automatalib.util.tries.SuffixTrie#add(java.lang.Object, net.automatalib.util.tries.SuffixTrieNode)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SuffixTrieNode<I> add(I symbol, SuffixTrieNode<I> parent) {
		if(parent.getClass() != SharedSuffixTrieNode.class) {
			throw new IllegalArgumentException("Invalid suffix trie node");
		}
		
		int symbolIdx = alphabet.getSymbolIndex(symbol);
		SharedSuffixTrieNode<I> sparent = (SharedSuffixTrieNode<I>)parent;
		
		SharedSuffixTrieNode<I> child;
		SharedSuffixTrieNode<I>[] children = sparent.children;
		if(children == null) {
			children = sparent.children = new SharedSuffixTrieNode[alphabet.size()];
		}
		else if((child = children[symbolIdx]) != null) {
			return child;
		}
		child = new SharedSuffixTrieNode<>(symbol, sparent);
		children[symbolIdx] = child;
		return child;
	}

	

}
