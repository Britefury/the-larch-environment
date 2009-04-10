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
	
	protected EditableLeafElement(DPContentLeafEditable widget, String content)
	{
		super( widget, content );
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
	
	public void setContent(String newContent)
	{
		int oldLength = content.length();
		int newLength = newContent.length();
		
		if ( newLength > oldLength )
		{
			getWidget().markerInsert( oldLength, newLength - oldLength );
		}
		else if ( newLength < oldLength )
		{
			getWidget().markerRemove( newLength, oldLength - newLength );
		}
		
		content = newContent;
	}
	
	
	
	public void insertContent(Marker marker, String x)
	{
		int index = marker.getIndex();
		content = content.substring( 0, index ) + x + content.substring( index );
		getWidget().markerInsert( index, x.length() );
		contentChanged();
	}

	public void removeContent(Marker m, int length)
	{
		int index = m.getIndex();
		content = content.substring( 0, index ) + content.substring( index + length );
		getWidget().markerRemove( index, length );
		contentChanged();
	}
	
	public void removeContentFromStart(int length)
	{
		length = Math.min( length, getContentLength() );
		int end = getContentLength();
		content = content.substring( length, end );
		getWidget().markerRemove( 0, length );
		contentChanged();
	}
	
	public void removeContentFromEnd(int length)
	{
		length = Math.min( length, getContentLength() );
		int index = getContentLength() - length;
		content = content.substring( 0, index );
		getWidget().markerRemove( index, length );
		contentChanged();
	}
	
	public void replaceContent(Marker m, int length, String x)
	{
		int index = m.getIndex();
		content = content.substring( 0, index )  +  x  +  content.substring( index + length );
		
		if ( x.length() > length )
		{
			getWidget().markerInsert( index + length, x.length() - length );
		}
		else if ( x.length() < length )
		{
			getWidget().markerRemove( index + x.length(), length - x.length() );
		}
		contentChanged();
	}
	
	public void clearContent()
	{
		int length = content.length();
		if ( length > 0 )
		{
			content = "";
			getWidget().markerRemove( 0, length );
			contentChanged();
		}
	}
}
