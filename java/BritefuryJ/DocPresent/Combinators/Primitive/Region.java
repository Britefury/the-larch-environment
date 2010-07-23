//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRegion;
import BritefuryJ.DocPresent.Clipboard.EditHandler;
import BritefuryJ.DocPresent.Combinators.Pres;

public class Region extends Pres
{
	private Pres child;
	private EditHandler editHandler = null;
	
	
	public Region(Object child)
	{
		this.child = coerce( child );
	}
	
	public Region(Object child, EditHandler editHandler)
	{
		this.child = coerce( child );
		this.editHandler = editHandler;
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPElement childElement = child.present( ctx );
		DPRegion element = new DPRegion();
		element.setChild( childElement );
		if ( editHandler != null )
		{
			element.setEditHandler( editHandler );
		}
		element.copyAlignmentFlagsFrom( childElement );
		return element;
	}
}
