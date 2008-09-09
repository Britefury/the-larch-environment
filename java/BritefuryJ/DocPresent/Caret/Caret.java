//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Caret;

import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Marker.MarkerListener;

public class Caret implements MarkerListener
{
	protected Marker marker;
	protected CaretListener listener;
	
	
	
	public Caret()
	{
		marker = new Marker();
		marker.setMarkerListener( this );
	}
	
	
	public void setCaretListener(CaretListener listener)
	{
		this.listener = listener;
	}
	
	
	public Marker getMarker()
	{
		return marker;
	}
	
	
	public DPContentLeaf getWidget()
	{
		if ( marker != null )
		{
			return marker.getWidget();
		}
		else
		{
			return null;
		}
	}
	
	
	
	public boolean isValid()
	{
		if ( marker != null )
		{
			return marker.isValid();
		}
		else
		{
			return false;
		}
	}
	
	
	
	public void markerChanged(Marker m)
	{
		changed();
	}
	
	
	protected void changed()
	{
		if ( listener != null )
		{
			listener.caretChanged( this );
		}
	}
}
