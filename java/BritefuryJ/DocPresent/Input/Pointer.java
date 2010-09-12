//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.Math.Point2;
import BritefuryJ.Utils.PriorityList;



public class Pointer extends PointerInterface
{
	static class ElementEntry
	{
		public PointerInputElement element;
		public ElementEntry pressGrabChild, childUnderPointer, navigationChild;
		private HashSet<ElementEntry> parents = new HashSet<ElementEntry>();
		
		
		public ElementEntry(PointerInputElement element)
		{
			this.element = element;
		}

	
		protected boolean handleButtonDown(Pointer pointer, PointerButtonEvent event)
		{
			if ( pressGrabChild == null )
			{
				PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( event.getPointer().getLocalPos() );
				if ( childElement != null )
				{
					ElementEntry childEntry = pointer.getEntryForElement( childElement );
					boolean bHandled = childEntry.handleButtonDown( pointer, (PointerButtonEvent)childElement.transformParentToLocalEvent( event ) );
					if ( bHandled  &&  element.isPointerInputElementRealised() )
					{
						pressGrabChild = childEntry;
						pressGrabChild.parents.add( this );
						return true;
					}
				}
				
				if ( pressGrabChild == null )
				{
					return element.handlePointerButtonDown( event );
				}
				else
				{
					return false;
				}
			}
			else
			{
				return pressGrabChild.handleButtonDown( pointer, (PointerButtonEvent)pressGrabChild.element.transformParentToLocalEvent( event ) );
			}
		}
		
		protected boolean handleButtonClicked(Pointer pointer, PointerButtonClickedEvent event)
		{
			boolean bHandled = false;
			
			PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( event.getPointer().getLocalPos() );
			if ( childElement != null )
			{
				ElementEntry childEntry = pointer.getEntryForElement( childElement );
				bHandled = childEntry.handleButtonClicked( pointer, (PointerButtonClickedEvent)childEntry.element.transformParentToLocalEvent( event ) );
			}
			
			if ( !bHandled )
			{
				bHandled = element.handlePointerButtonClicked( event );
			}
			
			return bHandled;
		}
		
		protected boolean handleButtonUp(Pointer pointer, PointerButtonEvent event)
		{
			if ( pressGrabChild != null )
			{
				PointerButtonEvent childSpaceEvent = (PointerButtonEvent)pressGrabChild.element.transformParentToLocalEvent( event );
				if ( pointer.isAButtonPressed() )
				{
					return pressGrabChild.handleButtonUp( pointer, childSpaceEvent );
				}
				else
				{
					Point2 localPos = event.getPointer().getLocalPos();
					if ( !pressGrabChild.element.containsParentSpacePoint( localPos ) )
					{
						pressGrabChild.handleLeave( pointer, new PointerMotionEvent( childSpaceEvent.getPointer(), PointerMotionEvent.Action.LEAVE ) );
					}
					
					boolean bHandled = pressGrabChild.handleButtonUp( pointer, childSpaceEvent );
					ElementEntry savedPressGrabChild = pressGrabChild;
					if ( pressGrabChild != null )
					{
						pressGrabChild.parents.remove( this );
					}
					pressGrabChild = null;
					
					if ( element.isPointerInputElementRealised()  &&  element.containsLocalSpacePoint( localPos ) )
					{
						PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( localPos );
						if ( childElement != null )
						{
							ElementEntry childEntry = pointer.getEntryForElement( childElement );
							PointerInputElement savedPressGrabChildElement = savedPressGrabChild != null  ?  savedPressGrabChild.element  :  null;
							if ( childElement != savedPressGrabChildElement )
							{
								childEntry.handleEnter( pointer, new PointerMotionEvent( childSpaceEvent.getPointer(), PointerMotionEvent.Action.ENTER ) );
							}
							childUnderPointer = childEntry;
							childUnderPointer.parents.add( this );
						}
						else
						{
							if ( childUnderPointer != null )
							{
								childUnderPointer.parents.remove( this );
							}
							childUnderPointer = null;
							element.handlePointerEnter( new PointerMotionEvent( event.getPointer(), PointerMotionEvent.Action.ENTER ) );
						}
					}
					
					return bHandled;
				}
			}
			else
			{
				return element.handlePointerButtonUp( event );
			}
		}
		


	
		protected void handleMotion(Pointer pointer, PointerMotionEvent event)
		{
			// Handle child elements
			if ( pressGrabChild != null )
			{
				pressGrabChild.handleMotion( pointer, (PointerMotionEvent)pressGrabChild.element.transformParentToLocalEvent( event ) );
			}
			else
			{
				ElementEntry oldPointerChild = childUnderPointer;
				
				if ( childUnderPointer != null )
				{
					if ( !childUnderPointer.element.containsParentSpacePoint( event.getPointer().getLocalPos() ) )
					{
						childUnderPointer.handleLeave( pointer, new PointerMotionEvent( childUnderPointer.element.transformParentToLocalPointer( event.getPointer() ), PointerMotionEvent.Action.LEAVE ) );
						childUnderPointer.parents.remove( this );
						childUnderPointer = null;
					}
					else
					{
						childUnderPointer.handleMotion( pointer, (PointerMotionEvent)childUnderPointer.element.transformParentToLocalEvent( event ) ); 
					}
				}
				
				if ( childUnderPointer == null )
				{
					PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( event.getPointer().getLocalPos() );
					if ( childElement != null )
					{
						ElementEntry childEntry = pointer.getEntryForElement( childElement );
						childEntry.handleEnter( pointer, (PointerMotionEvent)childElement.transformParentToLocalEvent( event ) );
						childUnderPointer = childEntry;
						childUnderPointer.parents.add( this );
					}
				}
				
				if ( oldPointerChild == null  &&  childUnderPointer != null )
				{
					element.handlePointerLeaveIntoChild( new PointerMotionEvent( event.getPointer(), PointerMotionEvent.Action.LEAVE ), childUnderPointer.element );
				}
				else if ( oldPointerChild != null  &&  childUnderPointer == null )
				{
					element.handlePointerEnterFromChild( new PointerMotionEvent( event.getPointer(), PointerMotionEvent.Action.ENTER ), oldPointerChild.element );
				}
			}
			
			
			// Handle @element
			if ( pointer.inputTable != null )
			{
				pointer.inputTable.addPointerWithinElementBounds( pointer, element );
			}
			
			
			element.handlePointerMotion( event );
		}
		
		
		
