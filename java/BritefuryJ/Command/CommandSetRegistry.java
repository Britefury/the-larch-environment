//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Command;

import java.util.HashMap;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;

public class CommandSetRegistry implements CommandSetSource
{
	protected class CommandSetRegistryInteractor implements GatherCommandSetInteractor
	{
		@Override
		public void gatherCommandSets(DPElement element, List<CommandSet> commandSets)
		{
			commandSets.addAll( nameToCmdSet.values() );
		}
	}

	
	private String name;
	private HashMap<String, CommandSet> nameToCmdSet = new HashMap<String, CommandSet>();
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
	}
	
	
	@Override
	public GatherCommandSetInteractor getInteractor()
	{
		return interactor;
	}
}
