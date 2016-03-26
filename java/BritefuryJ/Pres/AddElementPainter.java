//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
