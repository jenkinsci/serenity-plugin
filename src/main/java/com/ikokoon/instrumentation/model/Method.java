package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

import com.ikokoon.persistence.Identifier;
import com.ikokoon.persistence.Unique;
import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Unique(fields = { "parent", "name", "description" })
@NamedQueries( {
		@NamedQuery(name = Method.SELECT_METHODS, query = Method.SELECT_METHODS),
		@NamedQuery(name = Method.SELECT_METHODS_BY_NAME, query = Method.SELECT_METHODS_BY_NAME),
		@NamedQuery(name = Method.SELECT_METHOD_BY_CLASS_NAME_AND_METHOD_NAME_AND_METHOD_DESCRIPTION, query = Method.SELECT_METHOD_BY_CLASS_NAME_AND_METHOD_NAME_AND_METHOD_DESCRIPTION) })
public class Method implements Comparable<Method>, Serializable {

	public static final String SELECT_METHODS = "select a from Method as a";
	public static final String SELECT_METHODS_BY_NAME = "select a from Method as a where a.name = :name";
	public static final String SELECT_METHOD_BY_CLASS_NAME_AND_METHOD_NAME_AND_METHOD_DESCRIPTION = "select a from Method as a where a.parent.name = :className and a.name = :methodName and a.description = :methodDescription";

	public static final int PRECISION = 2;

	private Long id;
	private String name;
	private Class parent;
	private String className;
	private String description;
	private double complexity;
	private double lines;
	private double coverage;
	private Date timestamp;
	private Collection<Line> children = new ArrayList<Line>();

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
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

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	public Class getParent() {
		return parent;
	}

	public void setParent(Class parent) {
		this.parent = parent;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parent", fetch = FetchType.EAGER)
	public Collection<Line> getChildren() {
		return children;
	}

	public void setChildren(Collection<Line> children) {
		this.children = children;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getComplexity() {
		return Toolkit.format(complexity, PRECISION);
	}

	public void setComplexity(double complexity) {
		this.complexity = complexity;
	}

	public double getLines() {
		return Toolkit.format(lines, PRECISION);
	}

	public void setLines(double lines) {
		this.lines = lines;
	}

	public double getCoverage() {
		return Toolkit.format(coverage, PRECISION);
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public String toString() {
		return id + ":" + name;
	}

	public int compareTo(Method o) {
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
