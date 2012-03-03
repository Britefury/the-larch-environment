//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Selection;

import java.awt.Graphics2D;
import java.util.ArrayList;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.PresentationComponent;

public abstract class Selection
{
	private ArrayList<SelectionListener> listeners;
	protected PresentationComponent.RootElement rootElement;
	
	
	public Selection(LSElement element)
	{
		this.rootElement = element.getRootElement();
	}
	
	
	
	abstract public LSRegion getRegion();
	
	
	public abstract boolean isEditable();

	
	public void onPresentationTreeStructureChanged()
	{
	}
	
	
	
	abstract public void draw(Graphics2D graphics);
	
	
	
	public PresentationComponent.RootElement getRootElement()
	{
		return rootElement;
	}
	
	
	
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
