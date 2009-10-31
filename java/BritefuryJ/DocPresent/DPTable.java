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

import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.Layout.TableLayout;
import BritefuryJ.DocPresent.Layout.TablePackingParams;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
import BritefuryJ.Math.Point2;

public class DPTable extends DPContainer
{
	private DPWidget[][] children;
	private LReqBox columnBoxes[], rowBoxes[];
	private LAllocBox columnAllocBoxes[], rowAllocBoxes[];
	private int rowPositions[];
	private int numColumns, numRows;		// Can be -1, indicating that these values must be refreshed

	
	
	
	public DPTable(ElementContext context)
	{
		this( context, TableStyleSheet.defaultStyleSheet );
	}

	public DPTable(ElementContext context, TableStyleSheet styleSheet)
	{
		super( context, styleSheet );

		children = new DPWidget[0][];
		rowPositions = new int[0];
		numColumns = 0;
		numRows = 0;
	}

	
	
	
	public void setChildren(DPWidget[][] itemTable)
	{
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
	
	
	protected void updateRequisitionX()
	{
		refreshSize();
		
		LReqBox childBoxes[] = new LReqBox[registeredChildren.size()];
		TablePackingParams packingParams[] = new TablePackingParams[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionX();
			packingParams[i] = (TablePackingParams)registeredChildren.get( i ).getParentPacking();
		}

		columnBoxes = TableLayout.computeRequisitionX( layoutReqBox, childBoxes, packingParams, numColumns, numRows, getColumnSpacing(), getRowSpacing() );
		columnAllocBoxes = new LAllocBox[columnBoxes.length];
		for (int i = 0; i < columnAllocBoxes.length; i++)
		{
			columnAllocBoxes[i] = new LAllocBox( null );
		}
	}

	protected void updateRequisitionY()
	{
		refreshSize();
		
		LReqBox childBoxes[] = new LReqBox[registeredChildren.size()];
		TablePackingParams packingParams[] = new TablePackingParams[registeredChildren.size()];
		int childAlignmentFlags[] = new int[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionY();
			packingParams[i] = (TablePackingParams)registeredChildren.get( i ).getParentPacking();
			childAlignmentFlags[i] = registeredChildren.get( i ).getAlignmentFlags();
		}

		rowBoxes = TableLayout.computeRequisitionY( layoutReqBox, childBoxes, packingParams, childAlignmentFlags, numColumns, numRows, getColumnSpacing(), getRowSpacing() );
		rowAllocBoxes = new LAllocBox[rowBoxes.length];
		for (int i = 0; i < rowAllocBoxes.length; i++)
		{
			rowAllocBoxes[i] = new LAllocBox( null );
		}
	}
	


	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		refreshSize();
		
		LReqBox childBoxes[] = new LReqBox[registeredChildren.size()];
		LAllocBox childAllocBoxes[] = new LAllocBox[registeredChildren.size()];
		double prevWidths[] = new double[registeredChildren.size()];
		TablePackingParams packingParams[] = new TablePackingParams[registeredChildren.size()];
		int childAlignmentFlags[] = new int[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			DPWidget child = registeredChildren.get( i );
			childBoxes[i] = child.layoutReqBox;
			childAllocBoxes[i] = child.layoutAllocBox;
			prevWidths[i] = child.getAllocationX();
			packingParams[i] = (TablePackingParams)child.getParentPacking();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}
		
		TableLayout.allocateX( layoutReqBox, columnBoxes, childBoxes, layoutAllocBox, columnAllocBoxes, childAllocBoxes, packingParams, childAlignmentFlags, numColumns, numRows, getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.refreshAllocationX( prevWidths[i] );
			i++;
		}
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		refreshSize();
		
		LReqBox childBoxes[] = new LReqBox[registeredChildren.size()];
		LAllocBox childAllocBoxes[] = new LAllocBox[registeredChildren.size()];
		LAllocV prevAllocVs[] = new LAllocV[registeredChildren.size()];
		TablePackingParams[] packingParams = new TablePackingParams[registeredChildren.size()];
		int childAlignmentFlags[] = new int[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			DPWidget child = registeredChildren.get( i );
			childBoxes[i] = child.layoutReqBox;
			childAllocBoxes[i] = child.layoutAllocBox;
			prevAllocVs[i] = child.getAllocV();
			packingParams[i] = (TablePackingParams)child.getParentPacking();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}
		
		TableLayout.allocateY( layoutReqBox, rowBoxes, childBoxes, layoutAllocBox, rowAllocBoxes, childAllocBoxes, packingParams, childAlignmentFlags, numColumns, numRows, getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.refreshAllocationY( prevAllocVs[i] );
			i++;
		}
	}
	

	
	
	
	private boolean doesChildCoverCell(DPWidget child, int x, int y)
	{
		TablePackingParams packing = (TablePackingParams)child.getParentPacking();

		return x <= ( packing.x + packing.colSpan )  &&  y <= ( packing.y + packing.rowSpan );
	}
	
