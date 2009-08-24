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

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.Math.Point2;



public class Pointer extends PointerInterface
{
	private static class ElementEntry
	{
		public PointerInputElement element;
		public ElementEntry pressGrabChild, childUnderPointer;
		
		
		public ElementEntry(PointerInputElement element)
		{
			this.element = element;
		}

	
		protected boolean handleButtonDown(Pointer pointer, PointerButtonEvent event)
		{
			if ( pressGrabChild == null )
			{
				PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( event.pointer.getLocalPos() );
				if ( childElement != null )
				{
					ElementEntry childEntry = pointer.getEntryForElement( childElement );
					boolean bHandled = childEntry.handleButtonDown( pointer, (PointerButtonEvent)childElement.transformParentToLocalEvent( event ) );
					if ( bHandled  &&  element.isPointerInputElementRealised() )
					{
						pressGrabChild = childEntry;
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
		
		protected boolean handleButtonDown2(Pointer pointer, PointerButtonEvent event)
		{
			if ( pressGrabChild != null )
			{
				return pressGrabChild.handleButtonDown2( pointer, (PointerButtonEvent)pressGrabChild.element.transformParentToLocalEvent( event ) );
			}
			else
			{
				return element.handlePointerButtonDown2( event );
			}
		}
		
		protected boolean handleButtonDown3(Pointer pointer, PointerButtonEvent event)
		{
			if ( pressGrabChild != null )
			{
				return pressGrabChild.handleButtonDown3( pointer, (PointerButtonEvent)pressGrabChild.element.transformParentToLocalEvent( event ) );
			}
			else
			{
				return element.handlePointerButtonDown3( event );
			}
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
					Point2 localPos = event.pointer.getLocalPos();
					if ( !pressGrabChild.element.containsParentSpacePoint( localPos ) )
					{
						pressGrabChild.handleLeave( pointer, new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.LEAVE ) );
					}
					
					boolean bHandled = pressGrabChild.handleButtonUp( pointer, childSpaceEvent );
					ElementEntry savedPressGrabChild = pressGrabChild;
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
								childEntry.handleEnter( pointer, new PointerMotionEvent( childSpaceEvent.pointer, PointerMotionEvent.Action.ENTER ) );
							}
							childUnderPointer = childEntry;
						}
						else
						{
							childUnderPointer = null;
							element.handlePointerEnter( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ) );
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
					if ( !childUnderPointer.element.containsParentSpacePoint( event.pointer.getLocalPos() ) )
					{
						childUnderPointer.handleLeave( pointer, new PointerMotionEvent( childUnderPointer.element.transformParentToLocalPointer( event.pointer ), PointerMotionEvent.Action.LEAVE ) );
						childUnderPointer = null;
					}
					else
					{
						childUnderPointer.handleMotion( pointer, (PointerMotionEvent)childUnderPointer.element.transformParentToLocalEvent( event ) ); 
					}
				}
				
				if ( childUnderPointer == null )
				{
					PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( event.pointer.getLocalPos() );
					if ( childElement != null )
					{
						ElementEntry childEntry = pointer.getEntryForElement( childElement );
						childEntry.handleEnter( pointer, (PointerMotionEvent)childElement.transformParentToLocalEvent( event ) );
						childUnderPointer = childEntry;
					}
				}
				
				if ( oldPointerChild == null  &&  childUnderPointer != null )
				{
					element.handlePointerLeaveIntoChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.LEAVE ), childUnderPointer.element );
				}
				else if ( oldPointerChild != null  &&  childUnderPointer == null )
				{
					element.handlePointerEnterFromChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ), oldPointerChild.element );
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
			pressGrabChild.handleDrag( pointer, (PointerMotionEvent)pressGrabChild.element.transformParentToLocalEvent( event ) );
			
			
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
			Point2 localPos = event.pointer.getLocalPos();
			
			PointerInputElement childElement = element.getFirstPointerChildAtLocalPoint( localPos );
			if ( childElement != null )
			{
				ElementEntry childEntry = pointer.getEntryForElement( childElement );
			
				childEntry.handleEnter( pointer, (PointerMotionEvent)childElement.transformParentToLocalEvent( event ) );
				childUnderPointer = childEntry;
				element.handlePointerLeaveIntoChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.LEAVE ), childElement );
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
					element.handlePointerEnterFromChild( new PointerMotionEvent( event.pointer, PointerMotionEvent.Action.ENTER ), childUnderPointer.element );
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
			if ( pressGrabChild != null )
			{
				pressGrabChild.handleScroll( pointer, (PointerScrollEvent)pressGrabChild.element.transformParentToLocalEvent( event ) );
			}
			else if ( childUnderPointer != null )
			{
				childUnderPointer.handleScroll( pointer, (PointerScrollEvent)childUnderPointer.element.transformParentToLocalEvent( event ) );
			}
			return element.handlePointerScroll( event );
		}
		
		
		protected void notifyUnrealise(Pointer pointer)
		{
			if ( pressGrabChild != null )
			{
				pressGrabChild = null;
			}
			
			pointer.inputTable.removePointerWithinElementBounds( pointer, element );
		}
	}
	
	
	
	protected Point2 localPos;
	protected int modifiers;
	protected ElementEntry rootEntry;
	protected InputTable inputTable;
	protected DndDropLocal dndDrop;
	protected PointerDndController dndController;
	
	protected ReferenceQueue<ElementEntry> refQueue;
	protected HashMap<PointerInputElement, WeakReference<ElementEntry> > elementToEntryTable;
	
	
	public Pointer(InputTable inputTable, PointerInputElement rootElement, PointerDndController dndController)
	{
		this.inputTable = inputTable;
		localPos = new Point2();
		modifiers = 0;
		this.dndController = dndController;
		
		refQueue = new ReferenceQueue<ElementEntry>();
		elementToEntryTable = new HashMap<PointerInputElement, WeakReference<ElementEntry> >();

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
	
	
	
	public void setModifiers(int mods)
	{
		modifiers = mods;
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
	
	
	
	
	public void buttonDown(Point2 pos, int button)
	{
		PointerButtonEvent event = new PointerButtonEvent( this, button, PointerButtonEvent.Action.DOWN );
		dndButtonDownEvent( event );
		rootEntry.handleButtonDown( this, event );
	}
	
	public void buttonDown2(Point2 pos, int button)
	{
		rootEntry.handleButtonDown2( this, new PointerButtonEvent( this, button, PointerButtonEvent.Action.DOWN2 ) );
	}
	
	public void buttonDown3(Point2 pos, int button)
	{
		rootEntry.handleButtonDown3( this, new PointerButtonEvent( this, button, PointerButtonEvent.Action.DOWN3 ) );
	}
	
	public void buttonUp(Point2 pos, int button)
	{
		PointerButtonEvent event = new PointerButtonEvent( this, button, PointerButtonEvent.Action.UP );
		boolean bHandled = dndButtonUpEvent( event );
		if ( !bHandled )
		{
			rootEntry.handleButtonUp( this, event );
		}
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

	public void scroll(int scrollX, int scrollY)
	{
		rootEntry.handleScroll( this, new PointerScrollEvent( this, scrollX, scrollY ) );
	}




	
	private void dndButtonDownEvent(PointerButtonEvent event)
	{
		if ( dndController != null )
		{
			PointerInputElement sourceElement = rootEntry.element.getDndElement( event.pointer.getLocalPos(), null );
			
			if ( sourceElement != null )
			{
				DndDropLocal drop = new DndDropLocal( sourceElement, event.button );
				
				if ( drop != null )
				{
					dndDrop = drop;
				}
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
						drop.bInProgress = true;
						int requestedAction = drop.sourceElement.getDndHandler().getSourceRequestedAction( drop.sourceElement, event.pointer, drop.sourceButton );
						Transferable transferable = drop.sourceElement.getDndHandler().createTransferable( drop.sourceElement );
						drop.initialise( transferable, requestedAction );
						
						dndController.pointerDndInitiateDrag( this, drop, mouseEvent, requestedAction );
					}
				}
				else
				{
					Point2 targetPos[] = new Point2[] { null };
					PointerInputElement targetElement = rootEntry.element.getDndElement( event.pointer.getLocalPos(), targetPos );
					if ( targetElement != null )
					{
						drop.setTarget( targetElement, targetPos[0] );
						targetElement.getDndHandler().canDrop( targetElement, drop );
					}
				}
				
				return true;
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
				PointerInputElement targetElement = rootEntry.element.getDndElement( event.pointer.getLocalPos(), targetPos );
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
