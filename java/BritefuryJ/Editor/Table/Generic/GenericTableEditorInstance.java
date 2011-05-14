//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.Generic;

import java.util.List;

import BritefuryJ.Editor.Table.AbstractTableEditorInstance;
import BritefuryJ.Editor.Table.TableEditorStyle;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.HiddenContent;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Table;

public class GenericTableEditorInstance extends AbstractTableEditorInstance<GenericTableModelInterface>
{
	protected GenericTableEditorInstance(GenericTableEditor editor, GenericTableModelInterface model, boolean editable)
	{
		super( editor, model, editable );
	}

	
	@Override
	protected Pres presentTable()
	{
		int width = model.getWidth();
		int height = model.getHeight();
		GenericTableEditor genericEditor = (GenericTableEditor)editor;
		
		int numColumns = tableWToElementW( width );
		int headerColumns = tableWToHeaderW( width );
		int numRows = tableHToElementH( height );
		Object cells[][] = new Object[numRows][];
		
		if ( hasTopHeader() )
		{
			Object firstRow[] = new Object[headerColumns];
			if ( hasLeftHeader() )
			{
				firstRow[0] = new HiddenContent( "" );
			}
			for (int x = 0; x < width; x++)
			{
				int ex = tableXToElementX( x );
				firstRow[ex] = new Label( String.valueOf( x ) ).withStyleSheetFromAttr( TableEditorStyle.headerAttrs );
			}
			cells[0] = firstRow;
		}
		
		for (int y = 0; y < height; y++)
		{
			List<?> row = model.getRow( y );
			int ey = tableYToElementY( y );
			int rowCols = tableWToElementW( row.size() );
			
			if ( canGrowRight() )
			{
				Object tableRow[] = new Object[rowCols];
				if ( hasLeftHeader() )
				{
					tableRow[0] = new Label( String.valueOf( y ) ).withStyleSheetFromAttr( TableEditorStyle.headerAttrs );
				}
				for (int x = 0; x < row.size(); x++)
				{
					int ex = tableXToElementX( x );
					tableRow[ex] = new GenericTableCell( model, x, y );
				}
				tableRow[rowCols-1] = new GenericBlankTableCell( model, row.size(), y, genericEditor.blankPres );
				cells[ey] = tableRow;
			}
			else
			{
				Object tableRow[] = new Object[rowCols];
				if ( hasLeftHeader() )
				{
					tableRow[0] = new Label( String.valueOf( y ) ).withStyleSheetFromAttr( TableEditorStyle.headerAttrs );
				}
				for (int x = 0; x < row.size(); x++)
				{
					int ex = tableXToElementX( x );
					tableRow[ex] = new GenericTableCell( model, x, y );
				}
				cells[ey] = tableRow;
			}
		}
		
		if ( canGrowDown() )
		{
			Object lastRow[] = new Object[numColumns];
			if ( hasLeftHeader() )
			{
				lastRow[0] = new HiddenContent( "" );
			}
			for (int x = 0; x < width; x++)
			{
				int ex = tableXToElementX( x );
				lastRow[ex] = new GenericBlankTableCell( model, x, height, genericEditor.blankPres );
			}
			if ( canGrowRight() )
			{
				int ex = tableXToElementX( width );
				lastRow[ex] = new GenericBlankTableCell( model, width, height, genericEditor.blankPres );
			}
			cells[numRows-1] = lastRow;
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


	@Override
	protected int getMaxRowWidth()
	{
		return model.getWidth();
	}
}
