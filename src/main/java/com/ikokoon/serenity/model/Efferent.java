package com.ikokoon.serenity.model;

import java.io.Serializable;

/**
 * This class represents a package that the owner effects.
 * 
 * @author Michael Couck
 * @since 17.07.09
 * @version 01.00
 */
// @Entity
@Unique(fields = { Composite.NAME })
public class Efferent extends Composite<Object, Object> implements Comparable<Efferent>, Serializable {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return getId() + ":" + name;
	}

	public int compareTo(Efferent o) {
		int comparison = 0;
		if (this.getId() != null && o.getId() != null) {
			comparison = this.getId().compareTo(o.getId());
		} else {
			if (this.getName() != null && o.getName() != null) {
				comparison = this.getName().compareTo(o.getName());
			}
		}
		return comparison;
	}

}
