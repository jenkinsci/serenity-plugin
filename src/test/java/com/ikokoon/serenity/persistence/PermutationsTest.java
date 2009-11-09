package com.ikokoon.serenity.persistence;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ikokoon.serenity.ATest;

public class PermutationsTest extends ATest {

	@Test
	public void permutations() {
		Permutations permutations = new Permutations();

		String[] strings = new String[] { "one", "two", "three" };
		List<Object[]> permutationsList = new ArrayList<Object[]>();
		permutations.getPermutations(strings, permutationsList, strings.length);
		for (Object[] stringArray : permutationsList) {
			for (Object string : stringArray) {
				logger.debug(string + ":");
			}
		}
	}

}
