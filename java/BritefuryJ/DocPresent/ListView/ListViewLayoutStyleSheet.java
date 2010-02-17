//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ListView;

import java.util.List;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.ListView.ListViewStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.DocPresent.ListView.SeparatorElementFactory;

public abstract class ListViewLayoutStyleSheet extends StyleSheet
{
	protected ListViewLayoutStyleSheet()
	{
		super();
	}

	protected ListViewLayoutStyleSheet(StyleSheet prototype)
	{
		super( prototype );
	}
	
	
	
	public abstract DPWidget createListElement(List<DPWidget> children, PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
			ElementFactory spacing, ListViewStyleSheet.TrailingSeparator trailingSeparator);
}
