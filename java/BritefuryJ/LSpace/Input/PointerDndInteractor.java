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

import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.Math.Point2;

public class PointerDndInteractor extends PointerInteractor
{
	private DndController dndController;
	private PointerInputElement rootElement;
	private DndDragSwing dndDrop;
	
	
	public PointerDndInteractor(PointerInputElement rootElement, DndController dndController)
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

	
	
	public ArrayList<PointerInputElement.DndTarget> getDndTargets(Point2 localPos)
	{
		return PointerInputElement.getDndTargets( rootElement, localPos );
	}



	private boolean dndButtonDownEvent(PointerButtonEvent event)
	{
		if ( dndController != null )
		{
			ArrayList<PointerInputElement.DndTarget> targets = getDndTargets( event.getPointer().getLocalPos() );
			for (PointerInputElement.DndTarget target: targets)
			{
				if ( target.isSource() )
				{
					PointerInputElement sourceElement = target.getElement();
					DndHandler dndHandler = target.getDndHandler();
					int button = event.getButton();
				
					int requestedAction = dndHandler.getSourceRequestedAction( sourceElement, event.getPointer(), button );
					if ( requestedAction != TransferHandler.NONE )
					{
						int requestedAspect = dndHandler.getSourceRequestedAspect( sourceElement, event.getPointer(), button );
						if ( requestedAspect != DndHandler.ASPECT_NONE )
						{
							dndDrop = new DndDragSwing( sourceElement, button );
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
		if ( dndController != null )
		{
			DndDragSwing drop = dndDrop;
	
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
			}
		}
		return false;
	}

	private boolean dndButtonUpEvent(PointerButtonEvent event)
	{
		if ( dndController != null )
		{
			DndDragSwing drop = dndDrop;
			
			if ( drop != null  &&  drop.bInProgress )
			{
				dndDrop = null;
				
				return true;
			}
		}
		return false;
	}
}
