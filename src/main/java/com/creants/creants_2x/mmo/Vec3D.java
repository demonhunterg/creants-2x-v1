package com.creants.creants_2x.mmo;

import java.util.Arrays;
import java.util.List;

/**
 * @author LamHM
 *
 */
public class Vec3D {
	private final Number px;
	private final Number py;
	private final Number pz;
	private final boolean useFloat;


	public static Vec3D fromIntArray(List<Integer> array) {
		if (array.size() != 3) {
			throw new IllegalArgumentException("Wrong array size. Vec3D requires an array with 3 parameters (x,y,z)");
		}
		return new Vec3D(array.get(0), array.get(1), array.get(2));
	}


	public static Vec3D fromFloatArray(List<Float> array) {
		if (array.size() != 3) {
			throw new IllegalArgumentException("Wrong array size. Vec3D requires an array with 3 parameters (x,y,z)");
		}
		return new Vec3D(array.get(0), array.get(1), array.get(2));
	}


	public Vec3D(int ix, int iy, int iz) {
		this.px = ix;
		this.py = iy;
		this.pz = iz;
		this.useFloat = false;
	}


	public Vec3D(float fx, float fy, float fz) {
		this.px = fx;
		this.py = fy;
		this.pz = fz;
		this.useFloat = true;
	}


	public Vec3D(int ix, int iy) {
		this(ix, iy, 0);
	}


	public Vec3D(float fx, float fy) {
		this(fx, fy, 0.0f);
	}


	public boolean isFloat() {
		return useFloat;
	}


	public float floatX() {
		return px.floatValue();
	}


	public float floatY() {
		return py.floatValue();
	}


	public float floatZ() {
		return pz.floatValue();
	}


	public int intX() {
		return px.intValue();
	}


	public int intY() {
		return py.intValue();
	}


	public int intZ() {
		return pz.intValue();
	}


	@Override
	public String toString() {
		return String.format("(%s, %s, %s)", px, py, pz);
	}


	public List<Integer> toIntArray() {
		return Arrays.asList(intX(), intY(), intZ());
	}


	public List<Float> toFloatArray() {
		return Arrays.asList(floatX(), floatY(), floatZ());
	}
}
