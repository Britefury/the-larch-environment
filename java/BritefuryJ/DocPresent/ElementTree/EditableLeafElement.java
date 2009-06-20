//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPContentLeafEditable;

public class EditableLeafElement extends LeafElement
{
	//
	// Constructor
	//
	
	protected EditableLeafElement(DPContentLeafEditable widget, String textRepresentation)
	{
		super( widget, textRepresentation );
	}



	//
	// Widget
	//
	
	public DPContentLeafEditable getWidget()
	{
		return (DPContentLeafEditable)widget;
	}
	
	
	
	
	
	//
	// Text representation modification methods
	//
	
	public void setTextRepresentation(String newTextRepresentation)
	{
		getWidget().setTextRepresentation( newTextRepresentation );
	}
}
