//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;

public abstract class ElementInteractor
{
	public boolean onButtonDown(DPWidget element, PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean onButtonDown2(DPWidget element, PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean onButtonDown3(DPWidget element, PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean onButtonUp(DPWidget element, PointerButtonEvent event)
	{
		return false;
	}
	

	public void onMotion(DPWidget element, PointerMotionEvent event)
	{
	}
	
	public void onDrag(DPWidget element, PointerMotionEvent event)
	{
	}
	
	public void onEnter(DPWidget element, PointerMotionEvent event)
	{
	}
	
	public void onLeave(DPWidget element, PointerMotionEvent event)
	{
	}
	
	
	public boolean onScroll(DPWidget element, PointerScrollEvent event)
	{
		return false;
	}

	
	public void onCaretEnter(DPWidget element, Caret c)
	{
	}

	public void onCaretLeave(DPWidget element, Caret c)
	{
	}
	
	
	public void drawBackground(DPWidget element, Graphics2D graphics)
	{
	}

	public void draw(DPWidget element, Graphics2D graphics)
	{
	}
}
