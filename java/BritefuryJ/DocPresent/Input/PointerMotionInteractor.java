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
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.HoverElementInteractor;
import BritefuryJ.DocPresent.Interactor.MotionElementInteractor;
import BritefuryJ.Math.Point2;

public class PointerMotionInteractor extends PointerInteractor
{
	private Stack<PointerInputElement> elementsUnderPointer = new Stack<PointerInputElement>();
	private PointerInputElement rootElement;
	
	
	
	public PointerMotionInteractor(PointerInputElement rootElement)
	{
		this.rootElement = rootElement;
	}




	public boolean motion(Pointer pointer, PointerMotionEvent event, MouseEvent mouseEvent)
	{
		handleMotion( event );
		return true;
	}
	
	public boolean enter(Pointer pointer, PointerMotionEvent event)
	{
		handleEnter( rootElement, event );
		return true;
	}

	public boolean leave(Pointer pointer, PointerMotionEvent event)
	{
		handleLeave( 0, event );
		return true;
	}

	public void elementUnrealised(Pointer pointer, PointerInputElement element)
	{
		handleUnrealise( pointer, element );
	}
	
	

	public void handleMotion(PointerMotionEvent event)
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
		
		handleEnter( elementUnderPointer, event );
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
		
		if ( index < elementsUnderPointer.size() )
		{
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
	}

	private void handleUnrealise(Pointer pointer, PointerInputElement element)
	{
		int index = 0;
		PointerMotionEvent event = new PointerMotionEvent( pointer, PointerMotionEvent.Action.LEAVE );
		for (PointerInputElement e: elementsUnderPointer)
		{
			if ( e == element )
			{
				handleLeave( index, event );
				return;
			}
			else
			{
				event = (PointerMotionEvent)element.transformParentToLocalEvent( event );
			}
			
			index++;
		}
	}
	
	
	private void sendMotionEvent(PointerInputElement element, PointerMotionEvent event)
	{
		if ( element.isPointerInputElementRealised() )
		{
			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( MotionElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					MotionElementInteractor motionInt = (MotionElementInteractor)interactor;
					motionInt.pointerMotion( element, event );
				}
			}
		}
	}
	
	private void sendEnterEvent(PointerInputElement element, PointerMotionEvent event)
	{
		if ( element.isPointerInputElementRealised() )
		{
			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( HoverElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					HoverElementInteractor motionInt = (HoverElementInteractor)interactor;
					motionInt.pointerEnter( element, event );
				}
			}
			
			event.getPointer().concretePointer().notifyEnterElement( element );
			element.handlePointerEnter( event );
		}
	}
	
	private void sendLeaveEvent(PointerInputElement element, PointerMotionEvent event)
	{
		if ( element.isPointerInputElementRealised() )
		{
			event.getPointer().concretePointer().notifyLeaveElement( element );
			element.handlePointerLeave( event );

			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( HoverElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					HoverElementInteractor motionInt = (HoverElementInteractor)interactor;
					motionInt.pointerLeave( element, event );
				}
			}
		}
	}
	
	private void sendLeaveIntoChildEvent(PointerInputElement element, PointerMotionEvent event)
	{
		if ( element.isPointerInputElementRealised() )
		{
			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( MotionElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					MotionElementInteractor motionInt = (MotionElementInteractor)interactor;
					motionInt.pointerLeaveIntoChild( element, event );
				}
			}
		}
	}
	
	private void sendEnterFromChildEvent(PointerInputElement element, PointerMotionEvent event)
	{
		if ( element.isPointerInputElementRealised() )
		{
			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( MotionElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					MotionElementInteractor motionInt = (MotionElementInteractor)interactor;
					motionInt.pointerEnterFromChild( element, event );
				}
			}
		}
	}
}
