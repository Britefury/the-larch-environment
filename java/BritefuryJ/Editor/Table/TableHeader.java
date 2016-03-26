//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
