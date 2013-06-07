package com.ikokoon.toolkit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.toolkit.Permutations;

public class PermutationsTest extends ATest {

	@Test
	public void permutations() {
		Permutations permutations = new Permutations();

		String[] strings = new String[] { "one", "two", "three" };
		List<Object[]> permutationsList = new ArrayList<Object[]>();
		permutations.getPermutations(strings, permutationsList, strings.length);
		for (Object[] stringArray : permutationsList) {
			for (Object string : stringArray) {
				LOGGER.debug(string + ":");
			}
		}
	}

}
