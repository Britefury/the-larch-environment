//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.LayoutTree.ContentLeafLayoutNodeInterface;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.Math.Point2;
import BritefuryJ.Parser.ItemStream.ItemStream;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public abstract class DPContentLeaf extends DPWidget
{
	public static class CannotCreateMarkerWithEmptyContent extends RuntimeException
	{
		static final long serialVersionUID = 0L;
		
		public CannotCreateMarkerWithEmptyContent()
		{
		}
	}
	
	

	
	protected String textRepresentation;
	
	
	
	DPContentLeaf(String textRepresentation)
	{
		this( ContentLeafStyleSheet.defaultStyleSheet, textRepresentation );
	}
	
	DPContentLeaf(ContentLeafStyleSheet styleSheet, String textRepresentation)
	{
		super( styleSheet );
		
		this.textRepresentation = textRepresentation;
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
		DPBorder border = getMetaHeaderBorderWidget(); 
		if ( border != null )
		{
			border.setBorder( metaHeaderHighlightBorder );
		}
	}
	
	protected void onCaretLeave(Caret c)
	{
		DPBorder border = getMetaHeaderBorderWidget(); 
		if ( border != null )
		{
			border.setBorder( metaHeaderEmptyBorder );
		}
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

	
	

	// MARKER MOVEMENT METHODS
	
	protected void moveMarkerLeft(Marker marker, boolean bSkipWhitespace)
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
		else
		{
			moveMarker( marker, marker.getIndex() - 1, Marker.Bias.START );
		}
	}



	protected void moveMarkerRight(Marker marker, boolean bSkipWhitespace)
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
		else
		{
			moveMarker( marker, marker.getIndex(), Marker.Bias.END );
		}
	}
	
	protected void moveMarkerUp(Marker marker, boolean bSkipWhitespace)
	{
		Point2 cursorPos = getMarkerPosition( marker );
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
		Point2 cursorPos = getMarkerPosition( marker );
		DPContentLeaf below = getContentLeafBelow( cursorPos, bSkipWhitespace );
		if ( below != null )
		{
			Point2 cursorPosInBelow = getLocalPointRelativeTo( below, cursorPos );
			int contentPos = below.getMarkerPositonForPoint( cursorPosInBelow );
			below.moveMarker( marker, contentPos, Marker.Bias.START );
		}
	}
	
	protected void moveMarkerHome(Marker marker)
	{
		DPSegment segment = null;
		DPContentLeaf homeElement = null;
		segment = getSegment();
		homeElement = segment != null  ?  segment.getFirstEditableEntryLeafInSubtree()  :  null;
		if ( segment != null  &&  this == homeElement  &&  isMarkerAtStart( marker ) )
		{
			segment = segment.getParent().getSegment();
			homeElement = segment != null  ?  segment.getFirstEditableEntryLeafInSubtree()  :  null;
		}
		
		if ( homeElement != null )
		{
			homeElement.moveMarkerToStart( marker );
		}
	}
	
	protected void moveMarkerEnd(Marker marker)
	{
		DPSegment segment = null;
		DPContentLeaf endElement = null;
		segment = getSegment();
		endElement = segment != null  ?  segment.getLastEditableEntryLeafInSubtree()  :  null;
		if ( segment != null  &&  this == endElement  &&  isMarkerAtEnd( marker ) )
		{
			segment = segment.getParent().getSegment();
			endElement = segment != null  ?  segment.getLastEditableEntryLeafInSubtree()  :  null;
		}
		
		if ( endElement != null )
		{
			endElement.moveMarkerToEnd( marker );
		}
	}
	
	
	

	
	
	//
	//
	// ELEMENT TREE STRUCTURE METHODS
	//
	//
	
	public DPContentLeaf getFirstLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( leafFilter == null  ||  leafFilter.testElement( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}

	public DPContentLeaf getLastLeafInSubtree(WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( leafFilter == null  ||  leafFilter.testElement( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}

	

	public DPContentLeaf getPreviousLeaf()
	{
		return getPreviousLeaf( null, null, null );
	}

	public DPContentLeaf getNextLeaf()
	{
		return getNextLeaf( null, null, null );
	}


	public DPContentLeaf getPreviousLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( parent != null )
		{
			return parent.getPreviousLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
		}
		else
		{
			return null;
		}
	}
	
	public DPContentLeaf getNextLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter, WidgetFilter leafFilter)
	{
		if ( parent != null )
		{
			return parent.getNextLeafFromChild( this, subtreeRootFilter, branchFilter, leafFilter );
		}
		else
		{
			return null;
		}
	}
	
	
	public DPContentLeaf getPreviousEditableLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditable.EditableLeafElementFilter() );
	}

	public DPContentLeaf getNextEditableLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditable.EditableLeafElementFilter() );
	}


	public DPContentLeaf getPreviousEditableLeaf()
	{
		return getPreviousEditableLeaf( null, null );
	}

	public DPContentLeaf getNextEditableLeaf()
	{
		return getNextEditableLeaf( null, null );
	}


	public DPContentLeaf getPreviousEditableEntryLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditableEntry.EditableEntryLeafElementFilter() );
	}

	public DPContentLeaf getNextEditableEntryLeaf(WidgetFilter subtreeRootFilter, WidgetFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditableEntry.EditableEntryLeafElementFilter() );
	}


	public DPContentLeaf getPreviousEditableEntryLeaf()
	{
		return getPreviousEditableEntryLeaf( null, null );
	}

	public DPContentLeaf getNextEditableEntryLeaf()
	{
		return getNextEditableEntryLeaf( null, null );
	}

	
	
	

	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public DPContentLeafEditable getEditableContentLeafToLeft()
	{
		DPContentLeaf leaf = getContentLeafToLeft();
		
		while ( leaf != null  &&  !leaf.isEditable() )
		{
			leaf = leaf.getContentLeafToLeft();
		}
		
		return (DPContentLeafEditable)leaf;
	}
	
	public DPContentLeafEditable getEditableContentLeafToRight()
	{
		DPContentLeaf leaf = getContentLeafToRight();
		
		while ( leaf != null  &&  !leaf.isEditable() )
		{
			leaf = leaf.getContentLeafToRight();
		}
		
		return (DPContentLeafEditable)leaf;
	}
	

	
	public DPContentLeafEditableEntry getEditableEntryContentLeafToLeft()
	{
		DPContentLeaf leaf = getContentLeafToLeft();
		
		while ( leaf != null  &&  !leaf.isEditableEntry() )
		{
			leaf = leaf.getContentLeafToLeft();
		}
		
		return (DPContentLeafEditableEntry)leaf;
	}
	
	public DPContentLeafEditableEntry getEditableEntryContentLeafToRight()
	{
		DPContentLeaf leaf = getContentLeafToRight();
		
		while ( leaf != null  &&  !leaf.isEditableEntry() )
		{
			leaf = leaf.getContentLeafToRight();
		}
		
		return (DPContentLeafEditableEntry)leaf;
	}
	
	
	
	protected DPContentLeaf getContentLeafAbove(Point2 localPos, boolean bSkipWhitespace)
	{
		ContentLeafLayoutNodeInterface leafLayout = (ContentLeafLayoutNodeInterface)getLayoutNode();
		return leafLayout.getContentLeafAbove( localPos, bSkipWhitespace );
	}
	
	protected DPContentLeaf getContentLeafBelow(Point2 localPos, boolean bSkipWhitespace)
	{
		ContentLeafLayoutNodeInterface leafLayout = (ContentLeafLayoutNodeInterface)getLayoutNode();
		return leafLayout.getContentLeafBelow( localPos, bSkipWhitespace );
	}
	
	
	public DPContentLeaf getLeftContentLeaf()
	{
		return this;
	}

	public DPContentLeaf getRightContentLeaf()
	{
		return this;
	}

	public DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
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
	// TEXT REPRESENTATION METHODS
	//
	//
	
	public String getTextRepresentation()
	{
		return textRepresentation;
	}
	
	public int getTextRepresentationLength()
	{
		return textRepresentation.length();
	}
	
	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		builder.append( textRepresentation.substring( 0, marker.getClampedIndex() ) );
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
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

	protected void getTextRepresentationFromStartOfRootToMarker(StringBuilder builder, Marker marker, DPWidget root)
	{
		if ( this != root  &&  parent != null )
		{
			parent.getTextRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		builder.append( textRepresentation.substring( 0, marker.getClampedIndex() ) );
	}
	
	protected void getTextRepresentationFromMarkerToEndOfRoot(StringBuilder builder, Marker marker, DPWidget root)
	{
		builder.append( textRepresentation.substring( marker.getClampedIndex() ) );
		if ( this != root  &&  parent != null )
		{
			parent.getTextRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
		}
	}
	
	public DPContentLeaf getLeafAtTextRepresentationPosition(int position)
	{
		return this;
	}

	protected void onTextRepresentationModified()
	{
		super.onTextRepresentationModified();
		
		refreshMetaHeader();
	}
	
	
	
	
	
	
	
	//
	//
	// LINEAR REPRESENTATION METHODS
	//
	//
	
	protected void buildLinearRepresentation(ItemStreamBuilder builder)
	{
		builder.appendTextValue( textRepresentation );
	}
	
	protected void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
	{
		builder.appendTextValue( textRepresentation.substring( 0, marker.getClampedIndex() ) );
	}

	protected void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPWidget> path, int pathMyIndex)
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

	protected void getLinearRepresentationFromStartOfRootToMarker(ItemStreamBuilder builder, Marker marker, DPWidget root)
	{
		if ( this != root  &&  parent != null )
		{
			parent.getLinearRepresentationFromStartOfRootToMarkerFromChild( builder, marker, root, this );
		}
		builder.appendTextValue( textRepresentation.substring( 0, marker.getClampedIndex() ) );
	}
	
	protected void getLinearRepresentationFromMarkerToEndOfRoot(ItemStreamBuilder builder, Marker marker, DPWidget root)
	{
		builder.appendTextValue( textRepresentation.substring( marker.getClampedIndex() ) );
		if ( this != root  &&  parent != null )
		{
			parent.getLinearRepresentationFromMarkerToEndOfRootFromChild( builder, marker, root, this );
		}
	}
	
	
	
	
	
	
	
	//
	// Meta element methods
	//
	
	protected static TextStyleSheet headerTextRepTextStyle = new TextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), Color.BLACK );
	protected static SolidBorder metaHeaderHighlightBorder = new SolidBorder( 1.0, 1.0, 5.0, 5.0, new Color( 0.75f, 0.0f, 0.0f ), new Color( 1.0f, 0.9f, 0.8f ) );

	public DPWidget createMetaHeaderData()
	{
		return new DPText( headerTextRepTextStyle, "'" + textRepresentation.replace( "\n", "\\n" ) + "'" );
	}
	
	protected Border getMetaHeaderBorder()
	{
		Caret caret = presentationArea.getCaret();
		if ( caret != null )
		{
			DPContentLeaf e = caret.getMarker().getElement();
			if ( e == this )
			{
				return metaHeaderHighlightBorder;
			}
		}
		return metaHeaderEmptyBorder;
	}
	


	
	
	//
	// TYPE METHODS
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
	
	public boolean isEditableEntry()
	{
		return false;
	}
}
