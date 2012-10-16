//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Editor.Sequential.Item.EditableSequentialItem;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.Pres.Pres;

public class EditRule extends AbstractEditRule
{
	private List<TreeEventListener> editListeners;
	
	
	public EditRule(SequentialController editor, List<TreeEventListener> editListeners)
	{
		super( editor );
		this.editListeners = new ArrayList<TreeEventListener>();
		this.editListeners.addAll( editListeners );
	}

	
	public Pres applyToFragment(Pres view, Object model, SimpleAttributeTable inheritedState)
	{
		return new EditableSequentialItem( editor, editListeners, view );
	}
}
