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

import BritefuryJ.ChangeHistory.Change;
import BritefuryJ.ChangeHistory.ChangeHistory;

class DMList_changes
{
	private static class AddCommand extends Change
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

	
	
	private static class InsertCommand extends Change
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

	
	
	private static class AddAllCommand extends Change
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

	
	
	private static class InsertAllCommand extends Change
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

	
	
	private static class ClearCommand extends Change
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

	
	private static class RemoveCommand extends Change
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


	
	private static class SetCommand extends Change
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

	
	
	private static class SetContentsCommand extends Change
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

	private static class RemoveLastCommand extends Change
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

	private static class RemoveRangeCommand extends Change
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

	
	
	
	
	protected static void onAdd(ChangeHistory changeHistory, DMList ls, Object x)
	{
		if ( changeHistory != null )
		{
			changeHistory.addChange( new AddCommand( ls, x ) );
			changeHistory.track( x );
		}
	}

	protected static void onInsert(ChangeHistory changeHistory, DMList ls, int i, Object x)
	{
		if ( changeHistory != null )
		{
			changeHistory.addChange( new InsertCommand( ls, i, x ) );
			changeHistory.track( x );
		}
	}

	protected static void onAddAll(ChangeHistory changeHistory, DMList ls, List<Object> xs)
	{
		if ( changeHistory != null )
		{
			changeHistory.addChange( new AddAllCommand( ls, xs ) );
			for (Object x: xs)
			{
				changeHistory.track( x );
			}
		}
	}

	protected static void onInsertAll(ChangeHistory changeHistory, DMList ls, int i, List<Object> xs)
	{
		if ( changeHistory != null )
		{
			changeHistory.addChange( new InsertAllCommand( ls, i, xs ) );
			for (Object x: xs)
			{
				changeHistory.track( x );
			}
		}
	}

	protected static void onClear(ChangeHistory changeHistory, DMList ls, ArrayList<Object> contents)
	{
		if ( changeHistory != null )
		{
			for (Object x: contents)
			{
				changeHistory.stopTracking( x );
			}
			changeHistory.addChange( new ClearCommand( ls, contents ) );
		}
	}

	protected static void onRemove(ChangeHistory changeHistory, DMList ls, int i, Object x)
	{
		if ( changeHistory != null )
		{
			changeHistory.stopTracking( x );
			changeHistory.addChange( new RemoveCommand( ls, i, x ) );
		}
	}

	protected static void onSet(ChangeHistory changeHistory, DMList ls, int i, Object oldX, Object x)
	{
		if ( changeHistory != null )
		{
			changeHistory.stopTracking( oldX );
			changeHistory.addChange( new SetCommand( ls, i, oldX, x ) );
			changeHistory.track( x );
		}
	}

	protected static void onSetContents(ChangeHistory changeHistory, DMList ls, Object[] oldContents, Object[] newContents)
	{
		if ( changeHistory != null )
		{
			for (Object oldX: oldContents)
			{
				changeHistory.stopTracking( oldX );
			}
			changeHistory.addChange( new SetContentsCommand( ls, oldContents, newContents ) );
			for (Object x: newContents)
			{
				changeHistory.track( x );
			}
		}
	}

	protected static void onRemoveLast(ChangeHistory changeHistory, DMList ls, Object[] removedValues)
	{
		if ( changeHistory != null )
		{
			for (Object x: removedValues)
			{
				changeHistory.stopTracking( x );
			}
			changeHistory.addChange( new RemoveLastCommand( ls, removedValues ) );
		}
	}

	protected static void onRemoveRange(ChangeHistory changeHistory, DMList ls, int pos, Object[] removedValues)
	{
		if ( changeHistory != null )
		{
			for (Object x: removedValues)
			{
				changeHistory.stopTracking( x );
			}
			changeHistory.addChange( new RemoveRangeCommand( ls, pos, removedValues ) );
		}
	}
}
