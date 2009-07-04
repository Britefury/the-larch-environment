//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Marker;

import java.util.ArrayList;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.DPWidget.IsNotInSubtreeException;

public class Marker
{
	public static class InvalidMarkerPosition extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public InvalidMarkerPosition(String message)
		{
			super( message );
		}
	}

	
	public enum Bias { START, END };
	
	
	protected DPContentLeaf widget;
	protected int position;
	protected Bias bias;
	protected ArrayList<MarkerListener> listeners;
	
	
	
	public Marker()
	{
		this.widget = null;
		this.position = 0;
		this.bias = Bias.START;
	}
	
	public Marker(DPContentLeaf widget, int position, Bias bias)
	{
		checkPositionAndBias( widget, position, bias );
		
		this.widget = widget;
		this.position = position;
		this.bias = bias;
		
		this.widget.registerMarker( this );
	}
	
	
	public void addMarkerListener(MarkerListener listener)
	{
		if ( listeners == null )
		{
			listeners = new ArrayList<MarkerListener>();
		}
		listeners.add( listener );
	}
	
	public void removeMarkerListener(MarkerListener listener)
	{
		if ( listeners != null )
		{
			listeners.remove( listener );
			if ( listeners.isEmpty() )
			{
				listeners = null;
			}
		}
	}
	
	
	
	public DPContentLeaf getElement()
	{
		return widget;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public int getPositionInSubtree(DPWidget subtreeRoot) throws IsNotInSubtreeException
	{
		DPWidget element = getElement();
		if ( subtreeRoot == element )
		{
			return getPosition();
		}
		else
		{
			if ( subtreeRoot instanceof DPContainer )
			{
				DPContainer b = (DPContainer)subtreeRoot;
				if ( element != null  &&  element.isInSubtreeRootedAt( b ) )
				{
					return getPosition() + element.getTextRepresentationOffsetInSubtree( b );
				}
				else
				{
					throw new DPWidget.IsNotInSubtreeException();
				}
			}
			else if ( subtreeRoot instanceof DPContentLeaf )
			{
				if ( element != null  &&  element == subtreeRoot )
				{
					return getPosition();
				}
				else
				{
					throw new DPWidget.IsNotInSubtreeException();
				}
			}
			else
			{
				throw new DPWidget.IsNotInSubtreeException();
			}
		}
	}

	public Bias getBias()
	{
		return bias;
	}
	
	
	public int getIndex()
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	public int getIndexInSubtree(DPWidget subtreeRoot) throws IsNotInSubtreeException
	{
		int p = getPositionInSubtree( subtreeRoot );
		return getBias() == Bias.END  ?  p + 1  :  p;
	}
	

	public void setPosition(int position)
	{
		this.position = position;
		changed();
	}
	
	public void setPositionAndBias(int position, Bias bias)
	{
		this.position = position;
		this.bias = bias;
		changed();
	}
	
	public void set(DPContentLeaf widget, int position, Bias bias)
	{
		if ( widget != null )
		{
			checkPositionAndBias( widget, position, bias );
		}
		
		if ( this.widget != null )
		{
			this.widget.unregisterMarker( this );
		}
		
		this.widget = widget;
		this.position = position;
		this.bias = bias;
		
		this.widget.registerMarker( this );
		
		changed();
	}
	
	
	public void clear()
	{
		if ( widget != null )
		{
			widget.unregisterMarker( this );
			widget = null;
			position = 0;
			bias = Bias.START;
			changed();
		}
		else
		{
			if ( position != 0  ||  bias != Bias.START )
			{
				position = 0;
				bias = Bias.START;
				changed();
			}
		}
	}
	
	
	
	public Marker copy()
	{
		return widget.marker( position, bias );
	}
	
	public void moveTo(Marker marker)
	{
		set( marker.widget, marker.position, marker.bias );
	}
	
	
	public static int getIndex(int position, Bias bias)
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	
	
	public boolean isValid()
	{
		return widget != null  &&  widget.isRealised();
	}
	
	
	
	protected void changed()
	{
		if ( listeners != null )
		{
			for (MarkerListener listener: listeners)
			{
				listener.markerChanged( this );
			}
		}
	}




	public boolean equals(Marker m)
	{
		if ( m == this )
		{
			return true;
		}
		else
		{
			return widget == m.widget  &&  position == m.position  &&  bias == m.bias;
		}
	}
	
	
	
	private static void checkPositionAndBias(DPContentLeaf w, int position, Bias bias)
	{
		int markerRange = w.getMarkerRange();
		if ( position > markerRange )
		{
			throw new InvalidMarkerPosition( "Cannot place marker at " + position + ":" + bias + " - range is " + markerRange );
		}
		
		if ( markerRange == 0 && position == 0 )
		{
			bias = Marker.Bias.START;
		}
		
		if ( position == markerRange  &&  bias == Marker.Bias.END )
		{
			throw new InvalidMarkerPosition( "Cannot place marker at " + position + ":" + bias + " - range is " + markerRange );
		}
	}
}
