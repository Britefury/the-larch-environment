//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