		protected void handleDrag(Pointer pointer, PointerMotionEvent event)
		{
			if ( pressGrabChild != null )
			{
				pressGrabChild.handleDrag( pointer, (PointerMotionEvent)pressGrabChild.element.transformParentToLocalEvent( event ) );
			}
			
			
			element.handlePointerDrag( event );
		}
		
		
		
		
		
		protected void handleEnter(Pointer pointer, PointerMotionEvent event)
		{
			// Handle @element
			if ( pointer.inputTable != null )
			{
				pointer.inputTable.addPointerWithinElementBounds( pointer, element );
			}
			
			element.handlePointerEnter( event );

			
			
			// Handle child elements
			Point2 localPos = event.getPointer().getLocalPos();
			
			PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( localPos );
			if ( childElement != null )
			{
				ElementEntry childEntry = pointer.getEntryForElement( childElement );
			
				childEntry.handleEnter( pointer, (PointerMotionEvent)childElement.transformParentToLocalEvent( event ) );
				childUnderPointer = childEntry;
				childUnderPointer.parents.add( this );
				element.handlePointerLeaveIntoChild( new PointerMotionEvent( event.getPointer(), PointerMotionEvent.Action.LEAVE ), childElement );
			}
		}
		
		protected void handleLeave(Pointer pointer, PointerMotionEvent event)
		{
			// Handle child elements
			if ( pressGrabChild == null )
			{
				if ( childUnderPointer != null )
				{
					childUnderPointer.handleLeave( pointer, (PointerMotionEvent)childUnderPointer.element.transformParentToLocalEvent( event ) );
					element.handlePointerEnterFromChild( new PointerMotionEvent( event.getPointer(), PointerMotionEvent.Action.ENTER ), childUnderPointer.element );
					childUnderPointer.parents.remove( this );
					childUnderPointer = null;
				}
			}
			
			
			// Handle @element
			if ( pointer.inputTable != null )
			{
				pointer.inputTable.removePointerWithinElementBounds( pointer, element );
			}
			
			element.handlePointerLeave( event );
		}
		
		
		
