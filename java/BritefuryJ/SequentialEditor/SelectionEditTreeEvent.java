//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.SequentialEditor;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;

public class SelectionEditTreeEvent extends EditEvent
{
	protected SequentialClipboardHandler clipboardHandler;
	protected DPElement sourceElement;
	
	
	protected SelectionEditTreeEvent(SequentialClipboardHandler clipboardHandler, DPElement sourceElement)
	{
		this.clipboardHandler = clipboardHandler;
		this.sourceElement = sourceElement;
	}
	
	
	public SequentialClipboardHandler getClipboardHandler()
	{
		return clipboardHandler;
	}
	
	public DPElement getSourceElement()
	{
		return sourceElement;
	}
}
