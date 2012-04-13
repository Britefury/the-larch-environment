//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;

import BritefuryJ.Shortcut.Shortcut;

class ShortcutTable
{
	private HashMap<Shortcut, Command> shortcutToCommand = new HashMap<Shortcut, Command>();

	
	public ShortcutTable()
	{
	}
	
	
	public void addCommand(Command cmd)
	{
		shortcutToCommand.put( cmd.getShortcut(), cmd );
	}
	
	public void addCommands(List<Command> commands)
	{
		for (Command cmd: commands)
		{
			addCommand( cmd );
		}
	}
	
	public void addCommands(ShortcutTable commands)
	{
		shortcutToCommand.putAll( commands.shortcutToCommand );
	}
	
	
	public Command getCommandForKeyPressed(KeyEvent event)
	{
		Shortcut key = Shortcut.fromPressedEvent( event );
		return shortcutToCommand.get( key );
	}
}