		protected boolean handleScroll(Pointer pointer, PointerScrollEvent event)
		{
			boolean bHandled = false;
			if ( pressGrabChild != null )
			{
				bHandled = pressGrabChild.handleScroll( pointer, (PointerScrollEvent)pressGrabChild.element.transformParentToLocalEvent( event ) );
				if ( bHandled )
				{
					return true;
				}
			}
			else if ( childUnderPointer != null )
			{
				bHandled = childUnderPointer.handleScroll( pointer, (PointerScrollEvent)childUnderPointer.element.transformParentToLocalEvent( event ) );
				if ( bHandled )
				{
					return true;
				}
			}
			return element.handlePointerScroll( event );
		}
		
		
		
		protected void notifyUnrealise(Pointer pointer)
		{
			if ( pressGrabChild != null )
			{
				pressGrabChild = null;
			}
			
			if ( childUnderPointer != null )
			{
				childUnderPointer = null;
			}
			
			if ( navigationChild != null )
			{
				navigationChild = null;
			}
			
			for (ElementEntry parent: parents)
			{
				parent.notifyChildUnrealised( this );
			}
			
			pointer.inputTable.removePointerWithinElementBounds( pointer, element );
		}

		private void notifyChildUnrealised(ElementEntry childEntry)
		{
			if ( childEntry == pressGrabChild )
			{
				pressGrabChild = null;
			}

			if ( childEntry == childUnderPointer )
			{
				childUnderPointer = null;
			}

			if ( childEntry == navigationChild )
			{
				navigationChild = null;
			}
		}
	}
	
	
	
	private static int NAVIGATION_INTERACTOR_PRIORITY = -1000;
	private static int DND_INTERACTOR_PRIORITY = -500;
	private static int CONTEXTMENU_INTERACTOR_PRIORITY = -400;
	private static int PRESSANDHOLD_INTERACTOR_PRIORITY = 0;
	private static int CLICK_INTERACTOR_PRIORITY = 0;
	private static int MOTION_INTERACTOR_PRIORITY = 100;
	
	
	
	protected Point2 localPos = new Point2();
	protected int modifiers = 0;
	protected ElementEntry rootEntry;
	protected InputTable inputTable;
	protected DndDropLocal dndDrop;
	protected PresentationComponent component;
	protected PriorityList<PointerInteractor> interactors = new PriorityList<PointerInteractor>();
	
	protected ReferenceQueue<ElementEntry> refQueue = new ReferenceQueue<ElementEntry>();
	protected HashMap<PointerInputElement, WeakReference<ElementEntry> > elementToEntryTable = new HashMap<PointerInputElement, WeakReference<ElementEntry> >();
	
	public Pointer(InputTable inputTable, PointerInputElement rootElement, DndController dndController, PresentationComponent component)
	{
		this.inputTable = inputTable;
		this.component = component;
		
		rootEntry = getEntryForElement( rootElement );
		
		
		interactors.add( NAVIGATION_INTERACTOR_PRIORITY, new PointerNavigationInteractor() );
		interactors.add( DND_INTERACTOR_PRIORITY, new PointerDndInteractor( rootElement, dndController ) );
		interactors.add( CONTEXTMENU_INTERACTOR_PRIORITY, new PointerContextMenuInteractor() );
		interactors.add( PRESSANDHOLD_INTERACTOR_PRIORITY, new PointerPressAndHoldInteractor() );
		interactors.add( CLICK_INTERACTOR_PRIORITY, new PointerClickInteractor() );
		interactors.add( MOTION_INTERACTOR_PRIORITY, new PointerMotionInteractor( rootElement ) );
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
	
	
	
	
	protected void onElementUnrealised(PointerInputElement element)
	{
		for (PointerInteractor interactor: interactors)
		{
			interactor.elementUnrealised( this, element );
		}
		
		
		WeakReference<ElementEntry> entryRef = elementToEntryTable.get( element );
		if ( entryRef != null )
		{
			ElementEntry entry = entryRef.get();
			if ( entry != null )
			{
				entry.handleLeave( this, new PointerMotionEvent( this, PointerMotionEvent.Action.LEAVE ) );
				entry.notifyUnrealise( this );
			}
		}
	}

	public void onRootElementReallocate()
	{
		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.motion( this, new PointerMotionEvent( this, PointerMotionEvent.Action.MOTION ), null ) )
			{
				break;
			}
		}

