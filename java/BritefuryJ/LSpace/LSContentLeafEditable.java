//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.LSpace.Marker.Marker;
import BritefuryJ.LSpace.StyleParams.ContentLeafEditableStyleParams;
import BritefuryJ.LSpace.TextFocus.Caret;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.Util.WeakIdentityHashMap;

public abstract class LSContentLeafEditable extends LSContentLeaf
{
	public static final ElementFilter editableLeafElementFilter = new ElementFilter()
	{
		public boolean testElement(LSElement element)
		{
			if ( element instanceof LSContentLeafEditable )
			{
				return ((LSContentLeafEditable)element).isEditable();
			}
			else
			{
				return false;
			}
		}
	};
	
	public static final ElementFilter selectableLeafElementFilter = new ElementFilter()
	{
		public boolean testElement(LSElement element)
		{
			if ( element instanceof LSContentLeafEditable )
			{
				return ((LSContentLeafEditable)element).isSelectable();
			}
			else
			{
				return false;
			}
		}
	};

	
	public static final ElementFilter editableRealisedLeafElementFilter = new ElementFilter()
	{
		public boolean testElement(LSElement element)
		{
			if ( element.isRealised()  &&  element instanceof LSContentLeafEditable )
			{
				return ((LSContentLeafEditable)element).isEditable();
			}
			else
			{
				return false;
			}
		}
	};
	
	public static final ElementFilter selectableRealisedLeafElementFilter = new ElementFilter()
	{
		public boolean testElement(LSElement element)
		{
			if ( element.isRealised()  &&  element instanceof LSContentLeafEditable )
			{
				return ((LSContentLeafEditable)element).isSelectable();
			}
			else
			{
				return false;
			}
		}
	};

	
	
	
	public static final int FLAG_EDITABLE = FLAGS_CONTENTLEAF_END * 0x1;
	public static final int FLAG_SELECTABLE = FLAGS_CONTENTLEAF_END * 0x2;
	public static final int FLAG_HAS_MARKERS = FLAGS_CONTENTLEAF_END * 0x4;
	
	
	protected final static int FLAGS_CONTENTLEAFEDITABLE_END = FLAGS_CONTENTLEAF_END * 0x8;

	
	
	//
	// Constructors
	//
	
	
	protected LSContentLeafEditable(String textRepresentation)
	{
		this( ContentLeafEditableStyleParams.defaultStyleParams, textRepresentation );
	}
	
	protected LSContentLeafEditable(ContentLeafEditableStyleParams styleParams, String textRepresentation)
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
	

	public LSContentLeafEditable getLeftEditableContentLeaf()
	{
		return this;
	}

	public LSContentLeafEditable getRightEditableContentLeaf()
	{
		return this;
	}
	
	public LSContentLeafEditable getTopOrBottomEditableContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace)
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

	protected WeakIdentityHashMap<Marker, Object> getMarkersForLeaf()
	{
		if ( rootElement != null  &&  testFlag( FLAG_HAS_MARKERS ) )
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
			WeakIdentityHashMap<Marker, Object> markers = null;
			
			if ( testFlag( FLAG_HAS_MARKERS ) )
			{
				markers = rootElement.markersByLeaf.get( this );
			}
			
			if ( markers == null )
			{
				markers = new WeakIdentityHashMap<Marker, Object>(); 
				rootElement.markersByLeaf.put( this, markers );
				setFlag( FLAG_HAS_MARKERS );
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
		clearFlag( FLAG_HAS_MARKERS );
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
	
	
	
	
	private void markerInsertText(int position, int length)
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
	
	private void markerRemoveText(int position, int length)
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
	
	public static ElementFilter editableRealisedFilter = new ElementFilter()
	{
		@Override
		public boolean testElement(LSElement element)
		{
			return element.isRealised()  &&  element instanceof LSContentLeafEditable;
		}
	};
	
	protected void onUnrealise(LSElement unrealiseRoot)
	{
		super.onUnrealise( unrealiseRoot );
		
		WeakIdentityHashMap<Marker, Object> markers = getMarkersForLeaf();

		if ( markers != null )
		{
			ArrayList<Marker> xs = new ArrayList<Marker>( markers.keySet() );
			
			if ( xs.size() > 0 )
			{
				LSContentLeafEditable left = (LSContentLeafEditable)TreeTraversal.previousElement( unrealiseRoot, null, internalBranchChildrenFn, editableRealisedFilter );
				
				if ( left != null )
				{
					for (Marker x: xs)
					{
						try
						{
							x.moveToEndOfLeaf( left );
						}
						catch (Marker.InvalidMarkerPosition e)
						{
						}
					}
				}
				else
				{
					LSContentLeafEditable right = (LSContentLeafEditable)TreeTraversal.nextElement( unrealiseRoot, null, internalBranchChildrenFn, editableRealisedFilter );
					
					if ( right != null )
					{
						for (Marker x: xs)
						{
							try
							{
								x.moveToStartOfLeaf( right );
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
		markerInsertText( index, length );
	}

	protected void notifyTextRemoved(int index, int length)
	{
		markerRemoveText( index, length );
	}
	
	protected void notifyTextReplaced(int index, int originalLength, int newLength)
	{
		if ( newLength > originalLength )
		{
			markerInsertText( index + originalLength, newLength - originalLength );
		}
		else if ( newLength < originalLength )
		{
			markerRemoveText( index + newLength, originalLength - newLength );
		}
	}

	

	
	
	//
	//
	// INPUT EVENT HANDLING
	//
	//
	
	public boolean onContentKeyPress(Caret caret, KeyEvent event)
	{
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
			LSContentLeaf e = caret.getElement();
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
			LSContentLeaf e = caret.getElement();
			if ( e == this )
			{
				return metaHeaderHighlightBorderStyle;
			}
		}
		return metaHeaderEmptyBorderStyle;
	}
}
