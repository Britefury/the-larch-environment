//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.Marker.Marker;

public class EditableLeafElement extends LeafElement
{
	//
	// Constructor
	//
	
	protected EditableLeafElement(DPContentLeafEditable widget, String textRepresentation)
	{
		super( widget, textRepresentation );
	}



	//
	// Widget
	//
	
	public DPContentLeafEditable getWidget()
	{
		return (DPContentLeafEditable)widget;
	}
	
	
	
	
	
	//
	// Content modification methods
	//
	
	public void setTextRepresentation(String newTextRepresentation)
	{
		int oldLength = textRepresentation.length();
		int newLength = newTextRepresentation.length();
		
		if ( newLength > oldLength )
		{
			getWidget().markerInsert( oldLength, newLength - oldLength );
		}
		else if ( newLength < oldLength )
		{
			getWidget().markerRemove( newLength, oldLength - newLength );
		}
		
		textRepresentation = newTextRepresentation;
	}
	
	
	
	public void insertText(Marker marker, String x)
	{
		int index = marker.getIndex();
		textRepresentation = textRepresentation.substring( 0, index ) + x + textRepresentation.substring( index );
		getWidget().markerInsert( index, x.length() );
		textRepresentationChanged();
	}

	public void removeText(int index, int length)
	{
		length = Math.min( length, getTextRepresentationLength() - index );
		textRepresentation = textRepresentation.substring( 0, index ) + textRepresentation.substring( index + length );
		getWidget().markerRemove( index, length );
		textRepresentationChanged();
	}
	
	public void removeText(Marker m, int length)
	{
		removeText( m.getIndex(), length );
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
	
	public void replaceText(Marker m, int length, String x)
	{
		int index = m.getIndex();
		textRepresentation = textRepresentation.substring( 0, index )  +  x  +  textRepresentation.substring( index + length );
		
		if ( x.length() > length )
		{
			getWidget().markerInsert( index + length, x.length() - length );
		}
		else if ( x.length() < length )
		{
			getWidget().markerRemove( index + x.length(), length - x.length() );
		}
		textRepresentationChanged();
	}
	
	public boolean clearText()
	{
		int length = textRepresentation.length();
		if ( length > 0 )
		{
			textRepresentation = "";
			getWidget().markerRemove( 0, length );
			textRepresentationChanged();
			return true;
		}
		else
		{
			return false;
		}
	}
}
