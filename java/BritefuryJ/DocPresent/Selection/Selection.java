//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Selection;

import java.awt.Graphics2D;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPRegion;

public abstract class Selection
{
	private ArrayList<SelectionListener> listeners;
	
	
	public Selection()
	{
	}
	
	
	
	abstract public DPRegion getRegion();
	
	
	
	abstract public void draw(Graphics2D graphics);
	
	
	public void addSelectionListener(SelectionListener listener)
	{
		if ( listeners == null )
		{
			listeners = new ArrayList<SelectionListener>();
		}
		listeners.add( listener );
	}
	
	public void removeSelectionListener(SelectionListener listener)
	{
		if ( listeners != null )
		{
			listeners.remove( listener );
			if ( listeners.isEmpty() )
			{
				listeners = null;
			}
		}
	}
	
	protected void notifyListenersOfChange()
	{
		if ( listeners != null )
		{
			for (SelectionListener listener: listeners)
			{
				listener.selectionChanged( this );
			}
		}
	}
}
