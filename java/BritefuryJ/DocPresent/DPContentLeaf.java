package BritefuryJ.DocPresent;

import java.util.Vector;
import java.util.WeakHashMap;
import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.Math.Point2;

public abstract class DPContentLeaf extends DPWidget
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
	
	
	
	DPContentLeaf()
	{
		this( "" );
	}
	
	DPContentLeaf(String content)
	{
		super();
		
		markers = new WeakHashMap<Marker,Object>();
		this.content = content;
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
	
	public abstract int getContentPositonForPoint(Point2 localPos);
	
	
	
	public void insertContent(int position, String x)
	{
		content = content.substring( 0, position ) + x + content.substring( position );
		markerInsert( position, x.length() );
		contentChanged();
	}

	public void removeContent(int position, int length)
	{
		content = content.substring( 0, position ) + content.substring( position + length );
		markerRemove( position, length );
		contentChanged();
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
		contentChanged();
	}
	
	
	public void contentChanged()
	{
	}
	
	
	
	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public abstract void drawCaret(Graphics2D graphics, Caret c);
	
	
	protected void onCaretEnter(Caret c)
	{
	}
	
	protected void onCaretLeave(Caret c)
	{
	}
	
	
	protected void handleCaretEnter(Caret c)
	{
		onCaretEnter( c );
	}
	
	protected void handleCaretLeave(Caret c)
	{
		onCaretLeave( c );
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
	
	
	public void moveMarker(Marker m, int position, Marker.Bias bias)
	{
		if ( getContentLength() == 0 )
		{
			throw new CannotCreateMarkerWithEmptyContent();
		}
		
		if ( position > getContentLength() )
		{
			throw new InvalidMarkerPosition();
		}
		
		if ( position == getContentLength()  &&  bias == Marker.Bias.END )
		{
			throw new InvalidMarkerPosition();
		}
		
		m.getWidget().unregisterMarker( m );
		m.set( this, position, bias );
		registerMarker( m );
	}
	
	public void moveMarkerToStart(Marker m)
	{
		moveMarker( m, 0, Marker.Bias.START );
	}
	
	public void moveMarkerToEnd(Marker m)
	{
		moveMarker( m, content.length() - 1, Marker.Bias.END );
	}
	
	
	
	public boolean isMarkerAtStart(Marker m)
	{
		if ( m.getWidget() == this )
		{
			return m.getIndex() == 0;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isMarkerAtEnd(Marker m)
	{
		if ( m.getWidget() == this )
		{
			return m.getIndex() == getContentLength();
		}
		else
		{
			return false;
		}
	}
	
	
	
	
	
	protected void registerMarker(Marker m)
	{
		markers.put( m, null );
	}
	
	protected void unregisterMarker(Marker m)
	{
		markers.remove( m );
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

	
	

	// MARKER MOVEMENT METHODS
	
	protected void moveMarkerLeft(Marker marker, boolean bItemStep)
	{
		if ( isMarkerAtStart( marker ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			if ( left != null )
			{
				if ( bItemStep )
				{
					left.moveMarkerToStart( marker );
				}
				else
				{
					left.moveMarkerToEnd( marker );
				}
			}
		}
		else
		{
			moveMarker( marker, marker.getIndex() - 1, Marker.Bias.START );
		}
	}



	protected void moveMarkerRight(Marker marker, boolean bItemStep)
	{
		if ( isMarkerAtEnd( marker ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			if ( right != null )
			{
				if ( bItemStep )
				{
					right.moveMarkerToEnd( marker );
				}
				else
				{
					right.moveMarkerToStart( marker );
				}
			}
		}
		else
		{
			moveMarker( marker, marker.getIndex(), Marker.Bias.END );
		}
	}
	
	protected void moveMarkerUp(Marker marker)
	{
		DPContentLeaf above = getContentLeafAbove();
		if ( above != null )
		{
			Point2 cursorPosInAbove = getLocalPointRelativeTo( above, getCursorPosition() );
			int contentPos = above.getContentPositonForPoint( cursorPosInAbove );
			above.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	protected void moveMarkerDown(Marker marker)
	{
		DPContentLeaf below = getContentLeafBelow();
		if ( below != null )
		{
			Point2 cursorPosInBelow = getLocalPointRelativeTo( below, getCursorPosition() );
			int contentPos = below.getContentPositonForPoint( cursorPosInBelow );
			below.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	
	

	
	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public boolean isContentLeaf()
	{
		return true;
	}

	
	
	protected DPContentLeaf getContentLeafToLeft()
	{
		if ( parent != null )
		{
			return parent.getContentLeafToLeftFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	protected DPContentLeaf getContentLeafToRight()
	{
		if ( parent != null )
		{
			return parent.getContentLeafToRightFromChild( this );
		}
		else
		{
			return null;
		}
	}
	
	
	
	protected DPContentLeaf getContentLeafAbove()
	{
		return getContentLeafAboveOrBelow( false );
	}
	
	protected DPContentLeaf getContentLeafBelow()
	{
		return getContentLeafAboveOrBelow( true );
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelow(boolean bBelow)
	{
		if ( parent != null )
		{
			Point2 localCursorPos = getCursorPosition();
			return parent.getFocusLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localCursorPos ) );
		}
		else
		{
			return null;
		}
	}
	
	
	protected DPContentLeaf getLeftContentLeaf()
	{
		return this;
	}

	protected DPContentLeaf getRightContentLeaf()
	{
		return this;
	}

	protected DPContentLeaf getTopOrBottomFocusLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		return this;
	}
	
	
	
	
	//
	//
	// REALISE / UNREALISE
	//
	//

	protected void onRealise()
	{
		super.onRealise();
	}
	
	protected void onUnrealise()
	{
		super.onUnrealise();

		Vector<Marker> xs = new Vector<Marker>( markers.keySet() );
		
		DPContentLeaf left = getContentLeafToLeft();
		
		if ( left != null )
		{
			for (Marker x: xs)
			{
				left.moveMarkerToEnd( x );
			}
		}
		else
		{
			DPContentLeaf right = getContentLeafToRight();
			
			if ( right != null )
			{
				for (Marker x: xs)
				{
					right.moveMarkerToEnd( x );
				}
			}
			else
			{
				for (Marker x: xs)
				{
					unregisterMarker( x );
					x.set( null, 0, Marker.Bias.START );
				}
			}
		}
	}
}
