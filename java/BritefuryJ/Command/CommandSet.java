//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;

public class CommandSet
{
	protected class CommandSetInteractor implements AbstractElementInteractor
	{
		CommandSet getCommandSet(DPElement element)
		{
			return CommandSet.this;
		}
	}
	
	
	protected ArrayList<Command> commands = new ArrayList<Command>();
	private CommandSetInteractor interactor = new CommandSetInteractor();
	
	
	public CommandSet(List<Command> commands)
	{
		this.commands.addAll( commands );
	}

	public CommandSet(Command command)
	{
		this.commands.add( command );
	}
	
	
	
	public BoundCommandSet bindTo(Object binding)
	{
		return new BoundCommandSet( this, binding );
	}
	
	
	public CommandSetInteractor getInteractor()
	{
		return interactor;
	}
}