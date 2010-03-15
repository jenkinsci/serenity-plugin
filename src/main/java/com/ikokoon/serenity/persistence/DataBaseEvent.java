package com.ikokoon.serenity.persistence;

public class DataBaseEvent implements IDataBaseEvent {

	private IDataBase dataBase;
	private Type type;

	public DataBaseEvent(IDataBase dataBase, Type type) {
		this.dataBase = dataBase;
		this.type = type;
	}

	public IDataBase getDataBase() {
		return dataBase;
	}

	public Type getEventType() {
		return type;
	}

}
