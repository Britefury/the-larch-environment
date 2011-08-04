//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Dispatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.core.Py;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Editor.Table.ObjectList.AttributeColumn;
import BritefuryJ.Editor.Table.ObjectList.ObjectListTableEditor;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;

public class DispatchViewFragmentFunction
{
	public static class ProfileTiming
	{
		private int numInvocations = 0;
		private double time = 0.0;
		
		
		public int getNumInvocations()
		{
			return numInvocations;
		}
		
		public double getTime()
		{
			return time;
		}
		
		public double getTimePerInvocation()
		{
			return time / numInvocations;
		}
	}
	
	
	
	public static class ProfileResultEntry
	{
		private String name;
		private int numInvocations;
		private double time, timePerInvocation;
		
		
		public ProfileResultEntry()
		{
			name = "";
			numInvocations = 0;
			time = 0.0;
			timePerInvocation = 0.0;
		}
		
		public ProfileResultEntry(String name, ProfileTiming p)
		{
			this.name = name;
			numInvocations = p.numInvocations;
			time = p.time;
			timePerInvocation = p.getTimePerInvocation();
		}


		public String getName()
		{
			return name;
		}
		
		public int getNumInvocations()
		{
			return numInvocations;
		}
		
		public double getTime()
		{
			return time;
		}
		
		public double getTimePerInvocation()
		{
			return timePerInvocation;
		}
	}
	
	
	public static class ProfileResults implements Presentable
	{
		private static Comparator<ProfileResultEntry> nameCmp = new Comparator<ProfileResultEntry>()
		{
			@Override
			public int compare(ProfileResultEntry a, ProfileResultEntry b)
			{
				return a.name.compareTo( b.name );
			}
		};
		
		private static Comparator<ProfileResultEntry> numInvocationsCmp = new Comparator<ProfileResultEntry>()
		{
			@Override
			public int compare(ProfileResultEntry a, ProfileResultEntry b)
			{
				return -Integer.valueOf( a.numInvocations ).compareTo( Integer.valueOf( b.numInvocations ) );
			}
		};
		
		private static Comparator<ProfileResultEntry> timeCmp = new Comparator<ProfileResultEntry>()
		{
			@Override
			public int compare(ProfileResultEntry a, ProfileResultEntry b)
			{
				return -Double.valueOf( a.time ).compareTo( Double.valueOf( b.time ) );
			}
		};
		
		private static Comparator<ProfileResultEntry> timePerInvokeCmp = new Comparator<ProfileResultEntry>()
		{
			@Override
			public int compare(ProfileResultEntry a, ProfileResultEntry b)
			{
				return -Double.valueOf( a.timePerInvocation ).compareTo( Double.valueOf( b.timePerInvocation ) );
			}
		};
		
		
		private ArrayList<ProfileResultEntry> results = new ArrayList<ProfileResultEntry>();
		
		
		
		private ProfileResults(HashMap<String, ProfileTiming> timings)
		{
			for (Map.Entry<String, ProfileTiming> e: timings.entrySet())
			{
				results.add( new ProfileResultEntry( e.getKey(), e.getValue() ) );
			}
			sortByTime();
		}
		
		
		public void sortByName()
		{
			Collections.sort( results, nameCmp );
		}
		
		public void sortByNumInvocations()
		{
			Collections.sort( results, numInvocationsCmp );
		}
		
		public void sortByTime()
		{
			Collections.sort( results, timeCmp );
		}
		
		public void sortByTimerPerInvocation()
		{
			Collections.sort( results, timePerInvokeCmp );
		}
		
		
		public List<ProfileResultEntry> getResults()
		{
			return results;
		}


		@Override
		public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			return tableEditor.editTable( results );
		}
		
		
		
		private static AttributeColumn nameColumn = new AttributeColumn( "Name", Py.newString( "name" ) );
		private static AttributeColumn numInvokeColumn = new AttributeColumn( "Num Invocations", Py.newString( "numInvocations" ) );
		private static AttributeColumn timeColumn = new AttributeColumn( "Time", Py.newString( "time" ) );
		private static AttributeColumn timePerInvokeColumn = new AttributeColumn( "Time per invocation", Py.newString( "timePerInvocation" ) );
		
		private static ObjectListTableEditor tableEditor = new ObjectListTableEditor( Arrays.asList( new Object[] { nameColumn, numInvokeColumn, timeColumn, timePerInvokeColumn } ),
				ProfileResultEntry.class, false, true, false, false );
	}
	
	
	private HashMap<String, ProfileTiming> timings = null, resultTimings = null;

	public void startProfiling()
	{
		timings = new HashMap<String, ProfileTiming>();
	}
	
	public void stopProfiling()
	{
		resultTimings = timings;
		timings = null;
	}
	
	public void resetProfile()
	{
		stopProfiling();
		startProfiling();
	}
	
	public HashMap<String, ProfileTiming> getProfileTimings()
	{
		if ( timings != null )
		{
			return timings;
		}
		else
		{
			return resultTimings;
		}
	}
	
	public ProfileResults getProfileResults()
	{
		HashMap<String, ProfileTiming> t = getProfileTimings();
		if ( t != null )
		{
			return new ProfileResults( t );
		}
		else
		{
			return null;
		}
	}
	

	protected long profile_start()
	{
		if ( timings != null )
		{
			return System.nanoTime();
		}
		else
		{
			return 0;
		}
	}
	
	protected void profile_stop(String name, long startTime)
	{
		if ( timings != null )
		{
			long t2 = System.nanoTime();
			ProfileTiming p = timings.get( name );
			if ( p == null )
			{
				p = new ProfileTiming();
				timings.put( name, p );
			}
			p.numInvocations++;
			p.time += (double)( t2 - startTime ) * 1.0e-9;
		}
	}
}
