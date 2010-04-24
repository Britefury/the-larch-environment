//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Util;

import BritefuryJ.Incremental.IncrementalOwner;
import BritefuryJ.Incremental.IncrementalValue;


public class Range implements IncrementalOwner
{
	private IncrementalValue incr = new IncrementalValue( this );
	private double lower, upper;
	private double begin, end;
	private double stepSize;
	
	
	public Range(double lower, double upper, double begin, double end, double stepSize)
	{
		this.lower = lower;
		this.upper = upper;
		this.begin = begin;
		this.end = end;
		this.stepSize = stepSize;
	}
	
	
	public double getLower()
	{
		incr.onLiteralAccess();
		return lower;
	}

	public double getUpper()
	{
		incr.onLiteralAccess();
		return upper;
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
	
	public double getStepSize()
	{
		incr.onLiteralAccess();
		return stepSize;
	}
	
	
	public void setBounds(double lower, double upper)
	{
		this.lower = lower;
		this.upper = upper;
		incr.onChanged();
	}

	public void setValue(double begin, double end)
	{
		this.begin = begin;
		this.end = end;
		incr.onChanged();
	}
	
	public void setStepSize(double stepSize)
	{
		this.stepSize = stepSize;
		incr.onChanged();
	}
	
	public void move(double delta)
	{
		if ( delta < 0.0 )
		{
			delta = Math.max( delta, getLower() - getBegin() );
		}
		else if ( delta > 0.0 )
		{
			delta = Math.min( delta, getUpper() - getEnd() );
		}
		
		setValue( getBegin() + delta, getEnd() + delta );
	}
	
	public void moveBeginTo(double v)
	{
		move( v - begin );
	}
};

