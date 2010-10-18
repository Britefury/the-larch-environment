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
import java.awt.geom.Line2D;
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

public class DPTable extends DPContainer
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
	private double columnLines[][], rowLines[][];
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
	
	protected DPTable(DPTable element)
	{
		super( element );
		
		layoutNode = new LayoutNodeTable( this );

		childEntryTable = new TableChildEntry[0][];
		childEntries = new ArrayList<TableChildEntry>();
		rowStartIndices = new int[0];
		numColumns = 0;
		numRows = 0;
	}
	
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	protected void clonePostConstuct(DPElement src)
	{
		super.clonePostConstuct( src );
		DPTable table = (DPTable)src;
		List<DPElement> children = table.getChildren();
		List<? extends TableLayout.TablePackingParams> packingParams = table.getTablePackingParams();
		for (int i = 0; i < children.size(); i++)
		{
			TableLayout.TablePackingParams packing = packingParams.get( i );
			put( packing.x, packing.y, packing.colSpan, packing.rowSpan, children.get( i ) );
		}
	}
	
	public DPElement clonePresentationSubtree()
	{
		DPTable clone = new DPTable( this );
		clone.clonePostConstuct( this );
		return clone;
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
			itemArray[i++] = srcRow.toArray( new DPElement[0] );
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
	
	
	public DPElement get(int x, int y)
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
		TableLayout.TablePackingParams a[] = childEntries.toArray( new TableLayout.TablePackingParams[0] );
		return a;
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
	// QUEUE RESIZE
	//
	//
	
	@Override
	protected void queueResize()
	{
		super.queueResize();
		
		// Reset column and row lines
		columnLines = null;
		rowLines = null;
	}
	
	
	
	
	//
	//
	// CELL BOUNDARY LINES
	//
	//
	
	protected static int[] getSpanFromBitSet(BitSet bits, int startIndex)
	{
		int start = bits.nextSetBit( startIndex );
		int end = bits.nextClearBit( start );
		return new int[] { start, end - 1 };
	}
	
	
	private void refreshBoundaries()
	{
		if ( ( columnLines == null  ||  rowLines == null )  &&  getCellPaint() != null )
		{
			refreshSize();
			int numColLines = numColumns - 1;
			int numRowLines = numRows - 1;
			columnLines = new double[numColLines][];
			rowLines = new double[numRowLines][];
			LayoutNodeTable layout = (LayoutNodeTable)getLayoutNode();
			
			
			// Create and initialise bitsets for the column and row lines
			BitSet columnBits[] = new BitSet[numColLines];
			BitSet rowBits[] = new BitSet[numRowLines];
			
			for (int i = 0; i < columnBits.length; i++)
			{
				columnBits[i] = new BitSet( numRows );
			}
			for (int i = 0; i < rowBits.length; i++)
			{
				rowBits[i] = new BitSet( numColumns );
			}
			
			
			// For each cell, set the bits that mark its boundary
			for (TableChildEntry entry: childEntries)
			{
				int top = entry.y, bottom = entry.y + entry.rowSpan;
				int left = entry.x, right = entry.x + entry.colSpan;
				
				// NOTE: ignore lines that are on the table boundary
				
				// Top
				if ( top > 0 )
				{
					BitSet bits = rowBits[top-1];
					for (int x = left; x < right; x++)
					{
						bits.set( x );
					}
				}

				// Bottom
				if ( bottom < numRows )
				{
					BitSet bits = rowBits[bottom-1];
					for (int x = left; x < right; x++)
					{
						bits.set( x );
					}
				}
				
				// Left
				if ( left > 0 )
				{
					BitSet bits = columnBits[left-1];
					for (int y = top; y < bottom; y++)
					{
						bits.set( y );
					}
				}
				
				// Right
				if ( right < numColumns )
				{
					BitSet bits = columnBits[right-1];
					for (int y = top; y < bottom; y++)
					{
						bits.set( y );
					}
				}
			}
			
			ArrayList<Double> spanStarts = new ArrayList<Double>();
			ArrayList<Double> spanEnds = new ArrayList<Double>();
			
			double halfColumnSpacing = getColumnSpacing() * 0.5;
			double halfRowSpacing = getRowSpacing() * 0.5;

			for (int columnLine = 0; columnLine < numColLines; columnLine++)
			{
				spanStarts.clear();
				spanEnds.clear();
				int y = 0;
				while ( y < numRows )
				{
					int spanIndices[] = getSpanFromBitSet( columnBits[columnLine], y );
					double topSpacing = spanIndices[0] == 0  ?  0.0  :  halfRowSpacing;
					double bottomSpacing = spanIndices[1] == numRows-1  ?  0.0  :  halfRowSpacing;
					spanStarts.add( layout.getRowTop( spanIndices[0] ) - topSpacing );
					spanEnds.add( layout.getRowBottom( spanIndices[1] ) + bottomSpacing );
					y = spanIndices[1] + 1;
				}
				
				double col[] = new double[spanStarts.size()*2+1];
				col[0] = layout.getColumnRight( columnLine )  +  halfColumnSpacing;
				for (int i = 0; i < spanStarts.size(); i++)
				{
					col[i*2+1] = spanStarts.get( i );
					col[i*2+2] = spanEnds.get( i );
				}
				columnLines[columnLine] = col;
			}
			
			for (int rowLine = 0; rowLine < numRowLines; rowLine++)
			{
				spanStarts.clear();
				spanEnds.clear();
				int x = 0;
				while ( x < numColumns )
				{
					int spanIndices[] = getSpanFromBitSet( rowBits[rowLine], x );
					double leftSpacing = spanIndices[0] == 0  ?  0.0  :  halfColumnSpacing;
					double rightSpacing = spanIndices[1] == numColumns-1  ?  0.0  :  halfColumnSpacing;
					spanStarts.add( layout.getColumnLeft( spanIndices[0] ) - leftSpacing );
					spanEnds.add( layout.getColumnRight( spanIndices[1] ) + rightSpacing );
					x = spanIndices[1] + 1;
				}
				
				double row[] = new double[spanStarts.size()*2+1];
				row[0] = layout.getRowBottom( rowLine )  +  halfRowSpacing;
				for (int i = 0; i < spanStarts.size(); i++)
				{
					row[i*2+1] = spanStarts.get( i );
					row[i*2+2] = spanEnds.get( i );
				}
				rowLines[rowLine] = row;
			}
		}
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
		
		Paint cellPaint = getCellPaint();
		if ( cellPaint != null )
		{
			refreshBoundaries();
			
			Paint prevPaint = graphics.getPaint();
			graphics.setPaint( cellPaint );
			Stroke prevStroke = graphics.getStroke();
			graphics.setStroke( getCellStroke() );
			
			for (double col[]: columnLines)
			{
				double x = col[0];
				for (int i = 1; i < col.length; i += 2)
				{
					double y1 = col[i], y2 = col[i+1];
					Line2D.Double line = new Line2D.Double( x, y1, x, y2 );
					graphics.draw( line );
				}
			}
			
			for (double row[]: rowLines)
			{
				double y = row[0];
				for (int i = 1; i < row.length; i += 2)
				{
					double x1 = row[i], x2 = row[i+1];
					Line2D.Double line = new Line2D.Double( x1, y, x2, y );
					graphics.draw( line );
				}
			}
			
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


	public Stroke getCellStroke()
	{
		return ((TableStyleParams) styleParams).getCellStroke();
	}
	
	public Paint getCellPaint()
	{
		return ((TableStyleParams) styleParams).getCellPaint();
	}
}
