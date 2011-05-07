//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.Generic;

import java.util.List;

import BritefuryJ.Editor.Table.AbstractTableEditorInstance;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Table;

public class GenericTableEditorInstance extends AbstractTableEditorInstance<GenericTableModelInterface>
{
	protected GenericTableEditorInstance(GenericTableEditor editor, GenericTableModelInterface model)
	{
		super( editor, model );
	}

	
	@Override
	protected Pres presentTable()
	{
		int width = model.getWidth();
		int height = model.getHeight();
		GenericTableEditor genericEditor = (GenericTableEditor)editor;
		
		int numColumns = genericEditor.showEmptyColumnAtRight  ?  width + 1  :  width;
		int numRows = genericEditor.showEmptyRowAtBottom  ?  height + 1  :  height;
		Object cells[][] = new Object[numRows][];
		
		for (int y = 0; y < height; y++)
		{
			List<?> row = model.getRow( y );
			
			if ( genericEditor.showEmptyColumnAtRight )
			{
				cells[y] = new Object[numColumns];
				for (int x = 0; x < row.size(); x++)
				{
					cells[y][x] = new GenericTableCell( model, x, y );
				}
				cells[y][row.size()] = new GenericBlankTableCell( model, row.size(), y, genericEditor.blankPres );
			}
			else
			{
				cells[y] = new Object[row.size()];
				for (int x = 0; x < row.size(); x++)
				{
					cells[y][x] = new GenericTableCell( model, x, y );
				}
			}
		}
		
		if ( genericEditor.showEmptyRowAtBottom )
		{
			Object lastRow[] = new Object[numColumns];
			for (int x = 0; x < numColumns; x++)
			{
				lastRow[x] = new GenericBlankTableCell( model, x, height, genericEditor.blankPres );
			}
			cells[height] = lastRow;
		}
		
		return new Table( cells );
	}


	
	@Override
	protected int getHeight()
	{
		return model.getHeight();
	}


	@Override
	protected int getRowWidth(int row)
	{
		return model.getRow( row ).size();
	}
}
