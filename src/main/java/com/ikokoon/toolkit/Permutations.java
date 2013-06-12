package com.ikokoon.toolkit;

import java.util.ArrayList;
import java.util.List;

/**
 * This class generates the permutations for parameters.
 * 
 * @author Michael Couck
 * @since 06.10.09
 * @version 01.00
 */
public class Permutations {

	public void getPermutations(Object[] objects) {
		int N = objects.length;
		Object[] newObjects = new Object[N];
		for (int i = 0; i < N; i++) {
			newObjects[i] = objects[i];
		}
		List<Object[]> permutations = new ArrayList<Object[]>();
		getPermutations(newObjects, permutations, N);
	}

	public void getPermutations(Object[] objects, List<Object[]> permutations, int n) {
		if (n == 1) {
			Object[] objectsCopy = new Object[objects.length];
			System.arraycopy(objects, 0, objectsCopy, 0, objectsCopy.length);
			permutations.add(objectsCopy);
			// permutations.add(Arrays.copyOf(objects, objects.length));
			return;
		}
		for (int i = 0; i < n; i++) {
			swap(objects, i, n - 1);
			getPermutations(objects, permutations, n - 1);
			swap(objects, i, n - 1);
		}
	}

	private Object[] swap(Object[] objects, int i, int j) {
		Object s = objects[i];
		objects[i] = objects[j];
		objects[j] = s;
		return objects;
	}

}