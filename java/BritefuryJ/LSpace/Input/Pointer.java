//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Stack;

import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.LSRootElement;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Event.PointerScrollEvent;
import BritefuryJ.Math.Point2;
import BritefuryJ.Util.PriorityList;



public class Pointer extends PointerInterface
{
	protected interface ElementUnrealiseListener
	{
		public void notifyPointerElementUnrealised(Pointer pointer, PointerInputElement element);
	}
	
	
	private static final int TARGET_CLICK_INTERACTOR_PRIORITY = -2000;
	private static final int NAVIGATION_INTERACTOR_PRIORITY = -1000;
	private static final int CONTEXTMENU_INTERACTOR_PRIORITY = -500;
	private static final int DND_INTERACTOR_PRIORITY = -400;
	private static final int PRESSANDHOLD_INTERACTOR_PRIORITY = 0;
	private static final int CLICK_INTERACTOR_PRIORITY = 0;
	private static final int DRAG_INTERACTOR_PRIORITY = 50;
	private static final int MOTION_INTERACTOR_PRIORITY = 100;
	private static final int TARGET_DRAG_INTERACTOR_PRIORITY = 1000;
	
	
	
	protected Point2 localPos = new Point2();
	protected int modifiers = 0;
	protected LSRootElement rootElement;
	protected InputTable inputTable;
	protected DndDragSwing dndDrop;
	protected PresentationComponent component;
	protected PriorityList<PointerInteractor> interactors = new PriorityList<PointerInteractor>();
	protected boolean isWithinBoundsOfRoot = false;
	
	// Introduced as an optimisation -- elements become 'unrealised' VERY FREQUENTLY, so iterating through all interactors and notifying them is very inefficient.
	// Keeping a map of listener lists allows certain interactors to register an interest in ONLY the elements that they want to know about.
	protected IdentityHashMap<PointerInputElement, ArrayList<ElementUnrealiseListener>> unrealiseListeners = new IdentityHashMap<PointerInputElement, ArrayList<ElementUnrealiseListener>>();
	

	public Pointer(InputTable inputTable, LSRootElement rootElement, DndController dndController, PresentationComponent component)
	{
		this.inputTable = inputTable;
		this.component = component;
		
		this.rootElement = rootElement;
		
		
		PointerTargetInteractor targetInteractor = new PointerTargetInteractor();

		
		interactors.add( TARGET_CLICK_INTERACTOR_PRIORITY, targetInteractor.clickInteractor );
		interactors.add( NAVIGATION_INTERACTOR_PRIORITY, new PointerNavigationInteractor() );
		interactors.add( CONTEXTMENU_INTERACTOR_PRIORITY, new PointerContextMenuInteractor() );
		interactors.add( DND_INTERACTOR_PRIORITY, new PointerDndInteractor( rootElement, dndController ) );
		interactors.add( PRESSANDHOLD_INTERACTOR_PRIORITY, new PointerPushInteractor() );
		interactors.add( CLICK_INTERACTOR_PRIORITY, new PointerClickInteractor() );
		interactors.add( DRAG_INTERACTOR_PRIORITY, new PointerDragInteractor() );
		interactors.add( MOTION_INTERACTOR_PRIORITY, new PointerMotionInteractor( rootElement ) );
		interactors.add( TARGET_DRAG_INTERACTOR_PRIORITY, targetInteractor.dragInteractor );
	}
	
	

	protected void addInteractor(PointerInteractor interactor)
	{
		interactors.add( interactor );
	}
	
	protected void addInteractor(int priority, PointerInteractor interactor)
	{
		interactors.add( priority, interactor );
	}
	
	protected void removeInteractor(PointerInteractor interactor)
	{
		interactors.remove( interactor );
	}
	
	protected void interactorGrab(PointerInteractor interactor)
	{
		interactors.grab( interactor );
	}
	
	protected void interactorUngrab(PointerInteractor interactor)
	{
		interactors.ungrab( interactor );
	}
	
	

	
	public void setLocalPos(Point2 pos)
	{
		localPos = pos;
	}
	
	public Point2 getLocalPos()
	{
		return localPos;
	}
	
