//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.SequentialEditor;

import BritefuryJ.DocPresent.DPElement;

public class SelectionEditTreeEvent
{
	protected SequentialEditHandler editHandler;
	protected DPElement sourceElement;
	
	
	protected SelectionEditTreeEvent(SequentialEditHandler editHandler, DPElement sourceElement)
	{
		this.editHandler = editHandler;
		this.sourceElement = sourceElement;
	}
	
	
	public SequentialEditHandler getEditHandler()
	{
		return editHandler;
	}
	
	public DPElement getSourceElement()
	{
		return sourceElement;
	}
}
