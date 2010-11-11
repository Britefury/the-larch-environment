//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.GSym.SequentialEditor.EditorFragment;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class StructuralValue extends Pres
{
	protected static class ClearStructuralValueListener implements TreeEventListener
	{
		@Override
		public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
		{
			element.clearFixedValue();
			return false;
		}
	}
	
	
	protected static ClearStructuralValueListener listener = new ClearStructuralValueListener();
	
	
	private Pres child;
	private Object value;
	
	
	public StructuralValue(Object child, Object value)
	{
		this.child = coerce( child );
		this.value = value;
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.setFixedValue( value );
		element.addTreeEventListener( listener );
		return element;
	}
}
