package net.automatalib.util.tries;

/**
 * A node in a {@link SharedSuffixTrie}. This class maintains an
 * array containing all children, in order to avoid inserting
 * duplicates.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> symbol class.
 */
final class SharedSuffixTrieNode<I> extends SuffixTrieNode<I> {
	
	SharedSuffixTrieNode<I>[] children;

	/**
	 * Root constructor.
	 */
	public SharedSuffixTrieNode() {
	}

	/**
	 * Constructor.
	 * 
	 * @param symbol the symbol to prepend.
	 * @param parent the trie node representing the remaining suffix.
	 */
	public SharedSuffixTrieNode(I symbol, SuffixTrieNode<I> parent) {
		super(symbol, parent);
	}

}
