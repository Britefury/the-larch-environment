//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Live;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.ChangeHistory.Change;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;

public class TrackedLiveValue extends LiveValue implements Trackable
{
	private static class SetValueCommand extends Change
	{
		private TrackedLiveValue live;
		private Object oldValue, newValue;
		
		public SetValueCommand(TrackedLiveValue live, Object oldValue, Object newValue)
		{
			this.live = live;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		
		protected void execute()
		{
			live.setLiteralValue( newValue );
		}

		protected void unexecute()
		{
			live.setLiteralValue( oldValue );
		}

		protected String getDescription()
		{
			return "TrackedLiveValue set value";
		}
	}


	
	private ChangeHistory changeHistory;
	
	
	public TrackedLiveValue()
	{
		super();
	}
	
	public TrackedLiveValue(Object value)
	{
		super( value );
	}
	
	
	
	public void setLiteralValue(Object value)
	{
		Object oldValue = getStaticValue();

		super.setLiteralValue( value );
		
		if ( changeHistory != null )
		{
			if ( oldValue != null )
			{
				changeHistory.stopTracking( oldValue );
			}
			changeHistory.addChange( new SetValueCommand( this, oldValue, value ) );
			if ( value != null )
			{
				changeHistory.track( value );
			}
		}
	}


	
	//
	// Trackable interface
	//

	public void setChangeHistory(ChangeHistory h)
	{
		changeHistory = h;
	}
	
	public ChangeHistory getChangeHistory()
	{
		return changeHistory;
	}
	
	
	public List<Object> getTrackableContents()
	{
		Object val = getStaticValue();
		
		if ( val != null )
		{
			return Arrays.asList( new Object[] { val } );
		}
		else
		{
			return Arrays.asList( new Object[] {} );
		}
	}
}
