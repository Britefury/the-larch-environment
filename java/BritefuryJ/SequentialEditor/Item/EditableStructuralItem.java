//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.SequentialEditor.Item;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class EditableStructuralItem extends Pres
{
	private TreeEventListener editListeners[];
	private Pres child;
	private Object value;
	
	
	public EditableStructuralItem(TreeEventListener editListener, Object value, Object child)
	{
		this.editListeners = new TreeEventListener[] { editListener };
		this.child = coerce( child );
		this.value = value;
	}

	public EditableStructuralItem(List<TreeEventListener> editListeners, Object value, Object child)
	{
		this.editListeners = editListeners.toArray( new TreeEventListener[] {} );
		this.value = value;
		this.child = coerce( child );
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.setFixedValue( value );
		for (TreeEventListener listener: editListeners)
		{
			element.addTreeEventListener( listener );
		}
		return element;
	}
}
