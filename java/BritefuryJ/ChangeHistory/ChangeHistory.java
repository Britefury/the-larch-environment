//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ChangeHistory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Pres.UI.SectionHeading2;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.__builtin__;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.InnerFragment;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.Primitive.Arrow;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;

public class ChangeHistory extends AbstractChangeHistory implements Presentable
{
	public interface ChangeAction
	{
		void invoke();
	}
	
	
	
	private static class ActionChange extends Change
	{
		private ChangeAction executeAction, unexecuteAction;
		private String description;
		
		
		public ActionChange(ChangeAction executeAction, ChangeAction unexecuteAction, String description)
		{
			this.executeAction = executeAction;
			this.unexecuteAction = unexecuteAction;
			this.description = description;
		}
		
		
		@Override
		protected void execute()
		{
			executeAction.invoke();
		}

		@Override
		protected void unexecute()
		{
			unexecuteAction.invoke();
		}

		@Override
		protected String getDescription()
		{
			return description;
		}
	}
	
	
	
	private static abstract class Entry implements Presentable
	{
		public abstract void execute();
		public abstract void unexecute();
		public abstract boolean isEmpty();
		public abstract Change top();
	}
	
	private static class SingleEntry extends Entry
	{
		private Change command;
		
		public SingleEntry(Change command)
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
		
		public Change top()
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
		private ArrayList<Change> commands;
		
		public MultiEntry()
		{
			commands = new ArrayList<Change>();
		}
		
		public void execute()
		{
			for (Change c: commands)
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

		public Change top()
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
		
		
		public void add(Change command)
		{
			commands.add( command );
		}

		
		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return new ObjectBox( "Command group", new Column( Pres.mapCoerce( commands ) ) );
		}
	}
	
	
	
	private static class PyTrackable implements Trackable
	{
		private static final PyObject __change_history__ = __builtin__.intern( Py.newString( "__change_history__" ) );
		private static final PyObject __get_trackable_contents__ = __builtin__.intern( Py.newString( "__get_trackable_contents__" ) );
		
		
		private PyObject x;
		
		
		private PyTrackable(PyObject x)
		{
			this.x = x;
		}
		
		
		@Override
		public ChangeHistory getChangeHistory()
		{
			return Py.tojava( __builtin__.getattr( x, __change_history__ ), ChangeHistory.class );
		}

		@Override
		public void setChangeHistory(ChangeHistory h)
		{
			__builtin__.setattr( x, __change_history__, Py.java2py( h ) );
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Object> getTrackableContents()
		{
			return Py.tojava( __builtin__.getattr( x, __get_trackable_contents__ ).__call__(), List.class );
		}
		
		
		protected static boolean isTrackable(PyObject x)
		{
			boolean history = __builtin__.hasattr( x, __change_history__ );
			boolean trackable = __builtin__.hasattr( x, __get_trackable_contents__ );
			if ( history && !trackable )
			{
				System.out.println( "WARNING: Potentially trackable Python object " + x + " has __change_history__ but not __get_trackable_contents__" );
			}
			else if ( !history && trackable )
			{
				System.out.println( "WARNING: Potentially trackable Python object " + x + " has __get_trackable_contents__ but not __change_history__" );
			}
			return history && trackable;
		}
	}
	

	
	
	
	private ArrayList<Entry> past, future;
	private boolean changesBlocked, bFrozen;
	private int freezeCount;
	private PresentationStateListenerList presStateListeners = null;
	
	
	
	
	//
	//
	// Constructor
	//
	//
	public ChangeHistory()
	{
		past = new ArrayList<Entry>();
		future = new ArrayList<Entry>();
		changesBlocked = false;
		bFrozen = false;
		freezeCount = 0;
	}


	public ChangeHistory concreteChangeHistory() {
		return this;
	}

	
	public void addChange(Change change)
	{
		if ( !changesBlocked )
		{
			future.clear();
			Change top = topChange();
			// Attempt to merge @command into @top
			if ( top != null  &&  top.canMergeFrom( change ) )
			{
				top.mergeFrom( change );
			}
			else
			{
				if ( bFrozen )
				{
					((MultiEntry)past.get( past.size() - 1 )).add( change );
				}
				else
				{
					past.add( new SingleEntry( change ) );
				}
			}
			
			onModified();
		}
	}
	
