//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.Layout.ScriptLayout;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;
import BritefuryJ.Math.Point2;

public class LayoutNodeScript extends ArrangedLayoutNode
{
	public static int LEFTSUPER = 0;
	public static int LEFTSUB = 1;
	public static int MAIN = 2;
	public static int RIGHTSUPER = 3;
	public static int RIGHTSUB = 4;
	
	public static int NUMCHILDREN = 5;

	private static int LEFTCOLUMN = 0;
	private static int MAINCOLUMN = 1;
	private static int RIGHTCOLUMN = 2;
	
	protected LReqBox columnBoxes[];
	protected double rowBaselineY[];
	
	
	public LayoutNodeScript(DPScript element)
	{
		super( element );

		columnBoxes = new LReqBox[3];
		columnBoxes[0] = new LReqBox();
		columnBoxes[1] = new LReqBox();
		columnBoxes[2] = new LReqBox();
		rowBaselineY = new double[3];
	}

	

	private static double getChildScale()
	{
		return DPScript.getChildScale();
	}
	

	protected void updateRequisitionX()
	{
		DPScript script = (DPScript)element;
		double childScale = getChildScale();
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = script.getWrappedChild( i );
			if ( i != MAIN )
			{
				boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionX().scaled( childScale )  :  null;
			}
			else
			{
				boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionX()  :  null;
			}
		}
		
