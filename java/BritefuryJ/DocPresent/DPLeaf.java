package BritefuryJ.DocPresent;

import java.util.WeakHashMap;

import BritefuryJ.DocPresent.Marker.Marker;

public abstract class DPLeaf extends DPWidget
{
	public static class InvalidMarkerPosition extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public InvalidMarkerPosition()
		{
		}
	}
	
	
	public static class CannotCreateMarkerWithEmptyContent extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public CannotCreateMarkerWithEmptyContent()
		{
		}
	}
	
	

	
	private WeakHashMap<Marker,Object> markers;
	protected String content;
	
	
	
	DPLeaf()
	{
		super();
		
		markers = new WeakHashMap<Marker,Object>();
		content = "";
	}
	
	
	
	//
	//
	// CONTENT METHODS
	//
	//
	
	public String getContent()
	{
		return content;
	}
	
	public int getContentLength()
	{
		return content.length();
	}
	
	public void setContent(String x)
	{
		replaceContent( 0, content.length(), x );
	}
	
	
	
	public void insertContent(int position, String x)
	{
		content = content.substring( 0, position ) + x + content.substring( position );
		markerInsert( position, x.length() );
	}

	public void removeContent(int position, int length)
	{
		content = content.substring( 0, position ) + content.substring( position + length );
		markerRemove( position, length );
	}
	
	public void replaceContent(int position, int length, String x)
	{
		content = content.substring( 0, position )  +  x  +  content.substring( position + length );
		
		if ( x.length() > length )
		{
			markerInsert( position + length, x.length() - length );
		}
		else if ( x.length() < length )
		{
			markerRemove( position + x.length(), length - x.length() );
		}
	}
	
	
	private void markerInsert(int position, int length)
	{
		for (Marker m: markers.keySet())
		{
			if ( m.getIndex() >= position )
			{
				m.setPosition( m.getPosition() + length );
			}
		}
	}
	
	private void markerRemove(int position, int length)
	{
		int end = position + length;

		for (Marker m: markers.keySet())
		{
			if ( m.getIndex() >= position )
			{
				if ( m.getIndex() > end )
				{
					m.setPosition( m.getPosition() - length );
				}
				else
				{
					m.setPositionAndBias( position, Marker.Bias.START );
				}
			}
		}
	}

	
	
	
	
	//
	//
	// MARKER METHODS
	//
	//	
	
	public Marker marker(int position, Marker.Bias bias)
	{
		if ( getContentLength() == 0 )
		{
			throw new CannotCreateMarkerWithEmptyContent();
		}
		
		if ( position >= getContentLength() )
		{
			throw new InvalidMarkerPosition();
		}

		Marker m = new Marker( this, position, bias );
		registerMarker( m );
		
		return m;
	}
	
	public Marker markerAtStart()
	{
		return marker( 0, Marker.Bias.START );
	}
	
	public Marker markerAtEnd()
	{
		return marker( content.length() - 1, Marker.Bias.END );
	}
	
	
	public void setMarker(Marker m, int position, Marker.Bias bias)
	{
		if ( getContentLength() == 0 )
		{
			throw new CannotCreateMarkerWithEmptyContent();
		}
		
		if ( position >= getContentLength() )
		{
			throw new InvalidMarkerPosition();
		}
		
		m.getWidget().unregisterMarker( m );
		m.set( this, position, bias );
		registerMarker( m );
	}
	
	public void setMarkerAtStart(Marker m)
	{
		setMarker( m, 0, Marker.Bias.START );
	}
	
	public void setMarkerAtEnd(Marker m)
	{
		setMarker( m, content.length() - 1, Marker.Bias.END );
	}
	
	
	
	
	
	protected void registerMarker(Marker m)
	{
		markers.put( m, null );
	}
	
	protected void unregisterMarker(Marker m)
	{
		markers.remove( m );
	}
}
