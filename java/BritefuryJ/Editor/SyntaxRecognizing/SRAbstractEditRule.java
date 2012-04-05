//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Editor.SyntaxRecognizing.Precedence.PrecedenceHandler;
import BritefuryJ.Pres.Pres;

public abstract class SRAbstractEditRule
{
	protected SyntaxRecognizingEditor editor;
	protected boolean outer;
	private PrecedenceHandler precedenceHandler;
	
	
	public SRAbstractEditRule(SyntaxRecognizingEditor editor, boolean outer, PrecedenceHandler precedenceHandler)
	{
		this.editor = editor;
		this.outer = outer;
		this.precedenceHandler = precedenceHandler;
	}
	

	public Pres applyToFragment(Pres view, Object model, SimpleAttributeTable inheritedState)
	{
		boolean edit;
		
		if ( !outer )
		{
			Object editModeObj = inheritedState.getOptional( "__SREditor_edit" );
			SyntaxRecognizingEditor.EditMode editMode = editModeObj instanceof SyntaxRecognizingEditor.EditMode  ?
					(SyntaxRecognizingEditor.EditMode)editModeObj  :  SyntaxRecognizingEditor.EditMode.EDIT;
			edit = editMode == SyntaxRecognizingEditor.EditMode.EDIT;
		}
		else
		{
			edit = true;
		}
		
		if ( precedenceHandler != null )
		{
			view = precedenceHandler.applyPrecedenceBrackets( model, view, inheritedState );
		}
		
		if ( edit )
		{
			view = buildFragment( view, model, inheritedState );
		}
		
		return view;
	}
	
	
	protected abstract Pres buildFragment(Pres view, Object model, SimpleAttributeTable inheritedState);
}
