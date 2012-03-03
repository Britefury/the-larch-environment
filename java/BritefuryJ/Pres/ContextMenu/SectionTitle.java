//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.ContextMenu;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.ApplyStyleSheetFromAttribute;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.StyleSheet.StyleValues;

public class SectionTitle extends Pres
{
	private String text;
	
	
	public SectionTitle(String text)
	{
		this.text = text;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return new ApplyStyleSheetFromAttribute( ContextMenuStyle.sectionTitleStyle, new Label( text ) ).present( ctx, style );
	}
}