	public int getModifiers()
	{
		return modifiers;
	}
	
	public PresentationComponent getComponent()
	{
		return component;
	}

	public LSRootElement getRootElement()
	{
		return rootElement;
	}


	public Pointer concretePointer()
	{
		return this;
	}
	
	
	
	public void setButtonModifiers(int mods)
	{
		modifiers = ( modifiers & ~Modifier.BUTTONS_MASK )  |  ( mods & Modifier.BUTTONS_MASK );
	}
	
	public void setKeyModifiers(int mods)
	{
		modifiers = ( modifiers & ~Modifier.KEYS_MASK )  |  ( mods & Modifier.KEYS_MASK );
	}
	
	
	
	// Introduced as an optimisation -- elements become 'unrealised' VERY FREQUENTLY, so iterating through all interactors and notifying them is very inefficient.
	// Keeping a map of listener lists allows certain interactors to register an interest in ONLY the elements that they want to know about.
	protected void addUnrealiseListener(PointerInputElement element, ElementUnrealiseListener listener)
	{
		ArrayList<ElementUnrealiseListener> listeners = unrealiseListeners.get( element );
		if ( listeners == null )
		{
			listeners = new ArrayList<ElementUnrealiseListener>();
			unrealiseListeners.put( element, listeners );
		}
		listeners.add( listener );
	}
	
	protected void removeUnrealiseListener(PointerInputElement element, ElementUnrealiseListener listener)
	{
		ArrayList<ElementUnrealiseListener> listeners = unrealiseListeners.get( element );
		if ( listeners != null )
		{
			listeners.remove( listener );
			if ( listeners.isEmpty() )
			{
				unrealiseListeners.remove( element );
			}
		}
	}
	
	
	// Introduced as an optimisation -- elements become 'unrealised' VERY FREQUENTLY, so iterating through all interactors and notifying them is very inefficient.
	// Keeping a map of listener lists allows certain interactors to register an interest in ONLY the elements that they want to know about.
	protected void onElementUnrealised(PointerInputElement element)
	{
		ArrayList<ElementUnrealiseListener> listeners = unrealiseListeners.get( element );
		if ( listeners != null )
		{
			ArrayList<ElementUnrealiseListener> listenersCopy = new ArrayList<ElementUnrealiseListener>();
			listenersCopy.addAll( listeners );
			for (ElementUnrealiseListener listener: listenersCopy)
			{
				listener.notifyPointerElementUnrealised( this, element );
			}
		}
	}

	public void onRootElementReallocate()
	{
		if ( isWithinBoundsOfRoot )
		{
			for (PointerInteractor interactor: interactors)
			{
				if ( interactor.motion( this, new PointerMotionEvent( this, PointerMotionEvent.Action.MOTION ), null ) )
				{
					break;
				}
			}
		}
	}
	
	
	
