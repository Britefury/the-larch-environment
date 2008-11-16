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

abstract class ListViewLayout
{
	public enum TrailingSeparator { NEVER, ONE_ELEMENT, ALWAYS };
	

	abstract Element layoutChildren(List<Element> children, ElementFactory beginDelim, ElementFactory endDelim, ElementFactory separator);
	
	
	
	protected boolean trailingSeparatorRequired(List<Element> children, TrailingSeparator trailingSeparator)
	{
		return trailingSeparator == TrailingSeparator.ALWAYS  ||  ( trailingSeparator == TrailingSeparator.ONE_ELEMENT && children.size() == 1 );
	}
}
