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
import BritefuryJ.DocPresent.StyleParams.ContentLeafEditableStyleParams;
import BritefuryJ.Math.Point2;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public abstract class DPContentLeafEditable extends DPContentLeaf
{
	public static class EditableLeafElementFilter implements ElementFilter
	{
		public boolean testElement(DPElement element)
		{
			if ( element instanceof DPContentLeafEditable )
			{
				return ((DPContentLeafEditable)element).isEditable();
			}
			else
			{
				return false;
			}
		}
	}
	
	
	
	public static final int FLAG_EDITABLE = FLAGS_CONTENTLEAF_END * 0x1;
	
	
	protected final static int FLAGS_CONTENTLEAFEDITABLE_END = FLAGS_CONTENTLEAF_END;

	
	
	//
	// Constructors
	//
	
	
	protected DPContentLeafEditable(String textRepresentation)
	{
		this( ContentLeafEditableStyleParams.defaultStyleParams, textRepresentation );
	}
	
	protected DPContentLeafEditable(ContentLeafEditableStyleParams styleParams, String textRepresentation)
	{
		super(styleParams, textRepresentation );
		
		setFlagValue( FLAG_EDITABLE, styleParams.getEditable() );
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
		
		onDebugPresentationStateChanged();
	}
	
	protected void onCaretLeave(Caret c)
	{
		super.onCaretLeave( c );
		
		onDebugPresentationStateChanged();
	}
	
	
	public void grabCaret()
	{
		if ( isRealised() )
		{
			getRootElement().caretGrab( this );
		}
	}
	
	public void ungrabCaret()
	{
		if ( isRealised() )
		{
			getRootElement().caretUngrab( this );
		}
	}
	
	
	
	
	
	
	//
	//
	// SELECTION METHODS
	//
	//
	
	public abstract void drawSelection(Graphics2D graphics, Marker from, Marker to);
	

	protected void drawSubtreeSelection(Graphics2D graphics, Marker startMarker, List<DPElement> startPath, Marker endMarker, List<DPElement> endPath)
	{
		drawSelection( graphics, startMarker, endMarker );
	}
	
	

	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	

	public DPContentLeafEditable getLeftEditableContentLeaf()
	{
		return this;
	}

	public DPContentLeafEditable getRightEditableContentLeaf()
	{
		return this;
	}
	
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
		if ( rootElement != null )
		{
			return rootElement.markersByLeaf.get( this );
		}
		else
		{
			return null;
		}
	}
	
	protected WeakHashMap<Marker, Object> getValidMarkersForLeaf()
	{
		if ( rootElement != null )
		{
			WeakHashMap<Marker, Object> markers = rootElement.markersByLeaf.get( this );
			
			if ( markers == null )
			{
				markers = new WeakHashMap<Marker, Object>(); 
				rootElement.markersByLeaf.put( this, markers );
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
		rootElement.markersByLeaf.remove( this );
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

	protected void onUnrealise(DPElement unrealiseRoot)
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
	// TEXT REPRESENTATION METHODS
	//
	//
	
	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		builder.append( textRepresentation.substring( 0, marker.getClampedIndex() ) );
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		builder.append( textRepresentation.substring( marker.getClampedIndex() ) );
	}

	public String getTextRepresentationBetweenMarkers(Marker startMarker, Marker endMarker)
	{
		if ( startMarker.getElement() != this  ||  endMarker.getElement() != this )
		{
			throw new RuntimeException();
		}
		return textRepresentation.substring( startMarker.getClampedIndex(), endMarker.getClampedIndex() );
	}

	protected void getTextRepresentationFromStartOfRootToMarker(StringBuilder builder, Marker marker, DPElement root)
	{
		if ( this != root  &&  parent != null )
		{
			parent.getTextRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		builder.append( textRepresentation.substring( 0, marker.getClampedIndex() ) );
	}
	
	protected void getTextRepresentationFromMarkerToEndOfRoot(StringBuilder builder, Marker marker, DPElement root)
	{
		builder.append( textRepresentation.substring( marker.getClampedIndex() ) );
		if ( this != root  &&  parent != null )
		{
			parent.getTextRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
		}
	}
	
	
	
	
	
	
	//
	//
	// LINEAR REPRESENTATION METHODS
	//
	//
	
	protected void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		builder.appendTextValue( textRepresentation.substring( 0, marker.getClampedIndex() ) );
	}

	protected void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
		builder.appendTextValue( textRepresentation.substring( marker.getClampedIndex() ) );
	}

	public ItemStream getLinearRepresentationBetweenMarkers(Marker startMarker, Marker endMarker)
	{
		if ( startMarker.getElement() != this  ||  endMarker.getElement() != this )
		{
			throw new RuntimeException();
		}
		ItemStreamBuilder builder = new ItemStreamBuilder();
		builder.appendTextValue( textRepresentation.substring( startMarker.getClampedIndex(), endMarker.getClampedIndex() ) );
		return builder.stream();
	}

	protected void getLinearRepresentationFromStartOfRootToMarker(ItemStreamBuilder builder, Marker marker, DPElement root)
	{
		if ( this != root  &&  parent != null )
		{
			parent.getLinearRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		builder.appendTextValue( textRepresentation.substring( 0, marker.getClampedIndex() ) );
	}
	
	protected void getLinearRepresentationFromMarkerToEndOfRoot(ItemStreamBuilder builder, Marker marker, DPElement root)
	{
		builder.appendTextValue( textRepresentation.substring( marker.getClampedIndex() ) );
		if ( this != root  &&  parent != null )
		{
			parent.getLinearRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
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
	
	public void removeText(Marker start, Marker end)
	{
		removeText( start.getIndex(), end.getIndex() - start.getIndex() );
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
		if ( rootElement.isSelectionValid() )
		{
			rootElement.deleteSelection();
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
			if ( isEditable() )
			{
				removeText( caret.getMarker().getClampedIndex() - 1, 1 );
			}
			return true;
		}
	}
	
	protected boolean handleDelete(Caret caret)
	{
		if ( rootElement.isSelectionValid() )
		{
			rootElement.deleteSelection();
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
			if ( isEditable() )
			{
				removeText( caret.getMarker(), 1 );
			}
			return true;
		}
	}
	
	protected boolean onContentKeyPress(Caret caret, KeyEvent event)
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

	protected boolean onContentKeyRelease(Caret caret, KeyEvent event)
	{
		return false;
	}

	protected boolean onContentKeyTyped(Caret caret, KeyEvent event)
	{
		if ( event.getKeyChar() != KeyEvent.VK_BACK_SPACE  &&  event.getKeyChar() != KeyEvent.VK_DELETE )
		{
			String str = String.valueOf( event.getKeyChar() );
			if ( str.length() > 0 )
			{
				if ( rootElement.isSelectionValid() )
				{
					rootElement.replaceSelection( str );
				}
				else
				{
					if ( isEditable() )
					{
						insertText( caret.getMarker(), String.valueOf( event.getKeyChar() ) );
					}
				}
				return true;
			}
		}

		return false;
	}
	
	
	


	//
	//
	// EDITABILITY METHODS
	//
	//
	
	public void setEditable()
	{
		setFlag( FLAG_EDITABLE );
	}
	
	public void setNonEditable()
	{
		clearFlag( FLAG_EDITABLE );
	}
	
	public boolean isEditable()
	{
		return testFlag( FLAG_EDITABLE );
	}
}
