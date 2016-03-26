//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TableElement;
import BritefuryJ.LSpace.Focus.Selection;
import BritefuryJ.LSpace.Focus.SelectionPoint;

class TableSelectionPoint extends SelectionPoint
{
	private AbstractTableEditorInstance<?> editorInstance;
	private TableElement table;
	private int x, y;
	
	
	public TableSelectionPoint(AbstractTableEditorInstance<?> editorInstance, TableElement table, int x, int y)
	{
		this.editorInstance = editorInstance;
		this.table = table;
		this.x = x;
		this.y = y;
	}
	
	
	@Override
	public boolean isValid()
	{
		return ((LSElement)table).isRealised();
	}
	
	
	@Override
	public Selection createSelectionTo(SelectionPoint point)
	{
		if ( point instanceof TableSelectionPoint )
		{
			TableSelectionPoint tablePoint = (TableSelectionPoint)point;
			if ( table == tablePoint.table  &&  editorInstance == tablePoint.editorInstance )
			{
				int xa = x, ya = y, xb = tablePoint.x, yb = tablePoint.y;
				
				return new TableSelection( editorInstance, table, Math.min( xa, xb ), Math.min( ya, yb ), Math.max( xa, xb ), Math.max( ya, yb ) );
			}
		}

		return null;
	}

}
