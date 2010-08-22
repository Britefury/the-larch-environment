//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Util;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Paragraph;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.GenericPerspective.Presentable;
import BritefuryJ.GSym.GenericPerspective.PresCom.HorizontalField;
import BritefuryJ.GSym.GenericPerspective.PresCom.ObjectBoxWithFields;
import BritefuryJ.GSym.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.GSym.View.GSymFragmentView;
import BritefuryJ.Incremental.IncrementalOwner;


public class Range implements IncrementalOwner, Presentable
{
	public interface RangeListener
	{
		void onRangeModified(Range r);
	}
	
	
	private PresentationStateListenerList presStateListeners = null;
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
		return min;
	}

	public double getMax()
	{
		return max;
	}

	public double getBegin()
	{
		return begin;
	}

	public double getEnd()
	{
		return end;
	}
	
	public double getPageSize()
	{
		return end - begin;
	}
	
	public double getStepSize()
	{
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
		presStateListeners = PresentationStateListenerList.onPresentationStateChanged( presStateListeners, this );
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
	public Pres present(GSymFragmentView fragment, SimpleAttributeTable inheritedState)
	{
		presStateListeners = PresentationStateListenerList.addListener( presStateListeners, fragment );
		Pres rangeField = new HorizontalField( "Valid range:",
				new Paragraph( new Pres[] { numValueStyle.applyTo( new StaticText( String.valueOf( min ) ) ),
						new StaticText( " to " ),
						numValueStyle.applyTo( new StaticText( String.valueOf( max ) ) ) } ) );
		Pres valueField = new HorizontalField( "Value range:",
				new Paragraph( new Pres[] { numValueStyle.applyTo( new StaticText( String.valueOf( begin ) ) ),
						new StaticText( " to " ),
						numValueStyle.applyTo( new StaticText( String.valueOf( end ) ) ) } ) );
		Pres stepSizeField = new HorizontalField( "Step size:", numValueStyle.applyTo( new StaticText( String.valueOf( stepSize ) ) ) );
		return new ObjectBoxWithFields( getClass().getName(), new Pres[] { rangeField, valueField, stepSizeField } );
	}
	
	
	private final static StyleSheet numValueStyle = StyleSheet.instance.withAttr( Primitive.foreground, new Color( 0.5f, 0.0f, 0.25f ) );
};

