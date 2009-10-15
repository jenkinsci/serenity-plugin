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
import javax.persistence.ManyToMany;
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
@Unique(fields = { "name" })
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries( { @NamedQuery(name = Class.SELECT_CLASSES, query = Class.SELECT_CLASSES),
		@NamedQuery(name = Class.SELECT_CLASSS_BY_NAME, query = Class.SELECT_CLASSS_BY_NAME) })
public class Class implements Comparable<Class>, Serializable {

	public static final String SELECT_CLASSES = "select a from Class as a";
	public static final String SELECT_CLASSS_BY_NAME = "select a from Class as a where a.name = :className";

	public static final int PRECISION = 2;

	private Long id;
	private String name;
	private Package parent;
	private double complexity;
	private double coverage;
	private double stability;
	private double efferent;
	private double afferent;
	private boolean interfaze;
	private Date timestamp;
	private Collection<Method> children = new ArrayList<Method>();
	private Collection<Efferent> efferentPackages = new ArrayList<Efferent>();
	private Collection<Afferent> afferentPackages = new ArrayList<Afferent>();

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
	public Package getParent() {
		return parent;
	}

	public void setParent(Package parent) {
		this.parent = parent;
	}

	public String getNameTrimmed() {
		if (getName() != null) {
			int index = getName().lastIndexOf('.');
			if (index > -1) {
				return getName().substring(index + 1, getName().length());
			}
		}
		return null;
	}

	public double getComplexity() {
		return Toolkit.format(complexity, PRECISION);
	}

	public void setComplexity(double complexity) {
		this.complexity = complexity;
	}

	public double getCoverage() {
		return Toolkit.format(coverage, PRECISION);
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public double getStability() {
		return Toolkit.format(stability, PRECISION);
	}

	public void setStability(double stability) {
		this.stability = stability;
	}

	public double getEfferent() {
		return Toolkit.format(efferent, PRECISION);
	}

	public void setEfferent(double efferent) {
		this.efferent = efferent;
	}

	public double getAfferent() {
		return Toolkit.format(afferent, PRECISION);
	}

	public void setAfferent(double afferent) {
		this.afferent = afferent;
	}

	public boolean getInterfaze() {
		return interfaze;
	}

	public void setInterfaze(boolean interfaze) {
		this.interfaze = interfaze;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parent", fetch = FetchType.EAGER)
	public Collection<Method> getChildren() {
		return children;
	}

	public void setChildren(Collection<Method> children) {
		this.children = children;
	}

	@ManyToMany(targetEntity = Efferent.class, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	public Collection<Efferent> getEfferentPackages() {
		return efferentPackages;
	}

	public void setEfferentPackages(Collection<Efferent> errerent) {
		this.efferentPackages = errerent;
	}

	@ManyToMany(targetEntity = Afferent.class, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	public Collection<Afferent> getAfferentPackages() {
		return afferentPackages;
	}

	public void setAfferentPackages(Collection<Afferent> afferent) {
		this.afferentPackages = afferent;
	}

	public String toString() {
		return id + ":" + name;
	}

	public int compareTo(Class o) {
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
