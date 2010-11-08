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
import BritefuryJ.Parser.ParseResult;
import BritefuryJ.Parser.ParserExpression;

public abstract class SequentialParsingTreeEventListener implements TreeEventListener
{
	protected abstract Class<? extends SelectionEditTreeEvent> getSelectionEditTreeEventClass();
	
	
	protected boolean testValueEmpty(DPElement element, GSymFragmentView fragment, Object model, StreamValue value)
	{
		return false;
	}
	
	protected boolean testValue(DPElement element, GSymFragmentView fragment, Object model, StreamValue value)
	{
		return true;
	}
	
	
	protected abstract ParserExpression getParser();
	
	protected Object postParseResult(Object value)
	{
		return value;
	}


	protected boolean handleEmptyValue(DPElement element, GSymFragmentView fragment, Object event, Object model)
	{
		return false;
	}
	
	protected abstract boolean handleParseSuccess(DPElement element, GSymFragmentView fragment, Object event, Object model, StreamValue value, Object parsed);
	
	
	protected boolean handleParseFailure(DPElement element, GSymFragmentView fragment, Object event, Object model, StreamValue value)
	{
		return false;
	}
	
	
	
	@Override
	public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
	{
		if ( event instanceof TextEditEvent  ||  isSelectionEditEvent( event ) )
		{
			// If event is a selection edit event, and its source element is @element, then @element has had its fixed value
			// set by a SequentialEditHandler - so don't clear it.
			// Otherwise, clear all fixed values on a path from @sourceElement to @element
			if ( !( isSelectionEditEvent( event )  &&  getEventSourceElement( event ) == element ) )
			{
				sourceElement.clearFixedValuesOnPathUpTo( element );
				element.clearFixedValue();
			}
			
			StreamValue value = element.getStreamValue();
			GSymFragmentView fragment = (GSymFragmentView)element.getFragmentContext();
			Object model = fragment.getModel();
			
			if ( value.isEmpty()  ||  testValueEmpty( element, fragment, model, value ) )
			{
				return handleEmptyValue( element, fragment, event, model );
			}
			else if ( testValue( element, fragment, model, value ) )
			{
				Object parsed[] = parseStream( value );
				
				if ( parsed != null )
				{
					return handleParseSuccess( element, fragment, event, model, value, parsed[0] );
				}
				else
				{
					return handleParseFailure( element, fragment, event, model, value );
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	
	private Object[] parseStream(StreamValue value)
	{
		ParseResult res = getParser().parseStreamItems( value );
		if ( res.isValid() )
		{
			if ( res.getEnd() == value.length() )
			{
				return new Object[] { postParseResult( res.getValue() ) };
			}
		}
		return null;
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
