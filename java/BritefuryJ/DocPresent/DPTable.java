//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyTuple;

import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.Layout.TablePackingParams;
import BritefuryJ.DocPresent.LayoutTree.LayoutNodeTable;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;

public class DPTable extends DPContainer
{
	private DPWidget[][] children;
	private int rowPositions[];
	private int numColumns, numRows;		// Can be -1, indicating that these values must be refreshed

	
	
	
	public DPTable(ElementContext context)
	{
		this( context, TableStyleSheet.defaultStyleSheet );
	}

	public DPTable(ElementContext context, TableStyleSheet styleSheet)
	{
		super( context, styleSheet );
		
		layoutNode = new LayoutNodeTable( this );

		children = new DPWidget[0][];
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

			// Generate the list of registered children; an ordered list of all child widgets
			rowPositions = new int[itemTable.length];
			int rowI = 0;
			for (DPWidget[] row: itemTable)
			{
				rowPositions[rowI++] = registeredChildren.size();
				numColumns = Math.max( numColumns, row.length );
				for (DPWidget child: row)
				{
					if ( child != null )
					{
						registeredChildren.add( child );
					}
				}
			}
			
			// Fill in child table
			children = new DPWidget[itemTable.length][];
			int n = 0;
			for (rowI = 0; rowI < itemTable.length; rowI++)
			{
				DPWidget row[] = itemTable[rowI];
				children[rowI] = new DPWidget[row.length];
				System.arraycopy( row, 0, children[rowI], 0, row.length );
				n += row.length;
			}
			
			// Register added children
			int y = 0;
			for (DPWidget[] row: itemTable)
			{
				int x = 0;
				for (DPWidget child: row)
				{
					if ( child != null )
					{
						registerChild( child, new TablePackingParams( x, 1, y, 1 ) );
					}
					x++;
				}
				y++;
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
			
			// Recompute @registeredChildren
			registeredChildren.clear();
			rowPositions = new int[itemTable.length];
			int y = 0;
			for (DPWidget[] row: itemTable)
			{
				int x = 0;
				rowPositions[y] = registeredChildren.size();
				for (DPWidget child: row)
				{
					if ( child != null )
					{
						registeredChildren.add( child );
						child.setParentPacking( new TablePackingParams( x, 1, y, 1 ) );
					}
					x++;
				}
				y++;
			}
	
			// Fill in child table
			children = new DPWidget[itemTable.length][];
			int n = 0;
			for (int rowI = 0; rowI < itemTable.length; rowI++)
			{
				DPWidget row[] = itemTable[rowI];
				children[rowI] = new DPWidget[row.length];
				System.arraycopy( row, 0, children[rowI], 0, row.length );
				n += row.length;
			}
			
			// Register added children
			for (DPWidget child: added)
			{
				registerChild( child, child.getParentPacking() );
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
		DPWidget row[] = y >= children.length  ?  null  :  children[y];
		if ( row != null )
		{
			if ( x < row.length )
			{
				return row[x];
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
		DPWidget oldChild = null;
		TablePackingParams oldPacking = null;
		int oldEndX = -1, oldEndY = -1;
		if ( y < children.length )
		{
			if ( x < children[y].length )
			{
				oldChild = children[y][x];
				if ( oldChild != null )
				{
					oldPacking = (TablePackingParams)oldChild.getParentPacking();
					oldEndX = x + oldPacking.colSpan;
					oldEndY = y + oldPacking.rowSpan;
				}
			}
		}
		
		if ( item != oldChild )
		{
			if ( item.getLayoutNode() == null )
			{
				throw new ChildHasNoLayoutException();
			}

			// Get the index of the old child (in @registeredChildren), unregister the old child, and remove it from @registeredChildren
			int oldChildIndex = -1;
			if ( oldChild != null )
			{
				oldChildIndex = registeredChildren.indexOf( oldChild );
				unregisterChild( oldChild );
			}
			
			// If y is outside the bounds of the number of rows, add as many new rows as necessary
			if ( y >= children.length  &&  item != null )
			{
				DPWidget ch[][] = new DPWidget[y+1][];
				int rp[] = new int[y+1];
				System.arraycopy( children, 0, ch, 0, children.length );
				System.arraycopy( rowPositions, 0, rp, 0, rowPositions.length );
				for (int i = children.length; i < ch.length; i++)
				{
					ch[i] = new DPWidget[0];
					rp[i] = registeredChildren.size();
				}
				children = ch;
				rowPositions = rp;
			}
			
			// Get the row into which the child is being inserted
			DPWidget[] row = children[y];
			// Enlarge the row if x is outside the bounds of the row
			if ( x >= row.length  &&  item != null )
			{
				DPWidget r[] = new DPWidget[x+1];
				System.arraycopy( row, 0, r, 0, row.length );
				children[y] = r;
				row = r;
			}
			
			// Place the child into the row
			row[x] = item;
			
			// Add the child to @registeredChildren
			if ( oldChildIndex == -1 )
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
				for (int i = y + 1; i < rowPositions.length; i++)
				{
					rowPositions[i]++;
				}
			}
			else
			{
				if ( item == null )
				{
					// Removing an existing child
					registeredChildren.remove( oldChildIndex );
				}
				else
				{
					// Replacing an existing child
					registeredChildren.set( oldChildIndex, item );
				}
			}
			
			// Register the child
			if ( item != null )
			{
				registerChild( item, new TablePackingParams( x, colSpan, y, rowSpan ) );
			}

			onChildListModified();
			queueResize();
		}
		else
		{
			if ( item != null )
			{
				item.setParentPacking( new TablePackingParams( x, colSpan, y, rowSpan ) );
				// Queue a resize; width / height may have changed
				queueResize();
			}

			queueResize();
		}
		
		
		// Update numColumns and numRows
		if ( ( item == null  &&  oldChild != null ) )
		{
			// Removing a child; refresh later
			numColumns = -1;
			numRows = -1;
		}
		
		if ( item != null  &&  oldChild != null )
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
	
	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		for (DPWidget row[]: children)
		{
			int index = Arrays.asList( row ).indexOf( child );
			if ( index != -1 )
			{
				row[index] = new DPEmpty( context );
				break;
			}
		}
	}

	
	
	public List<DPWidget> getChildren()
	{
		return registeredChildren;
	}
	
	
	public int getChildX(DPWidget child)
	{
		return ((TablePackingParams)child.getParentPacking()).x;
	}
	
	public int getChildY(DPWidget child)
	{
		return ((TablePackingParams)child.getParentPacking()).y;
	}
	
	public int getChildColSpan(DPWidget child)
	{
		return ((TablePackingParams)child.getParentPacking()).colSpan;
	}
	
	public int getChildRowSpan(DPWidget child)
	{
		return ((TablePackingParams)child.getParentPacking()).rowSpan;
	}
	
	
	private void refreshSize()
	{
		// Refresh numColumns and numRows
		if ( numColumns == -1  ||  numRows == -1 )
		{
			numColumns = 0;
			numRows = 0;
			
			for (DPWidget child: registeredChildren)
			{
				TablePackingParams packing = (TablePackingParams)child.getParentPacking();
				numColumns = Math.max( numColumns, packing.x + packing.colSpan );
				numRows = Math.max( numRows, packing.y + packing.rowSpan );
			}
		}
	}
	
	

	//
	// Packing parameters
	//
	
	protected PackingParams getDefaultPackingParams()
	{
		// All child elements are given packing parameters at registration
		throw new RuntimeException( "Default packing parameters cannot be created for table element" );
	}
	
	
	//
	//
	// STYLE METHODS
	//
	//
	
	protected double getColumnSpacing()
	{
		return ((TableStyleSheet)styleSheet).getColumnSpacing();
	}

	protected boolean getColumnExpand()
	{
		return ((TableStyleSheet)styleSheet).getColumnExpand();
	}

	
	protected double getRowSpacing()
	{
		return ((TableStyleSheet)styleSheet).getRowSpacing();
	}

	protected boolean getRowExpand()
	{
		return ((TableStyleSheet)styleSheet).getRowExpand();
	}
}
