//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ContentLeafEditableStyleParams;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.WeakIdentityHashMap;

public abstract class DPContentLeafEditable extends DPContentLeaf
{
	public static final int FLAG_EDITABLE = FLAGS_CONTENTLEAF_END * 0x1;
	public static final int FLAG_SELECTABLE = FLAGS_CONTENTLEAF_END * 0x2;
	
	
	protected final static int FLAGS_CONTENTLEAFEDITABLE_END = FLAGS_CONTENTLEAF_END * 0x4;

	
	
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
		setFlagValue( FLAG_SELECTABLE, styleParams.getSelectable() );
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
	// TEXT SELECTION METHODS
	//
	//
	
	public abstract void drawTextSelection(Graphics2D graphics, int startIndex, int endIndex);
	

	

	
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
		if ( !isRealised() )
		{
			throw new RuntimeException( "Cannot create a marker within unrealised element " + this );
		}
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
		// We must ensure that an element with no content CANNOT have a marker given a bias of END, otherwise
		// empty text elements will consume a backspace character.
		int range = getMarkerRange();
		if ( range == 0 )
		{
			return marker( 0, Marker.Bias.START );
		}
		else
		{
			return marker( Math.max( range - 1, 0 ), Marker.Bias.END );
		}
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
	
	@Override
	public void moveMarkerToStart(Marker m)
	{
		moveMarker( m, 0, Marker.Bias.START );
	}
	
	public void moveMarkerToStartPlusOne(Marker m)
	{
		moveMarker( m, Math.min( 1, getMarkerRange() ), Marker.Bias.START );
	}
	
