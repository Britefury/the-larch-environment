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
	private PrecedenceHandler precedenceHandler;
	
	
	public SRAbstractEditRule(SyntaxRecognizingEditor editor, PrecedenceHandler precedenceHandler)
	{
		this.editor = editor;
		this.precedenceHandler = precedenceHandler;
	}
	

	public Pres applyToFragment(Pres view, Object model, SimpleAttributeTable inheritedState)
	{
		Object editModeObj = inheritedState.getOptional( "__SREditor_edit" );
		SyntaxRecognizingEditor.EditMode editMode = editModeObj instanceof SyntaxRecognizingEditor.EditMode  ?
				(SyntaxRecognizingEditor.EditMode)editModeObj  :  SyntaxRecognizingEditor.EditMode.EDIT;
		
		if ( precedenceHandler != null )
		{
			view = precedenceHandler.applyPrecedenceBrackets( model, view, inheritedState );
		}
		
		if ( editMode == SyntaxRecognizingEditor.EditMode.EDIT )
		{
			view = editFragment( view, model, inheritedState );
		}
		
		return view;
	}
	
	
	protected abstract Pres editFragment(Pres view, Object model, SimpleAttributeTable inheritedState);
}
