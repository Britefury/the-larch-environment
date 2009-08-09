//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.CommandHistory;

import junit.framework.TestCase;
import BritefuryJ.CommandHistory.Command;
import BritefuryJ.CommandHistory.CommandHistory;
import BritefuryJ.CommandHistory.CommandTracker;
import BritefuryJ.CommandHistory.CommandTrackerFactory;
import BritefuryJ.CommandHistory.Trackable;

public class Test_CommandHistory extends TestCase
{
	private static class DataTrackerFactory implements CommandTrackerFactory
	{
		public static DataTrackerFactory factory = new DataTrackerFactory();
		
		public CommandTracker createTracker(CommandHistory history)
		{
			return new DataCommandTracker( history );
		}
	}
	
	private static class DataXCommand extends Command
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
	}
	

	private static class DataYCommand extends Command
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
		
		protected boolean canMergeFrom(Command command)
		{
			return command instanceof DataYCommand;
		}
		
		protected void mergeFrom(Command command)
		{
			if ( command instanceof DataYCommand )
			{
				y = ((DataYCommand)command).y;
			}
			else
			{
				throw new CannotJoinCommandException();
			}
		}
	}
	
	private static class DataCommandTracker extends CommandTracker
	{
		public DataCommandTracker(CommandHistory commandHistory)
		{
			super( commandHistory );
		}
		
		
		protected void onX(Data d, int oldX, int x)
		{
			commandHistory.addCommand( new DataXCommand( d, oldX, x ) );
		}

		protected void onY(Data d, int oldY, int y)
		{
			commandHistory.addCommand( new DataYCommand( d, oldY, y ) );
		}
	}
	
	private static class Data implements Trackable
	{
		private int x, y;
		DataCommandTracker tracker;
		
		
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
			
			if ( tracker != null )
			{
				tracker.onX( this, oldX, x );
			}
		}


		public int getY()
		{
			return y;
		}
		
		public void setY(int y)
		{
			int oldY = this.y;
			this.y = y;
			
			if ( tracker != null )
			{
				tracker.onY( this, oldY, y );
			}
		}


		public CommandTrackerFactory getTrackerFactory()
		{
			return DataTrackerFactory.factory;
		}

		public void setTracker(CommandTracker tracker)
		{
			this.tracker = (DataCommandTracker)tracker;
		}
	}
	
	
	
	
	
	
	public void testUndoRedoClear()
	{
		CommandHistory h = new CommandHistory();
		Data d = new Data( 0, -1 );
		
		h.track( d );
		
		assertEquals( d.getX(), 0 );
		assertEquals( h.getNumUndoCommands(), 0 );
		assertEquals( h.getNumRedoCommands(), 0 );
		
		d.setX( 1 );
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setX( 2 );
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setX( 3 );
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoCommands(), 3 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setX( 4 );
		assertEquals( d.getX(), 4 );
		assertEquals( h.getNumUndoCommands(), 4 );
		assertEquals( h.getNumRedoCommands(), 0 );
		
		h.undo();
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoCommands(), 3 );
		assertEquals( h.getNumRedoCommands(), 1 );

		h.undo();
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 2 );

		h.redo();
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoCommands(), 3 );
		assertEquals( h.getNumRedoCommands(), 1 );

		d.setX( 5 );
		assertEquals( d.getX(), 5 );
		assertEquals( h.getNumUndoCommands(), 4 );
		assertEquals( h.getNumRedoCommands(), 0 );
		
		h.undo();
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoCommands(), 3 );
		assertEquals( h.getNumRedoCommands(), 1 );

		h.undo();
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 2 );
		
		h.clear();
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoCommands(), 0 );
		assertEquals( h.getNumRedoCommands(), 0 );
	}




	public void testCommandJoining()
	{
		CommandHistory h = new CommandHistory();
		Data d = new Data( -1, 0 );
		
		h.track( d );
		
		assertEquals( d.getY(), 0 );
		assertEquals( h.getNumUndoCommands(), 0 );
		assertEquals( h.getNumRedoCommands(), 0 );
		
		d.setY( 1 );
		assertEquals( d.getY(), 1 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setY( 2 );
		assertEquals( d.getY(), 2 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setY( 3 );
		assertEquals( d.getY(), 3 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setY( 4 );
		assertEquals( d.getY(), 4 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 0 );
		
		h.undo();
		assertEquals( d.getY(), 0 );
		assertEquals( h.getNumUndoCommands(), 0 );
		assertEquals( h.getNumRedoCommands(), 1 );

		h.redo();
		assertEquals( d.getY(), 4 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setY( 5 );
		assertEquals( d.getY(), 5 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 0 );
		
		h.undo();
		assertEquals( d.getY(), 0 );
		assertEquals( h.getNumUndoCommands(), 0 );
		assertEquals( h.getNumRedoCommands(), 1 );
	}





	public void testFreezeThaw()
	{
		CommandHistory h = new CommandHistory();
		Data d = new Data( 0, -1 );
		
		h.track( d );
		
		assertEquals( d.getX(), 0 );
		assertEquals( h.getNumUndoCommands(), 0 );
		assertEquals( h.getNumRedoCommands(), 0 );
		
		d.setX( 1 );
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 0 );

		h.freeze();
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 0 );
		d.setX( 2 );
		assertEquals( d.getX(), 2 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setX( 3 );
		assertEquals( d.getX(), 3 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 0 );

		d.setX( 4 );
		assertEquals( d.getX(), 4 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 0 );
		h.thaw();
		assertEquals( d.getX(), 4 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 0 );
		
		h.undo();
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 1 );

		h.undo();
		assertEquals( d.getX(), 0 );
		assertEquals( h.getNumUndoCommands(), 0 );
		assertEquals( h.getNumRedoCommands(), 2 );

		h.redo();
		assertEquals( d.getX(), 1 );
		assertEquals( h.getNumUndoCommands(), 1 );
		assertEquals( h.getNumRedoCommands(), 1 );

		h.redo();
		assertEquals( d.getX(), 4 );
		assertEquals( h.getNumUndoCommands(), 2 );
		assertEquals( h.getNumRedoCommands(), 0 );
	}
}
