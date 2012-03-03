//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.LSpace.LSElement;

public class CommandSet implements CommandSetSource
{
	protected class CommandSetInteractor implements GatherCommandSetInteractor
	{
		@Override
		public void gatherCommandSets(LSElement element, List<CommandSet> commandSets)
		{
			commandSets.add( CommandSet.this );
		}
	}
	
	
	private String name;
	protected ArrayList<Command> commands = new ArrayList<Command>();
	private CommandSetInteractor interactor = new CommandSetInteractor();
	
	
	public CommandSet(String name, List<Command> commands)
	{
		this.name = name;
		this.commands.addAll( commands );
	}

	public CommandSet(String name, Command command)
	{
		this.name = name;
		this.commands.add( command );
	}
	
	
	
	public String getName()
	{
		return name;
	}
	
	
	public BoundCommandSet bindTo(Object binding)
	{
		return new BoundCommandSet( this, binding );
	}
	
	
	@Override
	public GatherCommandSetInteractor getInteractor()
	{
		return interactor;
	}
}
