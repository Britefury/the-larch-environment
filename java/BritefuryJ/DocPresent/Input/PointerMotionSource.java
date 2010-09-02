//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.event.MouseEvent;
import java.util.Stack;

import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.Math.Point2;

public class PointerMotionSource
{
	private Stack<PointerInputElement> elementsUnderPointer = new Stack<PointerInputElement>();
	private PointerInputElement rootElement;
	private PointerInterface pointer;
	
	
	
	public PointerMotionSource(PointerInterface pointer, PointerInputElement rootElement)
	{
		this.pointer = pointer;
		this.rootElement = rootElement;
	}




	public void motion(Point2 pos, MouseEvent mouseEvent)
	{
		PointerMotionEvent event = new PointerMotionEvent( pointer, PointerMotionEvent.Action.MOTION );
		handleMotion( event );
	}
	
	public void enter(Point2 pos)
	{
		handleEnter( rootElement, new PointerMotionEvent( pointer, PointerMotionEvent.Action.ENTER ) );
	}

	public void leave(Point2 pos)
	{
		handleLeave( 0, new PointerMotionEvent( pointer, PointerMotionEvent.Action.LEAVE ) );
	}

	
	

	private void handleMotion(PointerMotionEvent event)
	{
		PointerInputElement elementUnderPointer = rootElement;
		
		int index = 0;
		for (PointerInputElement element: elementsUnderPointer)
		{
			if ( elementUnderPointer == element )
			{
				event = (PointerMotionEvent)element.transformParentToLocalEvent( event );
				
				sendMotionEvent( element, event );

				Point2 localPos = event.getPointer().getLocalPos();
				
				elementUnderPointer = element.getFirstPointerChildAtLocalPoint( localPos );
			}
			else
			{
				handleLeave( index, event );
				handleEnter( elementUnderPointer, event );
				return;
			}
			
			index++;
		}
	}

	private void handleEnter(PointerInputElement element, PointerMotionEvent event)
	{
		while ( element != null )
		{
			elementsUnderPointer.push( element );
			sendEnterEvent( element, event );
			
			
			// Handle child elements
			Point2 localPos = event.getPointer().getLocalPos();
			
			PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( localPos );
			if ( childElement != null )
			{
				sendLeaveIntoChildEvent( element, event.withAction( PointerMotionEvent.Action.LEAVE ) );
				event = (PointerMotionEvent)childElement.transformParentToLocalEvent( event );
			}
			element = childElement;
		}
	}
	
	private void handleLeave(int index, PointerMotionEvent event)
	{
		Stack<PointerMotionEvent> events = new Stack<PointerMotionEvent>();
		
		events.push( event );
		for (PointerInputElement element: elementsUnderPointer.subList( index + 1, elementsUnderPointer.size() ))
		{
			event = (PointerMotionEvent)element.transformParentToLocalEvent( event );
			events.push( event );
		}
		
		while ( elementsUnderPointer.size() > index )
		{
			PointerInputElement element = elementsUnderPointer.pop();
			event = events.pop();
			
			sendLeaveEvent( element, event );
			
			if ( elementsUnderPointer.size() > index )
			{
				sendEnterFromChildEvent( elementsUnderPointer.lastElement(), events.lastElement().withAction( PointerMotionEvent.Action.ENTER ) );
			}
		}
	}
	
	
	private void sendMotionEvent(PointerInputElement element, PointerMotionEvent event)
	{
		// TODO
	}
	
	private void sendEnterEvent(PointerInputElement element, PointerMotionEvent event)
	{
		// TODO
	}
	
	private void sendLeaveEvent(PointerInputElement element, PointerMotionEvent event)
	{
		// TODO
	}
	
	private void sendLeaveIntoChildEvent(PointerInputElement element, PointerMotionEvent event)
	{
		// TODO
	}
	
	private void sendEnterFromChildEvent(PointerInputElement element, PointerMotionEvent event)
	{
		// TODO
	}
}
