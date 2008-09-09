//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Marker;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.DPWidget;

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
	
	public int getPositionInSubtree(DPWidget subtreeRoot)
	{
		if ( subtreeRoot == widget )
		{
			return position;
		}
		else
		{
			DPContainer c = (DPContainer)subtreeRoot;
			if ( widget.isInSubtreeRootedAt( c ) )
			{
				return position + widget.getContentOffsetInSubtree( c );
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
	
	public int getIndexInSubtree(DPWidget subtreeRoot)
	{
		int p = getPositionInSubtree( subtreeRoot );
		return bias == Bias.END  ?  p + 1  :  p;
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
		this.widget = widget;
		this.position = position;
		this.bias = bias;
		changed();
	}
	
	
	public static int getIndex(int position, Bias bias)
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	
	
	public boolean isValid()
	{
		return widget != null;
	}
	
	
	
	protected void changed()
	{
		if ( listener != null )
		{
			listener.markerChanged( this );
		}
	}




	public Marker clone()
	{
		try {
			return (Marker)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
}
