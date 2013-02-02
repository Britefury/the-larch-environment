//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.ChangeHistory;

import java.util.List;

import junit.framework.TestCase;
import BritefuryJ.ChangeHistory.Change;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;

public class Test_ChangeHistory extends TestCase
{
	private static class DataXCommand extends Change
	{
		private Data d;
		private int oldX, x;
		
		public DataXCommand(Data d, int oldX, int x)
		{
			this.d = d;
			this.oldX = oldX;
			this.x = x;
		}

		protected void execute()
		{
			d.setX( x );
		}

		protected void unexecute()
		{
			d.setX( oldX );
		}

		protected String getDescription()
		{
			return "Test: DataX";
		}
	}
	

	private static class DataYCommand extends Change
	{
		private Data d;
		private int oldY, y;
		
		public DataYCommand(Data d, int oldY, int y)
		{
			this.d = d;
			this.oldY = oldY;
			this.y = y;
		}

		protected void execute()
		{
			d.setY( y );
		}

		protected void unexecute()
		{
			d.setY( oldY );
		}

		protected String getDescription()
		{
			return "Test: DataY";
		}
	
		protected boolean canMergeFrom(Change change)
		{
			return change instanceof DataYCommand;
		}
		
		protected void mergeFrom(Change change)
		{
			if ( change instanceof DataYCommand )
			{
				y = ((DataYCommand)change).y;
			}
			else
			{
				throw new CannotJoinCommandException();
			}
		}
	}
	
	private static class DataCommandTracker
	{
		protected static void onX(ChangeHistory changeHistory, Data d, int oldX, int x)
		{
			if ( changeHistory != null )
			{
				changeHistory.addChange( new DataXCommand( d, oldX, x ) );
			}
		}

		protected static void onY(ChangeHistory changeHistory, Data d, int oldY, int y)
		{
			if ( changeHistory != null )
			{
				changeHistory.addChange( new DataYCommand( d, oldY, y ) );
			}
		}
	}
	
	private static class Data implements Trackable
	{
		private int x, y;
		ChangeHistory changeHistory;
		
		
		public Data(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		
		
		public int getX()
		{
			return x;
		}
		
		public void setX(int x)
		{
			int oldX = this.x;
			this.x = x;
			
			DataCommandTracker.onX( changeHistory, this, oldX, x );
		}


		public int getY()
		{
			return y;
		}
		
		public void setY(int y)
		{
			int oldY = this.y;
			this.y = y;
			
			DataCommandTracker.onY( changeHistory, this, oldY, y );
		}

		
		@Override
		public ChangeHistory getChangeHistory()
		{
			return changeHistory;
		}

		@Override
		public void setChangeHistory(ChangeHistory h)
		{
			changeHistory = h;
		}

		
		@Override
		public List<Object> getTrackableContents()
		{
			return null;
		}
	}
	
	
	
	
	
	
	public void testUndoRedoClear()
	{
		ChangeHistory h = new ChangeHistory();
		Data d = new Data( 0, -1 );
		
		h.track( d );
		
		assertEquals( d.getX(), 0 );
		assertEquals( h.getNumUndoChanges(), 0 );
		assertEquals( h.getNumRedoChanges(), 0 );
		
		d.setX( 1 );
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setX( 2 );
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setX( 3 );
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoChanges(), 3 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setX( 4 );
		assertEquals( d.getX(), 4 );
		assertEquals( h.getNumUndoChanges(), 4 );
		assertEquals( h.getNumRedoChanges(), 0 );
		
		h.undo();
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoChanges(), 3 );
		assertEquals( h.getNumRedoChanges(), 1 );

		h.undo();
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 2 );

		h.redo();
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoChanges(), 3 );
		assertEquals( h.getNumRedoChanges(), 1 );

		d.setX( 5 );
		assertEquals( d.getX(), 5 );
		assertEquals( h.getNumUndoChanges(), 4 );
		assertEquals( h.getNumRedoChanges(), 0 );
		
		h.undo();
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoChanges(), 3 );
		assertEquals( h.getNumRedoChanges(), 1 );

		h.undo();
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 2 );
		
		h.clear();
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoChanges(), 0 );
		assertEquals( h.getNumRedoChanges(), 0 );
	}




	public void testCommandJoining()
	{
		ChangeHistory h = new ChangeHistory();
		Data d = new Data( -1, 0 );
		
		h.track( d );
		
		assertEquals( d.getY(), 0 );
		assertEquals( h.getNumUndoChanges(), 0 );
		assertEquals( h.getNumRedoChanges(), 0 );
		
		d.setY( 1 );
		assertEquals( d.getY(), 1 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setY( 2 );
		assertEquals( d.getY(), 2 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setY( 3 );
		assertEquals( d.getY(), 3 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setY( 4 );
		assertEquals( d.getY(), 4 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 0 );
		
		h.undo();
		assertEquals( d.getY(), 0 );
		assertEquals( h.getNumUndoChanges(), 0 );
		assertEquals( h.getNumRedoChanges(), 1 );

		h.redo();
		assertEquals( d.getY(), 4 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setY( 5 );
		assertEquals( d.getY(), 5 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 0 );
		
		h.undo();
		assertEquals( d.getY(), 0 );
		assertEquals( h.getNumUndoChanges(), 0 );
		assertEquals( h.getNumRedoChanges(), 1 );
	}





	public void testFreezeThaw()
	{
		ChangeHistory h = new ChangeHistory();
		Data d = new Data( 0, -1 );
		
		h.track( d );
		
		assertEquals( d.getX(), 0 );
		assertEquals( h.getNumUndoChanges(), 0 );
		assertEquals( h.getNumRedoChanges(), 0 );
		
		d.setX( 1 );
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 0 );

		h.freeze();
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 0 );
		d.setX( 2 );
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setX( 3 );
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 0 );

		d.setX( 4 );
		assertEquals( d.getX(), 4 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 0 );
		h.thaw();
		assertEquals( d.getX(), 4 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 0 );
		
		h.undo();
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 1 );

		h.undo();
		assertEquals( d.getX(), 0 );
		assertEquals( h.getNumUndoChanges(), 0 );
		assertEquals( h.getNumRedoChanges(), 2 );

		h.redo();
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoChanges(), 1 );
		assertEquals( h.getNumRedoChanges(), 1 );

		h.redo();
		assertEquals( d.getX(), 4 );
		assertEquals( h.getNumUndoChanges(), 2 );
		assertEquals( h.getNumRedoChanges(), 0 );
	}
}
