//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Projection;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Clipboard.ClipboardHandler;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.GSym.PresCom.ApplyPerspective;
import BritefuryJ.IncrementalView.FragmentView;


public abstract class AbstractPerspective
{
	protected abstract Pres presentModel(Object x, FragmentView fragment, SimpleAttributeTable inheritedState);
	
	public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		if ( x instanceof Pres )
		{
			return (Pres)x;
		}
		else
		{
			return presentModel( x, fragment, inheritedState );
		}
	}
	
	public abstract ClipboardHandler getClipboardHandler();

	
	public ApplyPerspective applyTo(Object x)
	{
		return new ApplyPerspective( this, x );
	}
	
	public ApplyPerspective __call__(Object x)
	{
		return new ApplyPerspective( this, x );
	}
}
