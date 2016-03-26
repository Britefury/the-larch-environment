//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Util;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.PresentationStateListenerList;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.HorizontalField;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.StyleSheet.StyleSheet;


public class Range implements Presentable
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
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
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


	private final static StyleSheet numValueStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.5f, 0.0f, 0.25f ) ) );
}

