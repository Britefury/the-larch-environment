//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Layout.GridLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.StyleSheets.TableStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;

public class DPRGrid extends DPContainerSequenceCollationRoot
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
	
	
	
	private void refreshSize()
	{
		numColumns = 0;
		for (DPWidget child: collationLeaves)
		{
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				numColumns = Math.max( numColumns, row.getCollatedChildren().size() );
			}
			else
			{
				numColumns = Math.max( numColumns, 1 );
			}
		}
	}
	
	
	protected void updateRequisitionX()
	{
		refreshCollation();
		
		refreshSize();
		
		int numRows = collationLeaves.length;
		LReqBox childBoxes[][] = new LReqBox[numRows][];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			DPWidget child = collationLeaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				childBoxes[i] = row.getCollatedChildrenRefreshedRequisitonXBoxes();
			}
			else
			{
				childBoxes[i] = new LReqBox[] { child.refreshRequisitionX() };
			}
		}
		
		columnBoxes = GridLayout.computeRequisitionX( layoutReqBox, childBoxes, numColumns, numRows, getColumnSpacing(), getRowSpacing() );

		// Copy the X-requisition to the child rows
		for (int i = 0; i < collationLeaves.length; i++)
		{
			DPWidget child = collationLeaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				row.layoutReqBox.setRequisitionX( layoutReqBox );
			}
		}

		columnAllocBoxes = new LAllocBox[columnBoxes.length];
		for (int i = 0; i < columnAllocBoxes.length; i++)
		{
			columnAllocBoxes[i] = new LAllocBox( null );
		}
	}

	protected void updateRequisitionY()
	{
		refreshSize();
		
		rowBoxes = getCollatedChildrenRefreshedRequistionYBoxes();
		
		GridLayout.computeRequisitionY( layoutReqBox, rowBoxes, getRowSpacing() );

		rowAllocBoxes = new LAllocBox[rowBoxes.length];
		for (int i = 0; i < rowAllocBoxes.length; i++)
		{
			rowAllocBoxes[i] = collationLeaves[i].layoutAllocBox;
		}
	}

	

	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		refreshSize();
		
		int numRows = collationLeaves.length;
		LReqBox childBoxes[][] = new LReqBox[numRows][];
		LAllocBox childAllocBoxes[][] = new LAllocBox[numRows][];
		double prevWidths[][] = new double[numRows][];
		int childAlignmentFlags[][] = new int[numRows][];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			DPWidget child = collationLeaves[i];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				childBoxes[i] = row.getCollatedChildrenRequisitionBoxes();
				childAllocBoxes[i] = row.getCollatedChildrenAllocationBoxes();
				prevWidths[i] = row.getCollatedChildrenAllocationX();
				childAlignmentFlags[i] = row.getCollatedChildrenAlignmentFlags();
				// Copy grid x-allocation to row x-allocation
				row.layoutAllocBox.allocateX( layoutAllocBox );
			}
			else
			{
				childBoxes[i] = new LReqBox[] { child.layoutReqBox };
				childAllocBoxes[i] = new LAllocBox[] { child.layoutAllocBox };
				prevWidths[i] = new double[] { child.getAllocationX() };
				childAlignmentFlags[i] = new int[] { child.getAlignmentFlags() };
			}
		}

		GridLayout.allocateX( layoutReqBox, columnBoxes, childBoxes, layoutAllocBox, columnAllocBoxes, childAllocBoxes, childAlignmentFlags, numColumns, numRows,
				getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		for (int r = 0; r < collationLeaves.length; r++)
		{
			double rowPrevWidths[] = prevWidths[r];

			DPWidget child = collationLeaves[r];
			if ( child instanceof DPGridRow )
			{
				DPGridRow row = (DPGridRow)child;
				
				child.onAllocationXRefreshed();
				for (int c = 0; c < row.collationLeaves.length; c++)
				{
					row.collationLeaves[c].refreshAllocationX( rowPrevWidths[c] );
				}
			}
			else
			{
				child.refreshAllocationX( rowPrevWidths[0] );
			}
		}
	}

	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		refreshSize();
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		LAllocV prevAllocVs[] = getCollatedChildrenAllocV();
		
		GridLayout.allocateY( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, getRowSpacing(), getRowExpand() );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationY( prevAllocVs[i] );
			i++;
		}
		
		columnBoxes = rowBoxes = null;
		columnAllocBoxes = rowAllocBoxes = null;
	}
	
	
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPointVertical( Arrays.asList( collationLeaves ), localPos, filter );
	}


	
	
	protected AABox2[] computeCollatedBranchBoundsBoxes(DPContainer collatedBranch, int rangeStart, int rangeEnd)
	{
		refreshCollation();
		
		DPWidget startLeaf = collationLeaves[rangeStart];
		DPWidget endLeaf = collationLeaves[rangeEnd-1];
		double yStart = startLeaf.getPositionInParentSpaceY();
		double yEnd = endLeaf.getPositionInParentSpaceY()  +  endLeaf.getAllocationInParentSpaceY();
		AABox2 box = new AABox2( 0.0, yStart, getAllocationX(), yEnd );
		return new AABox2[] { box };
	}

	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getCollatedChildren();
	}
	
	protected List<DPWidget> verticalNavigationList()
	{
		return getCollatedChildren();
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
