//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyTuple;

import BritefuryJ.DocPresent.Layout.TableLayout;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeTable;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;

public class DPTable extends DPContainer
{
	private static class TableChildEntry extends TableLayout.TablePackingParams
	{
		private DPWidget child;

		public TableChildEntry(DPWidget child, int x, int colSpan, int y, int rowSpan)
		{
			super( x, colSpan, y, rowSpan );
			this.child = child;
		}
	}



	private TableChildEntry[][] childEntryTable;
	private ArrayList<TableChildEntry> childEntries;
	private int rowPositions[];
	private int numColumns, numRows;		// Can be -1, indicating that these values must be refreshed

	
	
	
	public DPTable()
	{
		this( TableStyleParams.defaultStyleParams);
	}

	public DPTable(TableStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeTable( this );

		childEntryTable = new TableChildEntry[0][];
		childEntries = new ArrayList<TableChildEntry>();
		rowPositions = new int[0];
		numColumns = 0;
		numRows = 0;
	}

	
	
	
	public void setChildren(DPWidget[][] itemTable)
	{
		for (DPWidget row[]: itemTable)
		{
			for (DPWidget item: row)
			{
				if ( item.getLayoutNode() == null )
				{
					throw new ChildHasNoLayoutException();
				}
			}
		}
		
		if ( registeredChildren.isEmpty() )
		{
			// Empty; initialise from blank
			numColumns = 0;
			numRows = itemTable.length;

			// - Count children
			// - Generate row positions
			// - Compute number of columns
			childEntryTable = new TableChildEntry[itemTable.length][];
			rowPositions = new int[itemTable.length];
			int n = 0;
			int rowI = 0;
			for (DPWidget[] row: itemTable)
			{
				rowPositions[rowI] = n;
				numColumns = Math.max( numColumns, row.length );
				
				for (DPWidget child: row)
				{
					if ( child != null )
					{
						n++;
					}
				}
				rowI++;
			}

			
			// - Generate the list of registered children; an ordered list of all child widgets
			// - Create a table child entry for each child
			// - Register each child
			rowI = 0;
			registeredChildren.ensureCapacity( n );
			childEntries.ensureCapacity( n );
			for (DPWidget[] row: itemTable)
			{
				TableChildEntry destRow[] = new TableChildEntry[row.length];
				childEntryTable[rowI] = destRow;

				rowPositions[rowI] = registeredChildren.size();
				numColumns = Math.max( numColumns, row.length );
				
				int columnI = 0;
				for (DPWidget child: row)
				{
					if ( child != null )
					{
						TableChildEntry entry = new TableChildEntry( child, columnI, 1, rowI, 1 );
						registeredChildren.add( child );
						childEntries.add( entry );
						destRow[columnI] = entry;
						registerChild( child );
					}
					columnI++;
				}
				rowI++;
			}
		}
		else
		{
			HashSet<DPWidget> items, added, removed;
			
			numColumns = 0;
			numRows = itemTable.length;
			// Get a set of items being introduced
			items = new HashSet<DPWidget>();
			for (DPWidget[] row: itemTable)
			{
				numColumns = Math.max( numColumns, row.length );
				items.addAll( Arrays.asList( row ) );
			}
			items.remove( null );
			
			// Work out the set of widgets that are being added to this table
			// and the set that are being removed from this table
			added = new HashSet<DPWidget>( items );
			removed = new HashSet<DPWidget>( registeredChildren );
			added.removeAll( registeredChildren );
			removed.removeAll( items );

			
			// Unregister removed children
			for (DPWidget child: removed)
			{
				unregisterChild( child );
			}
			
			// - Count children
			// - Generate row positions
			// - Compute number of columns
			childEntryTable = new TableChildEntry[itemTable.length][];
			rowPositions = new int[itemTable.length];
			int n = 0;
			int rowI = 0;
			for (DPWidget[] row: itemTable)
			{
				rowPositions[rowI] = n;
				numColumns = Math.max( numColumns, row.length );
				
				for (DPWidget child: row)
				{
					if ( child != null )
					{
						n++;
					}
				}
				rowI++;
			}

			
			// - Generate the list of registered children; an ordered list of all child widgets
			// - Create a table child entry for each child
			// - Register each child
			rowI = 0;
			registeredChildren.ensureCapacity( n );
			childEntries.ensureCapacity( n );
			for (DPWidget[] row: itemTable)
			{
				TableChildEntry destRow[] = new TableChildEntry[row.length];
				childEntryTable[rowI] = destRow;

				rowPositions[rowI] = registeredChildren.size();
				numColumns = Math.max( numColumns, row.length );
				
				int columnI = 0;
				for (DPWidget child: row)
				{
					if ( child != null )
					{
						TableChildEntry entry = new TableChildEntry( child, columnI, 1, rowI, 1 );
						registeredChildren.add( child );
						childEntries.add( entry );
						destRow[columnI] = entry;
					}
					columnI++;
				}
				rowI++;
			}
			
			// Register added children
			for (DPWidget child: added)
			{
				registerChild( child );
			}
		}
		
		
		onChildListModified();
		queueResize();
	}

