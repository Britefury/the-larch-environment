//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Text;

public class GenericTableEditor extends AbstractTableEditor<GenericTableModelInterface>
{
	public interface EmptyCellFactory
	{
		Pres createEmptyCell();
	}
	
	
	private static final EmptyCellFactory emptyTextCellFactory = new EmptyCellFactory()
	{
		@Override
		public Pres createEmptyCell()
		{
			return new Text( "" );
		}
	};
	
	
	protected boolean showEmptyRowAtBottom, showEmptyColumnAtRight;
	protected EmptyCellFactory emptyCellFac;
	
	
	
	
	public GenericTableEditor(boolean showEmptyRowAtBottom, boolean showEmptyColumnAtRight, EmptyCellFactory emptyCellFac)
	{
		this.showEmptyRowAtBottom = showEmptyRowAtBottom;
		this.showEmptyColumnAtRight = showEmptyColumnAtRight;
		this.emptyCellFac = emptyCellFac;
	}
	
	public GenericTableEditor(boolean showEmptyRowAtBottom, boolean showEmptyColumnAtRight)
	{
		this( showEmptyRowAtBottom, showEmptyColumnAtRight, emptyTextCellFactory );
	}
	
	
	
	@Override
	protected void checkModel(GenericTableModelInterface model)
	{
		if ( !( model instanceof GenericTableModelInterface ) )
		{
			throw new RuntimeException( "Cannot edit model, as it is not an instance of GenericModelInterface" );
		}
	}

	@Override
	protected AbstractTableEditorInstance<GenericTableModelInterface> createInstance()
	{
		return new GenericTableEditorInstance( this );
	}

	@Override
	protected Object[][] getBlock(GenericTableModelInterface model, int x, int y, int w, int h)
	{
		return model.getBlock( x, y, w, h );
	}

	@Override
	protected void putBlock(GenericTableModelInterface model, int x, int y, Object[][] data, AbstractTableEditorInstance<GenericTableModelInterface> editorInstance)
	{
		model.putBlock( x, y, data );
	}
}
