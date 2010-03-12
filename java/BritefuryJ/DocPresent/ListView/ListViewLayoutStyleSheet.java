//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ListView;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFactory;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public abstract class ListViewLayoutStyleSheet extends StyleSheet
{
	protected ListViewLayoutStyleSheet()
	{
		super();
	}

	
	
	public abstract DPElement createListElement(List<DPElement> children, PrimitiveStyleSheet primitiveStyle, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator,
			ElementFactory spacing, TrailingSeparator trailingSeparator);
}