	@Override
	public void moveMarkerToEnd(Marker m)
	{
		// We must ensure that an element with no content CANNOT have a marker given a bias of END, otherwise
		// empty text elements will consume a backspace character.
		int range = getMarkerRange();
		if ( range == 0 )
		{
			moveMarker( m, 0, Marker.Bias.START );
		}
		else
		{
			moveMarker( m, Math.max( range - 1, 0 ), Marker.Bias.END );
		}
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
	
	
	
	
	
	protected WeakIdentityHashMap<Marker, Object> getMarkersForLeaf()
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
	
	protected WeakIdentityHashMap<Marker, Object> getValidMarkersForLeaf()
	{
		if ( rootElement != null )
		{
			WeakIdentityHashMap<Marker, Object> markers = rootElement.markersByLeaf.get( this );
			
			if ( markers == null )
			{
				markers = new WeakIdentityHashMap<Marker, Object>(); 
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
		WeakIdentityHashMap<Marker, Object> markers = getValidMarkersForLeaf();
		markers.put( m, null );
	}
	
	public void unregisterMarker(Marker m)
	{
		WeakIdentityHashMap<Marker, Object> markers = getMarkersForLeaf();
		if ( markers != null )
		{
			markers.remove( m );
		
			if ( markers.isEmpty() )
			{
				removeMarkersForLeaf();
			}
		}
	}
	
	
	
	
	private void markerInsert(int position, int length)
	{
		WeakIdentityHashMap<Marker, Object> markers = getMarkersForLeaf();
		if ( markers != null )
		{
			ArrayList<Marker> markersToMove = new ArrayList<Marker>();
			markersToMove.addAll( markers.keySet() );
			for (Marker m: markersToMove)
			{
				if ( m.isValid() )
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
	}
	
	private void markerRemove(int position, int length)
	{
		WeakIdentityHashMap<Marker, Object> markers = getMarkersForLeaf();
		if ( markers != null )
		{
			int end = position + length;
	
			ArrayList<Marker> markersToMove = new ArrayList<Marker>();
			markersToMove.addAll( markers.keySet() );
			for (Marker m: markersToMove)
			{
				if ( m.isValid() )
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
	}

	
	


	
	
	//
	//
	// REALISE / UNREALISE
	//
	//

	protected void onUnrealise(DPElement unrealiseRoot)
	{
		super.onUnrealise( unrealiseRoot );
		
		WeakIdentityHashMap<Marker, Object> markers = getMarkersForLeaf();

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
	
	public void setLeafTextRepresentation(String newTextRepresentation)
	{
		if ( newTextRepresentation == null )
		{
			throw new RuntimeException( "Text representation cannot be null" );
		}
		
		String oldText = textRepresentation;
		int oldLength = oldText.length();
		int newLength = newTextRepresentation.length();
		
		textRepresentation = newTextRepresentation;

		notifyTextReplaced( 0, oldLength, newLength );
		
		textRepresentationChanged( new TextEditEventReplace( this, getPreviousEditableLeaf(), getNextEditableLeaf(), 0, oldText, newTextRepresentation ) );
	}
	
	
	// Insert text at marker
	public void insertText(Marker marker, String x)
	{
		insertText( marker.getClampedIndex(), x );
	}

	
	// Remove text at marker
	public void removeText(Marker marker, int length)
	{
		removeText( marker.getClampedIndex(), length );
	}
	
	// Remove text between markers
	public void removeText(Marker start, Marker end)
	{
		removeText( start.getClampedIndex(), end.getClampedIndex() - start.getIndex() );
	}
	
	// Replace range of text at marker
	public void replaceText(Marker marker, int length, String x)
	{
		int index = marker.getClampedIndex();
		length = Math.min( length, getTextRepresentationLength() - index );
		replaceText( index, length, x );
	}
	
	// Replace range of text between markers
	public void replaceText(Marker startMarker, Marker endMarker, String x)
	{
		int start = startMarker.getClampedIndex();
		int end = endMarker.getClampedIndex();
		replaceText( start, end - start, x );
	}

	
	protected void notifyTextInserted(int index, int length)
	{
		markerInsert( index, length );
	}

	protected void notifyTextRemoved(int index, int length)
	{
		markerRemove( index, length );
	}
	
	protected void notifyTextReplaced(int index, int originalLength, int newLength)
	{
		if ( newLength > originalLength )
		{
			markerInsert( index + originalLength, newLength - originalLength );
		}
		else if ( newLength < originalLength )
		{
			markerRemove( index + newLength, originalLength - newLength );
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
				boolean bNonEditableContentCleared = false;
				while ( !left.isEditable()  ||  left.getTextRepresentationLength() == 0 )
				{
					bNonEditableContentCleared |= left.deleteText();
					if ( !isRealised() )
					{
						// Bail out if a response to the deletion of the text is this element becoming unrealised
						return true;
					}
					left = left.getContentLeafToLeft();
					if ( left == null )
					{
						return false;
					}
				}
				if ( caret.moveTo( left.markerAtEnd() ) )
				{
					if ( !bNonEditableContentCleared )
					{
						left.removeTextFromEnd( 1 );
					}
					caret.makeCurrentTarget();
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
		if ( isMarkerAtEnd( caret.getMarker() ) )
		{
			DPContentLeaf right = getContentLeafToRight();
			if ( right == null )
			{
				return false;
			}
			else
			{
				boolean bNonEditableContentCleared = false;
				while ( !right.isEditable()  ||  right.getTextRepresentationLength() == 0 )
				{
					bNonEditableContentCleared |= right.deleteText();
					if ( !isRealised() )
					{
						// Bail out if a response to the deletion of the text is this element becoming unrealised
						return true;
					}
					right = right.getContentLeafToRight();
					if ( right == null )
					{
						return false;
					}
				}
				if ( caret.moveTo( right.markerAtStart() ) )
				{
					if ( !bNonEditableContentCleared )
					{
						right.removeTextFromStart( 1 );
					}
					caret.makeCurrentTarget();
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
	
	public boolean onContentKeyPress(Caret caret, KeyEvent event)
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

	public boolean onContentKeyRelease(Caret caret, KeyEvent event)
	{
		return false;
	}

	public boolean onContentKeyTyped(Caret caret, KeyEvent event)
	{
		if ( !Character.isISOControl( event.getKeyChar() )  ||  event.getKeyChar() == '\n' )
		{
			String str = String.valueOf( event.getKeyChar() );
			if ( str.length() > 0 )
			{
				if ( isEditable() )
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
	// Meta element methods
	//

	protected static StyleSheet metaHeaderHighlightBorderStyle = StyleSheet.style( Primitive.border.as( new SolidBorder( 1.0, 1.0, 5.0, 5.0, new Color( 0.75f, 0.0f, 0.0f ), new Color( 1.0f, 0.9f, 0.8f ) ) ) );
	protected static StyleSheet metaHeaderCaretPosStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.25f, 0.0f, 0.5f ) ) );

	protected void createDebugPresentationHeaderContents(ArrayList<Object> elements)
	{
		super.createDebugPresentationHeaderContents( elements );
		Caret caret = rootElement != null  ?  rootElement.getCaret()  :  null;
		if ( caret != null )
		{
			DPContentLeaf e = caret.getElement();
			if ( e == this )
			{
				elements.add( metaHeaderCaretPosStyle.applyTo( new Label( "@[" + caret.getPosition() + ":" + caret.getBias() + "]" ) ) );
			}
		}
	}

	protected StyleSheet getDebugPresentationHeaderBorderStyle()
	{
		Caret caret = rootElement != null  ?  rootElement.getCaret()  :  null;
		if ( caret != null )
		{
			DPContentLeaf e = caret.getElement();
			if ( e == this )
			{
				return metaHeaderHighlightBorderStyle;
			}
		}
		return metaHeaderEmptyBorderStyle;
	}
}
