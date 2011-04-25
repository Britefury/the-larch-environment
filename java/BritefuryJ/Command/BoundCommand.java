//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

public class BoundCommand
{
	private Command command;
	private Object binding;
	

	protected  BoundCommand(Command command, Object binding)
	{
		this.command = command;
		this.binding = binding;
	}

	
	
	public Command getCommand()
	{
		return command;
	}


	protected void execute()
	{
		command.action.commandAction( binding );
	}
}
