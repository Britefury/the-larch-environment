//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Marker;

import BritefuryJ.DocPresent.DPContentLeaf;

public class Marker
{
	public static class InvalidMarkerPosition extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public InvalidMarkerPosition()
		{
		}
	}

	
	public enum Bias { START, END };
	
	
	protected DPContentLeaf widget;
	protected int position;
	protected Bias bias;
	protected MarkerListener listener;
	
	
	
	public Marker()
	{
		this.widget = null;
		this.position = 0;
		this.bias = Bias.START;
	}
	
	public Marker(DPContentLeaf widget, int position, Bias bias)
	{
		this.widget = widget;
		this.position = position;
		this.bias = bias;
	}
	
	
	public void setMarkerListener(MarkerListener listener)
	{
		this.listener = listener;
	}
	
	
	
	public DPContentLeaf getWidget()
	{
		return widget;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public Bias getBias()
	{
		return bias;
	}
	
	
	public int getIndex()
	{
		return bias == Bias.END  ?  position + 1  :  position;
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
		// ONLY call from 
		this.widget = widget;
		this.position = position;
		this.bias = bias;
		changed();
	}
	
	
	
	public Marker copy()
	{
		return widget.marker( position, bias );
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
		if ( listener != null )
		{
			listener.markerChanged( this );
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
}
