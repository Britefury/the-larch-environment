//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPContentLeaf;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.ElementTree.Caret.ElementCaret;
import BritefuryJ.DocPresent.ElementTree.Marker.ElementMarker;

public abstract class LeafElement extends Element
{
	//
	// Utility classes
	//
	
	public static class LeafFilterEditable implements ElementFilter
	{
		public boolean test(Element element)
		{
			return ((LeafElement)element).isEditable();
		}
	}
	
	public static class LeafFilterEditableEntry implements ElementFilter
	{
		public boolean test(Element element)
		{
			return ((LeafElement)element).isEditableEntry();
		}
	}
	
	
	
	
	//
	// Fields
	//
	
	protected String textRepresentation;
	
	
	
	
	
	//
	// Constructor
	//
	
	protected LeafElement(DPContentLeaf widget, String textRepresentation)
	{
		super( widget );
		
		this.textRepresentation = textRepresentation;
	}
	
	
	
	//
	// Widget
	//
	
	public DPContentLeaf getWidget()
	{
		return (DPContentLeaf)widget;
	}

	



	//
	// Element tree structure methods
	//
	
	public List<LeafElement> getLeavesInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		ArrayList<LeafElement> leaves = new ArrayList<LeafElement>();
		if ( leafFilter == null  ||  leafFilter.test( this ) )
		{
			leaves.add( this );
		}
		return leaves;
	}
	
	public LeafElement getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( leafFilter == null  ||  leafFilter.test( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}

	public LeafElement getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
	{
		if ( leafFilter == null  ||  leafFilter.test( this ) )
		{
			return this;
		}
		else
		{
			return null;
		}
	}
	
	
	public LeafElement getLeafAtTextRepresentationPosition(int position)
	{
		return this;
	}


	public LeafElement getPreviousLeaf()
	{
		return getPreviousLeaf( null, null, null );
	}

	public LeafElement getPreviousLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
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
	
	public LeafElement getNextLeaf()
	{
		return getNextLeaf( null, null, null );
	}

	public LeafElement getNextLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
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
	
	public LeafElement getPreviousEditableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new LeafFilterEditable() );
	}

	public LeafElement getPreviousEditableEntryLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new LeafFilterEditableEntry() );
	}

	public LeafElement getPreviousEditableLeaf()
	{
		return getPreviousEditableLeaf( null, null );
	}

	public LeafElement getNextEditableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new LeafFilterEditable() );
	}

	public LeafElement getNextEditableEntryLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new LeafFilterEditableEntry() );
	}

	public LeafElement getNextEditableLeaf()
	{
		return getNextEditableLeaf( null, null );
	}

	
	
	
	//
	// Text representation methods
	//
	
	public String getTextRepresentation()
	{
		return textRepresentation;
	}
	
	public int getTextRepresentationLength()
	{
		return textRepresentation.length();
	}
	
	protected void getTextRepresentationFromStartToPath(StringBuilder builder, ElementMarker marker, ArrayList<Element> path, int pathMyIndex)
	{
		builder.append( textRepresentation.substring( 0, marker.getIndex() ) );
	}

	protected void getTextRepresentationFromPathToEnd(StringBuilder builder, ElementMarker marker, ArrayList<Element> path, int pathMyIndex)
	{
		builder.append( textRepresentation.substring( marker.getIndex() ) );
	}



	

	
	
	//
	// Caret methods
	//
	
	public void onCaretEnter()
	{
		DPBorder border = getMetaHeaderBorderWidget(); 
		if ( border != null )
		{
			border.setBorder( metaHeaderHighlightBorder );
		}
	}
	
	public void onCaretLeave()
	{
		DPBorder border = getMetaHeaderBorderWidget(); 
		if ( border != null )
		{
			border.setBorder( metaHeaderEmptyBorder );
		}
	}

	
	
	//
	// Meta element methods
	//
	
	protected static SolidBorder metaHeaderHighlightBorder = new SolidBorder( 1.0, 5.0, 5.0, new Color( 0.75f, 0.0f, 0.0f ), new Color( 1.0f, 0.9f, 0.8f ) );

	protected Border getMetaHeaderBorder()
	{
		ElementCaret caret = tree.getCaret();
		if ( caret != null )
		{
			Element e = caret.getMarker().getElement();
			if ( e == this )
			{
				return metaHeaderHighlightBorder;
			}
		}
		return metaHeaderEmptyBorder;
	}
	

	
	
	//
	// Type methods
	//


	public boolean isWhitespace()
	{
		return getWidget().isWhitespace();
	}
	
	public boolean isEditable()
	{
		return getWidget().isEditable();
	}
	
	public boolean isEditableEntry()
	{
		return getWidget().isEditableEntry();
	}
}
