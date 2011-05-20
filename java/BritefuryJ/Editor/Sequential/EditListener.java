//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.EditEvent;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.IncrementalView.FragmentView;

public abstract class EditListener implements TreeEventListener
{
	public enum HandleEditResult
	{
		HANDLED,
		NO_CHANGE,
		NOT_HANDLED,
		PASS_TO_PARENT
	}
	
	
	protected abstract SequentialEditor getSequentialEditor();
	
	
	protected boolean isSelectionEditEvent(EditEvent event)
	{
		return getSequentialEditor().isSelectionEditEvent( event );
	}
	
	protected boolean isEditEvent(EditEvent event)
	{
		return getSequentialEditor().isEditEvent( event );
	}
	
	
	protected abstract HandleEditResult handleEditEvent(DPElement element, DPElement sourceElement, EditEvent event);
	
	
	
	public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
	{
		if ( event instanceof EditEvent )
		{
			EditEvent editEvent = (EditEvent)event;
			
			if ( editEvent instanceof TextEditEvent  ||  isSelectionEditEvent( editEvent )  ||  isEditEvent( editEvent ) )
			{
				HandleEditResult res = handleEditEvent( element, sourceElement, editEvent );
				
				if ( res == HandleEditResult.HANDLED )
				{
					FragmentView sourceFragment = (FragmentView)sourceElement.getFragmentContext();
					sourceFragment.queueRefresh();
					return true;
				}
				else if ( res == HandleEditResult.NO_CHANGE )
				{
					return true;
				}
				else if ( res == HandleEditResult.NOT_HANDLED )
				{
					return false;
				}
				else if ( res == HandleEditResult.PASS_TO_PARENT )
				{
					element.postTreeEventToParent( editEvent );
					return true;
				}
				else
				{
					throw new RuntimeException( "Invalid HandleEditResult" );
				}
			}
		}
		return false;
	}
}
