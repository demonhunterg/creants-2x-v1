package com.creants.creants_2x.mmo;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author LamHM
 *
 */
public class P3D {
	public final int px;
	public final int py;
	public final int pz;


	public P3D(final int x, final int y, final int z) {
		this.px = x;
		this.py = y;
		this.pz = z;
	}


	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof P3D)) {
			return false;
		}
		final P3D p2 = (P3D) obj;
		return p2.px == this.px && p2.py == this.py && p2.pz == this.pz;
	}


	@Override
	public int hashCode() {
		return new HashCodeBuilder(23, 31).append(this.px).append(this.py).append(this.pz).toHashCode();
	}


	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", this.px, this.py, this.pz);
	}
}
