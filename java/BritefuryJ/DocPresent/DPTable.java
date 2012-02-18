//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyTuple;

import BritefuryJ.DocPresent.Layout.TableLayout;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeTable;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;
import BritefuryJ.Math.Point2;

public class DPTable extends DPContainerNonOverlayed implements TableElement
{
	private static class TableChildEntry extends TableLayout.TablePackingParams
	{
		private DPElement child;

		public TableChildEntry(DPElement child, int x, int colSpan, int y, int rowSpan)
		{
			super( x, colSpan, y, rowSpan );
			this.child = child;
		}
	}
	
	
	public static class TableCell
	{
		private DPElement child;
		private int colSpan, rowSpan;
		
		
		public TableCell(DPElement child, int colSpan, int rowSpan)
		{
			this.child = child;
			this.colSpan = colSpan;
			this.rowSpan = rowSpan;
		}
	}



	private TableChildEntry[][] childEntryTable;
	private ArrayList<TableChildEntry> childEntries;
	private int rowStartIndices[];			// Indices into @childEntries, where each row starts
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
		rowStartIndices = new int[0];
		numColumns = 0;
		numRows = 0;
	}
	
	
	
	public void setChildren(DPElement[][] itemTable)
	{
		for (DPElement row[]: itemTable)
		{
			for (DPElement item: row)
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
			rowStartIndices = new int[itemTable.length];
			int n = 0;
			int rowI = 0;
			for (DPElement[] row: itemTable)
			{
				rowStartIndices[rowI] = n;
				numColumns = Math.max( numColumns, row.length );
				
				for (DPElement child: row)
				{
					if ( child != null )
					{
						n++;
					}
				}
				rowI++;
			}

			
			// - Generate the list of registered children; an ordered list of all child elements
			// - Create a table child entry for each child
			// - Register each child
			rowI = 0;
			registeredChildren.ensureCapacity( n );
			childEntries.ensureCapacity( n );
			for (DPElement[] row: itemTable)
			{
				TableChildEntry destRow[] = new TableChildEntry[row.length];
				childEntryTable[rowI] = destRow;

				rowStartIndices[rowI] = registeredChildren.size();
				numColumns = Math.max( numColumns, row.length );
				
				int columnI = 0;
				for (DPElement child: row)
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
			HashSet<DPElement> items, added, removed;
			
			numColumns = 0;
			numRows = itemTable.length;
			// Get a set of items being introduced
			items = new HashSet<DPElement>();
			for (DPElement[] row: itemTable)
			{
				numColumns = Math.max( numColumns, row.length );
				items.addAll( Arrays.asList( row ) );
			}
			items.remove( null );
			
			// Work out the set of elements that are being added to this table
			// and the set that are being removed from this table
			added = new HashSet<DPElement>( items );
			removed = new HashSet<DPElement>( registeredChildren );
			added.removeAll( registeredChildren );
			removed.removeAll( items );

			
			// Unregister removed children
			for (DPElement child: removed)
			{
				unregisterChild( child );
			}
			
			// - Count children
			// - Generate row positions
			// - Compute number of columns
			childEntryTable = new TableChildEntry[itemTable.length][];
			rowStartIndices = new int[itemTable.length];
			int n = 0;
			int rowI = 0;
			for (DPElement[] row: itemTable)
			{
				rowStartIndices[rowI] = n;
				numColumns = Math.max( numColumns, row.length );
				
				for (DPElement child: row)
				{
					if ( child != null )
					{
						n++;
					}
				}
				rowI++;
			}

			
			// - Generate the list of registered children; an ordered list of all child elements
			// - Create a table child entry for each child
			// - Register each child
			rowI = 0;
			registeredChildren.ensureCapacity( n );
			childEntries.ensureCapacity( n );
			for (DPElement[] row: itemTable)
			{
				TableChildEntry destRow[] = new TableChildEntry[row.length];
				childEntryTable[rowI] = destRow;

				rowStartIndices[rowI] = registeredChildren.size();
				numColumns = Math.max( numColumns, row.length );
				
				int columnI = 0;
				for (DPElement child: row)
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
			for (DPElement child: added)
			{
				registerChild( child );
			}
		}
		
		
		onChildListModified();
		queueResize();
	}

	public void setChildren(List<List<DPElement>> itemTable)
	{
		DPElement itemArray[][] = new DPElement[itemTable.size()][];
		int i = 0;
		for (List<DPElement> srcRow: itemTable)
		{
			itemArray[i++] = srcRow.toArray( new DPElement[srcRow.size()] );
		}
		setChildren( itemArray );
	}
	

	
	public void setCells(TableCell[][] cellTable)
	{
		for (TableCell row[]: cellTable)
		{
			for (TableCell item: row)
			{
				if ( item != null  &&  item.child.getLayoutNode() == null )
				{
					throw new ChildHasNoLayoutException();
				}
			}
		}
		
		if ( registeredChildren.isEmpty() )
		{
			// Empty; initialise from blank
			numColumns = 0;
			numRows = 0;

			// - Count children
			// - Generate row positions
			// - Compute number of columns
			childEntryTable = new TableChildEntry[cellTable.length][];
			rowStartIndices = new int[cellTable.length];
			int n = 0;
			int rowI = 0;
			for (TableCell[] row: cellTable)
			{
				rowStartIndices[rowI] = n;
				int colI = 0;
				for (TableCell cell: row)
				{
					if ( cell != null )
					{
						numColumns = Math.max( numColumns, colI + cell.colSpan );
						numRows = Math.max( numRows, rowI + cell.rowSpan );
						n++;
					}
					colI++;
				}
				rowI++;
			}

			
			// - Generate the list of registered children; an ordered list of all child elements
			// - Create a table child entry for each child
			// - Register each child
			rowI = 0;
			registeredChildren.ensureCapacity( n );
			childEntries.ensureCapacity( n );
			for (TableCell[] row: cellTable)
			{
				TableChildEntry destRow[] = new TableChildEntry[numColumns];
				childEntryTable[rowI] = destRow;

				rowStartIndices[rowI] = registeredChildren.size();
				
				int columnI = 0;
				for (TableCell cell: row)
				{
					if ( cell != null )
					{
						TableChildEntry entry = new TableChildEntry( cell.child, columnI, cell.colSpan, rowI, cell.rowSpan );
						registeredChildren.add( cell.child );
						childEntries.add( entry );
						destRow[columnI] = entry;
						registerChild( cell.child );
					}
					columnI++;
				}
				rowI++;
			}
		}
		else
		{
			HashSet<DPElement> items, added, removed;
			
			numColumns = 0;
			numRows = 0;
			// Get a set of items being introduced
			items = new HashSet<DPElement>();
			for (TableCell[] row: cellTable)
			{
				for (TableCell cell: row)
				{
					items.add( cell.child );
				}
			}
			items.remove( null );
			
			// Work out the set of elements that are being added to this table
			// and the set that are being removed from this table
			added = new HashSet<DPElement>( items );
			removed = new HashSet<DPElement>( registeredChildren );
			added.removeAll( registeredChildren );
			removed.removeAll( items );

			
			// Unregister removed children
			for (DPElement child: removed)
			{
				unregisterChild( child );
			}
			
			// - Count children
			// - Generate row positions
			// - Compute number of columns
			childEntryTable = new TableChildEntry[cellTable.length][];
			rowStartIndices = new int[cellTable.length];
			int n = 0;
			int rowI = 0;
			for (TableCell[] row: cellTable)
			{
				rowStartIndices[rowI] = n;
				int colI = 0;
				for (TableCell cell: row)
				{
					if ( cell != null )
					{
						numColumns = Math.max( numColumns, colI + cell.colSpan );
						numRows = Math.max( numRows, rowI + cell.rowSpan );
						n++;
					}
					colI++;
				}
				rowI++;
			}

			
			// - Generate the list of registered children; an ordered list of all child elements
			// - Create a table child entry for each child
			// - Register each child
			rowI = 0;
			registeredChildren.ensureCapacity( n );
			childEntries.ensureCapacity( n );
			for (TableCell[] row: cellTable)
			{
				TableChildEntry destRow[] = new TableChildEntry[row.length];
				childEntryTable[rowI] = destRow;

				rowStartIndices[rowI] = registeredChildren.size();
				
				int columnI = 0;
				for (TableCell cell: row)
				{
					if ( cell != null )
					{
						TableChildEntry entry = new TableChildEntry( cell.child, columnI, cell.colSpan, rowI, cell.rowSpan );
						registeredChildren.add( cell.child );
						childEntries.add( entry );
						destRow[columnI] = entry;
					}
					columnI++;
				}
				rowI++;
			}
			
			// Register added children
			for (DPElement child: added)
			{
				registerChild( child );
			}
		}
		
		
		onChildListModified();
		queueResize();
	}

	
	
	public int getNumColumns()
	{
		refreshSize();
		return numColumns;
	}
	
	public int getNumRows()
	{
		refreshSize();
		return numRows;
	}
	
	
	public DPElement get(int x, int y)
	{
		TableChildEntry childEntry = getChildEntry( x, y );
		return childEntry != null  ?  childEntry.child  :  null;
	}
	
	public boolean hasChildAt(int x, int y)
	{
		TableChildEntry childEntry = getChildEntry( x, y );
		return childEntry != null;
	}
	
	private TableChildEntry getChildEntry(int x, int y)
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
				return row[x];
			}
		}
		
		return null;
	}
	
	public DPElement __getitem__(PyTuple index)
	{
		if ( index.size() != 2 )
		{
			throw Py.ValueError( "Index should be a tuple of two integers" );
		}
		int x = index.__getitem__( 0 ).asInt();
		int y = index.__getitem__( 1 ).asInt();
		return get( x, y );
	}
	
	
	public void put(int x, int y, int colSpan, int rowSpan, DPElement item)
	{
		// Get the child that is being replaced
		TableChildEntry oldChildEntry = null, childEntry = null;
		DPElement oldChild = null;
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
				System.arraycopy( rowStartIndices, 0, rp, 0, rowStartIndices.length );
				for (int i = childEntryTable.length; i < ch.length; i++)
				{
					ch[i] = new TableChildEntry[0];
					rp[i] = registeredChildren.size();
				}
				childEntryTable = ch;
				rowStartIndices = rp;
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
					int insertionIndex = rowStartIndices[y];
					for (int i = 0; i < x; i++)
					{
						if ( row[i] != null )
						{
							insertionIndex++;
						}
					}
					registeredChildren.add( insertionIndex, item );
					childEntries.add( insertionIndex, childEntry );
					for (int i = y + 1; i < rowStartIndices.length; i++)
					{
						rowStartIndices[i]++;
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
					for (int i = y + 1; i < rowStartIndices.length; i++)
					{
						rowStartIndices[i]--;
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
	
	public void put(int x, int y, DPElement item)
	{
		put( x, y, 1, 1, item );
	}

	public void __setitem__(PyTuple index, DPElement item)
	{
		if ( index.size() != 2 )
		{
			throw Py.ValueError( "Index should be a tuple of two integers" );
		}
		int x = index.__getitem__( 0 ).asInt();
		int y = index.__getitem__( 1 ).asInt();
		put( x, y, item );
	}
	
	

	private TableChildEntry getEntryForChild(DPElement child)
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

	
	
	protected void replaceChildWithEmpty(DPElement child)
	{
		TableChildEntry entry = getEntryForChild( child );
		if ( entry != null )
		{
			put( entry.x, entry.y, entry.colSpan, entry.rowSpan, null );
		}
	}
	
	protected void replaceChild(DPElement child, DPElement replacement)
	{
		TableChildEntry entry = getEntryForChild( child );
		if ( entry != null )
		{
			put( entry.x, entry.y, entry.colSpan, entry.rowSpan, replacement );
		}
	}
	
	
	
	public List<DPElement> getLayoutChildren()
	{
		return registeredChildren;
	}

	public List<? extends TableLayout.TablePackingParams> getTablePackingParams()
	{
		return childEntries;
	}

	public TableLayout.TablePackingParams[] getTablePackingParamsArray()
	{
		return childEntries.toArray( new TableLayout.TablePackingParams[0] );
	}

	public TableLayout.TablePackingParams getTablePackingParamsForChild(DPElement child)
	{
		return getEntryForChild( child );
	}
	
	
	public List<DPElement> getChildren()
	{
		return registeredChildren;
	}
	
	
	public int getChildX(DPElement child)
	{
		TableChildEntry entry = getEntryForChild( child );
		return entry.x;
	}
	
	public int getChildY(DPElement child)
	{
		TableChildEntry entry = getEntryForChild( child );
		return entry.y;
	}
	
	public int getChildColSpan(DPElement child)
	{
		TableChildEntry entry = getEntryForChild( child );
		return entry.colSpan;
	}
	
	public int getChildRowSpan(DPElement child)
	{
		TableChildEntry entry = getEntryForChild( child );
		return entry.rowSpan;
	}

	public int getChildColSpan(int x, int y)
	{
		TableChildEntry childEntry = getChildEntry( x, y );
		return childEntry != null  ?  childEntry.colSpan  :  -1;
	}
	
	public int getChildRowSpan(int x, int y)
	{
		TableChildEntry childEntry = getChildEntry( x, y );
		return childEntry != null  ?  childEntry.rowSpan  :  -1;
	}
	
	public int[] getPositionOfChildCoveringCell(int x, int y)
	{
		LayoutNodeTable layout = (LayoutNodeTable)getLayoutNode();
		return layout.getPositionOfChildCoveringCell( x, y );
	}

	public int[] getCellPositionUnder(Point2 localPos)
	{
		LayoutNodeTable layout = (LayoutNodeTable)getLayoutNode();
		return layout.getCellPositionUnder( localPos );
	}

	public boolean isSingleElementContainer()
	{
		return false;
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
	// CELL BOUNDARY LINES
	//
	//
	
	public double getColumnBoundaryX(int column)
	{
		LayoutNodeTable layout = (LayoutNodeTable)getLayoutNode();
		
		if ( column == 0 )
		{
			return layout.getColumnLeft( 0 );
		}
		else if ( column == getNumColumns() )
		{
			return layout.getColumnRight( column - 1 );
		}
		else
		{
			double halfColumnSpacing = getColumnSpacing() * 0.5;
			return layout.getColumnRight( column - 1 )  +  halfColumnSpacing;
		}
	}
	
	public double getRowBoundaryY(int row)
	{
		LayoutNodeTable layout = (LayoutNodeTable)getLayoutNode();

		if ( row == 0 )
		{
			return layout.getRowTop( 0 );
		}
		else if ( row == getNumRows() )
		{
			return layout.getRowBottom( row - 1 );
		}
		else
		{
			double halfRowSpacing = getRowSpacing() * 0.5;
			return layout.getRowBottom( row - 1 )  +  halfRowSpacing;
		}
	}
	

	protected static int[] getSpanFromBitSet(BitSet bits, int startIndex)
	{
		int start = bits.nextSetBit( startIndex );
		if ( start == -1 )
		{
			return new int[] { -1, -1 };
		}
		int end = bits.nextClearBit( start );
		if ( end == -1 )
		{
			end = bits.length();
		}
		return new int[] { start, end - 1 };
	}
	
	
	
	//
	//
	// DRAW BACKGROUND
	//
	//
	
	@Override
	protected void drawBackground(Graphics2D graphics)
	{
		super.drawBackground( graphics );
		
		TableBackgroundPainter backgroundPainter = getTableBackgroundPainter();
		if ( backgroundPainter != null )
		{
			backgroundPainter.paintTableBackground( this, graphics );
		}
	}
		

	@Override
	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		Paint cellPaint = getCellBoundaryPaint();
		if ( cellPaint != null )
		{
			LayoutNodeTable layout = (LayoutNodeTable)getLayoutNode();
			
			Paint prevPaint = graphics.getPaint();
			graphics.setPaint( cellPaint );
			Stroke prevStroke = graphics.getStroke();
			graphics.setStroke( getCellBoundaryStroke() );
			
			layout.drawCellLines( graphics );
			
			graphics.setPaint( prevPaint );
			graphics.setStroke( prevStroke );
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


	public Stroke getCellBoundaryStroke()
	{
		return ((TableStyleParams) styleParams).getCellBoundaryStroke();
	}
	
	public Paint getCellBoundaryPaint()
	{
		return ((TableStyleParams) styleParams).getCellBoundaryPaint();
	}
	
	public TableBackgroundPainter getTableBackgroundPainter()
	{
		return ((TableStyleParams) styleParams).getTableBackgroundPainter();
	}
}
