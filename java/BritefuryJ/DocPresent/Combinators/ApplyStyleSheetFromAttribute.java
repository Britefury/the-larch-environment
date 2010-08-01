//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class ApplyStyleSheetFromAttribute extends Pres
{
	private AttributeBase attribute;
	private Pres child;
	
	
	public ApplyStyleSheetFromAttribute(AttributeBase attribute, Pres child)
	{
		this.attribute = attribute;
		this.child = child;
	}
	

	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet2 styleSheet = style.get( attribute, StyleSheet2.class );
		return child.present( ctx, style.withAttrs( styleSheet ) );
	}
}
