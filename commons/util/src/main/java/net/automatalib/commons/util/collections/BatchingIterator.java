package net.automatalib.commons.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator that aggregates elements of a given source iterator in batches of a given size. While elements are
 * collected eagerly within a batch, the overall batches are computed lazily.
 *
 * The source iterator is consumed in this process.
 *
 * @param <T> type of elements to aggregate
 *
 * @author frohme
 */
public class BatchingIterator<T> implements Iterator<List<T>> {

	private int batchSize;

	private Iterator<T> source;

	public BatchingIterator(final Iterator<T> source, final int batchSize) {
		this.batchSize = batchSize;
		this.source = source;
	}

	@Override
	public boolean hasNext() {
		return source.hasNext();
	}

	@Override
	public List<T> next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		final List<T> result = new ArrayList<>(batchSize);

		while (source.hasNext() && result.size() < batchSize) {
			result.add(source.next());
		}

		return result;
	}

}