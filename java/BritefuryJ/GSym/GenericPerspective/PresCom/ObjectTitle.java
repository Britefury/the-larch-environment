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
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class ObjectTitle extends Pres
{
	private String title;
	
	
	public ObjectTitle(String title)
	{
		this.title = title;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		StyleSheet2 style = ctx.getStyle().get( GenericStyle.objectTitleAttrs, StyleSheet2.class );
		return style.applyTo( new StaticText( title ) ).present( ctx );
	}
}
