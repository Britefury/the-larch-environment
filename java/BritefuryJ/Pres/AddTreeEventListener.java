//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.StyleSheet.StyleValues;

public class AddTreeEventListener extends Pres
{
	private TreeEventListener listener;
	private Pres child;
	
	
	public AddTreeEventListener(Pres child, TreeEventListener listener)
	{
		this.listener = listener;
		this.child = child;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.addTreeEventListener( listener );
		return element;
	}
}
