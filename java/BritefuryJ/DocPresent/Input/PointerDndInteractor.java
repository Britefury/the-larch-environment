//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;

import javax.swing.TransferHandler;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.Math.Point2;

public class PointerDndInteractor extends PointerInteractor
{
	private DndController dndController;
	private PointerInputElement rootElement;
	private DndDropLocal dndDrop;
	
	
	public PointerDndInteractor(PointerInputElement rootElement, DndController dndController)
	{
		this.dndController = dndController;
		this.rootElement = rootElement;
	}
	
	
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		dndButtonDownEvent( event );
		return false;
	}

	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		return dndButtonUpEvent( event );
	}

	public boolean drag(Pointer pointer, PointerMotionEvent event, MouseEvent mouseEvent)
	{
		return dndDragEvent( event, mouseEvent );
	}

	
	
	private PointerInputElement getDndElement(Point2 localPos, Point2 targetPos[])
	{
		return getDndElement( rootElement, localPos, targetPos );
	}

	private PointerInputElement getDndElement(PointerInputElement element, Point2 localPos, Point2 targetPos[])
	{
		PointerInputElement child = element.getFirstPointerChildAtLocalPoint( localPos );
		if ( child != null )
		{
			PointerInputElement e = getDndElement( child, child.transformParentToLocalPoint( localPos ), targetPos );
			if ( e != null )
			{
				return e;
			}
		}
		
		if ( element.getDndHandler() != null )
		{
			if ( targetPos != null )
			{
				targetPos[0] = localPos;
			}
			return element;
		}
		else
		{
			return null;
		}
	}



	private void dndButtonDownEvent(PointerButtonEvent event)
	{
		if ( dndController != null )
		{
			PointerInputElement sourceElement = getDndElement( event.getPointer().getLocalPos(), null );
			
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
								
									dndController.dndInitiateDrag( drop, mouseEvent, requestedAction );
									
									return true;
								}
							}
						}
					}
				}
				else
				{
					Point2 targetPos[] = new Point2[] { null };
					PointerInputElement targetElement = getDndElement( event.getPointer().getLocalPos(), targetPos );
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
				PointerInputElement targetElement = getDndElement( event.getPointer().getLocalPos(), targetPos );
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
}
