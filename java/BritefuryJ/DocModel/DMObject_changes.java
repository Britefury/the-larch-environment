//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.DocModel;

import BritefuryJ.ChangeHistory.Change;
import BritefuryJ.ChangeHistory.ChangeHistory;

public class DMObject_changes
{
	private static class SetCommand extends Change
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

	
	
	private static class UpdateCommand extends Change
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

	
	
	private static class BecomeCommand extends Change
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

	
	
	
	
	protected static void onSet(ChangeHistory changeHistory, DMObject obj, int i, Object oldX, Object x)
	{
		if ( changeHistory != null )
		{
			changeHistory.stopTracking( oldX );
			changeHistory.addChange( new SetCommand( obj, i, oldX, x ) );
			changeHistory.track( x );
		}
	}

	protected static void onUpdate(ChangeHistory changeHistory, DMObject obj, int[] indices, Object[] oldContents, Object[] newContents)
	{
		if ( changeHistory != null )
		{
			for (Object oldX: oldContents)
			{
				changeHistory.stopTracking( oldX );
			}
			changeHistory.addChange( new UpdateCommand( obj, indices, oldContents, newContents ) );
			for (Object x: newContents)
			{
				changeHistory.track( x );
			}
		}
	}
	
	protected static void onBecome(ChangeHistory changeHistory, DMObject obj, DMObjectClass oldClass, Object oldFieldData[], DMObjectClass newClass, Object newFieldData[])
	{
		if ( changeHistory != null )
		{
			for (Object oldX: oldFieldData)
			{
				changeHistory.stopTracking( oldX );
			}
			changeHistory.addChange( new BecomeCommand( obj, oldClass, oldFieldData, newClass, newFieldData ) );
			for (Object x: newFieldData)
			{
				changeHistory.track( x );
			}
		}
	}
}
