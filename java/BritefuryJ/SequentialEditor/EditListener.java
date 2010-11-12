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
import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.GSym.View.GSymFragmentView;

public abstract class EditListener implements TreeEventListener
{
	public enum HandleEditResult
	{
		HANDLED,
		NOT_HANDLED,
		PASS_TO_PARENT
	};
	
	
	protected abstract SequentialEditor getSequentialEditor();
	
	
	protected Class<? extends SelectionEditTreeEvent> getSelectionEditTreeEventClass()
	{
		return getSequentialEditor().getSelectionEditTreeEventClass();
	}
	
	private boolean isSelectionEditEvent(Object event)
	{
		return getSequentialEditor().isSelectionEditEvent( event );
	}
	
	protected boolean isEditEvent(Object event)
	{
		return getSequentialEditor().isEditEvent( event );
	}
	
	
	protected abstract HandleEditResult handleValue(DPElement element, DPElement sourceElement, GSymFragmentView fragment, Object event, Object model, StreamValue value);
	
	
	
	@Override
	public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
	{
		if ( event instanceof TextEditEvent  ||  isSelectionEditEvent( event )  ||  isEditEvent( event ) )
		{
			// If event is a selection edit event, and its source element is @element, then @element has had its fixed value
			// set by a SequentialEditHandler - so don't clear it.
			// Otherwise, clear all fixed values on a path from @sourceElement to @element
			if ( !( isSelectionEditEvent( event )  &&  SequentialEditor.getEventSourceElement( event ) == element ) )
			{
				element.clearFixedValue();
			}
			
			StreamValue value = element.getStreamValue();
			GSymFragmentView fragment = (GSymFragmentView)element.getFragmentContext();
			Object model = fragment.getModel();
			
			HandleEditResult res = handleValue( element, sourceElement, fragment, event, model, value );
			if ( res == HandleEditResult.HANDLED )
			{
				return true;
			}
			else if ( res == HandleEditResult.NOT_HANDLED )
			{
				return false;
			}
			else if ( res == HandleEditResult.PASS_TO_PARENT )
			{
				element.postTreeEventToParent( event );
				return true;
			}
			else
			{
				throw new RuntimeException( "Invalid HandleEditResult" );
			}
		}
		else
		{
			return false;
		}
	}
}
