//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Editor.Sequential.Item.SoftStructuralItem;
import BritefuryJ.Editor.SyntaxRecognizing.Precedence.PrecedenceHandler;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.Pres.Pres;

public class SRSoftStructuralEditRule extends SRAbstractEditRule
{
	private List<TreeEventListener> editListeners;
	
	
	public SRSoftStructuralEditRule(SyntaxRecognizingController controller, PrecedenceHandler precedenceHandler, List<TreeEventListener> editListeners)
	{
		super( controller, precedenceHandler );
		this.editListeners = new ArrayList<TreeEventListener>();
		this.editListeners.addAll( editListeners );
	}
	
	
	protected Pres buildFragment(Pres view, Object model, SimpleAttributeTable inheritedState)
	{
		return new SoftStructuralItem( controller, editListeners, model, view );
	}
}
