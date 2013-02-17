//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Input;

import java.awt.event.MouseEvent;

import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Event.PointerScrollEvent;

public class PointerInteractor
{
	public void addToPointer(Pointer pointer)
	{
		pointer.addInteractor( this );
	}
	
	public void removeFromPointer(Pointer pointer)
	{
		pointer.removeInteractor( this );
	}
	
	public void grabPointer(Pointer pointer)
	{
		pointer.interactorGrab( this );
	}
	
	public void ungrabPointer(Pointer pointer)
	{
		pointer.interactorUngrab( this );
	}
	
	
	
	
	public boolean buttonDown(Pointer pointer, PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean buttonUp(Pointer pointer, PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean buttonClicked(Pointer pointer, PointerButtonClickedEvent event)
	{
		return false;
	}
	
	public boolean motion(Pointer pointer, PointerMotionEvent event, MouseEvent mouseEvent)
	{
		return false;
	}

	public boolean drag(Pointer pointer, PointerMotionEvent event, MouseEvent mouseEvent)
	{
		return false;
	}

	public boolean enter(Pointer pointer, PointerMotionEvent event)
	{
		return false;
	}

	public boolean leave(Pointer pointer, PointerMotionEvent event)
	{
		return false;
	}

	public boolean scroll(Pointer pointer, PointerScrollEvent event)
	{
		return false;
	}


	public boolean dndDragExportDone(Pointer pointer)
	{
		return false;
	}
}
