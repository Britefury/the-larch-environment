//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.StyleSheet.StyleValues;

public class AddElementPainter extends Pres
{
	private ElementPainter painter;
	private Pres child;
	
	
	public AddElementPainter(Pres child, ElementPainter painter)
	{
		this.painter = painter;
		this.child = child;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.addPainter( painter );
		return element;
	}
}
