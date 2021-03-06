//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Live;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.ChangeHistory.Change;
import BritefuryJ.ChangeHistory.ChangeHistory;
import BritefuryJ.ChangeHistory.Trackable;

public class TrackedLiveValue extends LiveValue implements Trackable
{
	public static interface ChangeListener
	{
		public void onChanged(Object oldValue, Object newValue);
	}


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
	private ChangeListener changeListener;
	
	
	public TrackedLiveValue()
	{
		super();
		changeListener = null;
	}
	
	public TrackedLiveValue(Object value)
	{
		super( value );
		changeListener = null;
	}


	public ChangeListener getChangeListener()
	{
		return changeListener;
	}

	public void setChangeListener(ChangeListener listener)
	{
		changeListener = listener;
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

		if ( changeListener != null )
		{
			changeListener.onChanged( oldValue, value );
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
