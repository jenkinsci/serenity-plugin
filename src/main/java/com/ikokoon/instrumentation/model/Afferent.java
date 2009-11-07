package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.Date;

@Unique(fields = { Afferent.NAME })
public class Afferent extends Composite<Object, Object> implements Comparable<Afferent>, Serializable {

	private String name;
	private Date timestamp;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
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