	private DPWidget getChildCoveringCell(int x, int y)
	{
		refreshSize();

		DPWidget child = get( x, y );
		
		if ( child != null )
		{
			return child;
		}
		else
		{
			int maxRadius = Math.max( x, y );
			for (int radius = 1; radius <= maxRadius; radius++)
			{
				// Column to left, going up
				if ( radius <= x )		// Ensure that the column, that is @radius spaces to the left is within the bounds of the table
				{
					int colX = x - radius;
					for (int i = 0; i < radius; i++)
					{
						int searchY = y - i;
						if ( searchY >= 0 )
						{
							child = get( colX, searchY );
							if ( child != null  &&  doesChildCoverCell( child, x, y ) )
							{
								return child;
							}
						}
					}
				}
				
				// Row above, going left
				if ( radius <= y )		// Ensure that the row, that is @radius spaces above is within the bounds of the table
				{
					int rowY = y - radius;
					for (int i = 0; i < radius; i++)
					{
						int searchX = x - i;
						if ( searchX >= 0 )
						{
							child = get( searchX, rowY );
							if ( child != null  &&  doesChildCoverCell( child, x, y ) )
							{
								return child;
							}
						}
					}
				}
				
				// Cell above and to left
				if ( radius <= x  &&  radius <= y )
				{
					child = get( x - radius, y - radius );
					if ( child != null  &&  doesChildCoverCell( child, x, y ) )
					{
						return child;
					}
				}
			}
			
			return null;
		}
	}

	
	
	private int getColumnForLocalPoint(Point2 localPos)
	{
		if ( columnBoxes.length == 0 )
		{
			return -1;
		}
		else if ( columnBoxes.length == 1 )
		{
			return 0;
		}
		else
		{
			LAllocBox columnI = columnAllocBoxes[0];
			for (int i = 0; i < columnBoxes.length - 1; i++)
			{
				LAllocBox columnJ = columnAllocBoxes[i+1];
				double iUpperX = columnI.getPositionInParentSpaceX() + columnI.getAllocationX();
				double jLowerX = columnJ.getPositionInParentSpaceX();
				
				double midX = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midX )
				{
					return i;
				}
				
				columnI = columnJ;
			}
			
			return columnBoxes.length-1;
		}
	}

	
	
	private int getRowForLocalPoint(Point2 localPos)
	{
		if ( rowBoxes.length == 0 )
		{
			return -1;
		}
		else if ( rowBoxes.length == 1 )
		{
			return 0;
		}
		else
		{
			LAllocBox rowI = rowAllocBoxes[0];
			for (int i = 0; i < rowBoxes.length - 1; i++)
			{
				LAllocBox rowJ = rowAllocBoxes[i+1];
				double iUpperY = rowI.getPositionInParentSpaceY() + rowI.getAllocationY();
				double jLowerY = rowJ.getPositionInParentSpaceY();
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					return i;
				}
				
				rowI = rowJ;
			}
			
			return rowBoxes.length-1;
		}
	}

	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		refreshSize();

		int x = getColumnForLocalPoint( localPos );
		int y = getRowForLocalPoint( localPos );
		DPWidget child = getChildCoveringCell( x, y );
		if ( child != null )
		{
			return getLeafClosestToLocalPointFromChild( child, localPos, filter );
		}
		else
		{
			return null;
		}
	}


	
	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getInternalChildren();
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
