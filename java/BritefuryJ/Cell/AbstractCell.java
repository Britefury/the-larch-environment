//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.StaticText;

public abstract class AbstractCell implements Presentable
{
	public abstract Object getValue();
	public abstract void setValue(Object value);
	
	
	
	protected Pres presentCell(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Object value = getValue();
		
		Pres valuePres;
		if ( value == null )
		{
			valuePres = new StaticText( "" );
		}
		else
		{
			valuePres = CellEditPerspective.instance.applyTo( value );
		}
		
		return valuePres.withTreeEventListener( cellSetListener );
	}
	
	

	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presentCell( fragment, inheritedState );
	}
	
	
	
	private static final TreeEventListener cellSetListener = new TreeEventListener()
	{
		@Override
		public boolean onTreeEvent(DPElement element, DPElement sourceElement, Object event)
		{
			if ( event instanceof CellSetValueEvent )
			{
				CellSetValueEvent setValueEvent = (CellSetValueEvent)event;
				FragmentView fragment = (FragmentView)element.getFragmentContext();
				Object model = fragment.getModel();
				AbstractCell cell = (AbstractCell)model;
				cell.setValue( setValueEvent.value );
				return true;
			}
			return false;
		}
	};
}
