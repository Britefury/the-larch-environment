//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
