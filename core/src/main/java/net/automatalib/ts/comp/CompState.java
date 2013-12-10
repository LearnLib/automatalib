package net.automatalib.ts.comp;

import java.util.Objects;

public class CompState<S1, S2> {
	
	public final S1 s1;
	public final S2 s2;

	public CompState(S1 s1, S2 s2) {
		this.s1 = s1;
		this.s2 = s2;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Objects.hashCode(s1);
		result = prime * result + Objects.hashCode(s2);
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
		if (obj.getClass() != CompState.class)
			return false;
		CompState<?,?> other = (CompState<?,?>) obj;
		
		return Objects.equals(s1, other.s1) && Objects.equals(s2, other.s2);
	}
	
	

}
