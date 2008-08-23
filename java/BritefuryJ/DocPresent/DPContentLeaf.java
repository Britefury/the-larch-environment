package BritefuryJ.DocPresent;

import java.util.Vector;
import java.util.WeakHashMap;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;
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
	protected ContentListener listener;
	
	
	
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
	
	
	
	public void insertContent(Marker marker, String x)
	{
		int index = marker.getIndex();
		content = content.substring( 0, index ) + x + content.substring( index );
		markerInsert( index, x.length() );
		contentChanged();
		if ( listener != null )
		{
			listener.contentInserted( marker, x );
		}
	}

	public void removeContent(Marker m, int length)
	{
		int index = m.getIndex();
		content = content.substring( 0, index ) + content.substring( index + length );
		markerRemove( index, length );
		contentChanged();
		if ( listener != null )
		{
			listener.contentRemoved( m, length );
		}
	}
	
	public void replaceContent(Marker m, int length, String x)
	{
		int index = m.getIndex();
		content = content.substring( 0, index )  +  x  +  content.substring( index + length );
		
		if ( x.length() > length )
		{
			markerInsert( index + length, x.length() - length );
		}
		else if ( x.length() < length )
		{
			markerRemove( index + x.length(), length - x.length() );
		}
		contentChanged();
		if ( listener != null )
		{
			listener.contentReplaced( m, length, x );
		}
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
		if ( content.length() == 0 )
		{
			moveMarker( m, 0, Marker.Bias.START );
		}
		else
		{
			moveMarker( m, content.length() - 1, Marker.Bias.END );
		}
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
	
	protected Marker markerToLeft(Marker m, boolean bItemStep)
	{
		if ( isMarkerAtStart( m ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			if ( left != null )
			{
				if ( bItemStep )
				{
					return left.markerAtStart();
				}
				else
				{
					return left.markerAtEnd();
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



	protected Marker markerToRight(Marker m, boolean bItemStep)
	{
		if ( isMarkerAtEnd( m ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			if ( right != null )
			{
				if ( bItemStep )
				{
					return right.markerAtEnd();
				}
				else
				{
					return right.markerAtStart();
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
		Point2 cursorPos = getCursorPosition();
		DPContentLeaf above = getContentLeafAbove( cursorPos );
		if ( above != null )
		{
			Point2 cursorPosInAbove = getLocalPointRelativeTo( above, cursorPos );
			int contentPos = above.getContentPositonForPoint( cursorPosInAbove );
			above.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	protected void moveMarkerDown(Marker marker)
	{
		Point2 cursorPos = getCursorPosition();
		DPContentLeaf below = getContentLeafBelow( cursorPos );
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
	
	
	
	protected DPContentLeaf getContentLeafAbove(Point2 localPos)
	{
		return getContentLeafAboveOrBelow( localPos, false );
	}
	
	protected DPContentLeaf getContentLeafBelow(Point2 localPos)
	{
		return getContentLeafAboveOrBelow( localPos, true );
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelow(Point2 localPos, boolean bBelow)
	{
		if ( parent != null )
		{
			return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localPos ) );
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

	protected DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
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
	
	
	

	//
	//
	// INPUT EVENT HANDLING
	//
	//
	
	protected boolean handleBackspace(Caret caret)
	{
		if ( isMarkerAtStart( caret.getMarker() ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			if ( left == null )
			{
				return false;
			}
			else
			{
				left.moveMarkerToEnd( caret.getMarker() );
				return true;
			}
		}
		else
		{
			removeContent( markerToLeft( caret.getMarker(), false ), 1 );
			return true;
		}
	}
	
	protected boolean handleDelete(Caret caret)
	{
		if ( isMarkerAtEnd( caret.getMarker() ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			if ( right == null )
			{
				return false;
			}
			else
			{
				right.moveMarkerToStart( caret.getMarker() );
				return true;
			}
		}
		else
		{
			removeContent( caret.getMarker(), 1 );
			return true;
		}
	}
	
	protected boolean onKeyPress(Caret caret, KeyEvent event)
	{
		if ( event.getKeyCode() == KeyEvent.VK_BACK_SPACE )
		{
			return handleBackspace( caret );
		}
		else if ( event.getKeyCode() == KeyEvent.VK_DELETE )
		{
			return handleDelete( caret );
		}
		return false;
	}

	protected boolean onKeyRelease(Caret caret, KeyEvent event)
	{
		return false;
	}

	protected boolean onKeyTyped(Caret caret, KeyEvent event)
	{
		if ( event.getKeyChar() != KeyEvent.VK_BACK_SPACE  &&  event.getKeyChar() != KeyEvent.VK_DELETE )
		{
			insertContent( caret.getMarker(), String.valueOf( event.getKeyChar() ) );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	
	protected boolean onButtonDown(PointerButtonEvent event)
	{
		if ( event.getButton() == 1 )
		{
			Caret caret = presentationArea.getCaret();
			int contentPos = getContentPositonForPoint( event.getPointer().getLocalPos() );
			moveMarker( caret.getMarker(), contentPos, Marker.Bias.START );
			return true;
		}
		else
		{
			return false;
		}
	}
}
