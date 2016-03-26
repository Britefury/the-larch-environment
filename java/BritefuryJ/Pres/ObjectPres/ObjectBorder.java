//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.ObjectPres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class ObjectBorder extends Pres
{
	private Pres child;
	
	
	public ObjectBorder(Pres child)
	{
		this.child = child;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		StyleSheet borderStyle = ObjectPresStyle.objectBorderStyle.get( style );
		LSElement childElem = child.present( ctx, ObjectPresStyle.useObjectBorderAttrs( style ) );
		return borderStyle.applyTo( new Border( childElem ).alignVRefY() ).present( ctx, style );
	}
}
