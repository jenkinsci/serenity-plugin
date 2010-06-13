package com.ikokoon.serenity.model;

import java.io.Serializable;
import java.util.Date;

public class Snapshot<E, F> extends Composite<Package<?, ?>, Method<?, ?>> implements Comparable<Class<?, ?>>, Serializable {

	private long net;
	private long total;
	private long wait;
	private Date start;
	private Date end;

	public long getNet() {
		return net;
	}

	public void setNet(long net) {
		this.net = net;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public long getWait() {
		return wait;
	}

	public void setWait(long wait) {
		this.wait = wait;
	}

	public int compareTo(Class<?, ?> o) {
		int comparison = 0;
		if (this.getId() != null && o.getId() != null) {
			comparison = this.getId().compareTo(o.getId());
		}
		return comparison;
	}

}
