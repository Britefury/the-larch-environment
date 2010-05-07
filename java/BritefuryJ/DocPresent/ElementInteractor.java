//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.ContextMenu.ContextMenu;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Event.PointerScrollEvent;

public abstract class ElementInteractor
{
	public boolean onButtonDown(DPElement element, PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean onButtonUp(DPElement element, PointerButtonEvent event)
	{
		return false;
	}
	
	public boolean onButtonClicked(DPElement element, PointerButtonClickedEvent event)
	{
		return false;
	}
	
	public boolean onContextButton(DPElement element, ContextMenu menu)
	{
		return false;
	}
	

	public void onMotion(DPElement element, PointerMotionEvent event)
	{
	}
	
	public void onDrag(DPElement element, PointerMotionEvent event)
	{
	}
	
	public void onEnter(DPElement element, PointerMotionEvent event)
	{
	}
	
	public void onLeave(DPElement element, PointerMotionEvent event)
	{
	}
	
	
	public boolean onScroll(DPElement element, PointerScrollEvent event)
	{
		return false;
	}

	
	public void onCaretEnter(DPElement element, Caret c)
	{
	}

	public void onCaretLeave(DPElement element, Caret c)
	{
	}
	
	
	public boolean onKeyPress(DPElement element, KeyEvent event)
	{
		return false;
	}
	
	public boolean onKeyRelease(DPElement element, KeyEvent event)
	{
		return false;
	}
	
	public boolean onKeyTyped(DPElement element, KeyEvent event)
	{
		return false;
	}

	
	public void drawBackground(DPElement element, Graphics2D graphics)
	{
	}

	public void draw(DPElement element, Graphics2D graphics)
	{
	}
}
