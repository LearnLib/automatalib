/* Copyright (C) 2014 TU Dortmund
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
package net.automatalib.commons.util.functions;

import java.util.function.Function;
import java.util.function.IntFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides utility methods for Java 8 {@link Function} objects (and for the
 * corresponding primitive specializations).
 * 
 * @author Malte Isberner
 *
 */
public abstract class FunctionsUtil {

	/**
	 * Returns a default function if the argument is {@code null}. The default function's
	 * {@code Function#apply(Object) apply} method will always return {@code null}.
	 * If a non-{@code null} function is passed to this method, it is returned as-is.
	 * 
	 * @param func the function reference (may be {@code null})
	 * @return a non-{@code null} object identical to the passed function, if it is
	 * non-{@code null}, or a function object always returning {@code null} otherwise
	 */
	@Nonnull
	public static <T,R> Function<T, R> safeDefault(@Nullable Function<T,R> func) {
		if (func == null) {
			return (x) -> null;
		}
		return func;
	}
	
	/**
	 * Returns a default function if the argument is {@code null}. The default function's
	 * {@link IntFunction#apply(int) apply} method will always return {@code null}.
	 * If a non-{@code null} function is passed to this method, it is returned as-is.
	 * 
	 * @param func the function reference (may be {@code null})
	 * @return a non-{@code null} object identical to the passed function, if it is
	 * non-{@code null}, or a function object always returning {@code null} otherwise
	 */
	@Nonnull
	public static <R> IntFunction<R> safeDefault(@Nullable IntFunction<R> func) {
		if (func == null) {
			return (i) -> null;
		}
		return func;
	}
	
	private FunctionsUtil() {
		throw new AssertionError();
	}

}
