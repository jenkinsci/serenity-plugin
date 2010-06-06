package com.ikokoon.serenity.model;

import java.util.Date;

public class Snapshot {

	private long net;
	private long total;
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

}
