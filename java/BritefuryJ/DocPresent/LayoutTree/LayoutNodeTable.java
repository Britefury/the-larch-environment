//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPTable;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleParams.TableStyleParams;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.Layout.TableLayout;
import BritefuryJ.DocPresent.Layout.TableLayout.TablePackingParams;
import BritefuryJ.Math.Point2;

public class LayoutNodeTable extends ArrangedLayoutNode
{
	private LReqBox columnBoxes[], rowBoxes[];
	private LAllocBox columnAllocBoxes[], rowAllocBoxes[];

	
	public LayoutNodeTable(DPTable element)
	{
		super( element );
	}




	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPTable table = (DPTable)element;
		List<DPWidget> layoutChildren = table.getLayoutChildren();
		TableLayout.TablePackingParams packingParams[] = table.getTablePackingParamsArray();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[layoutChildren.size()];
		for (int i = 0; i < layoutChildren.size(); i++)
		{
			DPWidget child = layoutChildren.get( i );
			childBoxes[i] = child.getLayoutNode().refreshRequisitionX();
		}

		columnBoxes = TableLayout.computeRequisitionX( layoutReqBox, childBoxes, packingParams, table.width(), table.height(), getColumnSpacing(), getRowSpacing() );
		columnAllocBoxes = new LAllocBox[columnBoxes.length];
		for (int i = 0; i < columnAllocBoxes.length; i++)
		{
			columnAllocBoxes[i] = new LAllocBox( null );
		}
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPTable table = (DPTable)element;
		List<DPWidget> layoutChildren = element.getLayoutChildren();
		TableLayout.TablePackingParams packingParams[] = table.getTablePackingParamsArray();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[layoutChildren.size()];
		int childAlignmentFlags[] = new int[layoutChildren.size()];
		for (int i = 0; i < layoutChildren.size(); i++)
		{
			DPWidget child = layoutChildren.get( i );
			childBoxes[i] = child.getLayoutNode().refreshRequisitionY();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}

		rowBoxes = TableLayout.computeRequisitionY( layoutReqBox, childBoxes, packingParams, childAlignmentFlags, table.width(), table.height(), getColumnSpacing(), getRowSpacing() );
		rowAllocBoxes = new LAllocBox[rowBoxes.length];
		for (int i = 0; i < rowAllocBoxes.length; i++)
		{
			rowAllocBoxes[i] = new LAllocBox( null );
		}
	}
	


	
	
	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPTable table = (DPTable)element;
		List<DPWidget> layoutChildren = element.getLayoutChildren();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[layoutChildren.size()];
		LAllocBoxInterface childAllocBoxes[] = new LAllocBoxInterface[layoutChildren.size()];
		double prevWidths[] = new double[layoutChildren.size()];
		TableLayout.TablePackingParams packingParams[] = table.getTablePackingParamsArray();
		int childAlignmentFlags[] = new int[layoutChildren.size()];
		for (int i = 0; i < layoutChildren.size(); i++)
		{
			DPWidget child = layoutChildren.get( i );
			LayoutNode layoutNode = child.getLayoutNode();
			childBoxes[i] = layoutNode.getRequisitionBox();
			childAllocBoxes[i] = layoutNode.getAllocationBox();
			prevWidths[i] = layoutNode.getAllocationX();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}
		
		TableLayout.allocateX( layoutReqBox, columnBoxes, childBoxes, getAllocationBox(), columnAllocBoxes, childAllocBoxes, packingParams, childAlignmentFlags, table.width(), table.height(), getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		int i = 0;
		for (DPWidget child: layoutChildren)
		{
			child.getLayoutNode().refreshAllocationX( prevWidths[i] );
			i++;
		}
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPTable table = (DPTable)element;
		List<DPWidget> layoutChildren = element.getLayoutChildren();
		
		LReqBoxInterface childBoxes[] = new LReqBoxInterface[layoutChildren.size()];
		LAllocBoxInterface childAllocBoxes[] = new LAllocBoxInterface[layoutChildren.size()];
		TableLayout.TablePackingParams packingParams[] = table.getTablePackingParamsArray();
		LAllocV prevAllocVs[] = new LAllocV[layoutChildren.size()];
		int childAlignmentFlags[] = new int[layoutChildren.size()];
		for (int i = 0; i < layoutChildren.size(); i++)
		{
			DPWidget child = layoutChildren.get( i );
			LayoutNode layoutNode = child.getLayoutNode();
			childBoxes[i] = layoutNode.getRequisitionBox();
			childAllocBoxes[i] = layoutNode.getAllocationBox();
			prevAllocVs[i] = layoutNode.getAllocV();
			childAlignmentFlags[i] = child.getAlignmentFlags();
		}
		
		TableLayout.allocateY( layoutReqBox, rowBoxes, childBoxes, getAllocationBox(), rowAllocBoxes, childAllocBoxes, packingParams, childAlignmentFlags, table.width(), table.height(), getColumnSpacing(), getRowSpacing(), getColumnExpand(), getRowExpand() );
		
		int i = 0;
		for (DPWidget child: layoutChildren)
		{
			child.getLayoutNode().refreshAllocationY( prevAllocVs[i] );
			i++;
		}
	}
	

	
	
	
	private boolean doesChildCoverCell(DPWidget child, int x, int y)
	{
		DPTable table = (DPTable)element;
		TablePackingParams packing = table.getTablePackingParamsForChild( child );

		return x <= ( packing.x + packing.colSpan )  &&  y <= ( packing.y + packing.rowSpan );
	}
	
	private DPWidget getChildCoveringCell(int x, int y)
	{
		DPTable table = (DPTable)element;

		DPWidget child = table.get( x, y );
		
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
							child = table.get( colX, searchY );
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
							child = table.get( searchX, rowY );
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
					child = table.get( x - radius, y - radius );
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
				double iUpperX = columnI.getAllocPositionInParentSpaceX() + columnI.getAllocationX();
				double jLowerX = columnJ.getAllocPositionInParentSpaceX();
				
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
				double iUpperY = rowI.getAllocPositionInParentSpaceY() + rowI.getAllocationY();
				double jLowerY = rowJ.getAllocPositionInParentSpaceY();
				
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
	
	public List<DPWidget> horizontalNavigationList()
	{
		DPTable table = (DPTable)element;
		return table.getLayoutChildren();
	}
	

	//
	//
	// STYLE METHODS
	//
	//
	
	protected double getColumnSpacing()
	{
		return ((TableStyleParams)element.getStyleSheet()).getColumnSpacing();
	}

	protected boolean getColumnExpand()
	{
		return ((TableStyleParams)element.getStyleSheet()).getColumnExpand();
	}

	
	protected double getRowSpacing()
	{
		return ((TableStyleParams)element.getStyleSheet()).getRowSpacing();
	}

	protected boolean getRowExpand()
	{
		return ((TableStyleParams)element.getStyleSheet()).getRowExpand();
	}
}
