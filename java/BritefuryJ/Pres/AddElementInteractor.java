//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Interactor.AbstractElementInteractor;
import BritefuryJ.StyleSheet.StyleValues;

public class AddElementInteractor extends Pres
{
	private AbstractElementInteractor interactor;
	private Pres child;
	
	
	public AddElementInteractor(Pres child, AbstractElementInteractor interactor)
	{
		this.interactor = interactor;
		this.child = child;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		element.addElementInteractor( interactor );
		return element;
	}
}
