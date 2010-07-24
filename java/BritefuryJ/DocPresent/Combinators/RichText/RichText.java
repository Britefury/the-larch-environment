//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import BritefuryJ.AttributeTable.InheritedAttributeNonNull;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

class RichText
{
	public static final InheritedAttributeNonNull pageSpacing = new InheritedAttributeNonNull( "richtext", "pageSpacing", Double.class, 15.0 );
	
	public static StyleSheetValues usePageAttrs(StyleSheetValues style)
	{
		return style.useAttr( pageSpacing );
	}

	public static StyleSheetValues pageStyle(StyleSheetValues style)
	{
		return style.remapAttr( Primitive.vboxSpacing, pageSpacing );
	}
}
