//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import org.python.core.PySlice;

import BritefuryJ.DocPresent.Layout.GridLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
import BritefuryJ.Math.Point2;

public class DPRGrid extends DPContainerSequence
{
	private LReqBox columnBoxes[], rowBoxes[];
	private LAllocBox columnAllocBoxes[], rowAllocBoxes[];
	private int numColumns;

	
	
	public DPRGrid()
	{
		this( TableStyleSheet.defaultStyleSheet );
	}
	
	public DPRGrid(TableStyleSheet syleSheet)
	{
		super( syleSheet );
	}
	
	
	
	public void setChildren(DPWidget items[])
	{
		setChildren( Arrays.asList( items ) );
	}
	
	public void setChildren(List<DPWidget> items)
	{
		for (DPWidget child: items)
		{
			if ( !(child instanceof DPGridRow) )
			{
				throw new RuntimeException( "DPRGrid can only accept Grid Row elements as children" );
			}
		}
		
		super.setChildren( items );
	}
	
	
	
	public void set(int index, DPWidget child)
	{
		if ( !(child instanceof DPGridRow) )
		{
			throw new RuntimeException( "DPRGrid can only accept Grid Row elements as children" );
		}
		
		super.set( index, child );
	}
	
	public void __setitem__(PySlice slice, DPWidget[] items)
	{
		for (DPWidget child: items)
		{
			if ( !(child instanceof DPGridRow) )
			{
				throw new RuntimeException( "DPRGrid can only accept Grid Row elements as children" );
			}
		}
		
		super.__setitem__( slice, items );
	}
	
	
	
	public void append(DPWidget child)
	{
		if ( !(child instanceof DPGridRow) )
		{
			throw new RuntimeException( "DPRGrid can only accept Grid Row elements as children" );
		}
		
		super.append( child );
	}

	
	public void extend(List<DPWidget> children)
	{
		for (DPWidget child: children)
		{
			if ( !(child instanceof DPGridRow) )
			{
				throw new RuntimeException( "DPRGrid can only accept Grid Row elements as children" );
			}
		}

		super.extend( children );
	}
	
	
	public void insert(int index, DPWidget child)
	{
		if ( !(child instanceof DPGridRow) )
		{
			throw new RuntimeException( "DPRGrid can only accept Grid Row elements as children" );
		}
		
		super.insert( index, child );
	}
	
	

	protected void replaceChildWithEmpty(DPWidget child)
	{
		int index = registeredChildren.indexOf( child );
		set( index, new DPGridRow() );
	}

	
	
	
	
	
	
	private void refreshSize()
	{
		numColumns = 0;
		for (DPWidget r: registeredChildren)
		{
			DPGridRow row = (DPGridRow)r;
			numColumns = Math.max( numColumns, row.size() );
		}
	}
	
	
	protected void updateRequisitionX()
	{
		refreshSize();
		
		int numRows = registeredChildren.size();
		LReqBox childBoxes[][] = new LReqBox[numRows][];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			DPGridRow row = (DPGridRow)registeredChildren.get( i );
			childBoxes[i] = row.getChildrenRefreshedRequistionXBoxes();
		}
		
		columnBoxes = GridLayout.computeRequisitionX( layoutReqBox, childBoxes, numColumns, numRows, getColumnSpacing(), getRowSpacing() );
		columnAllocBoxes = new LAllocBox[columnBoxes.length];
		for (int i = 0; i < columnAllocBoxes.length; i++)
		{
			columnAllocBoxes[i] = new LAllocBox( null );
		}
	}

	protected void updateRequisitionY()
	{
		refreshSize();
		
		rowBoxes = getChildrenRefreshedRequistionYBoxes();
		
		GridLayout.computeRequisitionY( layoutReqBox, rowBoxes, getRowSpacing() );

		rowAllocBoxes = new LAllocBox[rowBoxes.length];
		for (int i = 0; i < rowAllocBoxes.length; i++)
		{
			rowAllocBoxes[i] = registeredChildren.get( i ).layoutAllocBox;
		}
	}

	

	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		refreshSize();
		
		int numRows = registeredChildren.size();
		LReqBox childBoxes[][] = new LReqBox[numRows][];
		LAllocBox childAllocBoxes[][] = new LAllocBox[numRows][];
		double prevWidths[][] = new double[numRows][];
		int childAlignmentFlags[][] = new int[numRows][];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			DPGridRow row = (DPGridRow)registeredChildren.get( i );
			childBoxes[i] = row.getChildrenRefreshedRequistionXBoxes();
			childAllocBoxes[i] = row.getChildrenAllocationBoxes();
			prevWidths[i] = row.getChildrenAllocationX();
			childAlignmentFlags[i] = row.getChildrenAlignmentFlags();
		}

		GridLayout.allocateX( layoutReqBox, columnBoxes, childBoxes, layoutAllocBox, columnAllocBoxes, childAllocBoxes, childAlignmentFlags, numColumns, numRows,
				getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.onAllocationXRefreshed();
			i++;
		}
		
		
		for (int r = 0; r < registeredChildren.size(); r++)
		{
			DPGridRow row = (DPGridRow)registeredChildren.get( r );
			double rowPrevWidths[] = prevWidths[r];
			
			for (int c = 0; c < row.registeredChildren.size(); c++)
			{
				row.registeredChildren.get( c ).refreshAllocationX( rowPrevWidths[c] );
			}
		}
	}

	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		refreshSize();
		
		LReqBox childBoxes[] = getChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getChildrenAllocationBoxes();
		LAllocV prevAllocVs[] = getChildrenAllocV();
		
		GridLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getRowSpacing(), getRowExpand() );
		
		int i = 0;
		for (DPWidget child: registeredChildren)
		{
			child.refreshAllocationY( prevAllocVs[i] );
			i++;
		}
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( registeredChildren, localPos, filter );
	}


	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getChildren();
	}
	
	protected List<DPWidget> verticalNavigationList()
	{
		return getChildren();
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


	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}
}
