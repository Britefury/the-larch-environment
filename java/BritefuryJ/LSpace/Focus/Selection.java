//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Focus;

import java.awt.Graphics2D;
import java.util.ArrayList;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.LSRootElement;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;

public abstract class Selection
{
	private ArrayList<SelectionListener> listeners;
	protected LSRootElement rootElement;
	
	
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
	
	
	
	public LSRootElement getRootElement()
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
	
	
	
	public boolean deleteContents(Target target)
	{
		LSRegion region = getRegion();
		if ( region != null )
		{
			ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
			if ( clipboardHandler != null )
			{
				return deleteSelectionInRegion( target, clipboardHandler );
			}
		}
		
		return false;
	}



	protected boolean deleteSelectionInRegion(Target target, ClipboardHandlerInterface clipboardHandler)
	{
		return clipboardHandler.deleteSelection( this, target );
	}




	public boolean replaceContentsWithText(String replacement, Target target)
	{
		LSRegion region = getRegion();
		if ( region != null )
		{
			ClipboardHandlerInterface clipboardHandler = region.getClipboardHandler();
			if ( clipboardHandler != null )
			{
				return clipboardHandler.replaceSelectionWithText( this, target, replacement );
			}
		}
		
		return false;
	}
}
