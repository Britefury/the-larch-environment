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

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;

public class CommandSetRegistry implements CommandSetSource
{
	protected class CommandSetRegistryInteractor implements GatherCommandSetInteractor, KeyElementInteractor 
	{
		@Override
		public void gatherCommandSets(LSElement element, List<CommandSet> commandSets)
		{
			commandSets.addAll( nameToCmdSet.values() );
		}

		@Override
		public boolean keyPressed(LSElement element, KeyEvent event)
		{
			Command cmd = shortcuts.getCommandForKeyPressed( event );
			if ( cmd != null )
			{
				cmd.bindTo( element ).execute();
				return true;
			}
			return false;
		}

		@Override
		public boolean keyReleased(LSElement element, KeyEvent event)
		{
			Command cmd = shortcuts.getCommandForKeyPressed( event );
			return cmd != null;
		}

		@Override
		public boolean keyTyped(LSElement element, KeyEvent event)
		{
			return false;
		}
	}

	
	private String name;
	private HashMap<String, CommandSet> nameToCmdSet = new HashMap<String, CommandSet>();
	protected ShortcutTable shortcuts = new ShortcutTable();
	private CommandSetRegistryInteractor interactor = new CommandSetRegistryInteractor();
	
	
	
	public CommandSetRegistry(String name)
	{
		this.name = name;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	
	public void registerCommandSet(CommandSet commandSet)
	{
		nameToCmdSet.put( commandSet.getName(), commandSet );
		shortcuts.addCommands( commandSet.shortcuts );
	}
	
	
	@Override
	public GatherCommandSetInteractor getInteractor()
	{
		return interactor;
	}
}
