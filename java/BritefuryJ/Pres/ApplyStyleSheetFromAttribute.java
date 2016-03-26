//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

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
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet styleSheet = style.get( attribute, StyleSheet.class );
		return child.present( ctx, style.withAttrs( styleSheet ) );
	}
}
