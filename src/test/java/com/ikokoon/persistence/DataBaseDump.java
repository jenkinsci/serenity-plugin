package com.ikokoon.persistence;

import java.io.File;

import org.junit.Test;

import com.ikokoon.ATest;

public class DataBaseDump extends ATest {

	@Test
	public void dump() {
		File file = new File("C:/Eclipse/workspace/serenity/serenity/serenity.db");
		IDataBase dataBase = IDataBase.DataBase.getDataBase(file);
	}

}
