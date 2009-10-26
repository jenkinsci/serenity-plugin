package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.Date;

@Unique(fields = { "name" }, discriminator = "com.ikokoon.instrumentation.model.Afferent")
public class Afferent implements Comparable<Afferent>, Serializable {

	private Long id;
	private String name;
	private Date timestamp;

	@Id
	public Long getId() {
		return id;
	}

	@Identifier
	public void setId(Long id) {
		this.id = id;
	}

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
		return id + ":" + name;
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
