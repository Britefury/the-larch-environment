//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View.ListView;

import java.util.List;

import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementFactory;

public abstract class ListViewLayout
{
	public enum TrailingSeparator { NEVER, ONE_ELEMENT, ALWAYS };
	

	public abstract Element createListElement(List<Element> children, ElementFactory beginDelim, ElementFactory endDelim, SeparatorElementFactory separator);
	
	
	
	protected boolean trailingSeparatorRequired(List<Element> children, TrailingSeparator trailingSeparator)
	{
		return children.size() > 0  &&  ( trailingSeparator == TrailingSeparator.ALWAYS  ||  ( trailingSeparator == TrailingSeparator.ONE_ELEMENT && children.size() == 1 ) );
	}
}
