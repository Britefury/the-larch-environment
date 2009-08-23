//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;

public class InteractionListener
{
	public boolean onButtonDown(PointerButtonEvent event)
	{
		return false;
	}

	public boolean onButtonDown2(PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean onButtonDown3(PointerButtonEvent event)
	{
		return false;
	}

	public boolean onButtonUp(PointerButtonEvent event)
	{
		return false;
	}


	public void onMotion(PointerMotionEvent event)
	{
	}

	public void onEnter(PointerMotionEvent event)
	{
	}

	public void onLeave(PointerMotionEvent event)
	{
	}
	
	public void onLeaveIntoChild(PointerMotionEvent event, PointerInputElement child)
	{
	}
	
	public void onEnterFromChild(PointerMotionEvent event, PointerInputElement child)
	{
	}
	
	
	public boolean onScroll(PointerScrollEvent event)
	{
		return false;
	}
}
