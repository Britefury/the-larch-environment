//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.TransferHandler;

import BritefuryJ.Controls.PopupMenu;
import BritefuryJ.Controls.VPopupMenu;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerNavigationEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.Math.Point2;



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

		protected boolean handleContextButton(Pointer pointer, PointerButtonEvent event, PopupMenu menu)
		{
			PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( event.getPointer().getLocalPos() );
			if ( childElement != null )
			{
				ElementEntry childEntry = pointer.getEntryForElement( childElement );
				boolean bHandled = childEntry.handleContextButton( pointer, (PointerButtonEvent)childElement.transformParentToLocalEvent( event ), menu );
				if ( bHandled  &&  element.isPointerInputElementRealised() )
				{
					return true;
				}
			}
			
			return element.handlePointerContextButton( menu );
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
		
		
		
		protected boolean handleNavigationGestureBegin(Pointer pointer, PointerButtonEvent event)
		{
			if ( navigationChild == null )
			{
				PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( event.getPointer().getLocalPos() );
				if ( childElement != null )
				{
					ElementEntry childEntry = pointer.getEntryForElement( childElement );
					boolean bHandled = childEntry.handleNavigationGestureBegin( pointer, (PointerButtonEvent)childElement.transformParentToLocalEvent( event ) );
					if ( bHandled  &&  element.isPointerInputElementRealised() )
					{
						navigationChild = childEntry;
						navigationChild.parents.add( this );
						return true;
					}
				}
				
				if ( navigationChild == null )
				{
					return element.handlePointerNavigationGestureBegin( event );
				}
				else
				{
					return false;
				}
			}
			else
			{
				throw new RuntimeException( "Navigation gesture already started" );
			}
		}
		
		protected boolean handleNavigationGestureEnd(Pointer pointer, PointerButtonEvent event)
		{
			if ( navigationChild != null )
			{
				PointerButtonEvent childSpaceEvent = (PointerButtonEvent)navigationChild.element.transformParentToLocalEvent( event );
				Point2 localPos = event.getPointer().getLocalPos();
				if ( !navigationChild.element.containsParentSpacePoint( localPos ) )
				{
					navigationChild.handleLeave( pointer, new PointerMotionEvent( childSpaceEvent.getPointer(), PointerMotionEvent.Action.LEAVE ) );
				}
				
				boolean bHandled = navigationChild.handleNavigationGestureEnd( pointer, childSpaceEvent );
				ElementEntry savedNavigationChild = navigationChild;
				navigationChild.parents.remove( this );
				navigationChild = null;
				
				if ( element.isPointerInputElementRealised()  &&  element.containsLocalSpacePoint( localPos ) )
				{
					PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( localPos );
					if ( childElement != null )
					{
						ElementEntry childEntry = pointer.getEntryForElement( childElement );
						PointerInputElement savedNavigationChildElement = savedNavigationChild != null  ?  savedNavigationChild.element  :  null;
						if ( childElement != savedNavigationChildElement )
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
			else
			{
				return element.handlePointerNavigationGestureEnd( event );
			}
		}
		
		protected boolean handleNavigationGestureDrag(Pointer pointer, PointerNavigationEvent event)
		{
			if ( navigationChild != null )
			{
				boolean bHandled = navigationChild.handleNavigationGestureDrag( pointer, (PointerNavigationEvent)navigationChild.element.transformParentToLocalEvent( event ) );
				if ( bHandled )
				{
					return true;
				}
			}
			
			return element.handlePointerNavigationGesture( event );
		}
		
		protected boolean handleNavigationGestureClick(Pointer pointer, PointerNavigationEvent event)
		{
			if ( childUnderPointer != null )
			{
				boolean bHandled = childUnderPointer.handleNavigationGestureClick( pointer, (PointerNavigationEvent)childUnderPointer.element.transformParentToLocalEvent( event ) );
				if ( bHandled )
				{
					return true;
				}
			}
			
			return element.handlePointerNavigationGesture( event );
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
	
	
	
	protected Point2 localPos = new Point2();
	protected int modifiers = 0;
	protected ElementEntry rootEntry;
	protected InputTable inputTable;
	protected DndDropLocal dndDrop;
	protected PointerDndController dndController;
	protected PresentationComponent component;
	
	protected ReferenceQueue<ElementEntry> refQueue = new ReferenceQueue<ElementEntry>();
	protected HashMap<PointerInputElement, WeakReference<ElementEntry> > elementToEntryTable = new HashMap<PointerInputElement, WeakReference<ElementEntry> >();
	
	public Pointer(InputTable inputTable, PointerInputElement rootElement, PointerDndController dndController, PresentationComponent component)
	{
		this.inputTable = inputTable;
		this.dndController = dndController;
		this.component = component;
		
		rootEntry = getEntryForElement( rootElement );
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


	public PointerInterface concretePointer()
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
		ArrayList<PointerInterface> pointers = inputTable.getPointersWithinBoundsOfElement( rootEntry.element );
		if ( dndDrop == null  &&  pointers != null  &&  pointers.contains( this ) )
		{
			motion( localPos, null );
		}			
	}
	
	
	
	public boolean buttonDown(Point2 pos, int button)
	{
		PointerButtonEvent event = new PointerButtonEvent( this, button, PointerButtonEvent.Action.DOWN );
		if ( button == 3  &&  getModifiers() == Modifier.BUTTON3 )
		{
			VPopupMenu menu = new VPopupMenu();
			rootEntry.handleContextButton( this, event, menu );
			if ( !menu.isEmpty() )
			{
				menu.popupAtMousePosition( component.getRootElement() );
				return true;
			}
			return false;
		}
		else
		{
			dndButtonDownEvent( event );
			return rootEntry.handleButtonDown( this, event );
		}
	}
	
	public boolean buttonUp(Point2 pos, int button)
	{
		PointerButtonEvent event = new PointerButtonEvent( this, button, PointerButtonEvent.Action.UP );
		boolean bHandled = dndButtonUpEvent( event );
		if ( bHandled )
		{
			return true;
		}
		else
		{
			return rootEntry.handleButtonUp( this, event );
		}
	}
	
	public boolean buttonClicked(Point2 pos, int button, int clickCount)
	{
		return rootEntry.handleButtonClicked( this, new PointerButtonClickedEvent( this, button, clickCount ) );
	}
	
	public void motion(Point2 pos, MouseEvent mouseEvent)
	{
		PointerMotionEvent event = new PointerMotionEvent( this, PointerMotionEvent.Action.MOTION );
		rootEntry.handleMotion( this, event );
	}

	public void drag(Point2 pos, MouseEvent mouseEvent)
	{
		PointerMotionEvent event = new PointerMotionEvent( this, PointerMotionEvent.Action.MOTION );
		boolean bHandled = dndDragEvent( event, mouseEvent );
		if ( !bHandled )
		{
			rootEntry.handleDrag( this, event );
		}
	}

	public void enter(Point2 pos)
	{
		rootEntry.handleEnter( this, new PointerMotionEvent( this, PointerMotionEvent.Action.ENTER ) );
	}

	public void leave(Point2 pos)
	{
		rootEntry.handleLeave( this, new PointerMotionEvent( this, PointerMotionEvent.Action.LEAVE ) );
	}

	public boolean scroll(int scrollX, int scrollY)
	{
		return rootEntry.handleScroll( this, new PointerScrollEvent( this, scrollX, scrollY ) );
	}




	
	private void dndButtonDownEvent(PointerButtonEvent event)
	{
		if ( dndController != null )
		{
			PointerInputElement sourceElement = rootEntry.element.getDndElement( event.getPointer().getLocalPos(), null );
			
			if ( sourceElement != null )
			{
				dndDrop = new DndDropLocal( sourceElement, event.getButton() );
			}
		}
	}
	
	private boolean dndDragEvent(PointerMotionEvent event, MouseEvent mouseEvent)
	{
		if ( dndController != null )
		{
			DndDropLocal drop = dndDrop;
	
			if ( drop != null )
			{
				if ( !drop.bInProgress )
				{
					if ( mouseEvent != null )
					{
						int requestedAction = drop.sourceElement.getDndHandler().getSourceRequestedAction( drop.sourceElement, event.getPointer(), drop.sourceButton );
						if ( requestedAction != TransferHandler.NONE )
						{
							int requestedAspect = drop.sourceElement.getDndHandler().getSourceRequestedAspect( drop.sourceElement, event.getPointer(), drop.sourceButton );
							if ( requestedAspect != DndHandler.ASPECT_NONE )
							{
								Transferable transferable = drop.sourceElement.getDndHandler().createTransferable( drop.sourceElement, requestedAspect );
								if ( transferable != null )
								{
									drop.bInProgress = true;
									drop.initialise( transferable, requestedAction );
								
									dndController.pointerDndInitiateDrag( this, drop, mouseEvent, requestedAction );
									
									return true;
								}
							}
						}
					}
				}
				else
				{
					Point2 targetPos[] = new Point2[] { null };
					PointerInputElement targetElement = rootEntry.element.getDndElement( event.getPointer().getLocalPos(), targetPos );
					if ( targetElement != null )
					{
						drop.setTarget( targetElement, targetPos[0] );
						targetElement.getDndHandler().canDrop( targetElement, drop );
					}
					
					return true;
				}
				
				return false;
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

	private boolean dndButtonUpEvent(PointerButtonEvent event)
	{
		if ( dndController != null )
		{
			DndDropLocal drop = dndDrop;
			
			if ( drop != null  &&  drop.bInProgress )
			{
				Point2 targetPos[] = new Point2[] { null };
				PointerInputElement targetElement = rootEntry.element.getDndElement( event.getPointer().getLocalPos(), targetPos );
				if ( targetElement != null )
				{
					drop.setTarget( targetElement, targetPos[0] );
					if ( targetElement.getDndHandler().canDrop( targetElement, drop ) )
					{
						targetElement.getDndHandler().acceptDrop( targetElement, drop );
					}
				}
				
				drop.bInProgress = false;
				dndDrop = null;
				
				return true;
			}
			else
			{
				dndDrop = null;
				return false;
			}
		}
		else
		{
			return false;
		}
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
}
