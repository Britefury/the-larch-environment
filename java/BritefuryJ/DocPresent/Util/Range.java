//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Util;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.GenericPerspectiveStyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Incremental.IncrementalOwner;
import BritefuryJ.Incremental.IncrementalValueMonitor;


public class Range implements IncrementalOwner, Presentable
{
	public interface RangeListener
	{
		void onRangeModified(Range r);
	}
	
	
	private IncrementalValueMonitor incr = new IncrementalValueMonitor( this );
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
		incr.onAccess();
		return min;
	}

	public double getMax()
	{
		incr.onAccess();
		return max;
	}

	public double getBegin()
	{
		incr.onAccess();
		return begin;
	}

	public double getEnd()
	{
		incr.onAccess();
		return end;
	}
	
	public double getPageSize()
	{
		incr.onAccess();
		return end - begin;
	}
	
	public double getStepSize()
	{
		incr.onAccess();
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
		incr.onAccess();
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


	@Override
	public DPElement present(GSymFragmentView fragment, GenericPerspectiveStyleSheet styleSheet, AttributeTable inheritedState)
	{
		incr.onAccess();
		DPElement rangeField = styleSheet.horizontalObjectField( "Valid range:",
				defaultStyle.paragraph( new DPElement[] { numValueStyle.staticText( String.valueOf( min ) ), defaultStyle.staticText( " to " ), numValueStyle.staticText( String.valueOf( max ) ) } ) );
		DPElement valueField = styleSheet.horizontalObjectField( "Value range:",
				defaultStyle.paragraph( new DPElement[] { numValueStyle.staticText( String.valueOf( begin ) ), defaultStyle.staticText( " to " ), numValueStyle.staticText( String.valueOf( end ) ) } ) );
		DPElement stepSizeField = styleSheet.horizontalObjectField( "Step size:", numValueStyle.staticText( String.valueOf( stepSize ) ) );
		return styleSheet.objectBoxWithFields( getClass().getName(), new DPElement[] { rangeField, valueField, stepSizeField } );
	}
	
	
	private final static PrimitiveStyleSheet defaultStyle = PrimitiveStyleSheet.instance;
	private final static PrimitiveStyleSheet numValueStyle = PrimitiveStyleSheet.instance.withForeground( new Color( 0.5f, 0.0f, 0.25f ) );
};

