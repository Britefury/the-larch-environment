//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandlerInterface;
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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement childElement = child.present( ctx, style );
		DPRegion element = new DPRegion();
		element.setChild( childElement );
		if ( clipboardHandler != null )
		{
			element.setClipboardHandler( clipboardHandler );
		}
		element.copyAlignmentFlagsFrom( childElement );
		return element;
	}
}