	public boolean buttonDown(Point2 pos, int button)
	{
		PointerButtonEvent event = new PointerButtonEvent( this, button, PointerButtonEvent.Action.DOWN );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.buttonDown( this, event ) )
			{
				return true;
			}
		}

		return false;
	}
	
	public boolean buttonUp(Point2 pos, int button)
	{
		PointerButtonEvent event = new PointerButtonEvent( this, button, PointerButtonEvent.Action.UP );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.buttonUp( this, event ) )
			{
				return true;
			}
		}

		return false;
	}
	
	public boolean buttonClicked(Point2 pos, int button, int clickCount)
	{
		PointerButtonClickedEvent event = new PointerButtonClickedEvent( this, button, clickCount );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.buttonClicked( this, event ) )
			{
				return true;
			}
		}

		return false;
	}
	
	public void motion(Point2 pos, MouseEvent mouseEvent)
	{
		PointerMotionEvent event = new PointerMotionEvent( this, PointerMotionEvent.Action.MOTION );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.motion( this, event, mouseEvent ) )
			{
				break;
			}
		}
	}

	public void drag(Point2 pos, MouseEvent mouseEvent)
	{
		PointerMotionEvent event = new PointerMotionEvent( this, PointerMotionEvent.Action.MOTION );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.drag( this, event, mouseEvent ) )
			{
				return;
			}
		}
	}

	public void enter(Point2 pos)
	{
		isWithinBoundsOfRoot = true;
		PointerMotionEvent event = new PointerMotionEvent( this, PointerMotionEvent.Action.ENTER );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.enter( this, event ) )
			{
				break;
			}
		}
	}

	public void leave(Point2 pos)
	{
		PointerMotionEvent event = new PointerMotionEvent( this, PointerMotionEvent.Action.LEAVE );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.leave( this, event ) )
			{
				break;
			}
		}
		isWithinBoundsOfRoot = false;
	}

	public boolean scroll(int scrollX, int scrollY)
	{
		PointerScrollEvent event = new PointerScrollEvent( this, scrollX, scrollY );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.scroll( this, event ) )
			{
				return true;
			}
		}

		return false;
	}



	
	public void notifyEnterElement(PointerInputElement element)
	{
		inputTable.addPointerWithinElementBounds( this, element );
	}

	public void notifyLeaveElement(PointerInputElement element)
	{
		inputTable.removePointerWithinElementBounds( this, element );
	}

	
	
	protected <E extends PointerEvent> PointerInputElement getFirstElementUnderPoint(Point2 p)
	{
		PointerInputElement element = rootElement;
		
		while ( element != null )
		{
			PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( p );
			if ( childElement == null )
			{
				return element;
			}
			else
			{
				p = childElement.transformParentToLocalPoint( p );
				element = childElement;
			}
		}
		
		return null;
	}

	protected <E extends PointerEvent> PointerInputElement getLastElementUnderPoint(Point2 p)
	{
		PointerInputElement element = rootElement;
		
		while ( element != null )
		{
			PointerInputElement childElement = element.getLastPointerChildAtLocalPoint( p );
			if ( childElement == null )
			{
				return element;
			}
			else
			{
				p = childElement.transformParentToLocalPoint( p );
				element = childElement;
			}
		}
		
		return null;
	}

	
	
	
	@SuppressWarnings("unchecked")
	protected static <E extends PointerEvent> Stack<E> eventStack(E event, Stack<PointerInputElement> elements)
	{
		Stack<E> eventStack = new Stack<E>();
		eventStack.ensureCapacity( elements.size() );
		
		for (PointerInputElement element: elements)
		{
			event = (E)element.transformParentToLocalEvent( event );
			eventStack.add( event );
		}
		
		return eventStack;
	}
	
	protected static Stack<AffineTransform> rootToLocalTransformStack(Stack<PointerInputElement> elements)
	{
		AffineTransform xform = new AffineTransform();
		Stack<AffineTransform> xformStack = new Stack<AffineTransform>();
		xformStack.ensureCapacity( elements.size() );
		
		for (PointerInputElement element: elements)
		{
			xform.concatenate( element.getParentToLocalAffineTransform() );
			xformStack.add( (AffineTransform)xform.clone() );
		}
		
		return xformStack;
	}
	
	protected static AffineTransform rootToLocalTransform(Stack<PointerInputElement> elements)
	{
		AffineTransform xform = new AffineTransform();
		
		for (PointerInputElement element: elements)
		{
			xform.concatenate( element.getParentToLocalAffineTransform() );
		}
		
		return xform;
	}
	
	
	protected Stack<PointerInputElement> getFirstElementPathUnderPoint(Point2 p)
	{
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		PointerInputElement element = rootElement;
		elements.push( element );
		
		while ( element != null )
		{
			PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( p );
			if ( childElement != null )
			{
				p = childElement.transformParentToLocalPoint( p );
				elements.push( childElement );
			}
			element = childElement;
		}
		
		return elements;
	}

	
	protected Stack<PointerInputElement> getLastElementPathUnderPoint(Point2 p)
	{
		Stack<PointerInputElement> elements = new Stack<PointerInputElement>();
		PointerInputElement element = rootElement;
		elements.push( element );
		
		while ( element != null )
		{
			PointerInputElement childElement = element.getLastPointerChildAtLocalPoint( p );
			if ( childElement != null )
			{
				p = childElement.transformParentToLocalPoint( p );
				elements.push( childElement );
			}
			element = childElement;
		}
		
		return elements;
	}
}
