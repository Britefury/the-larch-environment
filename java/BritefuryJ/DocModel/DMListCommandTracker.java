//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.CommandHistory.Command;
import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.CommandTrackerFactory;
import BritefuryJ.CommandHistory.Trackable;

class DMListCommandTracker extends CommandTracker
{
	private static class AddCommand extends Command
	{
		private DMList xls;
		private Object x;
		
		public AddCommand(DMList ls, Object x)
		{
			this.xls = ls;
			this.x = x;
		}

		
		protected void execute()
		{
			xls.add( x );
		}

		protected void unexecute()
		{
			xls.remove( xls.size() - 1 );
		}

		protected String getDescription()
		{
			return "List add";
		}
	}

	
	
	private static class InsertCommand extends Command
	{
		private DMList ls;
		private int i;
		private Object x;
		
		public InsertCommand(DMList ls, int i, Object x)
		{
			this.ls = ls;
			this.i = i;
			this.x = x;
		}

		
		protected void execute()
		{
			ls.add( i, x );
		}

		protected void unexecute()
		{
			ls.remove( i );
		}

		protected String getDescription()
		{
			return "List insert (" + i + ")";
		}
	}

	
	
	private static class AddAllCommand extends Command
	{
		private DMList ls;
		private List<Object> x;
		
		public AddAllCommand(DMList ls, List<Object> x)
		{
			this.ls = ls;
			this.x = x;
		}

		
		protected void execute()
		{
			ls.addAll( x );
		}

		protected void unexecute()
		{
			ls.removeLast( x.size() );
		}

		protected String getDescription()
		{
			return "List add all";
		}
	}

	
	
	private static class InsertAllCommand extends Command
	{
		private DMList ls;
		private int i;
		private List<Object> x;
		
		public InsertAllCommand(DMList ls, int i, List<Object> x)
		{
			this.ls = ls;
			this.i = i;
			this.x = x;
		}

		
		protected void execute()
		{
			ls.addAll( i, x );
		}

		protected void unexecute()
		{
			ls.removeRange( i, x.size() );
		}

		protected String getDescription()
		{
			return "List insert all (" + i + ")";
		}
	}

	
	
	private static class ClearCommand extends Command
	{
		private DMList ls;
		private ArrayList<Object> contents;
		
		public ClearCommand(DMList ls, ArrayList<Object> contents)
		{
			this.ls = ls;
			this.contents = contents;
		}

		
		protected void execute()
		{
			ls.clear();
		}

		protected void unexecute()
		{
			ls.addAll( contents );
		}

		protected String getDescription()
		{
			return "List clear";
		}
	}

	
	private static class RemoveCommand extends Command
	{
		private DMList ls;
		private int i;
		private Object x;
		
		public RemoveCommand(DMList ls, int i, Object x)
		{
			this.ls = ls;
			this.i = i;
			this.x = x;
		}

		
		protected void execute()
		{
			ls.remove( i );
		}

		protected void unexecute()
		{
			ls.add( i, x );
		}

		protected String getDescription()
		{
			return "List remove (" + i + ")";
		}
	}


	
	private static class SetCommand extends Command
	{
		private DMList ls;
		private int i;
		private Object oldX, x;
		
		public SetCommand(DMList ls, int i, Object oldX, Object x)
		{
			this.ls = ls;
			this.i = i;
			this.oldX = oldX;
			this.x = x;
		}

		
		protected void execute()
		{
			ls.set( i, x );
		}

		protected void unexecute()
		{
			ls.set( i, oldX );
		}

		protected String getDescription()
		{
			return "List set (" + i + ")";
		}
	}

	
	
	private static class SetContentsCommand extends Command
	{
		private DMList ls;
		private Object[] oldContents;
		private Object[] newContents;
		
		public SetContentsCommand(DMList ls, Object[] oldContents, Object[] newContents)
		{
			this.ls = ls;
			this.oldContents = oldContents;
			this.newContents = newContents;
		}

		
		protected void execute()
		{
			ls.commandTracker_setContents( newContents );
		}

		protected void unexecute()
		{
			ls.commandTracker_setContents( oldContents );
		}

		protected String getDescription()
		{
			return "List set contents";
		}
	}

