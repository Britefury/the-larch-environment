//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.Generic;

import BritefuryJ.Cell.PrimitiveCellEditPresenter;
import BritefuryJ.Editor.Table.AbstractTableEditor;
import BritefuryJ.Editor.Table.AbstractTableEditorInstance;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Util.UnaryFn;

public class GenericTableEditor extends AbstractTableEditor<GenericTableModelInterface>
{
	protected Pres blankPres;
	
	
	
	
	public GenericTableEditor(boolean showLeftHeader, boolean showTopHeader, boolean growRight, boolean growDown, UnaryFn converValueFn)
	{
		super( showLeftHeader, showTopHeader, growRight, growDown );
		this.blankPres = PrimitiveCellEditPresenter.presentEditableText( "", converValueFn );
	}
	
	public GenericTableEditor(boolean showLeftHeader, boolean showTopHeader, boolean growRight, boolean growDown)
	{
		this( showLeftHeader, showTopHeader, growRight, growDown, UnaryFn.identity );
	}
	
	
	
	
	
	@Override
	protected GenericTableModelInterface coerceModel(Object model)
	{
		return (GenericTableModelInterface)model;
	}

	@Override
	protected AbstractTableEditorInstance<GenericTableModelInterface> createInstance(GenericTableModelInterface model, boolean editable)
	{
		return new GenericTableEditorInstance( this, model, editable );
	}

	@Override
	protected Object[][] getBlock(GenericTableModelInterface model, int x, int y, int w, int h)
	{
		if ( y < model.getHeight() )
		{
			// Ensure the the height of the block does not go beyond the last row
			h = Math.min( h, model.getHeight() - y );
			
			return model.getBlock( x, y, w, h );
		}
		else
		{
			return new Object[0][];
		}
	}

	@Override
	protected void putBlock(GenericTableModelInterface model, int x, int y, Object[][] data, AbstractTableEditorInstance<GenericTableModelInterface> editorInstance)
	{
		model.putBlock( x, y, data );
	}

	@Override
	protected void deleteBlock(GenericTableModelInterface model, int x, int y, int w, int h, AbstractTableEditorInstance<GenericTableModelInterface> editorInstance)
	{
		model.deleteBlock( x, y, w, h );
	}
}