	public void addChange(ChangeAction executeAction, ChangeAction unexecuteAction, String description)
	{
		addChange( new ActionChange( executeAction, unexecuteAction, description ) );
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
			
			onModified();
		}
	}
	
	
	public void clear()
	{
		past.clear();
		future.clear();
		
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

	
	public int getNumUndoChanges()
	{
		return past.size();
	}

	public int getNumRedoChanges()
	{
		return future.size();
	}
	
	
	
	public void track(Trackable t)
	{
		ChangeHistory h = t.getChangeHistory();
		if ( h == null )
		{
			t.setChangeHistory( this );
			Iterable<Object> contents = t.getTrackableContents();
			if ( contents != null )
			{
				for (Object x: contents)
				{
					track( x );
				}
			}
		}
		else if ( h != this )
		{
			throw new RuntimeException( "Trackable object is already being tracked by a different command history" );
		}
	}

	public void stopTracking(Trackable t)
	{
		Iterable<Object> contents = t.getTrackableContents();
		if ( contents != null )
		{
			for (Object x: contents)
			{
				stopTracking( x );
			}
		}
		t.setChangeHistory( null );
	}
	
	
	public void track(Object x)
	{
		Trackable t = asTrackable( x );
		if ( t != null )
		{
			track( t );
		}
	}

	public void stopTracking(Object x)
	{
		Trackable t = asTrackable( x );
		if ( t != null )
		{
			stopTracking( t );
		}
	}
	
	
	
	
	protected Change topChange()
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
	
	
	
	
	
	
	private void blockChanges()
	{
		changesBlocked = true;
	}
	
	private void unblockChanges()
	{
		changesBlocked = false;
	}
	
	
	private void executeEntry(Entry entry)
	{
		blockChanges();
		entry.execute();
		unblockChanges();
	}

	private void unexecuteEntry(Entry entry)
	{
		blockChanges();
		entry.unexecute();
		unblockChanges();
	}
	
	
	@Override
	protected void onModified()
	{
		super.onModified();

		presStateListeners = PresentationStateListenerList.onPresentationStateChanged( presStateListeners, this );
	}
	
	
	public static Trackable asTrackable(Object x)
	{
		if ( x instanceof Trackable )
		{
			return (Trackable)x;
		}
		else if ( x instanceof PyObject )
		{
			PyObject pyX = (PyObject)x;
			if ( PyTrackable.isTrackable( pyX ) )
			{
				return new PyTrackable( pyX );
			}
		}

		return null;
	}
	
	public static ChangeHistory getChangeHistoryFor(Object x)
	{
		Trackable t = asTrackable( x );
		if ( t != null )
		{
			return t.getChangeHistory();
		}
		else
		{
			return null;
		}
	}



	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		presStateListeners = PresentationStateListenerList.addListener( presStateListeners, fragment );

		Pres pastTitleTop = pastTitleStyle.applyTo( new Row( new Pres[] { new Arrow( Arrow.Direction.DOWN, 14.0 ).alignVCentre(), new SectionHeading2( "Past" ) } ) );
		Pres pastContents = new Column( Pres.mapCoerce( past ) );
		Pres pastTitleBottom = pastTitleStyle.applyTo( new Row( new Pres[] { new Arrow( Arrow.Direction.UP, 14.0 ).alignVCentre(), new SectionHeading2( "Past" ) } ) );
		Pres pastBox = pastBorder.surround( listBoxStyle.applyTo( new Column( new Pres[] { pastTitleTop, pastContents, pastTitleBottom } ) ) );

		Pres futureTitleTop = futureTitleStyle.applyTo( new Row( new Pres[] { new Arrow( Arrow.Direction.DOWN, 14.0 ).alignVCentre(), new SectionHeading2( "Future" ) } ) );
		Pres futureContents = new Column( Pres.mapCoerce( future ) );
		Pres futureTitleBottom = futureTitleStyle.applyTo( new Row( new Pres[] { new Arrow( Arrow.Direction.UP, 14.0 ).alignVCentre(), new SectionHeading2( "Future" ) } ) );
		Pres futureBox = futureBorder.surround( listBoxStyle.applyTo( new Column( new Pres[] { futureTitleTop, futureContents, futureTitleBottom } ) ) );
		
		Pres mainBox = changeHistoryColumnStyle.applyTo( new Column( new Pres[] { pastBox, futureBox } ) );
		
		return new ObjectBox( "ChangeHistory", mainBox );
	}


	private static final StyleSheet pastTitleStyle = StyleSheet.style( Primitive.shapePainter.as( new FillPainter( new Color( 0.5f, 0.0f, 0.5f ) ) ), Primitive.fontSmallCaps.as( true ) );
	private static final StyleSheet futureTitleStyle = StyleSheet.style( Primitive.shapePainter.as( new FillPainter( new Color( 0.0f, 0.25f, 0.5f ) ) ), Primitive.fontSmallCaps.as( true ) );
	private static final SolidBorder pastBorder = new SolidBorder( 2.0, 3.0, 10.0, 10.0, new Color( 0.5f, 0.0f, 0.5f ), null );
	private static final SolidBorder futureBorder = new SolidBorder( 2.0, 3.0, 10.0, 10.0, new Color( 0.0f, 0.25f, 0.5f ), null );
	private static final StyleSheet listBoxStyle = StyleSheet.style( Primitive.columnSpacing.as( 10.0 ) );
	private static final StyleSheet changeHistoryColumnStyle = StyleSheet.style( Primitive.columnSpacing.as( 20.0 ) );
}
