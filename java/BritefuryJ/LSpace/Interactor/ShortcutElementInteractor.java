//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Interactor;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Shortcut.Shortcut;
import BritefuryJ.Shortcut.ShortcutElementAction;

public class ShortcutElementInteractor implements KeyElementInteractor
{
	private HashMap<Shortcut, ShortcutElementAction> shortcuts = new HashMap<Shortcut, ShortcutElementAction>();
	
	
	public ShortcutElementInteractor()
	{
	}
	
	
	public void addShortcut(Shortcut shortcut, ShortcutElementAction action)
	{
		shortcuts.put( shortcut, action );
	}
	
	public void removeShortcut(Shortcut shortcut)
	{
		shortcuts.remove( shortcut );
	}
	
	public boolean isEmpty()
	{
		return shortcuts.isEmpty();
	}
	

	@Override
	public boolean keyPressed(LSElement element, KeyEvent event)
	{
		ShortcutElementAction action = shortcuts.get( Shortcut.fromPressedEvent( event ) );
		if ( action != null )
		{
			action.invoke( element );
			return true;
		}
		return false;
	}

	@Override
	public boolean keyReleased(LSElement element, KeyEvent event)
	{
		ShortcutElementAction action = shortcuts.get( Shortcut.fromPressedEvent( event ) );
		return action != null;
	}

	@Override
	public boolean keyTyped(LSElement element, KeyEvent event)
	{
		return false;
	}
}
