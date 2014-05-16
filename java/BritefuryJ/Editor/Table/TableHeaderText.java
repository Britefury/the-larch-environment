//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2014.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;

import java.util.Arrays;
import java.util.List;

public abstract class TableHeaderText extends TableHeader {
	public static class TableHeaderTextList extends TableHeaderText {
		private List<String> texts;

		public TableHeaderTextList(List<String> texts, TableHeader defaultContents) {
			super(defaultContents);
			this.texts = texts;
		}

		public TableHeaderTextList(List<String> texts) {
			super();
			this.texts = texts;
		}

		public TableHeaderTextList(String texts[], TableHeader defaultContents) {
			this(Arrays.asList(texts), defaultContents);
		}

		public TableHeaderTextList(String texts[]) {
			this(Arrays.asList(texts));
		}


		public String headerCellText(int index) {
			if (index < texts.size()) {
				return texts.get(index);
			}
			else {
				return null;
			}
		}
	}


	private TableHeader defaultContents;


	public TableHeaderText() {
		this.defaultContents = index;
	}

	public TableHeaderText(TableHeader defaultContents) {
		this.defaultContents = defaultContents;
	}


	@Override
	public Pres headerCell(int index) {
		String text = headerCellText(index);
		return text != null  ?  new Label(text)  :  null;
	}

	public abstract String headerCellText(int index);


	public static TableHeaderTextList forList(List<String> texts, TableHeader defaultContents) {
		return new TableHeaderTextList(texts, defaultContents);
	}

	public static TableHeaderTextList forList(List<String> texts) {
		return new TableHeaderTextList(texts);
	}

	public static TableHeaderTextList forArray(String texts[], TableHeader defaultContents) {
		return new TableHeaderTextList(texts, defaultContents);
	}

	public static TableHeaderTextList forArray(String texts[]) {
		return new TableHeaderTextList(texts);
	}
}
