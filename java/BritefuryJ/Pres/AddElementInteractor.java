//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
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
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.addElementInteractor( interactor );
		return element;
	}
}
