//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class Region extends Pres
{
	private Pres child;
	private ClipboardHandler clipboardHandler = null;
	
	
	public Region(Object child)
	{
		this.child = coerce( child );
	}
	
	public Region(Object child, ClipboardHandler clipboardHandler)
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
