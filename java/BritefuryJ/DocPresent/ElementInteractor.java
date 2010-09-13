//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.Caret.Caret;

public abstract class ElementInteractor
{
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
	
	
	public void onRealise(DPElement element)
	{
	}

	public void onUnrealise(DPElement element)
	{
	}
}