		ArrayList<PointerInterface> pointers = inputTable.getPointersWithinBoundsOfElement( rootEntry.element );
		if ( dndDrop == null  &&  pointers != null  &&  pointers.contains( this ) )
		{
			motion( localPos, null );
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

		return rootEntry.handleButtonDown( this, event );
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

		return rootEntry.handleButtonUp( this, event );
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

		return rootEntry.handleButtonClicked( this, event );
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

		rootEntry.handleMotion( this, event );
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

		rootEntry.handleDrag( this, event );
	}

	public void enter(Point2 pos)
	{
		PointerMotionEvent event = new PointerMotionEvent( this, PointerMotionEvent.Action.ENTER );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.enter( this, event ) )
			{
				break;
			}
		}

		rootEntry.handleEnter( this, event );
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

		rootEntry.handleLeave( this, event);
	}

	public boolean scroll(int scrollX, int scrollY)
	{
		PointerScrollEvent event = new PointerScrollEvent( this, scrollX, scrollY );

		for (PointerInteractor interactor: interactors)
		{
			if ( interactor.scroll( this, event ) )
			{
				break;
			}
		}

		return rootEntry.handleScroll( this, event );
	}




	
	
	private ElementEntry getEntryForElement(PointerInputElement element)
	{
		cleanEntryTable();
		WeakReference<ElementEntry> ref = elementToEntryTable.get( element );
		
		if ( ref == null )
		{
			ElementEntry entry = new ElementEntry( element );
			ref = new WeakReference<ElementEntry>( entry, refQueue );
			elementToEntryTable.put( element, ref );
			return entry;
		}
		else
		{
			ElementEntry entry = ref.get();
			if ( entry == null )
			{
				entry = new ElementEntry( element );
				ref = new WeakReference<ElementEntry>( entry, refQueue );
				elementToEntryTable.put( element, ref );
				return entry;
			}
			else
			{
				return entry;
			}
		}
	}
	
	private void cleanEntryTable()
	{
		Reference<? extends ElementEntry> ref = refQueue.poll();
		if ( ref != null )
		{
			HashSet<Reference<? extends ElementEntry>> references = new HashSet<Reference<? extends ElementEntry>>();
			while ( ref != null )
			{
				references.add( ref );
				ref = refQueue.poll();
			}
		
			HashSet<PointerInputElement> elements = new HashSet<PointerInputElement>();
			for (Map.Entry<PointerInputElement, WeakReference<ElementEntry> > entry: elementToEntryTable.entrySet())
			{
				if ( references.contains( entry.getValue() ) )
				{
					elements.add( entry.getKey() );
				}
			}
			
			for (PointerInputElement element: elements)
			{
				elementToEntryTable.remove( element );
			}
		}
	}
	
	
	
	
	protected <E extends PointerEvent> PointerInputElement getFirstElementUnderPoint(Point2 p)
	{
		PointerInputElement element = rootEntry.element;
		
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
		PointerInputElement element = rootEntry.element;
		
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
	protected <E extends PointerEvent> void getFirstElementPathUnderPoint(E event, Stack<E> eventStack, Stack<PointerInputElement> elements, Point2 p)
	{
		PointerInputElement element = rootEntry.element;
		eventStack.push( event );
		elements.push( element );
		
		while ( element != null )
		{
			PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( p );
			if ( childElement != null )
			{
				p = childElement.transformParentToLocalPoint( p );
				event = (E)childElement.transformParentToLocalEvent( event );
				eventStack.push( event );
				elements.push( childElement );
			}
			element = childElement;
		}
	}

	protected void getFirstElementPathUnderPoint(Stack<PointerInputElement> elements, Point2 p)
	{
		PointerInputElement element = rootEntry.element;
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
	}

	
	@SuppressWarnings("unchecked")
	protected <E extends PointerEvent> void getLastElementPathUnderPoint(E event, Stack<E> eventStack, Stack<PointerInputElement> elements, Point2 p)
	{
		PointerInputElement element = rootEntry.element;
		eventStack.push( event );
		elements.push( element );
		
		while ( element != null )
		{
			PointerInputElement childElement = element.getLastPointerChildAtLocalPoint( p );
			if ( childElement != null )
			{
				p = childElement.transformParentToLocalPoint( p );
				event = (E)childElement.transformParentToLocalEvent( event );
				eventStack.push( event );
				elements.push( childElement );
			}
			element = childElement;
		}
	}
	
	protected void getLastElementPathUnderPoint(Stack<PointerInputElement> elements, Point2 p)
	{
		PointerInputElement element = rootEntry.element;
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
	}
}
