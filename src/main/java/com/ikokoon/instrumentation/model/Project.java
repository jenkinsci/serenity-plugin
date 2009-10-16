package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.Date;

//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Inheritance;
//import javax.persistence.InheritanceType;

import com.ikokoon.persistence.Id;
import com.ikokoon.persistence.Identifier;
import com.ikokoon.persistence.Parent;
import com.ikokoon.persistence.Unique;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
// @Entity
// @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Parent(parent = true)
@Unique(fields = { "name" })
public class Project implements Serializable {

	private Long id;
	private String name;
	private Date timestamp;
	private long linesExecuted;

	@Id
	// @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

	public long getLinesExecuted() {
		return linesExecuted;
	}

	public void setLinesExecuted(long linesExecuted) {
		this.linesExecuted = linesExecuted;
	}
}
