//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Sequential.Item;

import java.util.List;

import BritefuryJ.Editor.Sequential.SequentialController;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class EditableSequentialItem extends Pres
{
	private SequentialController controller;
	private TreeEventListener editListeners[];
	private Pres child;
	
	
	public EditableSequentialItem(SequentialController controller, TreeEventListener editListener, Object child)
	{
		this.controller = controller;
		this.editListeners = new TreeEventListener[] { editListener };
		this.child = coercePresentingNull(child);
	}

	public EditableSequentialItem(SequentialController controller, List<TreeEventListener> editListeners, Object child)
	{
		this.controller = controller;
		this.editListeners = editListeners.toArray( new TreeEventListener[editListeners.size()] );
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
		for (TreeEventListener listener: editListeners)
		{
			element.addTreeEventListener( listener );
		}
		return element;
	}
}
