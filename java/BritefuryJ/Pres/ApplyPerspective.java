//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.DefaultPerspective.DefaultPerspective;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSRegion;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Projection.AbstractPerspective;
import BritefuryJ.StyleSheet.StyleValues;

public class ApplyPerspective extends Pres
{
	private AbstractPerspective perspective;
	private Pres child;
	
	
	public ApplyPerspective(AbstractPerspective perspective, Object child)
	{
		this.perspective = perspective;
		this.child = coercePresentingNull(child);
	}
	
	
	public static ApplyPerspective defaultPerspective(Object child)
	{
		return new ApplyPerspective( null, child );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		AbstractPerspective p = perspective;
		if ( p == null )
		{
			p = DefaultPerspective.instance;
		}
		
		LSElement childElement = child.present( new PresentationContext( ctx.getFragment(), p, ctx.getInheritedState() ), style );

		// If we are changing perspective, wrap in a region element to ensure that the clipboard handler gets changed
		if (p != ctx.getPerspective()) {
			LSRegion regionElement = new LSRegion( Primitive.regionParams.get( style ), childElement );
			ClipboardHandlerInterface clipboardHandler = p.getClipboardHandler();
			if ( clipboardHandler != null )
			{
				regionElement.setClipboardHandler(clipboardHandler);
			}
			return regionElement;
		}
		else {
			return childElement;
		}
	}
}
