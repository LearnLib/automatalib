package net.automatalib.graphs.abstractimpl;

import java.util.HashMap;

import net.automatalib.commons.util.mappings.MapMapping;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.graphs.IndefiniteGraph;

public abstract class AbstractIndefiniteGraph<N, E> implements IndefiniteGraph<N, E> {

	/**
	 * Provides a realization for {@link IndefiniteGraph#createStaticNodeMapping()}
	 * by defaulting to a {@link HashMap} backed mapping.
	 * @see IndefiniteGraph#createStaticNodeMapping()
	 */
	public static <N,E,V> MutableMapping<N,V> createStaticNodeMapping(IndefiniteGraph<N,E> $this) {
		return new MapMapping<>(new HashMap<N,V>());
	}
	
	/**
	 * Provides a realization for {@link IndefiniteGraph#createDynamicNodeMapping()}
	 * by defaulting to a {@link HashMap} backed mapping.
	 * @see IndefiniteGraph#createDynamicNodeMapping()
	 */
	public static <N,E,V> MutableMapping<N,V> createDynamicNodeMapping(IndefiniteGraph<N,E> $this) {
		return new MapMapping<>(new HashMap<N,V>());
	}
	

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#createStaticNodeMapping()
	 */
	@Override
	public <V> MutableMapping<N, V> createStaticNodeMapping() {
		return createStaticNodeMapping(this);
	}

	/*
	 * (non-Javadoc)
	 * @see net.automatalib.graphs.IndefiniteGraph#createDynamicNodeMapping()
	 */
	@Override
	public <V> MutableMapping<N, V> createDynamicNodeMapping() {
		return createDynamicNodeMapping(this);
	}

}
