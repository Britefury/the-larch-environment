//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.LinearRepresentationEditor;

import BritefuryJ.DocPresent.DPElement;

public class SelectionEditTreeEvent
{
	protected LinearRepresentationEditHandler editHandler;
	protected DPElement sourceElement;
	
	
	protected SelectionEditTreeEvent(LinearRepresentationEditHandler editHandler, DPElement sourceElement)
	{
		this.editHandler = editHandler;
		this.sourceElement = sourceElement;
	}
	
	
	public LinearRepresentationEditHandler getEditHandler()
	{
		return editHandler;
	}
	
	public DPElement getSourceElement()
	{
		return sourceElement;
	}
}
