//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Sequential.Item;

import BritefuryJ.Editor.Sequential.SequentialController;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class StructuralItem extends Pres
{
	private SequentialController controller;
	private Object model;
	private Pres child;
	
	
	public StructuralItem(SequentialController controller, Object model, Object child)
	{
		this.controller = controller;
		this.model = model;
		this.child = coercePresentingNull(child);
	}


	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		if ( controller.isClearNeighbouringStructuresEnabled() )
		{
			element.addTreeEventListener( SequentialController.getClearNeighbouringStructuralValueListener() );
		}
		element.setFixedValue( model );
		return element;
	}
}
