//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import BritefuryJ.CommandHistory.Command;
import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.CommandHistory.CommandTracker;

class DMListCommandTracker extends CommandTracker
{
	private static class AddCommand extends Command
	{
		private DMList xs;
		private Object x;
		
		public AddCommand(DMList xs, Object x)
		{
			this.xs = xs;
			this.x = x;
		}

		
		protected void execute()
		{
			xs.add( x );
		}

		protected void unexecute()
		{
			xs.remove( xs.size() - 1 );
		}
	}

	
	
	private static class InsertCommand extends Command
	{
		private DMList xs;
		private int i;
		private Object x;
		
		public InsertCommand(DMList xs, int i, Object x)
		{
			this.xs = xs;
			this.i = i;
			this.x = x;
		}

		
		protected void execute()
		{
			xs.add( i, x );
		}

		protected void unexecute()
		{
			xs.remove( i );
		}
	}

	
	
	private static class AddAllCommand extends Command
	{
		private DMList xs;
		private List<Object> x;
		
		public AddAllCommand(DMList xs, List<Object> x)
		{
			this.xs = xs;
			this.x = x;
		}

		
		protected void execute()
		{
			xs.addAll( x );
		}

		protected void unexecute()
		{
			xs.removeLast( x.size() );
		}
	}

	
	
	private static class InsertAllCommand extends Command
	{
		private DMList xs;
		private int i;
		private List<Object> x;
		
		public InsertAllCommand(DMList xs, int i, List<Object> x)
		{
			this.xs = xs;
			this.i = i;
			this.x = x;
		}

		
		protected void execute()
		{
			xs.addAll( i, x );
		}

		protected void unexecute()
		{
			xs.removeRange( i, x.size() );
		}
	}

	
	
	private static class ClearCommand extends Command
	{
		private DMList xs;
		private Vector<Object> contents;
		
		public ClearCommand(DMList xs, Vector<Object> contents)
		{
			this.xs = xs;
			this.contents = contents;
		}

		
		protected void execute()
		{
			xs.clear();
		}

		protected void unexecute()
		{
			xs.addAll( contents );
		}
	}

	
	private static class RemoveCommand extends Command
	{
		private DMList xs;
		private int i;
		private Object x;
		
		public RemoveCommand(DMList xs, int i, Object x)
		{
			this.xs = xs;
			this.i = i;
			this.x = x;
		}

		
		protected void execute()
		{
			xs.remove( i );
		}

		protected void unexecute()
		{
			xs.add( i, x );
		}
	}


	
	private static class SetCommand extends Command
	{
		private DMList xs;
		private int i;
		private Object oldX, x;
		
		public SetCommand(DMList xs, int i, Object oldX, Object x)
		{
			this.xs = xs;
			this.i = i;
			this.oldX = oldX;
			this.x = x;
		}

		
		protected void execute()
		{
			xs.set( i, x );
		}

		protected void unexecute()
		{
			xs.set( i, oldX );
		}
	}

	
	
	private static class SetContentsCommand extends Command
	{
		private DMList xs;
		private List<Object> oldContents;
		private Object[] newContents;
		
		public SetContentsCommand(DMList xs, List<Object> oldContents, Object[] newContents)
		{
			this.xs = xs;
			this.oldContents = oldContents;
			this.newContents = newContents;
		}

		
		protected void execute()
		{
			xs.setContents( Arrays.asList( newContents ) );
		}

		protected void unexecute()
		{
			xs.setContents( oldContents );
		}
	}

	
	
	
	
	public DMListCommandTracker(CommandHistory commandHistory)
	{
		super( commandHistory );
	}
	
	
	
	protected void onAdd(DMList xs, Object x)
	{
		commandHistory.addCommand( new AddCommand( xs, x ) );
	}

	protected void onInsert(DMList xs, int i, Object x)
	{
		commandHistory.addCommand( new InsertCommand( xs, i, x ) );
	}

	protected void onAddAll(DMList xs, List<Object> x)
	{
		commandHistory.addCommand( new AddAllCommand( xs, x ) );
	}

	protected void onInsertAll(DMList xs, int i, List<Object> x)
	{
		commandHistory.addCommand( new InsertAllCommand( xs, i, x ) );
	}

	protected void onClear(DMList xs, Vector<Object> contents)
	{
		commandHistory.addCommand( new ClearCommand( xs, contents ) );
	}

	protected void onRemove(DMList xs, int i, Object x)
	{
		commandHistory.addCommand( new RemoveCommand( xs, i, x ) );
	}

	protected void onSet(DMList xs, int i, Object oldX, Object x)
	{
		commandHistory.addCommand( new SetCommand( xs, i, oldX, x ) );
	}

	protected void onSetContents(DMList xs, List<Object> oldContents, Object[] newContents)
	{
		commandHistory.addCommand( new SetContentsCommand( xs, oldContents, newContents ) );
	}

}
