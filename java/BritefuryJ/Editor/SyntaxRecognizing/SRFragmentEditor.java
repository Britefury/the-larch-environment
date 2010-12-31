//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.SyntaxRecognizing;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Interactor.AbstractElementInteractor;
import BritefuryJ.Editor.Sequential.Item.EditableSequentialItem;
import BritefuryJ.Editor.Sequential.Item.EditableStructuralItem;
import BritefuryJ.Editor.SyntaxRecognizing.Precedence.PrecedenceHandler;

public class SRFragmentEditor
{
	public static enum EditMode
	{
		DISPLAY,
		EDIT
	}
	
	private SyntaxRecognizingEditor editor;
	private boolean bStructural;
	private PrecedenceHandler precedenceHandler;
	private List<TreeEventListener> editListeners;
	private List<AbstractElementInteractor> elementInteractors;	
	
	
	public SRFragmentEditor(SyntaxRecognizingEditor editor, boolean bStructural, PrecedenceHandler precedenceHandler,
			List<TreeEventListener> editListeners, List<AbstractElementInteractor> elementInteractors)
	{
		this.editor = editor;
		this.bStructural = bStructural;
		this.precedenceHandler = precedenceHandler;
		this.editListeners = new ArrayList<TreeEventListener>();
		this.editListeners.addAll( editListeners );
		this.elementInteractors = new ArrayList<AbstractElementInteractor>();
		if ( elementInteractors != null )
		{
			this.elementInteractors.addAll( elementInteractors );
		}
	}
	
	public SRFragmentEditor(SyntaxRecognizingEditor editor, boolean bStructural, List<TreeEventListener> editListeners, List<AbstractElementInteractor> elementInteractors)
	{
		this( editor, bStructural, null, editListeners, elementInteractors );
	}
	
	public SRFragmentEditor(SyntaxRecognizingEditor editor, boolean bStructural, PrecedenceHandler precedenceHandler, List<TreeEventListener> editListeners)
	{
		this( editor, bStructural, precedenceHandler, editListeners, null );
	}
	
	public SRFragmentEditor(SyntaxRecognizingEditor editor, boolean bStructural, List<TreeEventListener> editListeners)
	{
		this( editor, bStructural, null, editListeners, null );
	}

	
	public Pres editFragment(Pres view, Object model, SimpleAttributeTable inheritedState)
	{
		Object editModeObj = inheritedState.getOptional( "gSym_SREditor_edit" );
		EditMode editMode = editModeObj instanceof EditMode  ?  (EditMode)editModeObj  :  EditMode.DISPLAY;
		
		if ( precedenceHandler != null )
		{
			view = precedenceHandler.applyPrecedenceBrackets( model, view, inheritedState );
		}
		
		if ( editMode == EditMode.EDIT )
		{
			if ( bStructural )
			{
				view = new EditableStructuralItem( editor, editListeners, model, view );
			}
			else
			{
				view = new EditableSequentialItem( editListeners, view );
			}
			
			for (AbstractElementInteractor interactor: elementInteractors)
			{
				view = view.withElementInteractor( interactor );
			}
		}
		
		return view;
	}
}
