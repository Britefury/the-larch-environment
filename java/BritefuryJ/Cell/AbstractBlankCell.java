//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.Pres.Pres;

public abstract class AbstractBlankCell implements Presentable
{
	private Pres blankPres;
	
	
	public AbstractBlankCell(Pres blankPres)
	{
		this.blankPres = blankPres;
	}
	
	
	public abstract void setValue(Object value);
	
	
	
	protected Pres presentCell(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return blankPres.withTreeEventListener( cellSetListener );
	}
	
	

	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return presentCell( fragment, inheritedState );
	}
	
	
	
	private static final TreeEventListener cellSetListener = new TreeEventListener()
	{
		@Override
		public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
		{
			if ( event instanceof CellSetValueEvent )
			{
				CellSetValueEvent setValueEvent = (CellSetValueEvent)event;
				FragmentView fragment = (FragmentView)element.getFragmentContext();
				Object model = fragment.getModel();
				AbstractBlankCell cell = (AbstractBlankCell)model;
				cell.setValue( setValueEvent.value );
				return true;
			}
			return false;
		}
	};
}
