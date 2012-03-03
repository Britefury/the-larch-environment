//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import java.util.ArrayList;

import BritefuryJ.LSpace.LayoutTree.ContentLeafLayoutNodeInterface;
import BritefuryJ.LSpace.StreamValue.StreamValueBuilder;
import BritefuryJ.LSpace.StyleParams.ContentLeafStyleParams;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.ObjectPres.UnescapedStringAsRow;

public abstract class LSContentLeaf extends LSElement
{
	public static class EditableLeafElementFilter implements ElementFilter
	{
		public boolean testElement(LSElement element)
		{
			if ( element instanceof LSContentLeaf )
			{
				return ((LSContentLeaf)element).isEditable();
			}
			else
			{
				return false;
			}
		}
	}
	
	public static class SelectableLeafElementFilter implements ElementFilter
	{
		public boolean testElement(LSElement element)
		{
			if ( element instanceof LSContentLeaf )
			{
				return ((LSContentLeaf)element).isSelectable();
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
	
	
	
	LSContentLeaf(String textRepresentation)
	{
		this( ContentLeafStyleParams.defaultStyleParams, textRepresentation );
	}
	
	LSContentLeaf(ContentLeafStyleParams styleParams, String textRepresentation)
	{
		super(styleParams);
		
		if ( textRepresentation == null )
		{
			throw new RuntimeException( "Text representation cannot be null" );
		}
		
		this.textRepresentation = textRepresentation;
	}
	
	
	
	
	//
	//
	// ELEMENT TREE STRUCTURE METHODS
	//
	//
	
	public LSContentLeaf getFirstLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
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

	public LSContentLeaf getLastLeafInSubtree(ElementFilter branchFilter, ElementFilter leafFilter)
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

	

	public LSContentLeaf getPreviousLeaf()
	{
		return getPreviousLeaf( null, null, null );
	}

	public LSContentLeaf getNextLeaf()
	{
		return getNextLeaf( null, null, null );
	}


	public LSContentLeaf getPreviousLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
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
	
	public LSContentLeaf getNextLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter, ElementFilter leafFilter)
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
	
	
	public LSContentLeaf getPreviousEditableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new LSContentLeafEditable.EditableLeafElementFilter() );
	}

	public LSContentLeaf getNextEditableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new LSContentLeafEditable.EditableLeafElementFilter() );
	}


	public LSContentLeaf getPreviousEditableLeaf()
	{
		return getPreviousEditableLeaf( null, null );
	}

	public LSContentLeaf getNextEditableLeaf()
	{
		return getNextEditableLeaf( null, null );
	}

	
	public LSContentLeaf getPreviousSelectableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getPreviousLeaf( subtreeRootFilter, branchFilter, new LSContentLeafEditable.SelectableLeafElementFilter() );
	}

	public LSContentLeaf getNextSelectableLeaf(ElementFilter subtreeRootFilter, ElementFilter branchFilter)
	{
		return getNextLeaf( subtreeRootFilter, branchFilter, new LSContentLeafEditable.SelectableLeafElementFilter() );
	}


	public LSContentLeaf getPreviousSelectableLeaf()
	{
		return getPreviousSelectableLeaf( null, null );
	}

	public LSContentLeaf getNextSelectableLeaf()
	{
		return getNextSelectableLeaf( null, null );
	}

	
	
	

	
	//
	//
	// CONTENT LEAF METHODS
	//
	//
	
	public LSContentLeafEditable getEditableContentLeafAbove(Point2 localPos)
	{
		ContentLeafLayoutNodeInterface leafLayout = (ContentLeafLayoutNodeInterface)getLayoutNode();
		return leafLayout.getEditableContentLeafAbove( localPos );
	}
	
	public LSContentLeafEditable getEditableContentLeafBelow(Point2 localPos)
	{
		ContentLeafLayoutNodeInterface leafLayout = (ContentLeafLayoutNodeInterface)getLayoutNode();
		return leafLayout.getEditableContentLeafBelow( localPos );
	}
	
	
	public LSContentLeaf getLeftContentLeaf()
	{
		return this;
	}

	public LSContentLeaf getRightContentLeaf()
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
	
	public LSContentLeaf getLeafAtTextRepresentationPosition(int position)
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
		boolean start = index == 0, end = index == textRepresentation.length();
		LSContentLeaf prev = start ? getPreviousEditableLeaf() : null;
		LSContentLeaf next = end ? getNextEditableLeaf() : null;
		textRepresentation = textRepresentation.substring( 0, index ) + x + textRepresentation.substring( index );
		notifyTextInserted( index, x.length() );
		textRepresentationChanged( new TextEditEventInsert( this, prev, next, index, x ) );
	}

	public void removeText(int index, int length)
	{
		boolean start = index == 0, end = ( index + length ) >= textRepresentation.length();
		LSContentLeaf prev = start ? getPreviousEditableLeaf() : null;
		LSContentLeaf next = end ? getNextEditableLeaf() : null;
		String textRemoved = textRepresentation.substring( index, index + length );
		textRepresentation = textRepresentation.substring( 0, index ) + textRepresentation.substring( index + length );
		notifyTextRemoved( index, length );
		textRepresentationChanged( new TextEditEventRemove( this, prev, next, index, textRemoved ) );
	}
	
	public void replaceText(int index, int length, String x)
	{
		boolean start = index == 0, end = ( index + length ) >= textRepresentation.length();
		LSContentLeaf prev = start ? getPreviousEditableLeaf() : null;
		LSContentLeaf next = end ? getNextEditableLeaf() : null;
		String oldText = textRepresentation.substring( index, index + length );
		textRepresentation = textRepresentation.substring( 0, index )  +  x  +  textRepresentation.substring( index + length );
		notifyTextReplaced( index, length, x.length() );
		textRepresentationChanged( new TextEditEventReplace( this, prev, next, index, oldText, x ) );
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
			textRepresentationChanged( new TextEditEventRemove( this, getPreviousEditableLeaf(), getNextEditableLeaf(), 0, oldText ) );
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
	//
	// WHITESPACE METHODS
	//
	//
	
	protected boolean isWhitespace()
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
