//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Vector;
import java.util.WeakHashMap;
import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;
import BritefuryJ.Math.Point2;

public abstract class DPContentLeaf extends DPWidget
{
	public static class CannotCreateMarkerWithEmptyContent extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public CannotCreateMarkerWithEmptyContent()
		{
		}
	}
	
	

	
	private WeakHashMap<Marker,Object> markers;
	
	
	
	DPContentLeaf()
	{
		this( ContentLeafStyleSheet.defaultStyleSheet );
	}
	
	DPContentLeaf(ContentLeafStyleSheet styleSheet)
	{
		super( styleSheet );
		
		markers = new WeakHashMap<Marker,Object>();
	}
	
	
	
	
	//
	// Marker range methods
	//
	
	protected abstract int getMarkerRange();
	
	protected void markerRangeChanged(int oldLength, int newLength)
	{
		if ( newLength > oldLength )
		{
			markerInsert( oldLength, newLength - oldLength );
		}
		else if ( newLength < oldLength )
		{
			markerRemove( newLength, oldLength - newLength );
		}
	}
	
	public abstract int getMarkerPositonForPoint(Point2 localPos);
	
	
	
	
	
	//
	//
	// CARET METHODS
	//
	//
	
	public abstract void drawCaret(Graphics2D graphics, Caret c);
	public abstract void drawCaretAtStart(Graphics2D graphics);
	public abstract void drawCaretAtEnd(Graphics2D graphics);
	
	
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
		if ( position > getMarkerRange() )
		{
			throw new Marker.InvalidMarkerPosition();
		}
		
		if ( getMarkerRange() == 0 && position == 0 )
		{
			bias = Marker.Bias.END;
		}
		
		if ( position == getMarkerRange()  &&  bias == Marker.Bias.END )
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
		return marker( Math.min( 1, getMarkerRange() ), Marker.Bias.START );
	}
	
	public Marker markerAtEnd()
	{
		return marker( Math.max( getMarkerRange() - 1, 0 ), Marker.Bias.END );
	}
	
	public Marker markerAtEndMinusOne()
	{
		return marker( Math.max( getMarkerRange() - 1, 0 ), Marker.Bias.START );
	}
	
	
	public void moveMarker(Marker m, int position, Marker.Bias bias)
	{
		if ( position > getMarkerRange() )
		{
			throw new Marker.InvalidMarkerPosition();
		}
		
		if ( getMarkerRange() == 0 && position == 0 )
		{
			bias = Marker.Bias.START;
		}
		
		if ( position == getMarkerRange()  &&  bias == Marker.Bias.END )
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
		moveMarker( m, Math.min( 1, getMarkerRange() ), Marker.Bias.START );
	}
	
	public void moveMarkerToEnd(Marker m)
	{
		moveMarker( m, Math.max( getMarkerRange() - 1, 0 ), Marker.Bias.END );
	}
	
	public void moveMarkerToEndMinusOne(Marker m)
	{
		moveMarker( m, Math.max( getMarkerRange() - 1, 0 ), Marker.Bias.START );
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
			return m.getIndex() == getMarkerRange();
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
	
	
	
	
	public void markerInsert(int position, int length)
	{
		for (Marker m: markers.keySet())
		{
			if ( m.getIndex() > position )
			{
				m.setPosition( m.getPosition() + length );
			}
			else if ( m.getIndex() == position )
			{
				m.setPositionAndBias( position + length - 1, Marker.Bias.END );
			}
		}
	}
	
	public void markerRemove(int position, int length)
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
			int contentPos = above.getMarkerPositonForPoint( cursorPosInAbove );
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
			int contentPos = below.getMarkerPositonForPoint( cursorPosInBelow );
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

	
	
	public DPContentLeaf getContentLeafToLeft()
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
	
	public DPContentLeaf getContentLeafToRight()
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
	
	public DPContentLeaf getEditableContentLeafToLeft()
	{
		DPContentLeaf leaf = this;
		
		while ( leaf != null  &&  !leaf.isEditable() )
		{
			leaf = leaf.getContentLeafToLeft();
		}
		
		return leaf;
	}
	
	public DPContentLeaf getEditableContentLeafToRight()
	{
		DPContentLeaf leaf = this;
		
		while ( leaf != null  &&  !leaf.isEditable() )
		{
			leaf = leaf.getContentLeafToRight();
		}
		
		return leaf;
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
			try
			{
				return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localPos ), bSkipWhitespace );
			}
			catch (IsNotInSubtreeException e)
			{
				throw new RuntimeException();
			}
		}
		else
		{
			return null;
		}
	}
	
	
	public DPContentLeaf getLeftContentLeaf()
	{
		return this;
	}

	public DPContentLeaf getRightContentLeaf()
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
