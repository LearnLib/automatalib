package net.automatalib.ts.comp;

import java.util.Objects;

public class CompTrans<T1, T2> {

	public final T1 t1;
	public final T2 t2;

	public CompTrans(T1 t1, T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(t1);
		result = prime * result + Objects.hashCode(t2);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj.getClass() != CompTrans.class)
			return false;
		CompTrans<?,?> other = (CompTrans<?,?>) obj;
		
		return Objects.equals(t1, other.t1) && Objects.equals(t2, other.t2);
	}

}
