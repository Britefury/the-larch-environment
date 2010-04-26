//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Util;

import java.util.ArrayList;

import BritefuryJ.Incremental.IncrementalOwner;
import BritefuryJ.Incremental.IncrementalValue;


public class Range implements IncrementalOwner
{
	public interface RangeListener
	{
		void onRangeModified(Range r);
	}
	
	
	private IncrementalValue incr = new IncrementalValue( this );
	private double min, max;
	private double begin, end;
	private double stepSize;
	private ArrayList<RangeListener> listeners;
	
	
	public Range(double min, double max, double begin, double end, double stepSize)
	{
		this.min = min;
		this.max = max;
		this.begin = begin;
		this.end = end;
		this.stepSize = stepSize;
	}
	
	
	public void addListener(RangeListener listener)
	{
		if ( listeners == null )
		{
			listeners = new ArrayList<RangeListener>();
		}
		listeners.add( listener );
	}
	
	public void removeListener(RangeListener listener)
	{
		if ( listeners != null )
		{
			listeners.add( listener );
			
			if ( listeners.isEmpty() )
			{
				listeners = null;
			}
		}
	}
	
	
	public double getMin()
	{
		incr.onLiteralAccess();
		return min;
	}

	public double getMax()
	{
		incr.onLiteralAccess();
		return max;
	}

	public double getBegin()
	{
		incr.onLiteralAccess();
		return begin;
	}

	public double getEnd()
	{
		incr.onLiteralAccess();
		return end;
	}
	
	public double getPageSize()
	{
		incr.onLiteralAccess();
		return end - begin;
	}
	
	public double getStepSize()
	{
		incr.onLiteralAccess();
		return stepSize;
	}
	
	
	public void setBounds(double min, double max)
	{
		this.min = min;
		this.max = max;
		onModified();
	}

	public void setValue(double begin, double end)
	{
		this.begin = begin;
		this.end = end;
		onModified();
	}
	
	public void setStepSize(double stepSize)
	{
		this.stepSize = stepSize;
		onModified();
	}
	
	public void move(double delta)
	{
		incr.onLiteralAccess();
		if ( delta < 0.0 )
		{
			delta = Math.max( delta, min - begin );
		}
		else if ( delta > 0.0 )
		{
			delta = Math.min( delta, max - end );
		}
		
		begin += delta;
		end += delta;
		begin = Math.min( Math.max( begin, min ), max );
		end = Math.min( Math.max( end, min ), max );
		onModified();
	}
	
	public void moveBeginTo(double v)
	{
		move( v - begin );
	}
	
	
	
	private void onModified()
	{
		incr.onChanged();
		if ( listeners != null )
		{
			for (RangeListener listener: listeners)
			{
				listener.onRangeModified( this );
			}
		}
	}
	
	
	public String toString()
	{
		return "Range( min=" + min + ", max=" + max + ", begin=" + begin + ", end=" + end + ", stepSize=" + stepSize + " )";
	}
};

