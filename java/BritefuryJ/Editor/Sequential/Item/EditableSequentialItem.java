//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential.Item;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class EditableSequentialItem extends Pres
{
	private TreeEventListener editListeners[];
	private Pres child;
	
	
	public EditableSequentialItem(TreeEventListener editListener, Object child)
	{
		this.editListeners = new TreeEventListener[] { editListener };
		this.child = coerceNonNull( child );
	}

	public EditableSequentialItem(List<TreeEventListener> editListeners, Object child)
	{
		this.editListeners = editListeners.toArray( new TreeEventListener[] {} );
		this.child = coerceNonNull( child );
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		for (TreeEventListener listener: editListeners)
		{
			element.addTreeEventListener( listener );
		}
		return element;
	}
}
