//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.geom.AffineTransform;
import java.util.Stack;

import BritefuryJ.LSpace.LSContentLeafEditable;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Focus.SelectionManager;
import BritefuryJ.LSpace.Focus.SelectionPoint;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.LSpace.Interactor.TargetElementInteractor;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.LSpace.TextFocus.TextSelectionPoint;
import BritefuryJ.Math.Point2;

public class PointerTargetInteractor
{
	private PointerInputElement targetDragElement = null;
	private AffineTransform targetDragElementRootToLocalXform = null;
	private TargetElementInteractor targetDragInteractor = null;
	private boolean lastMousePressPositionedCaret = false;
	
	
	public final DragInteractor dragInteractor = new DragInteractor();
	public final ClickInteractor clickInteractor = new ClickInteractor();

	
	class DragInteractor extends AbstractPointerDragInteractor
	{
		@Override
		public boolean dragBegin(PointerButtonEvent event)
		{
			boolean bHandled = false;
			
			
			if ( ( event.getModifiers() & ( Modifier.ALT | Modifier.ALT_GRAPH ) )  ==  0 )
			{
				PresentationComponent.RootElement rootElement = event.getPointer().concretePointer().getRootElement();
				SelectionPoint startPoint = null;
				
				
				PointerInterface pointer = event.getPointer();
				Stack<PointerInputElement> elements = pointer.concretePointer().getLastElementPathUnderPoint( pointer.getLocalPos() );
				Stack<PointerButtonEvent> events = Pointer.eventStack( event, elements );
				
				while ( !elements.isEmpty() )
				{
					PointerInputElement element = elements.peek();
					PointerButtonEvent elementSpaceEvent = events.peek();
					
					if ( element.isPointerInputElementRealised() )
					{
						Iterable<AbstractElementInteractor> interactors = element.getElementInteractors( TargetElementInteractor.class );
						if ( interactors != null )
						{
							for (AbstractElementInteractor interactor: interactors)
							{
								TargetElementInteractor pressInt = (TargetElementInteractor)interactor;
								startPoint = pressInt.targetDragBegin( element, elementSpaceEvent );
								if ( startPoint != null )
								{
									targetDragElement = element;
									targetDragElementRootToLocalXform = Pointer.rootToLocalTransform( elements );
									targetDragInteractor = pressInt;
									bHandled = true;
								}
							}
						}
					}
					
					elements.pop();
					events.pop();
				}
			
			
				if ( startPoint == null  &&  event.getButton() == 1 )
				{
					// Attempt to position the caret
					Point2 windowPos = event.getLocalPointerPos();
					Marker editableMarker = rootElement.getEditableMarkerClosestToLocalPoint( windowPos );
					
					if ( editableMarker != null )
					{
						Caret caret = rootElement.getCaret();
						caret.moveTo( editableMarker );
						caret.makeCurrentTarget();
						lastMousePressPositionedCaret = true;
						bHandled = true;
					}
		
					Marker selectableMarker = rootElement.getSelectableMarkerClosestToLocalPoint( windowPos );
					if ( selectableMarker != null )
					{
						startPoint = new TextSelectionPoint( selectableMarker );
						bHandled = true;
					}
				}

				if ( startPoint != null )
				{
					if ( ( event.getModifiers() & Modifier.SHIFT )  !=  0 )
					{
						rootElement.getSelectionManager().dragSelection( startPoint );
					}
					else
					{
						rootElement.getSelectionManager().moveSelection( startPoint );
					}
				}
				
				return bHandled;
			}
			
			
			
			return false;
		}
	
		@Override
		public void dragEnd(PointerButtonEvent event, Point2 dragStartPos, int dragButton)
		{
			if ( targetDragElement != null )
			{
				targetDragInteractor.targetDragEnd( targetDragElement, (PointerButtonEvent)event.transformed( targetDragElementRootToLocalXform ), dragStartPos.transform( targetDragElementRootToLocalXform ), dragButton );
				targetDragElement = null;
				targetDragElementRootToLocalXform = null;
				targetDragInteractor = null;
			}
		}
	
		@Override
		public void dragMotion(PointerMotionEvent event, Point2 dragStartPos, int dragButton)
		{
			PresentationComponent.RootElement rootElement = event.getPointer().concretePointer().getRootElement();
			SelectionManager selectionManager = rootElement.getSelectionManager();
			SelectionPoint selCurrent = null;
			
			if ( targetDragElement != null )
			{
				selCurrent = targetDragInteractor.targetDragMotion( targetDragElement, (PointerMotionEvent)event.transformed( targetDragElementRootToLocalXform ), dragStartPos.transform( targetDragElementRootToLocalXform ), dragButton );
			}
			
			if ( selCurrent == null  &&  dragButton == 1 )
			{
				// Drag the caret and text selection

				Point2 windowPos = event.getLocalPointerPos();
				Marker editableMarker = rootElement.getEditableMarkerClosestToLocalPoint( windowPos );
				if ( editableMarker != null )
				{
					Caret caret = rootElement.getCaret();
					caret.moveTo( editableMarker );
					caret.makeCurrentTarget();
				}

				Marker selectableMarker = rootElement.getSelectableMarkerClosestToLocalPoint( windowPos );
				if ( selectableMarker != null )
				{
					selCurrent = new TextSelectionPoint( selectableMarker );
				}
			}
			
			
			if ( selCurrent != null )
			{
				selectionManager.dragSelection( selCurrent );
			}
		}
	}
		
		
	class ClickInteractor extends PointerInteractor
	{
		public boolean buttonClicked(Pointer pointer, PointerButtonClickedEvent event)
		{
			if ( lastMousePressPositionedCaret  &&  event.getButton() == 1  &&  ( event.getModifiers() & ( Modifier.ALT | Modifier.ALT_GRAPH | Modifier.CTRL | Modifier.SHIFT ) )  ==  0 )
			{
				Point2 windowPos = event.getLocalPointerPos();
				PresentationComponent.RootElement rootElement = event.getPointer().concretePointer().getRootElement();
				LSContentLeafEditable selectableLeaf = (LSContentLeafEditable)rootElement.getSelectableLeafClosestToLocalPoint( windowPos );
				if ( selectableLeaf != null )
				{
					LSElement elementToSelect = null;
					
					int clickCount = event.getClickCount();
					if ( clickCount == 2 )
					{
						elementToSelect = selectableLeaf;
					}
					else if ( clickCount >= 3 )
					{
						elementToSelect = selectableLeaf.getSegment();
					}
						
					if ( elementToSelect != null )
					{
						Caret caret = rootElement.getCaret();
						SelectionManager selectionManager = rootElement.getSelectionManager();
						
						caret.moveTo( elementToSelect.markerAtEnd() );
						caret.makeCurrentTarget();
						selectionManager.selectElement( elementToSelect );
						return true;
					}
				}
			}
			
			return false;
		}
	}
}
