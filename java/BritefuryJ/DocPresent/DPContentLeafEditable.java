//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.ElementTree.EditableLeafElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;


public abstract class DPContentLeafEditable extends DPContentLeaf
{
	public static class EditableLeafWidgetFilter extends WidgetFilter
	{
		public boolean testLeaf(DPContentLeaf leaf)
		{
			return leaf instanceof DPContentLeafEditable;
		}
	}
	
	
	
	//
	// Constructors
	//
	
	
	protected DPContentLeafEditable()
	{
		super();
	}
	
	protected DPContentLeafEditable(ContentLeafStyleSheet styleSheet)
	{
		super( styleSheet );
	}




	//
	// Specialised getElement()
	//
	
	public EditableLeafElement getElement()
	{
		return (EditableLeafElement)element;
	}
	
	

	
	
	
	//
	// Content modification
	//
	
	public void insertContent(Marker marker, String x)
	{
		EditableLeafElement e = getElement();
		if ( e != null )
		{
			e.insertContent( marker, x );
		}
	}

	public void removeContent(Marker marker, int length)
	{
		EditableLeafElement e = getElement();
		if ( e != null )
		{
			e.removeContent( marker, length );
		}
	}
	
	public void replaceContent(Marker marker, int length, String x)
	{
		EditableLeafElement e = getElement();
		if ( e != null )
		{
			e.replaceContent( marker, length, x );
		}
	}
	
	public void clearContent()
	{
		EditableLeafElement e = getElement();
		if ( e != null )
		{
			e.clearContent();
		}
	}
	
	
	

	//
	// TYPE METHODS
	//
	
	public boolean isEditable()
	{
		return true;
	}
}
