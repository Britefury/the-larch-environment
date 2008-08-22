package BritefuryJ.DocPresent.Caret;

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
