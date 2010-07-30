//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.GenericPerspective.PresCom;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class ObjectBorder extends Pres
{
	private Pres child;
	
	
	public ObjectBorder(Pres child)
	{
		this.child = child;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet2 borderStyle = GenericStyle.objectBorderStyle.get( style );
		DPElement childElem = child.present( ctx, GenericStyle.useObjectBorderAttrs( style ) );
		return borderStyle.applyTo( new Border( childElem ) ).present( ctx, style );
	}
}
