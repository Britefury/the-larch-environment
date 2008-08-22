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
	}
	
	
	public void setCaretListener(CaretListener listener)
	{
		this.listener = listener;
	}
	
	
	public void setMarker(Marker m)
	{
		if ( marker != null )
		{
			marker.setMarkerListener( null );
		}
		
		marker = m;

		if ( marker != null )
		{
			marker.setMarkerListener( this );
		}
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
