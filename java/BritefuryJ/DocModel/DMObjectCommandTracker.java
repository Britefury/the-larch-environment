//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import BritefuryJ.CommandHistory.Command;
import BritefuryJ.CommandHistory.CommandHistory;

public class DMObjectCommandTracker
{
	private static class SetCommand extends Command
	{
		private DMObject obj;
		private int i;
		private Object oldX, x;
		
		public SetCommand(DMObject obj, int i, Object oldX, Object x)
		{
			this.obj = obj;
			this.i = i;
			this.oldX = oldX;
			this.x = x;
		}

		
		protected void execute()
		{
			obj.set( i, x );
		}

		protected void unexecute()
		{
			obj.set( i, oldX );
		}

		protected String getDescription()
		{
			return "Object set (" + i + ")";
		}
	}

	
	
	private static class UpdateCommand extends Command
	{
		private DMObject obj;
		private int[] indices;
		private Object[] oldContents;
		private Object[] newContents;
		
		public UpdateCommand(DMObject obj, int[] indices, Object[] oldContents, Object[] newContents)
		{
			this.obj = obj;
			this.indices = indices;
			this.oldContents = oldContents;
			this.newContents = newContents;
		}

		
		protected void execute()
		{
			obj.update( indices, newContents );
		}

		protected void unexecute()
		{
			obj.update( indices, oldContents );
		}

		protected String getDescription()
		{
			return "Object update";
		}
	}

	
	
	private static class BecomeCommand extends Command
	{
		private DMObject obj;
		private DMObjectClass oldClass;
		private Object oldFieldData[];
		private DMObjectClass newClass;
		private Object newFieldData[];
		
		public BecomeCommand(DMObject obj, DMObjectClass oldClass, Object oldFieldData[], DMObjectClass newClass, Object newFieldData[])
		{
			this.obj = obj;
			this.oldClass = oldClass;
			this.oldFieldData = oldFieldData;
			this.newClass = newClass;
			this.newFieldData = newFieldData;
		}

		
		protected void execute()
		{
			obj.become( newClass, newFieldData );
		}

		protected void unexecute()
		{
			obj.become( oldClass, oldFieldData );
		}

		protected String getDescription()
		{
			return "Object become";
		}
	}

	
	
	
	
	protected static void onSet(CommandHistory commandHistory, DMObject obj, int i, Object oldX, Object x)
	{
		if ( commandHistory != null )
		{
			commandHistory.stopTracking( oldX );
			commandHistory.addCommand( new SetCommand( obj, i, oldX, x ) );
			commandHistory.track( x );
		}
	}

	protected static void onUpdate(CommandHistory commandHistory, DMObject obj, int[] indices, Object[] oldContents, Object[] newContents)
	{
		if ( commandHistory != null )
		{
			for (Object oldX: oldContents)
			{
				commandHistory.stopTracking( oldX );
			}
			commandHistory.addCommand( new UpdateCommand( obj, indices, oldContents, newContents ) );
			for (Object x: newContents)
			{
				commandHistory.track( x );
			}
		}
	}
	
	protected static void onBecome(CommandHistory commandHistory, DMObject obj, DMObjectClass oldClass, Object oldFieldData[], DMObjectClass newClass, Object newFieldData[])
	{
		if ( commandHistory != null )
		{
			for (Object oldX: oldFieldData)
			{
				commandHistory.stopTracking( oldX );
			}
			commandHistory.addCommand( new BecomeCommand( obj, oldClass, oldFieldData, newClass, newFieldData ) );
			for (Object x: newFieldData)
			{
				commandHistory.track( x );
			}
		}
	}
}
