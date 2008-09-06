package BritefuryJ.DocPresent;

import java.util.Vector;
import java.util.WeakHashMap;
import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;
import BritefuryJ.Math.Point2;

public abstract class DPContentLeaf extends DPWidget implements ContentInterface
{
	public static class CannotCreateMarkerWithEmptyContent extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public CannotCreateMarkerWithEmptyContent()
		{
		}
	}
	
	

	
	private WeakHashMap<Marker,Object> markers;
	protected String content;
	protected WidgetContentListener listener;
	
	
	
	DPContentLeaf()
	{
		this( ContentLeafStyleSheet.defaultStyleSheet, "" );
	}
	
	DPContentLeaf(ContentLeafStyleSheet styleSheet)
	{
		this( styleSheet, "" );
	}
	
	DPContentLeaf(ContentLeafStyleSheet styleSheet, String content)
	{
		super( styleSheet );
		
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
		int oldLength = content.length();
		content = x;
		
		if ( x.length() > oldLength )
		{
			markerInsert( oldLength, x.length() - oldLength );
		}
		else if ( x.length() < oldLength )
		{
			markerRemove( x.length(), oldLength - x.length() );
		}
		contentChanged();
	}
	
	public abstract int getContentPositonForPoint(Point2 localPos);
	
	
	
	public void setContentListener(WidgetContentListener listener)
	{
		this.listener = listener;
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
			throw new Marker.InvalidMarkerPosition();
		}

		Marker m = new Marker( this, position, bias );
		registerMarker( m );
		
		return m;
	}
	
	public Marker markerAtStart()
	{
		return marker( 0, Marker.Bias.START );
	}
	
	public Marker markerAtStartPlusOne()
	{
		return marker( Math.min( 1, content.length() ), Marker.Bias.START );
	}
	
	public Marker markerAtEnd()
	{
		return marker( Math.max( content.length() - 1, 0 ), Marker.Bias.END );
	}
	
	public Marker markerAtEndMinusOne()
	{
		return marker( Math.max( content.length() - 1, 0 ), Marker.Bias.START );
	}
	
	
	public void moveMarker(Marker m, int position, Marker.Bias bias)
	{
		if ( position > getContentLength() )
		{
			throw new Marker.InvalidMarkerPosition();
		}
		
		if ( position == getContentLength()  &&  bias == Marker.Bias.END )
		{
			throw new Marker.InvalidMarkerPosition();
		}
		
		DPContentLeaf oldWidget = m.getWidget();
		if ( oldWidget != null )
		{
			oldWidget.unregisterMarker( m );
		}
		m.set( this, position, bias );
		registerMarker( m );
	}
	
	public void moveMarkerToStart(Marker m)
	{
		moveMarker( m, 0, Marker.Bias.START );
	}
	
	public void moveMarkerToStartPlusOne(Marker m)
	{
		moveMarker( m, Math.min( 1, content.length() ), Marker.Bias.START );
	}
	
	public void moveMarkerToEnd(Marker m)
	{
		moveMarker( m, Math.max( content.length() - 1, 0 ), Marker.Bias.END );
	}
	
	public void moveMarkerToEndMinusOne(Marker m)
	{
		moveMarker( m, Math.max( content.length() - 1, 0 ), Marker.Bias.START );
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
	
	
	
	
	protected void markerInsert(int position, int length)
	{
		for (Marker m: markers.keySet())
		{
			if ( m.getIndex() >= position )
			{
				m.setPosition( m.getPosition() + length );
			}
		}
	}
	
	protected void markerRemove(int position, int length)
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
	
	protected Marker markerToLeft(Marker m, boolean bItemStep, boolean bSkipWhitespace)
	{
		if ( isMarkerAtStart( m ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			boolean bSkippedWhitespace = false;
			
			if ( bSkipWhitespace )
			{
				while ( left != null  &&  left.isWhitespace() )
				{
					left = left.getContentLeafToLeft();
					bSkippedWhitespace = true;
				}
			}
			
			if ( left != null )
			{
				if ( bItemStep )
				{
					return left.markerAtStart();
				}
				else
				{
					if ( bSkippedWhitespace )
					{
						return left.markerAtEnd();
					}
					else
					{
						return left.markerAtEndMinusOne();
					}
				}
			}
			else
			{
				return m;
			}
		}
		else
		{
			return marker( m.getIndex() - 1, Marker.Bias.START );
		}
	}

	protected void moveMarkerLeft(Marker marker, boolean bItemStep, boolean bSkipWhitespace)
	{
		if ( isMarkerAtStart( marker ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			boolean bSkippedWhitespace = false;
			

			if ( bSkipWhitespace )
			{
				while ( left != null  &&  left.isWhitespace() )
				{
					left = left.getContentLeafToLeft();
					bSkippedWhitespace = true;
				}
			}

			if ( left != null )
			{
				if ( bItemStep )
				{
					left.moveMarkerToStart( marker );
				}
				else
				{
					if ( bSkippedWhitespace )
					{
						left.moveMarkerToEnd( marker );
					}
					else
					{
						left.moveMarkerToEndMinusOne( marker );
					}
				}
			}
		}
		else
		{
			moveMarker( marker, marker.getIndex() - 1, Marker.Bias.START );
		}
	}



	protected Marker markerToRight(Marker m, boolean bItemStep, boolean bSkipWhitespace)
	{
		if ( isMarkerAtEnd( m ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			boolean bSkippedWhitespace = false;
			

			if ( bSkipWhitespace )
			{
				while ( right != null  &&  right.isWhitespace() )
				{
					right = right.getContentLeafToRight();
					bSkippedWhitespace = true;
				}
			}

			if ( right != null )
			{
				if ( bItemStep )
				{
					return right.markerAtEnd();
				}
				else
				{
					if ( bSkippedWhitespace )
					{
						return right.markerAtStart();
					}
					else
					{
						return right.markerAtStartPlusOne();
					}
				}
			}
			else
			{
				return m;
			}
		}
		else
		{
			return marker( m.getIndex(), Marker.Bias.END );
		}
	}
	
	protected void moveMarkerRight(Marker marker, boolean bItemStep, boolean bSkipWhitespace)
	{
		if ( isMarkerAtEnd( marker ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			boolean bSkippedWhitespace = false;
			

			if ( bSkipWhitespace )
			{
				while ( right != null  &&  right.isWhitespace() )
				{
					right = right.getContentLeafToRight();
					bSkippedWhitespace = true;
				}
			}

			if ( right != null )
			{
				if ( bItemStep )
				{
					right.moveMarkerToEnd( marker );
				}
				else
				{
					if ( bSkippedWhitespace )
					{
						right.moveMarkerToStart( marker );
					}
					else
					{
						right.moveMarkerToStartPlusOne( marker );
					}
				}
			}
		}
		else
		{
			moveMarker( marker, marker.getIndex(), Marker.Bias.END );
		}
	}
	
	protected void moveMarkerUp(Marker marker, boolean bSkipWhitespace)
	{
		Point2 cursorPos = getCursorPosition();
		DPContentLeaf above = getContentLeafAbove( cursorPos, bSkipWhitespace );
		if ( above != null )
		{
			Point2 cursorPosInAbove = getLocalPointRelativeTo( above, cursorPos );
			int contentPos = above.getContentPositonForPoint( cursorPosInAbove );
			above.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	protected void moveMarkerDown(Marker marker, boolean bSkipWhitespace)
	{
		Point2 cursorPos = getCursorPosition();
		DPContentLeaf below = getContentLeafBelow( cursorPos, bSkipWhitespace );
		if ( below != null )
		{
			Point2 cursorPosInBelow = getLocalPointRelativeTo( below, cursorPos );
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
	
	public boolean isWhitespace()
	{
		return false;
	}
	
	public boolean isEditable()
	{
		return false;
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
	
	
	
	protected DPContentLeaf getContentLeafAbove(Point2 localPos, boolean bSkipWhitespace)
	{
		return getContentLeafAboveOrBelow( localPos, false, bSkipWhitespace );
	}
	
	protected DPContentLeaf getContentLeafBelow(Point2 localPos, boolean bSkipWhitespace)
	{
		return getContentLeafAboveOrBelow( localPos, true, bSkipWhitespace );
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelow(Point2 localPos, boolean bBelow, boolean bSkipWhitespace)
	{
		if ( parent != null )
		{
			return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localPos ), bSkipWhitespace );
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

	protected DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		if ( bSkipWhitespace && isWhitespace() )
		{
			return null;
		}
		else
		{
			return this;
		}
	}
	
	
	public DPContentLeaf getLeafAtContentPosition(int position)
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
		
		while ( left != null  &&  !left.isRealised() )
		{
			left = left.getContentLeafToLeft();
		}
		
		if ( left != null )
		{
			for (Marker x: xs)
			{
				try
				{
					left.moveMarkerToEnd( x );
				}
				catch (Marker.InvalidMarkerPosition e)
				{
				}
			}
		}
		else
		{
			DPContentLeaf right = getContentLeafToRight();
			
			while ( right != null  &&  !right.isRealised() )
			{
				right = right.getContentLeafToRight();
			}
			
			if ( right != null )
			{
				for (Marker x: xs)
				{
					try
					{
						right.moveMarkerToEnd( x );
					}
					catch (Marker.InvalidMarkerPosition e)
					{
					}
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
