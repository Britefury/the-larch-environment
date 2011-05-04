//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Clipboard;

import BritefuryJ.DocPresent.Selection.Selection;
import BritefuryJ.DocPresent.Target.Target;

public class SelectionEditor <SelectionType extends Selection> extends SelectionEditorInterface
{
	public interface ReplaceSelectionWithTextFn <SelectionType extends Selection>
	{
		public boolean replaceSelectionWithText(SelectionType selection, Target target, String replacement);
	}

	public interface DeleteSelectionFn <SelectionType extends Selection>
	{
		public boolean deleteSelection(SelectionType selection, Target target);
	}

	
	
	private Class<? extends Selection> selectionClass;
	private ReplaceSelectionWithTextFn<SelectionType> replaceSelectionWithTextFn;
	private DeleteSelectionFn<SelectionType> deleteSelectionFn;
	
	
	public SelectionEditor(Class<? extends Selection> selectionClass, ReplaceSelectionWithTextFn<SelectionType> replaceSelectionWithTextFn,  DeleteSelectionFn<SelectionType> deleteSelectionFn)
	{
		this.selectionClass = selectionClass;
		this.replaceSelectionWithTextFn = replaceSelectionWithTextFn;
		this.deleteSelectionFn = deleteSelectionFn;
	}

	public SelectionEditor(Class<? extends Selection> selectionClass, ReplaceSelectionWithTextFn<SelectionType> replaceSelectionWithTextFn)
	{
		this( selectionClass, replaceSelectionWithTextFn, null );
	}

	
	@Override
	public Class<? extends Selection> getSelectionClass()
	{
		return selectionClass;
	}

	public ReplaceSelectionWithTextFn<SelectionType> getReplaceSelectionWithTextFn()
	{
		return replaceSelectionWithTextFn;
	}
	
	public DeleteSelectionFn<SelectionType> getDeleteSelectionFn()
	{
		return deleteSelectionFn;
	}
	
	
	
	@SuppressWarnings("unchecked")
	protected boolean replaceSelectionWithText(Selection selection, Target target, String replacement)
	{
		if ( replaceSelectionWithTextFn != null )
		{
			if ( !selectionClass.isInstance( selection ) )
			{
				throw new RuntimeException( "SelectionEditor.replaceSelectionWithText(): selection is not an instance of " + selectionClass.getName() );
			}
			
			return replaceSelectionWithTextFn.replaceSelectionWithText( (SelectionType)selection, target, replacement );
		}
		else
		{
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean deleteSelection(Selection selection, Target target)
	{
		if ( deleteSelectionFn != null )
		{
			if ( !selectionClass.isInstance( selection ) )
			{
				throw new RuntimeException( "SelectionEditor.deleteSelection(): selection is not an instance of " + selectionClass.getName() );
			}
			
			return deleteSelectionFn.deleteSelection( (SelectionType)selection, target );
		}
		else
		{
			return replaceSelectionWithText( selection, target, "" );
		}
	}
}
