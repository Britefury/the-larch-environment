//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.SequentialEditor;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;

public abstract class EditListener implements TreeEventListener
{
	public enum HandleEditResult
	{
		HANDLED,
		NOT_HANDLED,
		PASS_TO_PARENT
	};
	
	
	protected abstract SequentialEditor getSequentialEditor();
	
	
	protected boolean isSelectionEditEvent(EditEvent event)
	{
		return getSequentialEditor().isSelectionEditEvent( event );
	}
	
	protected boolean isEditEvent(EditEvent event)
	{
		return getSequentialEditor().isEditEvent( event );
	}
	
	
	protected abstract boolean handleEditEvent(DPElement element, DPElement sourceElement, EditEvent event);
	
	
	
	@Override
	public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
	{
		if ( event instanceof EditEvent )
		{
			EditEvent editEvent = (EditEvent)event;
			
			if ( editEvent instanceof TextEditEvent  ||  isSelectionEditEvent( editEvent )  ||  isEditEvent( editEvent ) )
			{
				return handleEditEvent( element, sourceElement, editEvent );
			}
		}
		return false;
	}
}
