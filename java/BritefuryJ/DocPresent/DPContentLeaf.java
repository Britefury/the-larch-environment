//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.LayoutTree.ContentLeafLayoutNodeInterface;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.GSym.DefaultPerspective.DefaultPerspectiveStyleSheet;
import BritefuryJ.Math.Point2;
import BritefuryJ.Parser.ItemStream.ItemStreamBuilder;

public abstract class DPContentLeaf extends DPElement
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
	
	protected DPContentLeaf(DPContentLeaf element)
	{
		super( element );
		
		this.textRepresentation = element.textRepresentation;
	}
	
	
	
	
	//
	//
	// ELEMENT TREE STRUCTURE METHODS
	//
	//
	
	public DPContentLeaf getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
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

	public DPContentLeaf getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
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


	public DPContentLeaf getPreviousLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
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
	
	public DPContentLeaf getNextLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
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
	
	
	public DPContentLeaf getPreviousEditableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditable.EditableLeafElementFilter() );
	}

	public DPContentLeaf getNextEditableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
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
	
	protected void getTextRepresentationFromStartToPath(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
	}

	public DPContentLeaf getLeafAtTextRepresentationPosition(int position)
	{
		return this;
	}

	protected void onTextRepresentationModified()
	{
		super.onTextRepresentationModified();
		
		onDebugPresentationStateChanged();
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
	
	protected void getLinearRepresentationFromStartToPath(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
	}

	protected void getLinearRepresentationFromPathToEnd(ItemStreamBuilder builder, Marker marker, ArrayList<DPElement> path, int pathMyIndex)
	{
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
	
	protected static PrimitiveStyleSheet metaHeaderHighlightBorderStyle = PrimitiveStyleSheet.instance.withBorder(
			new SolidBorder( 1.0, 1.0, 5.0, 5.0, new Color( 0.75f, 0.0f, 0.0f ), new Color( 1.0f, 0.9f, 0.8f ) ) );

	public DPElement createDebugPresentationHeaderData()
	{
		return DefaultPerspectiveStyleSheet.instance.unescapedStringAsHBox( textRepresentation );
	}
	
	protected PrimitiveStyleSheet getDebugPresentationHeaderBorderStyle()
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
	


	
	
	//
	// TYPE METHODS
	//

	public boolean isEditable()
	{
		return false;
	}
}
