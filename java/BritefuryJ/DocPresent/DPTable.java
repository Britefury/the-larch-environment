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

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.Metrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
import BritefuryJ.Math.Point2;
import BritefuryJ.Utils.BinarySearch;

public class DPTable extends DPContainer
{
	public enum RowAlignment { TOP, CENTRE, BOTTOM, EXPAND, BASELINES };
	public enum ColumnAlignment { LEFT, CENTRE, RIGHT, EXPAND };

	
	protected static class TableParentPacking extends ParentPacking
	{
		public int x, y, colSpan, rowSpan;
		public int packFlagsX, packFlagsY;
		public double paddingX, paddingY;
		
		public TableParentPacking(int x, int colSpan, boolean bExpandX, double paddingX, int y, int rowSpan, boolean bExpandY, double paddingY)
		{
			this.x = x;
			this.colSpan = colSpan;
			this.packFlagsX = Metrics.packFlags( bExpandX );
			this.paddingX = paddingX;
			this.y = y;
			this.rowSpan = rowSpan;
			this.packFlagsY = Metrics.packFlags( bExpandY );
			this.paddingY = paddingY;
		}
	}
	
	
	
	private DPWidget[][] children;
	private double columnBounds[], rowBounds[];
	private double minColWidths[], prefColWidths[], minRowHeights[], prefRowHeights[];
	private VMetricsTypeset minRowVMetrics[], prefRowVMetrics[];
	private int rowPositions[];
	private int numColumns, numRows;		// Can be -1, indicating that these values must be refreshed

	
	
	
	public DPTable()
	{
		this( TableStyleSheet.defaultStyleSheet );
	}

