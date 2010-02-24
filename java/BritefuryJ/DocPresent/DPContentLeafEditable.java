//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.Math.Point2;

public abstract class DPContentLeafEditable extends DPContentLeaf
{
	public static class EditableLeafElementFilter implements WidgetFilter
	{
		public boolean testElement(DPWidget element)
		{
			return element instanceof DPContentLeafEditable;
		}
	}
	
	
	
	public static final int FLAG_SELECTABLE = FLAGS_CONTENTLEAF_END * 0x1;
	
	
	protected final static int FLAGS_CONTENTLEAFEDITABLE_END = FLAGS_CONTENTLEAF_END * 0x2;

	
	
	//
	// Constructors
	//
	
	
	protected DPContentLeafEditable(String textRepresentation)
	{
		super( textRepresentation );
	}
	
	protected DPContentLeafEditable(ContentLeafStyleParams styleParams, String textRepresentation)
	{
		super(styleParams, textRepresentation );
	}
	

	
	
	
	//
	// Marker range methods
	//
	
	public abstract int getMarkerRange();
	
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
		super.onCaretEnter( c );
		
		DPBorder border = getMetaHeaderBorderWidget(); 
		if ( border != null )
		{
			border.setBorder( metaHeaderHighlightBorder );
		}
	}
	
	protected void onCaretLeave(Caret c)
	{
		super.onCaretLeave( c );
		
		DPBorder border = getMetaHeaderBorderWidget(); 
		if ( border != null )
		{
			border.setBorder( metaHeaderEmptyBorder );
		}
	}
	
	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public abstract void drawSelection(Graphics2D graphics, Marker from, Marker to);
	

	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPWidget> startPath, Marker endMarker, List<DPWidget> endPath)
	{
		drawSelection( graphics, startMarker, endMarker );
	}
	
	

	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	

	public DPContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
	{
		if ( isEditable() )
		{
			return this;
		}
		else
		{
			return null;
		}
	}
	
	
	
	
	//
	//
	// MARKER METHODS
	//
	//	
	
	public Marker marker(int position, Marker.Bias bias)
	{
		return new Marker( this, position, bias );
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
	
	
	public Marker markerAtPoint(Point2 localPos)
	{
		int markerPos = getMarkerPositonForPoint( localPos );
		return marker( markerPos, Marker.Bias.START );
	}



	public void moveMarker(Marker m, int position, Marker.Bias bias)
	{
		m.set( this, position, bias );
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
	
	public void moveMarkerToPoint(Marker m, Point2 localPos)
	{
		int markerPos = getMarkerPositonForPoint( localPos );
		moveMarker( m, markerPos, Marker.Bias.START );
	}

	
	
	public boolean isMarkerAtStart(Marker m)
	{
		if ( m.getElement() == this )
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
		if ( m.getElement() == this )
		{
			// The index (position and bias) is at the last position,
			// OR
			// range is 0, and position is 0, bias is 1, which would make index 1
			return m.getIndex() == getMarkerRange()  ||  m.getPosition() == getMarkerRange();
		}
		else
		{
			return false;
		}
	}
	
	
	
	
	
	protected WeakHashMap<Marker, Object> getMarkersForLeaf()
	{
		if ( presentationArea != null )
		{
			return presentationArea.markersByLeaf.get( this );
		}
		else
		{
			return null;
		}
	}
	
	protected WeakHashMap<Marker, Object> getValidMarkersForLeaf()
	{
		if ( presentationArea != null )
		{
			WeakHashMap<Marker, Object> markers = presentationArea.markersByLeaf.get( this );
			
			if ( markers == null )
			{
				markers = new WeakHashMap<Marker, Object>(); 
				presentationArea.markersByLeaf.put( this, markers );
			}
			
			return markers;
		}
		else
		{
			return null;
		}
	}
	
	protected void removeMarkersForLeaf()
	{
		presentationArea.markersByLeaf.remove( this );
	}
	
	
	public void registerMarker(Marker m)
	{
		WeakHashMap<Marker, Object> markers = getValidMarkersForLeaf();
		markers.put( m, null );
	}
	
	public void unregisterMarker(Marker m)
	{
		WeakHashMap<Marker, Object> markers = getMarkersForLeaf();
		if ( markers != null )
		{
			markers.remove( m );
		
			if ( markers.isEmpty() )
			{
				removeMarkersForLeaf();
			}
		}
	}
	
	
	
	
	public void markerInsert(int position, int length)
	{
		WeakHashMap<Marker, Object> markers = getMarkersForLeaf();
		if ( markers != null )
		{
			for (Marker m: markers.keySet())
			{
				if ( m.getClampedIndex() > position )
				{
					m.setPosition( m.getPosition() + length );
				}
				else if ( m.getClampedIndex() == position )
				{
					m.setPositionAndBias( position + length - 1, Marker.Bias.END );
				}
			}
		}
	}
	
	public void markerRemove(int position, int length)
	{
		WeakHashMap<Marker, Object> markers = getMarkersForLeaf();
		if ( markers != null )
		{
			int end = position + length;
	
			for (Marker m: markers.keySet())
			{
				if ( m.getClampedIndex() >= position )
				{
					if ( m.getClampedIndex() > end )
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
	
	protected void onUnrealise(DPWidget unrealiseRoot)
	{
		super.onUnrealise( unrealiseRoot );
		
		WeakHashMap<Marker, Object> markers = getMarkersForLeaf();

		if ( markers != null )
		{
			ArrayList<Marker> xs = new ArrayList<Marker>( markers.keySet() );
			
			if ( xs.size() > 0 )
			{
				DPContentLeaf left = unrealiseRoot.getContentLeafToLeft();
				
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
					DPContentLeaf right = unrealiseRoot.getContentLeafToRight();
					
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
								right.moveMarkerToStart( x );
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
							x.clear();
						}
					}
				}
			}
		}
	}
	
	
	

	
	
	//
	//
	// TEXT REPRESENTATION MODIFICATION METHODS
	//
	//
	
	public void setTextRepresentation(String newTextRepresentation)
	{
		int oldLength = textRepresentation.length();
		int newLength = newTextRepresentation.length();
		
		textRepresentation = newTextRepresentation;

		if ( newLength > oldLength )
		{
			markerInsert( oldLength, newLength - oldLength );
		}
		else if ( newLength < oldLength )
		{
			markerRemove( newLength, oldLength - newLength );
		}
		
		textRepresentationChanged( new LinearRepresentationEventTextReplace( this, 0, oldLength, newTextRepresentation ) );
	}
	
	
	public void insertText(Marker marker, String x)
	{
		int index = marker.getClampedIndex();
		markerInsert( index, x.length() );
		textRepresentation = textRepresentation.substring( 0, index ) + x + textRepresentation.substring( index );
		textRepresentationChanged( new LinearRepresentationEventTextInsert( this, index, x ) );
	}

	public void removeText(Marker marker, int length)
	{
		removeText( marker.getIndex(), length );
	}
	
	public void replaceText(Marker marker, int length, String x)
	{
		int index = marker.getClampedIndex();
		textRepresentation = textRepresentation.substring( 0, index )  +  x  +  textRepresentation.substring( index + length );
		
		if ( x.length() > length )
		{
			markerInsert( index + length, x.length() - length );
		}
		else if ( x.length() < length )
		{
			markerRemove( index + x.length(), length - x.length() );
		}
		textRepresentationChanged( new LinearRepresentationEventTextReplace( this, index, length, x ) );
	}
	
	protected void notifyTextRemoved(int index, int length)
	{
		markerRemove( index, length );
	}

	

	
	
	//
	//
	// INPUT EVENT HANDLING
	//
	//
	
	protected boolean handleBackspace(Caret caret)
	{
		if ( presentationArea.isSelectionValid() )
		{
			presentationArea.deleteSelection();
			return true;
		}
		else if ( isMarkerAtStart( caret.getMarker() ) )
		{
			DPContentLeaf left = getContentLeafToLeft();
			if ( left == null )
			{
				return false;
			}
			else
			{
				boolean bNonEditableContentCleared = false;
				while ( !left.isEditable() )
				{
					bNonEditableContentCleared |= left.clearText();
					left = left.getContentLeafToLeft();
					if ( left == null )
					{
						return false;
					}
				}
				left.moveMarkerToEnd( caret.getMarker() );
				if ( !bNonEditableContentCleared )
				{
					left.removeTextFromEnd( 1 );
				}
				return true;
			}
		}
		else
		{
			removeText( caret.getMarker().getClampedIndex() - 1, 1 );
			return true;
		}
	}
	
	protected boolean handleDelete(Caret caret)
	{
		if ( presentationArea.isSelectionValid() )
		{
			presentationArea.deleteSelection();
			return true;
		}
		else if ( isMarkerAtEnd( caret.getMarker() ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			if ( right == null )
			{
				return false;
			}
			else
			{
				boolean bNonEditableContentCleared = false;
				while ( !right.isEditable() )
				{
					bNonEditableContentCleared |= right.clearText();
					right = right.getContentLeafToRight();
					if ( right == null )
					{
						return false;
					}
				}
				right.moveMarkerToStart( caret.getMarker() );
				if ( !bNonEditableContentCleared )
				{
					right.removeTextFromStart( 1 );
				}
				return true;
			}
		}
		else
		{
			removeText( caret.getMarker(), 1 );
			return true;
		}
	}
	
	protected boolean onKeyPress(Caret caret, KeyEvent event)
	{
		if ( propagateKeyPress( event ) )
		{
			return true;
		}
		
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
		return propagateKeyRelease( event );
	}

	protected boolean onKeyTyped(Caret caret, KeyEvent event)
	{
		if ( propagateKeyTyped( event ) )
		{
			return true;
		}
		
		if ( event.getKeyChar() != KeyEvent.VK_BACK_SPACE  &&  event.getKeyChar() != KeyEvent.VK_DELETE )
		{
			String str = String.valueOf( event.getKeyChar() );
			if ( str.length() > 0 )
			{
				if ( presentationArea.isSelectionValid() )
				{
					presentationArea.replaceSelection( str );
				}
				else
				{
					insertText( caret.getMarker(), String.valueOf( event.getKeyChar() ) );
				}
				return true;
			}
		}

		return false;
	}
	
	
	


	//
	//
	// SELECTABILITY METHODS
	//
	//
	
	public void setSelectable()
	{
		setFlag( FLAG_SELECTABLE );
	}
	
	public void setUnselectable()
	{
		clearFlag( FLAG_SELECTABLE );
	}
	
	public boolean isSelectable()
	{
		return testFlag( FLAG_SELECTABLE );
	}
	
	
	
	
	
	
	
	//
	// TYPE METHODS
	//
	
	public boolean isEditable()
	{
		return true;
	}
}
