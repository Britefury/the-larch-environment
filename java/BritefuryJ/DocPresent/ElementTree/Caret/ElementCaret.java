//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree.Caret;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ElementTree;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;

public class ElementCaret
{
	protected ElementTree tree;
	protected Caret widgetCaret;
	protected ElementMarker marker;
	
	
	
	public ElementCaret(ElementTree tree, Caret widgetCaret)
	{
		this.tree = tree;
		this.widgetCaret = widgetCaret;
		this.marker = new ElementMarker( tree, widgetCaret.getMarker() );
	}
	
	
	public ElementMarker getMarker()
	{
		return marker;
	}
	
	
	public Element getElement()
	{
		if ( marker != null )
		{
			return marker.getElement();
		}
		else
		{
			return null;
		}
	}
	
	
	
	public boolean isValid()
	{
		if ( marker != null )
		{
			return marker.isValid();
		}
		else
		{
			return false;
		}
	}
}
