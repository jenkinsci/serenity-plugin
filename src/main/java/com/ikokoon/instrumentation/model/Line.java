package com.ikokoon.instrumentation.model;

import java.io.Serializable;
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

import com.ikokoon.persistence.Identifier;
import com.ikokoon.persistence.Unique;
import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
@Entity
@Unique(fields = { "className", "methodName", "number" })
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries( {
		@NamedQuery(name = Line.SELECT_LINES, query = Line.SELECT_LINES),
		@NamedQuery(name = Line.SELECT_LINES_BY_CLASS_NAME_AND_METHOD_NAME_AND_DESCRIPTION_AND_NUMBER, query = Line.SELECT_LINES_BY_CLASS_NAME_AND_METHOD_NAME_AND_DESCRIPTION_AND_NUMBER) })
public class Line implements Comparable<Line>, Serializable {

	public static final String SELECT_LINES = "select a from Line as a";
	public static final String SELECT_LINES_BY_CLASS_NAME_AND_METHOD_NAME_AND_DESCRIPTION_AND_NUMBER = "select l from Line as l where l.parent = " // Line
			+ "(select m from Method as m where m.parent = " // Method
			+ "(select c from Class as c where c.name = :className) " // Class
			+ "and m.name = :methodName and m.description = :methodDescription) " // Method
			+ "and l.number = :number"; // Line

	public static final int PRECISION = 2;

	private Long id;
	private Method parent;
	private String methodName;
	private String className;
	private double number;
	private double counter;
	private Date timestamp;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}

	@Identifier
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.EAGER)
	public Method getParent() {
		return parent;
	}

	public void setParent(Method parent) {
		this.parent = parent;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
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

	public double getNumber() {
		return Toolkit.format(number, PRECISION);
	}

	public void setNumber(double number) {
		this.number = number;
	}

	public double getCounter() {
		return Toolkit.format(counter, PRECISION);
	}

	public void setCounter(double counter) {
		this.counter = counter;
	}
	
	public void increment() {
		this.counter++;
	}

	public String toString() {
		return id + ":" + (parent != null ? parent.toString() : "No parent method?!") + " : " + number;
	}

	public int compareTo(Line o) {
		int comparison = 0;
		if (this.getId() != null && o.getId() != null) {
			comparison = this.getId().compareTo(o.getId());
		}
		return comparison;
	}

}
