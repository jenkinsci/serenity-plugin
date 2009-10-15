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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.ikokoon.persistence.Identifier;
import com.ikokoon.persistence.Unique;

@Entity
@Unique(fields = { "name" })
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries( { @NamedQuery(name = Efferent.SELECT_EFFERENTS, query = Efferent.SELECT_EFFERENTS),
		@NamedQuery(name = Efferent.SELECT_EFFERENTS_BY_NAME, query = Efferent.SELECT_EFFERENTS_BY_NAME),
		@NamedQuery(name = Efferent.SELECT_EFFERENTS_BY_NAME_AND_TYPE, query = Efferent.SELECT_EFFERENTS_BY_NAME_AND_TYPE) })
public class Efferent implements Comparable<Efferent>, Serializable {

	public static final String SELECT_EFFERENTS = "select a from Efferent as a";
	public static final String SELECT_EFFERENTS_BY_NAME = "select a from Efferent as a where a.name = :efferentName";
	public static final String SELECT_EFFERENTS_BY_NAME_AND_TYPE = "select a from Efferent as a where a.name = :efferentName and a.type = :type";

	private Long id;
	private String name;
	private Date timestamp;
	private Collection<Class> bases = new ArrayList<Class>();

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

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@ManyToMany(targetEntity = Class.class, mappedBy = "efferentPackages", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }, fetch = FetchType.LAZY)
	public Collection<Class> getBases() {
		return bases;
	}

	public void setBases(Collection<Class> bases) {
		this.bases = bases;
	}

	public String toString() {
		return id + ":" + name;
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
