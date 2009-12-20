package com.ikokoon.serenity.model;

import java.io.Serializable;

import javax.persistence.Entity;

/**
 * This class represents a package that the owner is affected by.
 * 
 * @author Michael Couck
 * @since 17.07.09
 * @version 01.00
 */
@Entity
@Unique(fields = { Afferent.NAME })
public class Afferent extends Composite<Object, Object> implements Comparable<Afferent>, Serializable {

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

	public int compareTo(Afferent o) {
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
