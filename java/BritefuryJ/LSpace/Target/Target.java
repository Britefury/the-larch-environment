//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Target;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Selection.SelectionPoint;

public abstract class Target
{
	private ArrayList<TargetListener> listeners = new ArrayList<TargetListener>();


	public abstract void draw(Graphics2D graphics);
	
	public boolean isAnimated()
	{
		return false;
	}
	
	
	public abstract LSElement getElement();
	
	public LSElement getKeyboardInputElement()
	{
		return null;
	}
	
	
	public abstract boolean isEditable();
	
	
	
	public abstract SelectionPoint createSelectionPoint();
	
	
	public boolean isValid()
	{
		return true;
	}
	
	
	public boolean onContentKeyPress(KeyEvent event)
	{
		return false;
	}

	public boolean onContentKeyRelease(KeyEvent event)
	{
		return false;
	}

	public boolean onContentKeyTyped(KeyEvent event)
	{
		return false;
	}



	public void moveLeft()
	{
	}

	public void moveRight()
	{
	}
	
	public void moveUp()
	{
	}
	
	public void moveDown()
	{
	}
	

	public void moveToHome()
	{
	}
	
	public void moveToEnd()
	{
	}
	
	
	
	public void ensureVisible()
	{
	}

	
	
	public void addTargetListener(TargetListener listener)
	{
		listeners.add( listener );
	}
	
	public void removeTargetListener(TargetListener listener)
	{
		listeners.remove( listener );
	}



	protected void notifyListenersOfChange()
	{
		for (TargetListener listener: listeners)
		{
			listener.targetChanged( this );
		}
	}
}
