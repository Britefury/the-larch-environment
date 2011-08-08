//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;

import BritefuryJ.DocPresent.LayoutTree.ContentLeafLayoutNodeInterface;
import BritefuryJ.DocPresent.StreamValue.StreamValueBuilder;
import BritefuryJ.DocPresent.StyleParams.ContentLeafStyleParams;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.ObjectPres.UnescapedStringAsRow;

public abstract class DPContentLeaf extends DPElement
{
	public static class EditableLeafElementFilter implements ElementFilter
	{
		public boolean testElement(DPElement element)
		{
			if ( element instanceof DPContentLeaf )
			{
				return ((DPContentLeaf)element).isEditable();
			}
			else
			{
				return false;
			}
		}
	}
	
	public static class SelectableLeafElementFilter implements ElementFilter
	{
		public boolean testElement(DPElement element)
		{
			if ( element instanceof DPContentLeaf )
			{
				return ((DPContentLeaf)element).isSelectable();
			}
			else
			{
				return false;
			}
		}
	}
	
	
	
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
		
		if ( textRepresentation == null )
		{
			throw new RuntimeException( "Text representation cannot be null" );
		}
		
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

	
	public DPContentLeaf getPreviousSelectableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditable.SelectableLeafElementFilter() );
	}

	public DPContentLeaf getNextSelectableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new DPContentLeafEditable.SelectableLeafElementFilter() );
	}


	public DPContentLeaf getPreviousSelectableLeaf()
	{
		return getPreviousSelectableLeaf( null, null );
	}

	public DPContentLeaf getNextSelectableLeaf()
	{
		return getNextSelectableLeaf( null, null );
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
	
	public String getLeafTextRepresentation()
	{
		return textRepresentation;
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
	// VALUE METHODS
	//
	//
	
	public Object getDefaultValue()
	{
		return textRepresentation;
	}
	
	
	
	
	// Stream value computation
	
	public void addToStreamValue(StreamValueBuilder builder)
	{
		builder.appendTextValue( textRepresentation );
	}
	
	
	
	
	//
	//
	// TEXT REPRESENTATION MODIFICATION METHODS
	//
	//
	
	public void insertText(int index, String x)
	{
		textRepresentation = textRepresentation.substring( 0, index ) + x + textRepresentation.substring( index );
		notifyTextInserted( index, x.length() );
		textRepresentationChanged( new TextEditEventInsert( this, index, x ) );
	}

	public void removeText(int index, int length)
	{
		String textRemoved = textRepresentation.substring( index, index + length );
		textRepresentation = textRepresentation.substring( 0, index ) + textRepresentation.substring( index + length );
		notifyTextRemoved( index, length );
		textRepresentationChanged( new TextEditEventRemove( this, index, textRemoved ) );
	}
	
	public void replaceText(int index, int length, String x)
	{
		String oldText = textRepresentation.substring( index, index + length );
		textRepresentation = textRepresentation.substring( 0, index )  +  x  +  textRepresentation.substring( index + length );
		notifyTextReplaced( index, length, x.length() );
		textRepresentationChanged( new TextEditEventReplace( this, index, oldText, x ) );
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

	
	protected boolean deleteText()
	{
		int length = textRepresentation.length();
		if ( length > 0 )
		{
			String oldText = textRepresentation;
			textRepresentation = "";
			notifyTextRemoved( 0, length );
			textRepresentationChanged( new TextEditEventRemove( this, 0, oldText ) );
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	protected void notifyTextInserted(int index, int length)
	{
		
	}
	
	protected void notifyTextRemoved(int index, int length)
	{
	}
	
	protected void notifyTextReplaced(int index, int oldLength, int newLength)
	{
	}
	
	
	protected void revert_insert(int index, String x)
	{
		textRepresentation = textRepresentation.substring( 0, index ) + x + textRepresentation.substring( index );
		notifyTextInserted( index, x.length() );
	}
	
	protected void revert_remove(int index, int length)
	{
		textRepresentation = textRepresentation.substring( 0, index ) + textRepresentation.substring( index + length );
		notifyTextRemoved( index, length );
	}
	
	protected void revert_replace(int index, int length, String x)
	{
		textRepresentation = textRepresentation.substring( 0, index )  +  x  +  textRepresentation.substring( index + length );
		notifyTextReplaced( index, length, x.length() );
	}
	


	
	
	//
	// EDITABILITY METHODS
	//

	public boolean isEditable()
	{
		return false;
	}

	
	
	
	//
	//
	// SELECTABILITY METHODS
	//
	//
	
	public boolean isSelectable()
	{
		return false;
	}
	
	
	
	
	//
	// Meta element methods
	//
	
	protected void createDebugPresentationHeaderContents(ArrayList<Object> elements)
	{
		elements.add( new UnescapedStringAsRow( textRepresentation ) );
		
		super.createDebugPresentationHeaderContents( elements );
	}
}
