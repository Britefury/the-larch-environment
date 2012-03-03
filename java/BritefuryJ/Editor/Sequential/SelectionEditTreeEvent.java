//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.EditEvent;

public class SelectionEditTreeEvent extends EditEvent
{
	protected SequentialEditor sequentialEditor;
	protected LSElement sourceElement;
	
	
	protected SelectionEditTreeEvent(SequentialEditor sequentialEditor, LSElement sourceElement)
	{
		this.sequentialEditor = sequentialEditor;
		this.sourceElement = sourceElement;
	}
	
	
	public SequentialEditor getSequentialEditor()
	{
		return sequentialEditor;
	}
	
	public LSElement getSourceElement()
	{
		return sourceElement;
	}
}
