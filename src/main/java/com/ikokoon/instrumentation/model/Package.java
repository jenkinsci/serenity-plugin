package com.ikokoon.instrumentation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

//import javax.persistence.CascadeType;
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Inheritance;
//import javax.persistence.InheritanceType;
//import javax.persistence.NamedQueries;
//import javax.persistence.NamedQuery;
//import javax.persistence.OneToMany;

import com.ikokoon.persistence.Id;
import com.ikokoon.persistence.Identifier;
import com.ikokoon.persistence.Parent;
import com.ikokoon.persistence.Unique;
import com.ikokoon.toolkit.Toolkit;

/**
 * @author Michael Couck
 * @since 12.08.09
 * @version 01.00
 */
// @Entity
// @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
// @NamedQueries( { @NamedQuery(name = Package.SELECT_PACKAGES, query = Package.SELECT_PACKAGES),
// @NamedQuery(name = Package.SELECT_PACKAGES_BY_NAME, query = Package.SELECT_PACKAGES_BY_NAME) })
@Parent(parent = true)
@Unique(fields = { "name" })
public class Package implements Comparable<Package>, Serializable {

	public static final String SELECT_PACKAGES = "select a from Package as a";
	public static final String SELECT_PACKAGES_BY_NAME = "select a from Package as a where a.name = :name";

	public static final int PRECISION = 2;

	private Long id;
	private String name;
	private double complexity;
	private double coverage;
	private double abstractness;
	private double stability;
	private double distance;
	private double interfaces;
	private double implementations;
	private double efferent;
	private double afferent;
	private Date timestamp;
	private Collection<Class> children = new ArrayList<Class>();

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

	// @OneToMany(cascade = { CascadeType.ALL }, mappedBy = "parent", fetch = FetchType.EAGER)
	public Collection<Class> getChildren() {
		return children;
	}

	public void setChildren(Collection<Class> children) {
		this.children = children;
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

	public double getAbstractness() {
		return Toolkit.format(abstractness, PRECISION);
	}

	public void setAbstractness(double abstractness) {
		this.abstractness = abstractness;
	}

	public double getStability() {
		return Toolkit.format(stability, PRECISION);
	}

	public void setStability(double stability) {
		this.stability = stability;
	}

	public double getDistance() {
		return Toolkit.format(distance, PRECISION);
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getInterfaces() {
		return Toolkit.format(interfaces, PRECISION);
	}

	public void setInterfaces(double interfaces) {
		this.interfaces = interfaces;
	}

	public double getImplementations() {
		return Toolkit.format(implementations, PRECISION);
	}

	public void setImplementations(double implementations) {
		this.implementations = implementations;
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

	public String toString() {
		return id + ":" + name;
	}

	public int compareTo(Package o) {
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