//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.TransferHandler;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.Math.Point2;

public class PointerDndInteractor extends PointerInteractor
{
	private DndController dndController;
	private LSElement rootElement;
	private DndDragSwing dndDrag;
	
	
	public PointerDndInteractor(LSElement rootElement, DndController dndController)
	{
		this.dndController = dndController;
		this.rootElement = rootElement;
	}
	
	
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		return dndButtonDownEvent( event );
	}

	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		return dndButtonUpEvent( event );
	}

	public boolean drag(Pointer pointer, PointerMotionEvent event, MouseEvent mouseEvent)
	{
		return dndDragEvent( event, mouseEvent );
	}

	
	
	public ArrayList<DndTarget> getDndTargets(Point2 localPos)
	{
		return LSElement.getDndTargets( rootElement, localPos );
	}



	private boolean dndButtonDownEvent(PointerButtonEvent event)
	{
		if ( dndController != null )
		{
			ArrayList<DndTarget> targets = getDndTargets( event.getPointer().getLocalPos() );
			for (DndTarget target: targets)
			{
				if ( target.isSource() )
				{
					LSElement sourceElement = target.getElement();
					DndHandler dndHandler = target.getDndHandler();
					int button = event.getButton();
				
					int requestedAction = dndHandler.getSourceRequestedAction( sourceElement, event.getPointer(), button );
					if ( requestedAction != TransferHandler.NONE )
					{
						int requestedAspect = dndHandler.getSourceRequestedAspect( sourceElement, event.getPointer(), button );
						if ( requestedAspect != DndHandler.ASPECT_NONE )
						{
							dndDrag = new DndDragSwing( sourceElement, button );
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean dndDragEvent(PointerMotionEvent event, MouseEvent mouseEvent)
	{
		if ( dndController != null  &&  dndDrag != null  &&  !dndDrag.bInProgress )
		{
			if ( mouseEvent != null )
			{
				int requestedAction = dndDrag.sourceElement.getDndHandler().getSourceRequestedAction( dndDrag.sourceElement, event.getPointer(), dndDrag.sourceButton );
				if ( requestedAction != TransferHandler.NONE )
				{
					int requestedAspect = dndDrag.sourceElement.getDndHandler().getSourceRequestedAspect( dndDrag.sourceElement, event.getPointer(), dndDrag.sourceButton );
					if ( requestedAspect != DndHandler.ASPECT_NONE )
					{
						Transferable transferable = dndDrag.sourceElement.getDndHandler().createTransferable( dndDrag.sourceElement, requestedAspect );
						if ( transferable != null )
						{
							dndDrag.bInProgress = true;
							dndDrag.initialise( transferable, requestedAction );
						
							try
							{
								dndController.dndInitiateDrag( dndDrag, mouseEvent, requestedAction );
							}
							catch (Throwable e)
							{
								dndDrag.sourceElement.notifyExceptionDuringElementInteractor( dndController, "dndInitiateDrag", e );
							}
							
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean dndButtonUpEvent(PointerButtonEvent event)
	{
		if ( dndController != null  &&  dndDrag != null  &&  dndDrag.bInProgress )
		{
			dndDrag = null;
				
			return true;
		}
		return false;
	}
}