	public void setChildren(List<List<DPWidget>> itemTable)
	{
		DPWidget itemArray[][] = new DPWidget[itemTable.size()][];
		int i = 0;
		for (List<DPWidget> srcRow: itemTable)
		{
			DPWidget row[] = new DPWidget[srcRow.size()];
			itemArray[i++] = srcRow.toArray( row );
		}
		setChildren( itemArray );
	}
	

	
	
	
	public int width()
	{
		refreshSize();
		return numColumns;
	}
	
	public int height()
	{
		refreshSize();
		return numRows;
	}
	
	
	public DPWidget get(int x, int y)
	{
		if ( x >= numColumns )
		{
			throw new IndexOutOfBoundsException();
		}
		if ( y >= numRows )
		{
			throw new IndexOutOfBoundsException();
		}
		TableChildEntry row[] = y >= childEntryTable.length  ?  null  :  childEntryTable[y];
		if ( row != null )
		{
			if ( x < row.length )
			{
				return row[x].child;
			}
		}
		
		return null;
	}
	
	public DPWidget __getitem__(PyTuple index)
	{
		if ( index.size() != 2 )
		{
			throw Py.ValueError( "Index should be a tuple of two integers" );
		}
		int x = index.__getitem__( 0 ).asInt();
		int y = index.__getitem__( 1 ).asInt();
		return get( x, y );
	}
	
	
	public void put(int x, int y, int colSpan, int rowSpan, DPWidget item)
	{
		// Get the child that is being replaced
		TableChildEntry oldChildEntry = null, childEntry = null;
		DPWidget oldChild = null;
		int oldEndX = -1, oldEndY = -1;
		if ( y < childEntryTable.length )
		{
			TableChildEntry row[] = childEntryTable[y];
			if ( x < row.length )
			{
				oldChildEntry = row[x];
				if ( oldChildEntry != null )
				{
					oldChild = oldChildEntry.child;
					oldEndX = x + oldChildEntry.colSpan;
					oldEndY = y + oldChildEntry.rowSpan;
				}
			}
		}
		
		if ( item != null  &&  item.getLayoutNode() == null )
		{
			throw new ChildHasNoLayoutException();
		}

		if ( item != oldChild )
		{
			// Get the index of the old child (in @registeredChildren), unregister the old child, and remove it from @registeredChildren
			int oldChildIndex = -1;
			if ( oldChildEntry != null )
			{
				oldChildIndex = childEntries.indexOf( oldChildEntry );
				unregisterChild( oldChildEntry.child );
			}
			
			//
			// If y is outside the bounds of the number of rows, add as many new rows as necessary
			if ( y >= childEntryTable.length  &&  item != null )
			{
				TableChildEntry ch[][] = new TableChildEntry[y+1][];
				int rp[] = new int[y+1];
				System.arraycopy( childEntryTable, 0, ch, 0, childEntryTable.length );
				System.arraycopy( rowPositions, 0, rp, 0, rowPositions.length );
				for (int i = childEntryTable.length; i < ch.length; i++)
				{
					ch[i] = new TableChildEntry[0];
					rp[i] = registeredChildren.size();
				}
				childEntryTable = ch;
				rowPositions = rp;
			}
			
			// Get the row into which the child is being inserted
			TableChildEntry[] row = childEntryTable[y];
			// Enlarge the row if x is outside the bounds of the row
			if ( x >= row.length  &&  item != null )
			{
				TableChildEntry r[] = new TableChildEntry[x+1];
				System.arraycopy( row, 0, r, 0, row.length );
				childEntryTable[y] = r;
				row = r;
			}
			
			// Place the child into the row
			childEntry = item != null  ?  new TableChildEntry( item, x, colSpan, y, rowSpan )  :  null;
			row[x] = childEntry;
			
			// Add the child to @registeredChildren
			if ( oldChildIndex == -1 )
			{
				if ( item != null )
				{
					// Don't need to worry about the possibility of @item being null;
					// If it is null, then we can only get this far if it is replacing an existing non-null
					// child, else we won't get this far, due to the if-statement which ensures
					// that work is not done, if a child is being replaced by itself
					int insertionIndex = rowPositions[y];
					for (int i = 0; i < x; i++)
					{
						if ( row[i] != null )
						{
							insertionIndex++;
						}
					}
					registeredChildren.add( insertionIndex, item );
					childEntries.add( insertionIndex, childEntry );
					for (int i = y + 1; i < rowPositions.length; i++)
					{
						rowPositions[i]++;
					}
				}
			}
			else
			{
				if ( item == null   )
				{
					// Removing an existing child
					registeredChildren.remove( oldChildIndex );
					childEntries.remove( oldChildIndex );
					for (int i = y + 1; i < rowPositions.length; i++)
					{
						rowPositions[i]--;
					}
				}
				else
				{
					// Replacing an existing child
					registeredChildren.set( oldChildIndex, item );
					childEntries.set( oldChildIndex, childEntry );
				}
			}
			
			// Register the child
			if ( item != null )
			{
				registerChild( item );
			}

			onChildListModified();
			queueResize();
		}
		else
		{
			if ( item != null )
			{
				queueResize();
			}
		}
		
		
		// Update numColumns and numRows
		if ( ( item == null  &&  oldChildEntry != null ) )
		{
			// Removing a child; refresh later
			numColumns = -1;
			numRows = -1;
		}
		
		if ( item != null  &&  oldChildEntry != null )
		{
			// Replacing a child; refresh later if 
			if ( ( x + colSpan )  <  oldEndX )
			{
				numColumns = -1;
			}
			if ( ( y + rowSpan )  <  oldEndY )
			{
				numRows = -1;
			}
		}
		
		if ( item != null )
		{
			if ( ( ( x + colSpan )  >  oldEndX  ||  oldEndX == -1 )  &&  numColumns != -1 )
			{
				numColumns = Math.max( numColumns, x + colSpan );
			}
			if ( ( ( y + rowSpan )  >  oldEndY  ||  oldEndY == -1 )  &&  numRows != -1 )
			{
				numRows = Math.max( numRows, y + rowSpan );
			}
		}
	}
	
