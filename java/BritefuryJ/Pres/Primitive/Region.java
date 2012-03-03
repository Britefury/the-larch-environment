//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Region extends Pres
{
	private Pres child;
	private ClipboardHandlerInterface clipboardHandler = null;
	
	
	public Region(Object child)
	{
		this.child = coerce( child );
	}
	
	public Region(Object child, ClipboardHandlerInterface clipboardHandler)
	{
		this.child = coerce( child );
		this.clipboardHandler = clipboardHandler;
	}
	

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement childElement = child.present( ctx, Primitive.useRegionParams.get( style ) );
		LSRegion element = new LSRegion( Primitive.regionParams.get( style ) );
		element.setChild( childElement );
		if ( clipboardHandler != null )
		{
			element.setClipboardHandler( clipboardHandler );
		}
		return element;
	}
}
