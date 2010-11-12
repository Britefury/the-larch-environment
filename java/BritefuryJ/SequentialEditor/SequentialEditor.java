//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.SequentialEditor;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;

public abstract class SequentialEditor
{
	protected class ClearStructuralValueListener implements TreeEventListener
	{
		@Override
		public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
		{
			if ( event instanceof TextEditEvent  ||  isSelectionEditEvent( event )  ||  isEditEvent( event ) )
			{
				if ( !( isSelectionEditEvent( event )  &&  getEventSourceElement( event ) == element ) )
				{
					element.clearFixedValue();
				}
			}
			return false;
		}
	}
	
	
	protected ClearStructuralValueListener clearListener = new ClearStructuralValueListener();
	
	
	
	public ClearStructuralValueListener getClearStructuralValueListener()
	{
		return clearListener;
	}
	
	

	
	protected abstract Class<? extends SelectionEditTreeEvent> getSelectionEditTreeEventClass();
	
	protected boolean isEditEvent(Object event)
	{
		return false;
	}

	protected boolean isSelectionEditEvent(Object event)
	{
		return getSelectionEditTreeEventClass().isInstance( event );
	}



	protected static DPElement getEventSourceElement(Object event)
	{
		if ( event instanceof SelectionEditTreeEvent )
		{
			return ((SelectionEditTreeEvent)event).getSourceElement();
		}
		else
		{
			throw new RuntimeException( "Cannot get event source element for an event that is not a SelectionEditTreeEvent" );
		}
	}
}
