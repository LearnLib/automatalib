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
package net.automatalib.commons.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class ReflectUtil {
	
	private static final Class<?>[][] W2P_MAPPING = {
		{ Void.class, void.class },
		{ Boolean.class, boolean.class },
		{ Byte.class, byte.class },
		{ Character.class, char.class },
		{ Short.class, short.class },
		{ Integer.class, int.class },
		{ Long.class, long.class },
		{ Float.class, float.class },
		{ Double.class, double.class }
	};
	
	private static final Map<Class<?>,Class<?>> w2pMap;

	
	static {
		w2pMap = new HashMap<Class<?>,Class<?>>();
		
		for(Class<?>[] w2p : W2P_MAPPING)
			w2pMap.put(w2p[0], w2p[1]);
	}
	
	private ReflectUtil() {}
	
	
	
	public static Class<?> wrapperToPrimitive(Class<?> wrapperClazz) {
		Class<?> prim = w2pMap.get(wrapperClazz);
		if(prim == null)
			return wrapperClazz;
		return prim;
	}
	
	public static Class<?>[] wrapperToPrimitive(Class<?> ...clazzes) {
		Class<?>[] result = clazzes;
		for(int i = 0; i < result.length; i++) {
			Class<?> curr = result[i];
			Class<?> prim = wrapperToPrimitive(curr);
			if(prim != curr) {
				if(result == clazzes)
					result = clazzes.clone();
				result[i] = prim;
			}
		}
		
		return result;
	}
	
	public static boolean w2pEquals(Class<?> a, Class<?> b) {
		a = wrapperToPrimitive(a);
		b = wrapperToPrimitive(b);
		return a.equals(b);
	}
	
	public static boolean w2pEquals(Class<?>[] a, Class<?> ...b) {
		if(a.length != b.length)
			return false;
		
		for(int i = 0; i < a.length; i++) {
			if(!w2pEquals(a[i], b[i]))
				return false;
		}
		
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?> ...params)
			throws SecurityException, NoSuchMethodException {
		try {
			return clazz.getConstructor(params);
		} catch (NoSuchMethodException e) {
			Class<?>[] primParams = wrapperToPrimitive(params);
			if(primParams != params) {
				try {
					return clazz.getConstructor(primParams);
				}
				catch(NoSuchMethodException e2) {}
			}
			
			Constructor<T>[] ctors = (Constructor<T>[])clazz.getConstructors();
					
			for(Constructor<T> candidate : ctors) {
				if(w2pEquals(candidate.getParameterTypes(), params))
					return candidate;
			}
			
			throw e;
		}
	}
	
	public static Method findMatchingMethod(Class<?> clazz, String name, Object... args) {
		for (Method m : clazz.getMethods()) {
			if (!m.getName().equals(name)) {
				continue;
			}
			
			if (isMatch(m.getParameterTypes(), args)) {
				return m;
			}
		}
		
		return null;
	}
	
	public static boolean isMatch(Class<?>[] paramTypes, Object... args) {
		if (paramTypes.length != args.length) {
			return false;
		}
		
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> paramType = paramTypes[i];
			Object arg = args[i];
			if (paramType.isPrimitive()) {
				if (arg == null) {
					return false;
				}
				Class<?> argType = arg.getClass();
				if (paramType != wrapperToPrimitive(argType)) {
					return false;
				}
			}
			else {
				if (arg != null) {
					Class<?> argType = arg.getClass();
					if (!paramType.isAssignableFrom(argType)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	public static Method findMethod(Class<?> clazz, String name, Class<?> ...params)
			throws SecurityException, NoSuchMethodException {
		try {
			return clazz.getMethod(name, params);
		} catch (NoSuchMethodException e) {
			Class<?>[] primParams = wrapperToPrimitive(params);
			if(primParams != params) {
				try {
					return clazz.getMethod(name, primParams);
				}
				catch(NoSuchMethodException e2) {}
			}
			
			Method[] methods = clazz.getMethods();
			
			for(Method candidate : methods) {
				if(w2pEquals(candidate.getParameterTypes()))
					return candidate;
			}
			
			throw e;
		}
	}
	
	public static Method findMethodRT(Class<?> clazz, String name, Class<?> returnType, Class<?> ...params)
			throws SecurityException, NoSuchMethodException {
		Method m = findMethod(clazz, name, params);
		
		if(returnType == null)
			return m;
		
		Class<?> rt = m.getReturnType();
		
		if(w2pEquals(rt, returnType) || returnType.isAssignableFrom(rt))
			return m;
		
		throw new NoSuchMethodException("Method with matching parameters but incompatible return type " + rt.getName()
				+ " (expected " + returnType.getName() + ") found");
	}
}