		ScriptLayout.computeRequisitionX( layoutReqBox, columnBoxes, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], getColumnSpacing(), getRowSpacing() );
	}

	protected void updateRequisitionY()
	{
		DPScript script = (DPScript)element;
		double childScale = getChildScale();
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = script.getWrappedChild( i );
			if ( i != MAIN )
			{
				boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionY().scaled( childScale )  :  null;
			}
			else
			{
				boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionY()  :  null;
			}
		}
		
		ScriptLayout.computeRequisitionY( layoutReqBox, rowBaselineY, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], getColumnSpacing(), getRowSpacing() );
	}
	

	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		DPScript script = (DPScript)element;
		double childScale = getChildScale();
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		double prevChildWidths[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = script.getWrappedChild( i );
			if ( i != MAIN )
			{
				reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox().scaled( childScale )  :  null;
			}
			else
			{
				reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			}
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildWidths[i] = child != null  ?  allocBoxes[i].getAllocationX()  :  0.0;
		}
		
		ScriptLayout.allocateX( layoutReqBox, reqBoxes[LEFTSUPER], reqBoxes[LEFTSUB], reqBoxes[MAIN], reqBoxes[RIGHTSUPER], reqBoxes[RIGHTSUB], columnBoxes,
				layoutAllocBox, allocBoxes[LEFTSUPER], allocBoxes[LEFTSUB], allocBoxes[MAIN], allocBoxes[RIGHTSUPER], allocBoxes[RIGHTSUB],
				getColumnSpacing(), getRowSpacing() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = script.getWrappedChild( i );
			if ( child != null )
			{
				if ( i != MAIN )
				{
					allocBoxes[i].scaleAllocationX( 1.0 / childScale );
				}
				child.getLayoutNode().refreshAllocationX( prevChildWidths[i] );
			}
		}
	}

	
	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		DPScript script = (DPScript)element;
		double childScale = getChildScale();
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		LAllocV prevChildAllocVs[] = new LAllocV[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = script.getWrappedChild( i );
			if ( i != MAIN )
			{
				reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox().scaled( childScale )  :  null;
			}
			else
			{
				reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			}
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildAllocVs[i] = child != null  ?  allocBoxes[i].getAllocV()  :  null;
		}
		
		ScriptLayout.allocateY( layoutReqBox, reqBoxes[LEFTSUPER], reqBoxes[LEFTSUB], reqBoxes[MAIN], reqBoxes[RIGHTSUPER], reqBoxes[RIGHTSUB], rowBaselineY,
				layoutAllocBox, allocBoxes[LEFTSUPER], allocBoxes[LEFTSUB], allocBoxes[MAIN], allocBoxes[RIGHTSUPER], allocBoxes[RIGHTSUB],
				getColumnSpacing(), getRowSpacing() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPWidget child = script.getWrappedChild( i );
			if ( child != null )
			{
				if ( i != MAIN )
				{
					allocBoxes[i].scaleAllocationY( 1.0 / childScale );
				}
				child.getLayoutNode().refreshAllocationY( prevChildAllocVs[i] );
			}
		}
	}
	

	
	
	
	private ArrayList<DPWidget> getChildrenInSlots(int[] slots)
	{
		DPScript script = (DPScript)element;

		ArrayList<DPWidget> entries = new ArrayList<DPWidget>();
		for (int slot: slots)
		{
			DPWidget child = script.getWrappedChild( slot );
			if ( child != null )
			{
				entries.add( child );
			}
		}
		
		return entries;
	}
	
	private ArrayList<DPWidget> getChildrenInColumn(int column)
	{
		if ( column == LEFTCOLUMN )
		{
			int slots[] = { LEFTSUPER, LEFTSUB };
			return getChildrenInSlots( slots );
		}
		else if ( column == MAINCOLUMN )
		{
			int slots[] = { MAIN };
			return getChildrenInSlots( slots );
		}
		else if ( column == RIGHTCOLUMN )
		{
			int slots[] = { RIGHTSUPER, RIGHTSUB };
			return getChildrenInSlots( slots );
		}
		else
		{
			throw new RuntimeException();
		}
	}
	
	
	private double getColumnXEdge(ArrayList<DPWidget> column, boolean bRightEdge)
	{
		double columnEdgeX = 0.0;
		for (int i = 0; i < column.size(); i++)
		{
			LayoutNode childNode = column.get( i ).getLayoutNode();
			
			double edgeX = 0.0;
			if ( bRightEdge )
			{
				edgeX = childNode.getPositionInParentSpaceX() + childNode.getAllocationInParentSpaceX();
			}
			else
			{
				edgeX = childNode.getPositionInParentSpaceX();
			}
			
			if ( i > 0 )
			{
				if ( bRightEdge )
				{
					columnEdgeX = Math.max( columnEdgeX, edgeX );
				}
				else
				{
					columnEdgeX = Math.min( columnEdgeX, edgeX );
				}
			}
			else
			{
				columnEdgeX= edgeX;
			}
		}
		
		return columnEdgeX;
	}
	
	
	
	private DPWidget getLeafClosestToLocalPointInColumn(ArrayList<DPWidget> column, Point2 localPos, WidgetFilter filter)
	{
		// Now determine which child entry is the closest
		if ( column.size() == 1 )
		{
			// One entry; only 1 choice
			return getLeafClosestToLocalPointFromChild( column.get( 0 ), localPos, filter );
		}
		else if ( column.size() == 2 )
		{
			DPWidget childI = column.get( 0 );
			DPWidget childJ = column.get( 1 );
			double iUpperY = childI.getPositionInParentSpace().y + childI.getAllocationInParentSpace().y;
			double jLowerY = childJ.getPositionInParentSpace().y;
				
			double midY = ( iUpperY + jLowerY ) * 0.5;
			
			DPWidget childA = localPos.y < midY  ?  childI  :  childJ;
			DPWidget c = getLeafClosestToLocalPointFromChild( childA, localPos, filter );
			if ( c != null )
			{
				return c;
			}

			DPWidget childB = childA == childI  ?  childJ  :  childI;
			return getLeafClosestToLocalPointFromChild( childB, localPos, filter );
		}
		else
		{
			throw new RuntimeException();
		}
	}
	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		List<DPWidget> layoutChildren = element.getLayoutChildren();
		if ( layoutChildren.size() == 0 )
		{
			// No children
			return null;
		}
		else if ( layoutChildren.size() == 1 )
		{
			// Only 1 child
			return layoutChildren.get( 0 );
		}
		else
		{
			// Group children by column
			ArrayList< ArrayList<DPWidget> > childrenByColumn = new ArrayList< ArrayList<DPWidget> >();
			
			int[] columns = { LEFTCOLUMN, MAINCOLUMN, RIGHTCOLUMN };
			
			for (int col: columns)
			{
				ArrayList<DPWidget> childEntries = getChildrenInColumn( col );
				
				if ( childEntries.size() > 0 )
				{
					childrenByColumn.add( childEntries );
				}
			}
			
			
			// Determine which column is closest
			ArrayList<DPWidget> closestColumn = null;
			int columnIndex = -1;
			
			if ( childrenByColumn.size() == 1 )
			{
				closestColumn = childrenByColumn.get( 0 );
				columnIndex = 0;
			}
			else
			{
				ArrayList<DPWidget> colI = childrenByColumn.get( 0 );
				for (int i = 0; i < childrenByColumn.size() - 1; i++)
				{
					ArrayList<DPWidget> colJ = childrenByColumn.get( i + 1 );
					double rightEdgeI = getColumnXEdge( colI, true );
					double leftEdgeJ = getColumnXEdge( colJ, false );
					
					double midPoint = ( rightEdgeI + leftEdgeJ ) * 0.5;
					
					if ( localPos.x < midPoint )
					{
						closestColumn = colI;
						columnIndex = i;
						break;
					}
					
					colI = colJ;
				}
				
				if ( closestColumn == null )
				{
					columnIndex = childrenByColumn.size() - 1;
					closestColumn = childrenByColumn.get( columnIndex );
				}
			}
			
			
			
			DPWidget c = getLeafClosestToLocalPointInColumn( closestColumn, localPos, filter );
			
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPWidget next = null;
				for (int j = columnIndex + 1; j < childrenByColumn.size(); j++)
				{
					next = getLeafClosestToLocalPointInColumn( childrenByColumn.get( j ), localPos, filter );
					if ( next != null )
					{
						break;
					}
				}

				DPWidget prev = null;
				for (int j = columnIndex - 1; j >= 0; j--)
				{
					prev = getLeafClosestToLocalPointInColumn( childrenByColumn.get( j ), localPos, filter );
					if ( prev != null )
					{
						break;
					}
				}
				
				
				if ( prev == null  &&  next == null )
				{
					return null;
				}
				else if ( prev == null  &&  next != null )
				{
					return next;
				}
				else if ( prev != null  &&  next == null )
				{
					return prev;
				}
				else
				{
					double distToPrev = localPos.y - ( prev.getPositionInParentSpace().y + prev.getAllocationInParentSpace().y );
					double distToNext = next.getPositionInParentSpace().y - localPos.y;
					
					return distToPrev > distToNext  ?  prev  :  next;
				}
			}
		}
	}


	
	//
	// Focus navigation methods
	//
	
	public List<DPWidget> horizontalNavigationList()
	{
		return element.getLayoutChildren();
	}
	
	
	
	
	//
	// STYLESHEET METHODS
	//
	
	protected double getColumnSpacing()
	{
		return ((ScriptStyleSheet)element.getStyleSheet()).getColumnSpacing();
	}

	protected double getRowSpacing()
	{
		return ((ScriptStyleSheet)element.getStyleSheet()).getRowSpacing();
	}
}
