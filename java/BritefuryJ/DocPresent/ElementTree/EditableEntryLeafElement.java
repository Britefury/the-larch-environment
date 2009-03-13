//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPContentLeafEditableEntry;

public class EditableEntryLeafElement extends EditableLeafElement
{
	//
	// Constructor
	//
	
	protected EditableEntryLeafElement(DPContentLeafEditableEntry widget, String content)
	{
		super( widget, content );
	}
	
	
	
	//
	// Widget
	//
	
	public DPContentLeafEditableEntry getWidget()
	{
		return (DPContentLeafEditableEntry)widget;
	}
}
