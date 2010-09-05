//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input;

import java.awt.event.MouseEvent;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.Math.Point2;

public class PointerDragInteractor extends PointerInteractor
{
	private int dragButton = -1;
	private Point2 dragStartPos = null;
	
	
	
	public PointerDragInteractor()
	{
	}
	
	
	
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		if ( canStartDrag( event ) )
		{
			dragButton = event.getButton();
			dragStartPos = event.getPointer().getLocalPos();
			dragBegin( event );
			grabPointer( pointer );
			return true;
		}
		
		return false;
	}
	
	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		if ( event.getButton() == dragButton )
		{
			dragEnd( event, dragStartPos, dragButton );
			ungrabPointer( pointer );
			dragButton = -1;
			dragStartPos = null;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean buttonClicked(Pointer pointer, PointerButtonEvent event)
	{
		return dragStartPos != null;
	}
	
	public boolean drag(Pointer pointer, PointerMotionEvent event, MouseEvent mouseEvent)
	{
		if ( dragStartPos != null )
		{
			dragMotion( event, dragStartPos, dragButton );
			return true;
		}
		else
		{
			return false;
		}
	}

	
	
	
	public boolean canStartDrag(PointerButtonEvent event)
	{
		return true;
	}
	
	public void dragBegin(PointerButtonEvent event)
	{
	}
	
	public void dragEnd(PointerButtonEvent event, Point2 dragStartPos, int dragButton)
	{
	}
	
	public void dragMotion(PointerMotionEvent event, Point2 dragStartPos, int dragButton)
	{
	}
}
