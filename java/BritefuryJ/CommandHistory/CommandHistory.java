//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.CommandHistory;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DefaultPerspective.Pres.ObjectBox;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Arrow;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;

public class CommandHistory implements CommandHistoryController, Presentable
{
	private static abstract class Entry implements Presentable
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

		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return new InnerFragment( command );
		}
	}

	private static class MultiEntry extends Entry
	{
		private ArrayList<Command> commands;
		
		public MultiEntry()
		{
			commands = new ArrayList<Command>();
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
				return commands.get( commands.size() - 1 );
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

		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return new ObjectBox( "Command group", new Column( Pres.mapCoerce( commands ) ) );
		}
	}
	
	
	
	private ArrayList<Entry> past, future;
	private boolean bCommandsBlocked, bFrozen;
	private int freezeCount;
	private CommandHistoryListener listener;
	private PresentationStateListenerList presStateListeners = null;
	
	
	
	
	//
	//
	// Constructor
	//
	//
	public CommandHistory()
	{
		past = new ArrayList<Entry>();
		future = new ArrayList<Entry>();
		bCommandsBlocked = false;
		bFrozen = false;
		freezeCount = 0;
		listener = null;
	}
	
	
	
	public void setCommandHistoryListener(CommandHistoryListener listener)
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
					((MultiEntry)past.get( past.size() - 1 )).add( command );
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
			
			onModified();
		}
	}
	
	
	
	public void undo()
	{
		if ( bFrozen )
		{
			thaw();
		}
		
		if ( past.size() >= 1 )
		{
			Entry entry = past.get( past.size() - 1 );
			past.remove( past.size() - 1 );
			unexecuteEntry( entry );
			future.add( entry );
			
			if ( listener != null )
			{
				listener.onCommandHistoryChanged( this );
			}

			onModified();
		}
	}
	
	
	public void redo()
	{
		if ( bFrozen )
		{
			thaw();
		}
		
		if ( future.size() >= 1 )
		{
			Entry entry = future.get( future.size() - 1 );
			future.remove( future.size() - 1 );
			executeEntry( entry );
			past.add( entry );
			
			if ( listener != null )
			{
				listener.onCommandHistoryChanged( this );
			}

			onModified();
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
		
		onModified();
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
				past.remove( past.size() - 1 );
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
		CommandHistory h = t.getCommandHistory();
		if ( h == null )
		{
			t.setCommandHistory( this );
			t.trackContents( this );
		}
		else if ( h != this )
		{
			throw new RuntimeException( "Trackable object is already being tracked by a different command history" );
		}
	}

	public void stopTracking(Trackable t)
	{
		t.stopTrackingContents( this );
		t.setCommandHistory( null );
	}
	
	
	public void track(Object x)
	{
		if ( x != null  &&  x instanceof Trackable )
		{
			track( (Trackable)x );
		}
	}

	public void stopTracking(Object x)
	{
		if ( x != null  &&  x instanceof Trackable )
		{
			stopTracking( (Trackable)x );
		}
	}
	
	
	
	
	protected Command topCommand()
	{
		if ( past.size() > 0 )
		{
			return past.get( past.size() - 1 ).top();
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
			return past.get( past.size() - 1 );
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
	
	
	private void onModified()
	{
		presStateListeners = PresentationStateListenerList.onPresentationStateChanged( presStateListeners, this );
	}



	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		presStateListeners = PresentationStateListenerList.addListener( presStateListeners, fragment );

		Pres pastTitleTop = pastTitleStyle.applyTo( new Row( new Pres[] { new Arrow( Arrow.Direction.DOWN, 14.0 ).alignVCentre(), new Label( "Past" ) } ) );
		Pres pastContents = new Column( Pres.mapCoerce( past ) );
		Pres pastTitleBottom = pastTitleStyle.applyTo( new Row( new Pres[] { new Arrow( Arrow.Direction.UP, 14.0 ).alignVCentre(), new Label( "Past" ) } ) );
		Pres pastBox = pastBorderStyle.applyTo( new Border( listBoxStyle.applyTo( new Column( new Pres[] { pastTitleTop, pastContents, pastTitleBottom } ) ) ) );

		Pres futureTitleTop = futureTitleStyle.applyTo( new Row( new Pres[] { new Arrow( Arrow.Direction.DOWN, 14.0 ).alignVCentre(), new Label( "Future" ) } ) );
		Pres futureContents = new Column( Pres.mapCoerce( future ) );
		Pres futureTitleBottom = futureTitleStyle.applyTo( new Row( new Pres[] { new Arrow( Arrow.Direction.UP, 14.0 ).alignVCentre(), new Label( "Future" ) } ) );
		Pres futureBox = futureBorderStyle.applyTo( new Border( listBoxStyle.applyTo( new Column( new Pres[] { futureTitleTop, futureContents, futureTitleBottom } ) ) ) );
		
		Pres mainBox = commandHistoryColumnStyle.applyTo( new Column( new Pres[] { pastBox, futureBox } ) );
		
		return new ObjectBox( "CommandHistory", mainBox );
	}
	
	
	private static final StyleSheet pastTitleStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.5f ) )
			.withAttr( Primitive.shapePainter, new FillPainter( new Color( 0.5f, 0.0f, 0.5f ) ) )
			.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.fontSmallCaps, true );
	private static final StyleSheet futureTitleStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.0f, 0.25f, 0.5f ) )
			.withAttr( Primitive.shapePainter, new FillPainter( new Color( 0.0f, 0.25f, 0.5f ) ) )
			.withAttr( Primitive.fontFace, "Serif" ).withAttr( Primitive.fontSmallCaps, true );
	private static final StyleSheet pastBorderStyle = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 3.0, 10.0, 10.0, new Color( 0.5f, 0.0f, 0.5f ), null ) );
	private static final StyleSheet futureBorderStyle = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 3.0, 10.0, 10.0, new Color( 0.0f, 0.25f, 0.5f ), null ) );
	private static final StyleSheet listBoxStyle = StyleSheet.instance.withAttr( Primitive.columnSpacing, 10.0 );
	private static final StyleSheet commandHistoryColumnStyle = StyleSheet.instance.withAttr( Primitive.columnSpacing, 20.0 );
}