	public DPTable(TableStyleSheet styleSheet)
	{
		super( styleSheet );

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
						registerChild( child, new TableParentPacking( x, 1, getExpandX(), getPaddingX(), y, 1, getExpandY(), getPaddingY() ) );
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
						child.setParentPacking( new TableParentPacking( x, 1, getExpandX(), getPaddingX(), y, 1, getExpandY(), getPaddingY() ) );
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
		
		
		childListModified();
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
		TableParentPacking oldPacking = null;
		int oldEndX = -1, oldEndY = -1;
		if ( y < children.length )
		{
			if ( x < children[y].length )
			{
				oldChild = children[y][x];
				if ( oldChild != null )
				{
					oldPacking = (TableParentPacking)oldChild.getParentPacking();
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
				registerChild( item, new TableParentPacking( x, colSpan, getExpandX(), getPaddingX(), y, rowSpan, getExpandY(), getPaddingY() ) );
			}

			childListModified();
			queueResize();
		}
		else
		{
			if ( item != null )
			{
				item.setParentPacking( new TableParentPacking( x, colSpan, getExpandX(), getPaddingX(), y, rowSpan, getExpandY(), getPaddingY() ) );
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
				row[index] = new DPEmpty();
			}
		}
	}

	
	
	protected List<DPWidget> getChildren()
	{
		return registeredChildren;
	}
	
	
	public int getChildX(DPWidget child)
	{
		return ((TableParentPacking)child.getParentPacking()).x;
	}
	
	public int getChildY(DPWidget child)
	{
		return ((TableParentPacking)child.getParentPacking()).y;
	}
	
	public int getChildColSpan(DPWidget child)
	{
		return ((TableParentPacking)child.getParentPacking()).colSpan;
	}
	
	public int getChildRowSpan(DPWidget child)
	{
		return ((TableParentPacking)child.getParentPacking()).rowSpan;
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
				TableParentPacking packing = (TableParentPacking)child.getParentPacking();
				numColumns = Math.max( numColumns, packing.x + packing.colSpan );
				numRows = Math.max( numRows, packing.y + packing.rowSpan );
			}
		}
	}
	
	
	private double[] computeWidthByColumn(HMetrics hmetricsTable[][])
	{
		refreshSize();
		
		double spacingX = getSpacingX();
		
		double columnWidth[] = new double[numColumns];
		Arrays.fill( columnWidth, 0.0 );
		
		
		// First phase; fill only with children who span 1 column
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			
			if ( packing.colSpan == 1 )
			{
				HMetrics chm = hmetricsTable[packing.y][packing.x];
				columnWidth[packing.x] = Math.max( columnWidth[packing.x], chm.width + packing.paddingX * 2.0 );
			}
		}
		
		
		// Second phase; fill with children who span >1 columns
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			
			if ( packing.colSpan > 1 )
			{
				// First, total up the space available by combining the columns
				int endColumn = packing.x + packing.colSpan;
				
				double widthAvailable = 0.0;
				for (int c = packing.x; c < endColumn; c++)
				{
					double colW = columnWidth[c];
					
					if ( c != endColumn - 1 )
					{
						colW += spacingX;
					}
					
					widthAvailable += colW;
				}
				
				
				// Now compare with what is required
				HMetrics chm = hmetricsTable[packing.y][packing.x];

				if ( widthAvailable < chm.width )
				{
					// Need more width; compute how much we need, and distrubute among columns
					double additionalWidth = chm.width - widthAvailable;
					double additionalWidthPerColumn = additionalWidth / (double)packing.colSpan;
					
					for (int c = packing.x; c < endColumn; c++)
					{
						columnWidth[c] += additionalWidthPerColumn;
					}
				}
			}
		}

		return columnWidth;
	}

	private double[] computeHeightByRow(VMetrics vmetricsTable[][])
	{
		refreshSize();

		double spacingY = getSpacingY();
		
		double rowHeight[] = new double[numRows];
		Arrays.fill( rowHeight, 0.0 );
		
		
		// First phase; fill only with children who span 1 row
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			
			if ( packing.rowSpan == 1 )
			{
				VMetrics chm = vmetricsTable[packing.y][packing.x];
				rowHeight[packing.y] = Math.max( rowHeight[packing.y], chm.height + packing.paddingY * 2.0 );
			}
		}
		
		
		// Second phase; fill with children who span >1 rows
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			
			if ( packing.rowSpan > 1 )
			{
				// First, total up the space available by combining the rows
				int endRow = packing.y + packing.rowSpan;
				
				double heightAvailable = 0.0;
				for (int r = packing.y; r < endRow; r++)
				{
					double rowH = rowHeight[r];
					
					if ( r != endRow - 1 )
					{
						rowH += spacingY;
					}
					
					heightAvailable += rowH;
				}
				
				
				// Now compare with what is required
				VMetrics chm = vmetricsTable[packing.y][packing.x];

				if ( heightAvailable < chm.height )
				{
					// Need more height; compute how much we need, and distrubute among rows
					double additionalHeight = chm.height - heightAvailable;
					double additionalHeightPerRow = additionalHeight / (double)packing.rowSpan;
					
					for (int r = packing.y; r < endRow; r++)
					{
						rowHeight[r] += additionalHeightPerRow;
					}
				}
			}
		}

		return rowHeight;
	}

	private VMetricsTypeset[] computeVMetricsTypesetByRow(VMetrics vmetricsTable[][])
	{
		refreshSize();

		double spacingY = getSpacingY();
		
		double rowAscent[] = new double[numRows];
		double rowDescent[] = new double[numRows];
		Arrays.fill( rowAscent, 0.0 );
		Arrays.fill( rowDescent, 0.0 );
		
		
		// First phase; fill only with children who span 1 row
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			
			if ( packing.rowSpan == 1 )
			{
				double chAscent, chDescent;
				VMetrics chm = vmetricsTable[packing.y][packing.x];
				if ( chm.isTypeset() )
				{
					VMetricsTypeset chmt = (VMetricsTypeset)chm;
					chAscent = chmt.ascent;
					chDescent = chmt.descent;
				}
				else
				{
					chAscent = chm.height * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
					chDescent = chm.height * 0.5  +  NON_TYPESET_CHILD_BASELINE_OFFSET;
				}
				rowAscent[packing.y] = Math.max( rowAscent[packing.y], chAscent + packing.paddingY );
				rowDescent[packing.y] = Math.max( rowDescent[packing.y], chDescent + packing.paddingY );
			}
		}
		
		
		// Second phase; fill with children who span >1 rows
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			
			if ( packing.rowSpan > 1 )
			{
				// First, total up the space available by combining the rows
				int endRow = packing.y + packing.rowSpan;
				
				double heightAvailable = 0.0;
				for (int r = packing.y; r < endRow; r++)
				{
					double rowH = rowAscent[r] + rowDescent[r];
					
					if ( r != endRow - 1 )
					{
						rowH += spacingY;
					}
					
					heightAvailable += rowH;
				}
				
				
				// Now compare with what is required
				VMetrics chm = vmetricsTable[packing.y][packing.x];

				if ( heightAvailable < chm.height )
				{
					// Need more height; compute how much we need, and distrubute among rows
					double additionalHeight = chm.height - heightAvailable;
					double additionalHeightPerRow = additionalHeight / (double)packing.rowSpan;
					
					for (int r = packing.y; r < endRow; r++)
					{
						rowDescent[r] += additionalHeightPerRow;
					}
				}
			}
		}

		VMetricsTypeset rowMetrics[] = new VMetricsTypeset[numRows];
		for (int r = 0; r < numRows; r++)
		{
			rowMetrics[r] = new VMetricsTypeset( rowAscent[r], rowDescent[r], 0.0 );
		}

		return rowMetrics;
	}

	
	
	
	
	private HMetrics columnWidthsToHMetrics(double columnWidths[])
	{
		double spacingX = getSpacingX();
		double totalWidth = 0.0;
		for (double w: columnWidths)
		{
			totalWidth += w;
		}
		totalWidth += spacingX * (double)Math.max( columnWidths.length - 1, 0 );
		
		return new HMetrics( totalWidth, 0.0 );
	}
	
	private VMetrics rowHeightsToVMetrics(double rowHeights[])
	{
		double spacingY = getSpacingY();
		double totalHeight = 0.0;
		for (double h: rowHeights)
		{
			totalHeight += h;
		}
		totalHeight += spacingY * (double)Math.max( rowHeights.length - 1, 0 );
		
		return new VMetrics( totalHeight, 0.0 );
	}
	
	private VMetrics rowVMetricsTypesetToVMetrics(VMetricsTypeset rowMetrics[])
	{
		double spacingY = getSpacingY();
		double totalHeight = 0.0;
		for (VMetricsTypeset m: rowMetrics)
		{
			totalHeight += m.height;
		}
		totalHeight += spacingY * (double)Math.max( rowMetrics.length - 1, 0 );
		
		return new VMetrics( totalHeight, 0.0 );
	}
	
	
	protected HMetrics computeMinimumHMetrics()
	{
		refreshSize();

		HMetrics metricsTable[][] = new HMetrics[children.length][];
		for (int r = 0; r < children.length; r++)
		{
			DPWidget row[] = children[r];
			HMetrics rowMetrics[] = new HMetrics[row.length];
			metricsTable[r] = rowMetrics;
			for (int c = 0; c < row.length; c++)
			{
				DPWidget child = row[c];
				if ( child != null )
				{
					rowMetrics[c] = child.refreshMinimumHMetrics();
				}
			}
		}
		
		minColWidths = computeWidthByColumn( metricsTable );
		return columnWidthsToHMetrics( minColWidths );
	}

	protected HMetrics computePreferredHMetrics()
	{
		refreshSize();

		HMetrics metricsTable[][] = new HMetrics[children.length][];
		for (int r = 0; r < children.length; r++)
		{
			DPWidget row[] = children[r];
			HMetrics rowMetrics[] = new HMetrics[row.length];
			metricsTable[r] = rowMetrics;
			for (int c = 0; c < row.length; c++)
			{
				DPWidget child = row[c];
				if ( child != null )
				{
					rowMetrics[c] = child.refreshPreferredHMetrics();
				}
			}
		}
		
		prefColWidths = computeWidthByColumn( metricsTable );
		return columnWidthsToHMetrics( prefColWidths );
	}
	

	protected VMetrics computeMinimumVMetrics()
	{
		refreshSize();

		VMetrics metricsTable[][] = new VMetrics[children.length][];
		for (int r = 0; r < children.length; r++)
		{
			DPWidget row[] = children[r];
			VMetrics rowMetrics[] = new VMetrics[row.length];
			metricsTable[r] = rowMetrics;
			for (int c = 0; c < row.length; c++)
			{
				DPWidget child = row[c];
				if ( child != null )
				{
					rowMetrics[c] = child.refreshMinimumVMetrics();
				}
			}
		}
		
		
		if ( getRowAlignment() == RowAlignment.BASELINES )
		{
			minRowHeights = null;
			minRowVMetrics = computeVMetricsTypesetByRow( metricsTable );
			return rowVMetricsTypesetToVMetrics( minRowVMetrics );
		}
		else
		{
			minRowHeights = computeHeightByRow( metricsTable );
			minRowVMetrics = null;
			return rowHeightsToVMetrics( minRowHeights );
		}
	}

	protected VMetrics computePreferredVMetrics()
	{
		refreshSize();

		VMetrics metricsTable[][] = new VMetrics[children.length][];
		for (int r = 0; r < children.length; r++)
		{
			DPWidget row[] = children[r];
			VMetrics rowMetrics[] = new VMetrics[row.length];
			metricsTable[r] = rowMetrics;
			for (int c = 0; c < row.length; c++)
			{
				DPWidget child = row[c];
				if ( child != null )
				{
					rowMetrics[c] = child.refreshPreferredVMetrics();
				}
			}
		}
		
		if ( getRowAlignment() == RowAlignment.BASELINES )
		{
			prefRowHeights = null;
			prefRowVMetrics = computeVMetricsTypesetByRow( metricsTable );
			return rowVMetricsTypesetToVMetrics( prefRowVMetrics );
		}
		else
		{
			prefRowHeights = computeHeightByRow( metricsTable );
			prefRowVMetrics = null;
			return rowHeightsToVMetrics( prefRowHeights );
		}
	}

	
	
	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );
		
		refreshSize();

		// Compute packing flags, per-column
		int columnPackFlags[] = new int[numColumns];
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			int endCol = packing.x + packing.colSpan;
			for (int c = packing.x; c < endCol; c++)
			{
				columnPackFlags[c] = Metrics.combinePackFlags( columnPackFlags[c], packing.packFlagsX );
			}
		}
		
		// Generate arrays of metrics; minimum and preferred
		HMetrics minHM[] = new HMetrics[numColumns];
		HMetrics prefHM[] = new HMetrics[numColumns];
		for (int c = 0; c < numColumns; c++)
		{
			minHM[c] = new HMetrics( minColWidths[c], 0.0 );
			prefHM[c] = new HMetrics( prefColWidths[c], 0.0 );
		}
		
		// Allocate space to the columns
		// Padding was taken into account in the computeMinimumHMetrics() and computePreferredHMetrics() methods, which updated minColWidths and prefColWidths; only take spacing
		// into account
		double spacingX = getSpacingX();
		double totalSpacing = spacingX * Math.max( numColumns - 1, 0 );
		HMetrics allocated[] = (HMetrics[])Metrics.allocateSpacePacked( minHM, prefHM, columnPackFlags, allocation - totalSpacing );
		columnBounds = new double[numColumns-1];
		double columnX[] = new double[numColumns+1];
		double x = 0.0;
		for (int c = 0; c < numColumns; c++)
		{
			columnX[c] = x;
			x += allocated[c].width;
			if ( c != numColumns - 1 )
			{
				columnBounds[c] = x + spacingX * 0.5;
				x += spacingX;
			}
		}
		columnX[numColumns] = x;
		
		// Allocate children
		ColumnAlignment colAlignment = getColumnAlignment();
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			HMetrics chm = child.prefH;
			
			int startCol = packing.x;
			int endCol = packing.x + packing.colSpan;
			double spacing = endCol == numColumns  ?  0.0  :  spacingX;
			double xStart = columnX[startCol] + packing.paddingX;
			double xEnd = columnX[endCol] - ( packing.paddingX + spacing );
			double widthAvailable = xEnd - xStart;
			
			if ( widthAvailable <= chm.width )
			{
				allocateChildX( child, xStart, widthAvailable );
			}
			else
			{
				if ( colAlignment == ColumnAlignment.LEFT )
				{
					allocateChildX( child, xStart, chm.width );
				}
				else if ( colAlignment == ColumnAlignment.RIGHT )
				{
					allocateChildX( child, xEnd - chm.width, chm.width );
				}
				else if ( colAlignment == ColumnAlignment.CENTRE )
				{
					allocateChildX( child, xStart + ( widthAvailable - chm.width ) * 0.5, chm.width );
				}
				else if ( colAlignment == ColumnAlignment.EXPAND )
				{
					allocateChildX( child, xStart, widthAvailable );
				}
			}
		}
	}
	
	
	
	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		refreshSize();

		// Compute packing flags, per-column
		int rowPackFlags[] = new int[numRows];
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			int endRow = packing.y + packing.rowSpan;
			for (int r = packing.y; r < endRow; r++)
			{
				rowPackFlags[r] = Metrics.combinePackFlags( rowPackFlags[r], packing.packFlagsY );
			}
		}
		
		// Generate arrays of metrics; minimum and preferred
		VMetrics minVM[], prefVM[];
		RowAlignment rowAlignment = getRowAlignment();
		if ( rowAlignment == RowAlignment.BASELINES )
		{
			minVM = minRowVMetrics;
			prefVM = prefRowVMetrics;
		}
		else
		{
			minVM= new VMetrics[numRows];
			prefVM = new VMetrics[numRows];
			for (int r = 0; r < numRows; r++)
			{
				minVM[r] = new VMetrics( minRowHeights[r], 0.0 );
				prefVM[r] = new VMetrics( prefRowHeights[r], 0.0 );
			}
		}
		
		// Allocate space to the rows
		// Padding was taken into account in the computeMinimumVMetrics() and computePreferredVMetrics() methods, which updated minRowHeights and prefRowHeights; only take spacing
		// into account
		double spacingY = getSpacingY();
		double totalSpacing = spacingY * Math.max( numRows - 1, 0 );
		VMetrics allocated[] = (VMetrics[])Metrics.allocateSpacePacked( minVM, prefVM, rowPackFlags, allocation - totalSpacing );
		rowBounds = new double[numRows-1];
		double rowY[] = new double[numRows+1];
		double y = 0.0;
		for (int i = 0; i < numRows; i++)
		{
			rowY[i] = y;
			y += allocated[i].height;
			if ( i != numRows - 1 )
			{
				rowBounds[i] = y + spacingY * 0.5;
				y += spacingY;
			}
		}
		rowY[numRows] = y;
		
		// Allocate children
		for (DPWidget child: registeredChildren)
		{
			TableParentPacking packing = (TableParentPacking)child.getParentPacking();
			VMetrics chm = child.prefV;
			
			if ( packing.rowSpan == 1  &&  prefRowVMetrics != null  &&  chm.isTypeset() )
			{
				int startRow = packing.y;

				VMetricsTypeset chmt = (VMetricsTypeset)chm;
				VMetricsTypeset rwmt = prefRowVMetrics[packing.y];
				
				double yOffset = rwmt.ascent - chmt.ascent;			// Row ascent includes padding; so yOffset also includes padding
				double yStart = rowY[startRow];
				double yPos = yStart + yOffset;
				allocateChildY( child, yPos, chmt.height );
			}
			else
			{
				int startRow = packing.y;
				int endRow = packing.y + packing.rowSpan;
				double spacing = endRow == numRows  ?  0.0  :  spacingY;
				double yStart = rowY[startRow] + packing.paddingY;
				double yEnd = rowY[endRow] - ( packing.paddingY + spacing );
				double heightAvailable = yEnd - yStart;
				
				if ( rowAlignment == RowAlignment.TOP )
				{
					allocateChildY( child, yStart, chm.height );
				}
				else if ( rowAlignment == RowAlignment.BOTTOM )
				{
					allocateChildY( child, yEnd - chm.height, chm.height );
				}
				else if ( rowAlignment == RowAlignment.CENTRE )
				{
					allocateChildY( child, yStart + ( heightAvailable - chm.height ) * 0.5, chm.height );
				}
				else if ( rowAlignment == RowAlignment.EXPAND )
				{
					allocateChildY( child, yStart, heightAvailable );
				}
				else if ( rowAlignment == RowAlignment.BASELINES )
				{
					allocateChildY( child, yStart + ( heightAvailable - chm.height ) * 0.5, chm.height );
				}
			}
		}
	}
	
	
	
	
	private boolean doesChildCoverCell(DPWidget child, int x, int y)
	{
		TableParentPacking packing = (TableParentPacking)child.getParentPacking();

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

	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		refreshSize();

		int x = BinarySearch.binarySearchInsertionPoint( columnBounds, localPos.x );
		int y = BinarySearch.binarySearchInsertionPoint( rowBounds, localPos.y );
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
		return getChildren();
	}
	

	
	
	//
	//
	// STYLE METHODS
	//
	//
	
	protected RowAlignment getRowAlignment()
	{
		return ((TableStyleSheet)styleSheet).getRowAlignment();
	}

	protected ColumnAlignment getColumnAlignment()
	{
		return ((TableStyleSheet)styleSheet).getColumnAlignment();
	}

	
	protected double getSpacingX()
	{
		return ((TableStyleSheet)styleSheet).getSpacingX();
	}

	protected boolean getExpandX()
	{
		return ((TableStyleSheet)styleSheet).getExpandX();
	}

	protected double getPaddingX()
	{
		return ((TableStyleSheet)styleSheet).getPaddingX();
	}

	
	protected double getSpacingY()
	{
		return ((TableStyleSheet)styleSheet).getSpacingY();
	}

	protected boolean getExpandY()
	{
		return ((TableStyleSheet)styleSheet).getExpandY();
	}

	protected double getPaddingY()
	{
		return ((TableStyleSheet)styleSheet).getPaddingY();
	}
}
