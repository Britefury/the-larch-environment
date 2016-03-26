//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class ApplyStyleSheet extends Pres
{
	private StyleSheet styleSheet;
	private Pres child;
	
	
	public ApplyStyleSheet(StyleSheet styleSheet, Object child)
	{
		this.styleSheet = styleSheet;
		this.child = coerce( child );
	}
	

	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return child.present( ctx, style.withAttrs( styleSheet ) );
	}
}
