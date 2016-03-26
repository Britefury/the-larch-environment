//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Editor.Sequential.AbstractEditRule;
import BritefuryJ.Editor.SyntaxRecognizing.Precedence.PrecedenceHandler;
import BritefuryJ.Pres.Pres;

public abstract class SRAbstractEditRule extends AbstractEditRule
{
	private PrecedenceHandler precedenceHandler;
	
	
	public SRAbstractEditRule(SyntaxRecognizingController controller, PrecedenceHandler precedenceHandler)
	{
		super( controller );
		this.precedenceHandler = precedenceHandler;
	}
	

	public Pres applyToFragment(Pres view, Object model, SimpleAttributeTable inheritedState)
	{
		if ( precedenceHandler != null )
		{
			view = precedenceHandler.applyPrecedenceBrackets( model, view, inheritedState );
		}
		
		view = buildFragment( view, model, inheritedState );
		
		return view;
	}
	
	
	protected abstract Pres buildFragment(Pres view, Object model, SimpleAttributeTable inheritedState);
}
