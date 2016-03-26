//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
		LSRegion element = new LSRegion( Primitive.regionParams.get( style ), childElement );
		if ( clipboardHandler != null )
		{
			element.setClipboardHandler( clipboardHandler );
		}
		return element;
	}
}
