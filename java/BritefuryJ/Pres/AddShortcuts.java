//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Shortcut.Shortcut;
import BritefuryJ.Shortcut.ShortcutElementAction;
import BritefuryJ.StyleSheet.StyleValues;

public class AddShortcuts extends Pres
{
	private static class Entry
	{
		private Shortcut shortcut;
		private ShortcutElementAction action;
		private Entry next;
		
		
		public Entry(Shortcut shortcut, ShortcutElementAction action, Entry next)
		{
			this.shortcut = shortcut;
			this.action = action;
			this.next = next;
		}
	}
	
	private Entry shortcutsHead;
	private Pres child;
	
	
	protected AddShortcuts(Pres child, Shortcut shortcut, ShortcutElementAction action, Entry existingShortcuts)
	{
		shortcutsHead = new Entry( shortcut, action, existingShortcuts );
		this.child = child;
	}
	
	public AddShortcuts(Pres child, Shortcut shortcut, ShortcutElementAction action)
	{
		this( child, shortcut, action, null );
	}
	
	
	@Override
	public AddShortcuts withShortcut(Shortcut shortcut, ShortcutElementAction action)
	{
		return new AddShortcuts( child, shortcut, action, shortcutsHead );
	}

	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		Entry e = shortcutsHead;
		while ( e != null )
		{
			element.addShortcut( e.shortcut, e.action );
			e = e.next;
		}
		return element;
	}
}
