//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;

public class CommandSet implements Iterable<Command>
{
	protected class CommandSetInteractor implements AbstractElementInteractor
	{
		CommandSet getCommandSet(DPElement element)
		{
			return CommandSet.this;
		}
	}
	
	
	private ArrayList<Command> commands = new ArrayList<Command>();
	private CommandSetInteractor interactor = new CommandSetInteractor();
	
	
	public CommandSet(List<Command> commands)
	{
		this.commands.addAll( commands );
	}

	public CommandSet(Command command)
	{
		this.commands.add( command );
	}
	
	
	
	public Iterator<Command> iterator()
	{
		return commands.iterator();
	}
	
	
	public Command getCommand(String charSequence)
	{
		for (Command cmd: commands)
		{
			if ( cmd.getCharSequence().equals( charSequence ) )
			{
				return cmd;
			}
		}
		
		return null;
	}

	
	public void buildListOfCommandsStartingWith(List<Command> cmdList, String charSequence)
	{
		for (Command cmd: commands)
		{
			if ( cmd.getCharSequence().startsWith( charSequence ) )
			{
				cmdList.add( cmd );
			}
		}
	}
	
	
	public CommandSetInteractor getInteractor()
	{
		return interactor;
	}
}
