//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
	
	
	public EditRule(SequentialController controller, List<TreeEventListener> editListeners)
	{
		super( controller );
		this.editListeners = new ArrayList<TreeEventListener>();
		this.editListeners.addAll( editListeners );
	}

	
	public Pres applyToFragment(Pres view, Object model, SimpleAttributeTable inheritedState)
	{
		return new EditableSequentialItem( controller, editListeners, view );
	}
}
