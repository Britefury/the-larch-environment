//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CommandHistory;

import java.util.HashMap;
import java.util.Vector;

public class CommandHistory
{
	private static abstract class Entry
	{
		public abstract void execute();
		public abstract void unexecute();
		public abstract boolean isEmpty();
		public abstract Command top();
	}
	
	private static class SingleEntry extends Entry
	{
		private Command command;
		
		public SingleEntry(Command command)
		{
			this.command = command;
		}
		
		public void execute()
		{
			command.execute();
		}

		public void unexecute()
		{
			command.unexecute();
		}
		
		public boolean isEmpty()
		{
			return false;
		}
		
		public Command top()
		{
			return command;
		}
		
		public Command getCommand()
		{
			return command;
		}
	}

	private static class MultiEntry extends Entry
	{
		private Vector<Command> commands;
		
		public MultiEntry()
		{
			commands = new Vector<Command>();
		}
		
		public MultiEntry(SingleEntry entry)
		{
			commands = new Vector<Command>();
			commands.add( entry.getCommand() );
		}
		
		public void execute()
		{
			for (Command c: commands)
			{
				c.execute();
			}
		}

		public void unexecute()
		{
			for (int i = commands.size() - 1; i >= 0; i--)
			{
				commands.get( i ).unexecute();
			}
		}

		public boolean isEmpty()
		{
			return commands.size() == 0;
		}

		public Command top()
		{
			if ( commands.size() > 0 )
			{
				return commands.lastElement();
			}
			else
			{
				return null;
			}
		}
		
		
		public void add(Command command)
		{
			commands.add( command );
		}
	}
	
	
	
	private Vector<Entry> past, future;
	private HashMap<CommandTrackerFactory, CommandTracker> trackers;
	private boolean bCommandsBlocked, bFrozen;
	private int freezeCount;
	private CommandHistoryListener listener;
	
	
	
	
	//
	//
	// Constructor
	//
	//
	public CommandHistory()
	{
		past = new Vector<Entry>();
		future = new Vector<Entry>();
		trackers = new HashMap<CommandTrackerFactory, CommandTracker>();
		bCommandsBlocked = false;
		bFrozen = false;
		freezeCount = 0;
		listener = null;
	}
	
	
	
	public void setListener(CommandHistoryListener listener)
	{
		this.listener = listener;
	}
	
	
	public void addCommand(Command command)
	{
		if ( !bCommandsBlocked )
		{
			future.clear();
			Command top = topCommand();
			// Attempt to merge @command into @top
			if ( top != null  &&  top.canMergeFrom( command ) )
			{
				top.mergeFrom( command );
			}
			else
			{
				if ( bFrozen )
				{
					((MultiEntry)past.lastElement()).add( command );
				}
				else
				{
					past.add( new SingleEntry( command ) );
				}
			}
			
			if ( listener != null )
			{
				listener.onCommandHistoryChanged( this );
			}
		}
	}
	
	
	
	public void undo()
	{
		if ( bFrozen )
		{
			thaw();
		}
		
		Entry entry = past.lastElement();
		past.setSize( past.size() - 1 );
		unexecuteEntry( entry );
		future.add( entry );
		
		if ( listener != null )
		{
			listener.onCommandHistoryChanged( this );
		}
	}
	
	
	public void redo()
	{
		if ( bFrozen )
		{
			thaw();
		}
		
		Entry entry = future.lastElement();
		future.setSize( future.size() - 1 );
		executeEntry( entry );
		past.add( entry );
		
		if ( listener != null )
		{
			listener.onCommandHistoryChanged( this );
		}
	}
	
	
	public void clear()
	{
		past.clear();
		future.clear();
		
		if ( listener != null )
		{
			listener.onCommandHistoryChanged( this );
		}
	}
	
	
	
	public void freeze()
	{
		if ( !bFrozen )
		{
			// Add a new empty multi-entry
			past.add( new MultiEntry() );
		}
		
		bFrozen = true;
		freezeCount++;
	}
	
	public void thaw()
	{
		freezeCount--;
		if ( freezeCount == 0 )
		{
			// If there is an existing top entry, and it is empty, remove it.
			Entry top = topEntry();
			if ( top != null  &&  top.isEmpty() )
			{
				past.setSize( past.size() - 1 );
			}
			bFrozen = false;
		}
		
		freezeCount = Math.max( freezeCount, 0 );
	}
	
		
	public boolean canUndo()
	{
		return past.size() > 0;
	}

	public boolean canRedo()
	{
		return future.size() > 0;
	}

	
	public int getNumUndoCommands()
	{
		return past.size();
	}

	public int getNumRedoCommands()
	{
		return future.size();
	}
	
	
	
	public void track(Trackable t)
	{
		CommandTrackerFactory factory = t.getTrackerFactory();
		CommandTracker tracker = trackers.get( factory );
		if ( tracker == null )
		{
			tracker = factory.createTracker( this );
			trackers.put( factory, tracker );
		}
		
		t.setTracker( tracker );
	}

	
	public void stopTracking(Trackable t)
	{
		CommandTrackerFactory factory = t.getTrackerFactory();
		CommandTracker tracker = trackers.get( factory );
		assert tracker != null;
		
		t.setTracker( null );
	}
	
	
	
	
	protected Command topCommand()
	{
		if ( past.size() > 0 )
		{
			return past.lastElement().top();
		}
		else
		{
			return null;
		}
	}
	
	protected Entry topEntry()
	{
		if ( past.size() > 0 )
		{
			return past.lastElement();
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	
	
	private void blockCommands()
	{
		bCommandsBlocked = true;
	}
	
	private void unblockCommands()
	{
		bCommandsBlocked = false;
	}
	
	
	private void executeEntry(Entry entry)
	{
		blockCommands();
		entry.execute();
		unblockCommands();
	}

	private void unexecuteEntry(Entry entry)
	{
		blockCommands();
		entry.unexecute();
		unblockCommands();
	}
}
