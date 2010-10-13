//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.ContextMenu;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.ApplyStyleSheetFromAttribute;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class SectionTitle extends Pres
{
	private String text;
	
	
	public SectionTitle(String text)
	{
		this.text = text;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return new ApplyStyleSheetFromAttribute( ContextMenuStyle.sectionTitleStyle, new Label( text ) ).present( ctx, style );
	}
}
