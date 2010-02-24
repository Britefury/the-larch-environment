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
import java.util.ArrayList;

import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.LayoutTree.ContentLeafLayoutNodeInterface;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;
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
	
	
	
	protected final static int FLAGS_CONTENTLEAF_END = FLAGS_ELEMENT_END;
	

	
	protected String textRepresentation;
	
	
	
	DPContentLeaf(String textRepresentation)
	{
		this( ContentLeafStyleParams.defaultStyleParams, textRepresentation );
	}
	
	DPContentLeaf(ContentLeafStyleParams styleParams, String textRepresentation)
	{
		super(styleParams);
		
		this.textRepresentation = textRepresentation;
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

	
	
	

	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public DPContentLeafEditable getEditableContentLeafAbove(Point2 localPos)
	{
		ContentLeafLayoutNodeInterface leafLayout = (ContentLeafLayoutNodeInterface)getLayoutNode();
		return leafLayout.getEditableContentLeafAbove( localPos );
	}
	
	public DPContentLeafEditable getEditableContentLeafBelow(Point2 localPos)
	{
		ContentLeafLayoutNodeInterface leafLayout = (ContentLeafLayoutNodeInterface)getLayoutNode();
		return leafLayout.getEditableContentLeafBelow( localPos );
	}
	
	
	public DPContentLeaf getLeftContentLeaf()
	{
		return this;
	}

	public DPContentLeaf getRightContentLeaf()
	{
		return this;
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
	//
	// TEXT REPRESENTATION MODIFICATION METHODS
	//
	//
	
	public void removeText(int index, int length)
	{
		index = Math.min( Math.max( index, 0 ), textRepresentation.length() );
		length = Math.min( length, getTextRepresentationLength() - index );
		textRepresentation = textRepresentation.substring( 0, index ) + textRepresentation.substring( index + length );
		notifyTextRemoved( index, length );
		textRepresentationChanged( new LinearRepresentationEventTextRemove( this, index, length ) );
	}
	
	public void removeTextFromStart(int length)
	{
		removeText( 0, length );
	}
	
	public void removeTextFromEnd(int length)
	{
		length = Math.min( length, getTextRepresentationLength() );
		removeText( getTextRepresentationLength() - length, length );
	}

	
	public boolean clearText()
	{
		int length = textRepresentation.length();
		if ( length > 0 )
		{
			textRepresentation = "";
			notifyTextRemoved( 9, length );
			textRepresentationChanged( new LinearRepresentationEventTextRemove( this, 0, length ) );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	protected void notifyTextRemoved(int index, int length)
	{
	}

	
	
	
	//
	// Meta element methods
	//
	
	protected static TextStyleParams headerTextRepTextStyle = new TextStyleParams( null, new Font( "Sans serif", Font.PLAIN, 14 ), Color.BLACK, null, false );
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

	public boolean isEditable()
	{
		return false;
	}
}