	private static class RemoveLastCommand extends Command
	{
		private DMList ls;
		private Object removedValues[];
		
		public RemoveLastCommand(DMList ls, Object removedValues[])
		{
			this.ls = ls;
			this.removedValues = removedValues;
		}

		
		protected void execute()
		{
			ls.removeLast( removedValues.length );
		}

		protected void unexecute()
		{
			ls.addAll( Arrays.asList( removedValues ) );
		}

		protected String getDescription()
		{
			return "List remove last";
		}
	}

	private static class RemoveRangeCommand extends Command
	{
		private DMList ls;
		private int pos;
		private Object removedValues[];
		
		public RemoveRangeCommand(DMList ls, int pos, Object removedValues[])
		{
			this.ls = ls;
			this.pos = pos;
			this.removedValues = removedValues;
		}

		
		protected void execute()
		{
			ls.removeRange( pos, removedValues.length );
		}

		protected void unexecute()
		{
			ls.addAll( pos, Arrays.asList( removedValues ) );
		}

		protected String getDescription()
		{
			return "List remove range";
		}
	}

	
	
	
	
	public DMListCommandTracker(CommandHistory commandHistory)
	{
		super( commandHistory );
	}
	
	
	
	protected void track(Trackable t)
	{
		super.track( t );
		
		DMList ls = (DMList)t;
		for (Object x: ls)
		{
			commandHistory.track( x );
		}
	}
	
	protected void stopTracking(Trackable t)
	{
		DMList ls = (DMList)t;
		for (Object x: ls)
		{
			commandHistory.stopTracking( x );
		}

		super.stopTracking( t );
	}
	
	
	
	protected void onAdd(DMList ls, Object x)
	{
		commandHistory.track( x );
		commandHistory.addCommand( new AddCommand( ls, x ) );
	}

	protected void onInsert(DMList ls, int i, Object x)
	{
		commandHistory.track( x );
		commandHistory.addCommand( new InsertCommand( ls, i, x ) );
	}

	protected void onAddAll(DMList ls, List<Object> xs)
	{
		for (Object x: xs)
		{
			commandHistory.track( x );
		}
		commandHistory.addCommand( new AddAllCommand( ls, xs ) );
	}

	protected void onInsertAll(DMList ls, int i, List<Object> xs)
	{
		for (Object x: xs)
		{
			commandHistory.track( x );
		}
		commandHistory.addCommand( new InsertAllCommand( ls, i, xs ) );
	}

	protected void onClear(DMList ls, ArrayList<Object> contents)
	{
		commandHistory.addCommand( new ClearCommand( ls, contents ) );
		for (Object x: contents)
		{
			commandHistory.stopTracking( x );
		}
	}

	protected void onRemove(DMList ls, int i, Object x)
	{
		commandHistory.addCommand( new RemoveCommand( ls, i, x ) );
		commandHistory.stopTracking( x );
	}

	protected void onSet(DMList ls, int i, Object oldX, Object x)
	{
		commandHistory.track( x );
		commandHistory.addCommand( new SetCommand( ls, i, oldX, x ) );
		commandHistory.stopTracking( oldX );
	}

	protected void onSetContents(DMList ls, Object[] oldContents, Object[] newContents)
	{
		for (Object x: newContents)
		{
			commandHistory.track( x );
		}
		commandHistory.addCommand( new SetContentsCommand( ls, oldContents, newContents ) );
		for (Object oldX: oldContents)
		{
			commandHistory.stopTracking( oldX );
		}
	}
	
	protected void onRemoveLast(DMList ls, Object[] removedValues)
	{
		commandHistory.addCommand( new RemoveLastCommand( ls, removedValues ) );
		for (Object x: removedValues)
		{
			commandHistory.stopTracking( x );
		}
	}
	
	protected void onRemoveRange(DMList ls, int pos, Object[] removedValues)
	{
		commandHistory.addCommand( new RemoveRangeCommand( ls, pos, removedValues ) );
		for (Object x: removedValues)
		{
			commandHistory.stopTracking( x );
		}
	}
	
	
	public static CommandTrackerFactory factory = new CommandTrackerFactory()
	{
		@Override
		public CommandTracker createTracker(CommandHistory history)
		{
			return new DMListCommandTracker( history );
		}
	};
}
