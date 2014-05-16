//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2014.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Label;

public abstract class TableHeader {
	private static class TableHeaderBlank extends TableHeader {
		@Override
		public Pres headerCell(int index) {
			return new Blank();
		}
	}

	private static class TableHeaderIndex extends TableHeader {
		@Override
		public Pres headerCell(int index) {
			return new Label(String.valueOf(index));
		}
	}


	public static final TableHeader blank = new TableHeaderBlank();
	public static final TableHeader index = new TableHeaderIndex();


	public abstract Pres headerCell(int index);
}
