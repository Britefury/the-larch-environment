//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Stack;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.HoverElementInteractor;
import BritefuryJ.LSpace.Interactor.MotionElementInteractor;
import BritefuryJ.Math.Point2;

public class PointerMotionInteractor extends PointerInteractor implements Pointer.ElementUnrealiseListener
{
	private Stack<LSElement> elementsUnderPointer = new Stack<LSElement>();
	private LSElement rootElement;
	
	
	
	public PointerMotionInteractor(LSElement rootElement)
	{
		this.rootElement = rootElement;
	}




	public boolean motion(Pointer pointer, PointerMotionEvent event, MouseEvent mouseEvent)
	{
		handleMotion( pointer, event );
		return true;
	}
	
	public boolean enter(Pointer pointer, PointerMotionEvent event)
	{
		handleEnter( pointer, rootElement, event );
		return true;
	}

	public boolean leave(Pointer pointer, PointerMotionEvent event)
	{
		handleLeave( pointer, 0, event );
		return true;
	}

	

	public void handleMotion(Pointer pointer, PointerMotionEvent event)
	{
		LSElement elementUnderPointer = rootElement;
		
		int index = 0;
		for (LSElement element: elementsUnderPointer)
		{
			if ( elementUnderPointer == element )
			{
				event = (PointerMotionEvent)element.transformParentToLocalEvent( event );
				
				sendMotionEvent( element, event );

				Point2 localPos = event.getPointer().getLocalPos();
				
				elementUnderPointer = element.getFirstChildAtLocalPoint( localPos );
			}
			else
			{
				handleLeave( pointer, index, event );
				handleEnter( pointer, elementUnderPointer, event );
				return;
			}
			
			index++;
		}
		
		handleEnter( pointer, elementUnderPointer, event );
	}

	private void handleEnter(Pointer pointer, LSElement element, PointerMotionEvent event)
	{
		while ( element != null )
		{
			elementsUnderPointer.push( element );
			pointer.addUnrealiseListener( element, this );
			sendEnterEvent( element, event );
			
			
			// Handle child elements
			Point2 localPos = event.getPointer().getLocalPos();
			
			LSElement childElement = element.getFirstChildAtLocalPoint( localPos );
			if ( childElement != null )
			{
				sendLeaveIntoChildEvent( element, event.withAction( PointerMotionEvent.Action.LEAVE ) );
				event = (PointerMotionEvent)childElement.transformParentToLocalEvent( event );
			}
			element = childElement;
		}
	}
	
	private void handleLeave(Pointer pointer, int index, PointerMotionEvent event)
	{
		Stack<PointerMotionEvent> events = new Stack<PointerMotionEvent>();
		
		if ( index < elementsUnderPointer.size() )
		{
			events.push( event );
			for (LSElement element: elementsUnderPointer.subList( index + 1, elementsUnderPointer.size() ))
			{
				event = (PointerMotionEvent)element.transformParentToLocalEvent( event );
				events.push( event );
			}
			
			while ( elementsUnderPointer.size() > index )
			{
				LSElement element = elementsUnderPointer.pop();
				pointer.addUnrealiseListener( element, this );
				event = events.pop();
				
				sendLeaveEvent( element, event );
				
				if ( elementsUnderPointer.size() > index )
				{
					sendEnterFromChildEvent( elementsUnderPointer.lastElement(), events.lastElement().withAction( PointerMotionEvent.Action.ENTER ) );
				}
			}
		}
	}

	public void notifyPointerElementUnrealised(Pointer pointer, LSElement element)
	{
		int index = 0;
		PointerMotionEvent event = new PointerMotionEvent( pointer, PointerMotionEvent.Action.LEAVE );
		for (LSElement e: elementsUnderPointer)
		{
			if ( e == element )
			{
				handleLeave( pointer, index, event );
				return;
			}
			else
			{
				event = (PointerMotionEvent)element.transformParentToLocalEvent( event );
			}
			
			index++;
		}
	}
	
	
	private void sendMotionEvent(LSElement element, PointerMotionEvent event)
	{
		if ( element.isRealised() )
		{
			List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( MotionElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					MotionElementInteractor motionInt = (MotionElementInteractor)interactor;
					try
					{
						motionInt.pointerMotion( element, event );
					}
					catch (Throwable e)
					{
						element.notifyExceptionDuringElementInteractor( motionInt, "pointerMotion", e );
					}
				}
			}
		}
	}
	
	private void sendEnterEvent(LSElement element, PointerMotionEvent event)
	{
		if ( element.isRealised() )
		{
			List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( HoverElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					HoverElementInteractor motionInt = (HoverElementInteractor)interactor;
					try
					{
						motionInt.pointerEnter( element, event );
					}
					catch (Throwable e)
					{
						element.notifyExceptionDuringElementInteractor( motionInt, "pointerEnter", e );
					}
				}
			}
			
			event.getPointer().concretePointer().notifyEnterElement( element );
			element.handlePointerEnter( event );
		}
	}
	
	private void sendLeaveEvent(LSElement element, PointerMotionEvent event)
	{
		if ( element.isRealised() )
		{
			event.getPointer().concretePointer().notifyLeaveElement( element );
			element.handlePointerLeave( event );

			List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( HoverElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					HoverElementInteractor motionInt = (HoverElementInteractor)interactor;
					try
					{
						motionInt.pointerLeave( element, event );
					}
					catch (Throwable e)
					{
						element.notifyExceptionDuringElementInteractor( motionInt, "pointerLeave", e );
					}
				}
			}
		}
	}
	
	private void sendLeaveIntoChildEvent(LSElement element, PointerMotionEvent event)
	{
		if ( element.isRealised() )
		{
			List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( MotionElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					MotionElementInteractor motionInt = (MotionElementInteractor)interactor;
					try
					{
						motionInt.pointerLeaveIntoChild( element, event );
					}
					catch (Throwable e)
					{
						element.notifyExceptionDuringElementInteractor( motionInt, "pointerLeaveIntoChild", e );
					}
				}
			}
		}
	}
	
	private void sendEnterFromChildEvent(LSElement element, PointerMotionEvent event)
	{
		if ( element.isRealised() )
		{
			List<AbstractElementInteractor> interactors = element.getElementInteractorsCopy( MotionElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					MotionElementInteractor motionInt = (MotionElementInteractor)interactor;
					try
					{
						motionInt.pointerEnterFromChild( element, event );
					}
					catch (Throwable e)
					{
						element.notifyExceptionDuringElementInteractor( motionInt, "pointerEnterFromChild", e );
					}
				}
			}
		}
	}
}
