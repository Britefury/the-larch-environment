//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input;

import java.util.Stack;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationPanEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationZoomEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.DocPresent.Interactor.NavigationElementInteractor;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class PointerNavigationInteractor extends PointerDragInteractor
{
	private int navigationButton = 0;
	private Point2 navigationDragStartPos = new Point2();
	private Point2 navigationDragCurrentPos = new Point2();
	private boolean bNavigationDragInProgress = false;
	
	private PointerInputElement navElement = null;
	private NavigationElementInteractor navInteractor = null;
	
	
	public PointerNavigationInteractor()
	{
	}





	private boolean testNavigationModifiers(PointerInterface pointer)
	{
		int modifiers = pointer.getModifiers();
		int keys = modifiers & Modifier.KEYS_MASK;
		return keys == Modifier.ALT  ||  keys == Modifier.ALT_GRAPH;
	}
	
	
	
	public boolean canStartDrag(PointerButtonEvent event)
	{
		PointerInterface pointer = event.getPointer();
		return testNavigationModifiers( pointer );
	}
	
	public void dragBegin(PointerButtonEvent event)
	{
		PointerInterface pointer = event.getPointer();
		if ( !bNavigationDragInProgress )
		{
			Point2 pos = pointer.getLocalPos();
			navigationButton = event.getButton();
			navigationDragStartPos = pos.clone();
			navigationDragCurrentPos = pos.clone();
			bNavigationDragInProgress = true;
			handleNavigationGestureBegin( pointer, event );
		}
	}

	public void dragEnd(PointerButtonEvent event, Point2 dragStartPos, int dragButton)
	{
		if ( bNavigationDragInProgress )
		{
			if ( event.getButton() == navigationButton ) 
			{
				handleNavigationGestureEnd( event.getPointer(), event );
				bNavigationDragInProgress = false;
			}
		}
	}


	public void dragMotion(PointerMotionEvent event, Point2 dragStartPos, int dragButton)
	{
		if ( bNavigationDragInProgress )
		{
			Point2 pos = event.getPointer().getLocalPos();
			Vector2 delta = pos.sub( navigationDragCurrentPos );
			navigationDragCurrentPos = pos.clone();
			
			PointerNavigationEvent navEvent = null;
			
			if ( navigationButton == 1  ||  navigationButton == 2 )
			{
				navEvent = new PointerNavigationPanEvent( event.getPointer(), delta );
			}
			else if ( navigationButton == 3 )
			{
				double scaleDeltaPixels = delta.x + delta.y;
				double scaleDelta = Math.pow( 2.0, scaleDeltaPixels / 200.0 );
				
				navEvent = new PointerNavigationZoomEvent( event.getPointer(), navigationDragStartPos, scaleDelta );
			}
		
			if ( navEvent != null )
			{
				handleNavigationGestureDrag( event.getPointer(), navEvent );
			}
		}
	}
	
	
	public boolean scroll(Pointer pointer, PointerScrollEvent event)
	{
		if ( testNavigationModifiers( pointer ) )
		{
			double delta = (double)event.getScrollY();
			double scaleDelta = Math.pow( 2.0,  ( delta / 1.5 ) );
			
			handleNavigationGestureClick( pointer, new PointerNavigationZoomEvent( pointer, pointer.getLocalPos(), scaleDelta ) );
		}
		else
		{
			double delta = (double)event.getScrollY();
			handleNavigationGestureClick( pointer, new PointerNavigationPanEvent( pointer, new Vector2( 0.0, delta * 75.0 ) ) );
		}
		
		return true;
	}
	
	
	
	
	private void handleNavigationGestureBegin(PointerInterface pointer, PointerButtonEvent event)
	{
		Stack<PointerButtonEvent> events = new Stack<PointerButtonEvent>();
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		
		pointer.concretePointer().getLastElementPathUnderPoint( event, events, elements, pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.pop();
			PointerButtonEvent elementSpaceEvent = events.pop();
			
			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( NavigationElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					NavigationElementInteractor navInt = (NavigationElementInteractor)interactor;
					if ( navInt.navigationGestureBegin( element, elementSpaceEvent ) )
					{
						navElement = element;
						navInteractor = navInt;
						return;
					}
				}
			}
		}
	}

	private void handleNavigationGestureEnd(PointerInterface pointer, PointerButtonEvent event)
	{
		if ( navInteractor != null )
		{
			navInteractor.navigationGestureEnd( navElement, event );
		}
	}

	private void handleNavigationGestureDrag(PointerInterface pointer, PointerNavigationEvent event)
	{
		if ( navInteractor != null )
		{
			navInteractor.navigationGesture( navElement, event );
		}
	}

	private static void handleNavigationGestureClick(PointerInterface pointer, PointerNavigationEvent event)
	{
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		
		pointer.concretePointer().getLastElementPathUnderPoint( elements, pointer.getLocalPos() );
		
		while ( !elements.isEmpty() )
		{
			PointerInputElement element = elements.pop();
			
			Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( NavigationElementInteractor.class );
			if ( interactors != null )
			{
				for (AbstractElementInteractor interactor: interactors )
				{
					NavigationElementInteractor navInt = (NavigationElementInteractor)interactor;
					navInt.navigationGesture( element, event );
					return;
				}
			}
		}
	}
}
