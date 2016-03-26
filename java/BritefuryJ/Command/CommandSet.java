//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Command;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Interactor.KeyElementInteractor;

public class CommandSet implements CommandSetSource
{
	protected class CommandSetInteractor implements GatherCommandSetInteractor, KeyElementInteractor 
	{
		@Override
		public void gatherCommandSets(LSElement element, List<CommandSet> commandSets)
		{
			commandSets.add( CommandSet.this );
		}

		@Override
		public boolean keyPressed(LSElement element, KeyEvent event)
		{
			Command cmd = shortcuts.getCommandForKeyPressed( event );
			if ( cmd != null )
			{
				cmd.bindTo( element ).execute( element.getRootElement().getPageController() );
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
	protected ArrayList<Command> commands = new ArrayList<Command>();
	private CommandSetInteractor interactor = new CommandSetInteractor();
	protected ShortcutTable shortcuts = new ShortcutTable();
	
	
	public CommandSet(String name, List<Command> commands)
	{
		this.name = name;
		this.commands.addAll( commands );
		shortcuts.addCommands( commands );
	}

	public CommandSet(String name, Command command)
	{
		this.name = name;
		this.commands.add( command );
		shortcuts.addCommand( command );
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
