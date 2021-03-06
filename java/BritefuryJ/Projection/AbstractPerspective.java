//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Projection;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.Clipboard.ClipboardHandlerInterface;
import BritefuryJ.Pres.ApplyPerspective;
import BritefuryJ.Pres.Pres;


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
	
	public abstract ClipboardHandlerInterface getClipboardHandler();

	
	public ApplyPerspective applyTo(Object x)
	{
		return new ApplyPerspective( this, x );
	}
	
	public ApplyPerspective __call__(Object x)
	{
		return new ApplyPerspective( this, x );
	}
}
