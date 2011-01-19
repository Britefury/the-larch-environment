//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential.Item;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.Editor.Sequential.SequentialEditor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class BreakableStructuralItem extends Pres
{
	private SequentialEditor editor;
	private Object value;
	private Pres child;
	
	
	public BreakableStructuralItem(SequentialEditor editor, Object value, Object child)
	{
		this.editor = editor;
		this.value = value;
		this.child = coerceNonNull( child );
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.setFixedValue( value );
		element.addTreeEventListener( editor.getClearStructuralValueListener() );
		return element;
	}
}
