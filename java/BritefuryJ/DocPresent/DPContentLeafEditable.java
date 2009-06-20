//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.ElementTree.EditableLeafElement;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;


public abstract class DPContentLeafEditable extends DPContentLeaf
{
	public static class EditableLeafWidgetFilter extends WidgetFilter
	{
		public boolean testLeaf(DPContentLeaf leaf)
		{
			return leaf instanceof DPContentLeafEditable;
		}
	}
	
	
	
	//
	// Constructors
	//
	
	
	protected DPContentLeafEditable(String textRepresentation)
	{
		super( textRepresentation );
	}
	
	protected DPContentLeafEditable(ContentLeafStyleSheet styleSheet, String textRepresentation)
	{
		super( styleSheet, textRepresentation );
	}




	//
	// Specialised getElement()
	//
	
	public EditableLeafElement getElement()
	{
		return (EditableLeafElement)element;
	}
	
	

	
	
	
	//
	// Text representation modification
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
		
		textRepresentationChanged();
	}
	
	
	public void insertText(Marker marker, String x)
	{
		int index = marker.getIndex();
		index = Math.min( Math.max( index, 0 ), textRepresentation.length() );
		textRepresentation = textRepresentation.substring( 0, index ) + x + textRepresentation.substring( index );
		markerInsert( index, x.length() );
		textRepresentationChanged();
	}

	public void removeText(int index, int length)
	{
		index = Math.min( Math.max( index, 0 ), textRepresentation.length() );
		length = Math.min( length, getTextRepresentationLength() - index );
		textRepresentation = textRepresentation.substring( 0, index ) + textRepresentation.substring( index + length );
		markerRemove( index, length );
		textRepresentationChanged();
	}
	
	public void removeText(Marker marker, int length)
	{
		removeText( marker.getIndex(), length );
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
	
	public void replaceText(Marker marker, int length, String x)
	{
		int index = marker.getIndex();
		index = Math.min( Math.max( index, 0 ), textRepresentation.length() );
		textRepresentation = textRepresentation.substring( 0, index )  +  x  +  textRepresentation.substring( index + length );
		
		if ( x.length() > length )
		{
			markerInsert( index + length, x.length() - length );
		}
		else if ( x.length() < length )
		{
			markerRemove( index + x.length(), length - x.length() );
		}
		textRepresentationChanged();
	}
	
	public boolean clearText()
	{
		int length = textRepresentation.length();
		if ( length > 0 )
		{
			textRepresentation = "";
			markerRemove( 0, length );
			textRepresentationChanged();
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	

	//
	// TYPE METHODS
	//
	
	public boolean isEditable()
	{
		return true;
	}
}