	public void put(int x, int y, DPWidget item)
	{
		put( x, y, 1, 1, item );
	}

	public void __setitem__(PyTuple index, DPWidget item)
	{
		if ( index.size() != 2 )
		{
			throw Py.ValueError( "Index should be a tuple of two integers" );
		}
		int x = index.__getitem__( 0 ).asInt();
		int y = index.__getitem__( 1 ).asInt();
		put( x, y, item );
	}
	
	

	private TableChildEntry getEntryForChild(DPWidget child)
	{
		for (TableChildEntry entry: childEntries)
		{
			if ( child == entry.child )
			{
				return entry;
			}
		}
		
		return null;
	}

	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		TableChildEntry entry = getEntryForChild( child );
		if ( entry != null )
		{
			put( entry.x, entry.y, entry.colSpan, entry.rowSpan, null );
		}
	}
	
	
	
	public List<DPWidget> getLayoutChildren()
	{
		return registeredChildren;
	}

	public List<? extends TableLayout.TablePackingParams> getTablePackingParams()
	{
		return childEntries;
	}

	public TableLayout.TablePackingParams[] getTablePackingParamsArray()
	{
		TableLayout.TablePackingParams a[] = new TableLayout.TablePackingParams[childEntries.size()];
		a = childEntries.toArray( a );
		return a;
	}

	public TableLayout.TablePackingParams getTablePackingParamsForChild(DPWidget child)
	{
		return getEntryForChild( child );
	}
	
	
	public List<DPWidget> getChildren()
	{
		return registeredChildren;
	}
	
	
	public int getChildX(DPWidget child)
	{
		TableChildEntry entry = getEntryForChild( child );
		return entry.x;
	}
	
	public int getChildY(DPWidget child)
	{
		TableChildEntry entry = getEntryForChild( child );
		return entry.y;
	}
	
	public int getChildColSpan(DPWidget child)
	{
		TableChildEntry entry = getEntryForChild( child );
		return entry.colSpan;
	}
	
	public int getChildRowSpan(DPWidget child)
	{
		TableChildEntry entry = getEntryForChild( child );
		return entry.rowSpan;
	}
	
	
	private void refreshSize()
	{
		// Refresh numColumns and numRows
		if ( numColumns == -1  ||  numRows == -1 )
		{
			numColumns = 0;
			numRows = 0;
			
			for (TableChildEntry row[]: childEntryTable)
			{
				for (TableChildEntry child: row)
				{
					if ( child != null )
					{
						numColumns = Math.max( numColumns, child.x + child.colSpan );
						numRows = Math.max( numRows, child.y + child.rowSpan );
					}
				}
			}
		}
	}
	
	

	//
	//
	// STYLE METHODS
	//
	//
	
	protected double getColumnSpacing()
	{
		return ((TableStyleParams) styleParams).getColumnSpacing();
	}

	protected boolean getColumnExpand()
	{
		return ((TableStyleParams) styleParams).getColumnExpand();
	}

	
	protected double getRowSpacing()
	{
		return ((TableStyleParams) styleParams).getRowSpacing();
	}

	protected boolean getRowExpand()
	{
		return ((TableStyleParams) styleParams).getRowExpand();
	}
}
