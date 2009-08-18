//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.StyleSheets.ElementStyleSheet;


public abstract class DPContentLeafEditable extends DPContentLeaf
{
	public static class EditableLeafElementFilter implements WidgetFilter
	{
		public boolean testElement(DPWidget element)
		{
			return element instanceof DPContentLeafEditable;
		}
	}
	
	
	
	//
	// Constructors
	//
	
	
	protected DPContentLeafEditable(String textRepresentation)
	{
		super( textRepresentation );
	}
	
	protected DPContentLeafEditable(ElementStyleSheet styleSheet, String textRepresentation)
	{
		super( styleSheet, textRepresentation );
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
		
		textRepresentationChanged( new LinearRepresentationEventTextReplace( this, 0, oldLength, newTextRepresentation ) );
	}
	
	
	public void insertText(Marker marker, String x)
	{
		int index = marker.getClampedIndex();
		markerInsert( index, x.length() );
		textRepresentation = textRepresentation.substring( 0, index ) + x + textRepresentation.substring( index );
		textRepresentationChanged( new LinearRepresentationEventTextInsert( this, index, x ) );
	}

	public void removeText(int index, int length)
	{
		index = Math.min( Math.max( index, 0 ), textRepresentation.length() );
		length = Math.min( length, getTextRepresentationLength() - index );
		textRepresentation = textRepresentation.substring( 0, index ) + textRepresentation.substring( index + length );
		markerRemove( index, length );
		textRepresentationChanged( new LinearRepresentationEventTextRemove( this, index, length ) );
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
	
	public boolean clearText()
	{
		int length = textRepresentation.length();
		if ( length > 0 )
		{
			textRepresentation = "";
			markerRemove( 0, length );
			textRepresentationChanged( new LinearRepresentationEventTextRemove( this, 0, length ) );
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
