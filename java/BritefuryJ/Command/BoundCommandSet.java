//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.util.List;

public class BoundCommandSet
{
	private CommandSet commandSet;
	private Object binding;
	

	protected  BoundCommandSet(CommandSet commandSet, Object binding)
	{
		this.commandSet = commandSet;
		this.binding = binding;
	}
	
	
	
	public BoundCommand getCommand(String charSequence)
	{
		for (Command cmd: commandSet.commands)
		{
			if ( cmd.getName().getCharSequence().equals( charSequence ) )
			{
				return cmd.bindTo( binding );
			}
		}
		
		return null;
	}

	
	public void buildListOfCommandsStartingWith(List<BoundCommand> cmdList, String charSequence)
	{
		for (Command cmd: commandSet.commands)
		{
			if ( cmd.getName().getCharSequence().startsWith( charSequence ) )
			{
				cmdList.add( cmd.bindTo( binding ) );
			}
		}
	}
}
