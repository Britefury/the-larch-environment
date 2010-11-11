//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.SequentialEditor;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TextEditEvent;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class EditListener implements TreeEventListener
{
	protected abstract Class<? extends SelectionEditTreeEvent> getSelectionEditTreeEventClass();
	
	protected boolean isEditEvent(Object event)
	{
		return false;
	}
	
	
	protected abstract boolean handleValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment, Object event, Object model, StreamValue value);
	
	
	
	@Override
	public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
	{
		if ( event instanceof TextEditEvent  ||  isSelectionEditEvent( event )  ||  isEditEvent( event ) )
		{
			// If event is a selection edit event, and its source element is @element, then @element has had its fixed value
			// set by a SequentialEditHandler - so don't clear it.
			// Otherwise, clear all fixed values on a path from @sourceElement to @element
			if ( !( isSelectionEditEvent( event )  &&  getEventSourceElement( event ) == element ) )
			{
				element.clearFixedValue();
			}
			
			StreamValue value = element.getStreamValue();
			GSymFragmentView fragment = (GSymFragmentView)element.getFragmentContext();
			Object model = fragment.getModel();
			
			return handleValue( element, sourceElement, fragment, event, model, value );
		}
		else
		{
			return false;
		}
	}
	
	
	private boolean isSelectionEditEvent(Object event)
	{
		return getSelectionEditTreeEventClass().isInstance( event );
	}
	
	private DPElement getEventSourceElement(Object event)
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
