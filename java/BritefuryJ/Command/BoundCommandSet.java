//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import java.awt.event.KeyEvent;
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

	public void buildAutocompleteList(List<BoundCommand> autocomplete, String text)
	{
		for (Command cmd: commandSet.commands)
		{
			String cmdName = cmd.getName().getName().toLowerCase();
			
			if ( cmdName.startsWith( text )  ||  cmdName.contains( " " + text ) )
			{
				autocomplete.add( cmd.bindTo( binding ) );
			}
		}
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
	
	
	public BoundCommand getCommandForKeyPressed(KeyEvent event)
	{
		Command cmd = commandSet.shortcuts.getCommandForKeyPressed( event );
		return cmd != null  ?  cmd.bindTo( binding )  :  null;
	}
}
