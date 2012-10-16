//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential.Item;

import java.util.List;

import BritefuryJ.Editor.Sequential.SequentialController;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class SoftStructuralItem extends Pres
{
	private SequentialController editor;
	private TreeEventListener editListeners[];
	private Object model;
	private Pres child;
	
	
	public SoftStructuralItem(SequentialController editor, Object model, Object child)
	{
		this.editor = editor;
		this.editListeners = new TreeEventListener[] {};
		this.child = coerceNonNull( child );
		this.model = model;
	}

	public SoftStructuralItem(SequentialController editor, TreeEventListener editListener, Object model, Object child)
	{
		this.editor = editor;
		this.editListeners = new TreeEventListener[] { editListener };
		this.child = coerceNonNull( child );
		this.model = model;
	}

	public SoftStructuralItem(SequentialController editor, List<TreeEventListener> editListeners, Object model, Object child)
	{
		this.editor = editor;
		this.editListeners = editListeners.toArray( new TreeEventListener[editListeners.size()] );
		this.model = model;
		this.child = coerceNonNull( child );
	}


	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		if ( editor.isClearNeighbouringStructuresEnabled() )
		{
			element.addTreeEventListener( SequentialController.getClearNeighbouringStructuralValueListener() );
		}
		element.setFixedValue( model );
		element.addTreeEventListener( editor.getClearStructuralValueListener() );
		for (TreeEventListener listener: editListeners)
		{
			element.addTreeEventListener( listener );
		}
		return element;
	}
}
