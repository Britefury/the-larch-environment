//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Layout.LBox;
import BritefuryJ.DocPresent.Layout.ScriptLayout;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;
import BritefuryJ.Math.Point2;


public class DPScript extends DPContainer
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
	
	private static double childScale = 0.7;
	
	
	
	protected DPWidget[] children;
	protected LBox columnBoxes[];
	protected double rowBaselineY[];
	
	
	
	public DPScript()
	{
		this( ScriptStyleSheet.defaultStyleSheet );
	}
	
	public DPScript(ScriptStyleSheet styleSheet)
	{
		super( styleSheet );
		
		children = new DPWidget[NUMCHILDREN];
		columnBoxes = new LBox[3];
		columnBoxes[0] = new LBox( null );
		columnBoxes[1] = new LBox( null );
		columnBoxes[2] = new LBox( null );
		rowBaselineY = new double[3];
	}

	
	
	public DPWidget getChild(int slot)
	{
		return children[slot];
	}
	
	public void setChild(int slot, DPWidget child)
	{
		DPWidget existingChild = children[slot];
		if ( child != existingChild )
		{
			if ( existingChild != null )
			{
				unregisterChild( existingChild );
				registeredChildren.remove( existingChild );
			}
			
			children[slot] = child;
			
			if ( child != null )
			{
				registeredChildren.add( child );
				registerChild( child, null );
			}
			
			queueResize();
		}
	}
	
	
	
	
	public DPWidget getMainChild()
	{
		return getChild( MAIN );
	}
	
	public DPWidget getLeftSuperscriptChild()
	{
		return getChild( LEFTSUPER );
	}
	
	public DPWidget getLeftSubscriptChild()
	{
		return getChild( LEFTSUB );
	}
	
	public DPWidget getRightSuperscriptChild()
	{
		return getChild( RIGHTSUPER );
	}
	
	public DPWidget getRightSubscriptChild()
	{
		return getChild( RIGHTSUB );
	}
	
	
	public void setMainChild(DPWidget child)
	{
		setChild( MAIN, child );
	}
	
	public void setLeftSuperscriptChild(DPWidget child)
	{
		setChild( LEFTSUPER, child );
	}
	
	public void setLeftSubscriptChild(DPWidget child)
	{
		setChild( LEFTSUB, child );
	}
	
	public void setRightSuperscriptChild(DPWidget child)
	{
		setChild( RIGHTSUPER, child );
	}
	
	public void setRightSubscriptChild(DPWidget child)
	{
		setChild( RIGHTSUB, child );
	}
	

	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		int slot = Arrays.asList( children ).indexOf( child );
		setChild( slot, null );
	}
	
	
	
	protected List<DPWidget> getChildren()
	{
		ArrayList<DPWidget> ch = new ArrayList<DPWidget>();
		
		for (int slot = 0; slot < NUMCHILDREN; slot++)
		{
			if ( children[slot] != null )
			{
				ch.add( children[slot] );
			}
		}
		
		return ch;
	}

	
	
	
	
	protected boolean hasMainChild()
	{
		return children[MAIN] != null;
	}
	
	protected boolean hasLeftChild()
	{
		return children[LEFTSUB] != null  ||  children[LEFTSUPER] != null;
	}
	
	protected boolean hasRightChild()
	{
		return children[RIGHTSUB] != null  ||  children[RIGHTSUPER] != null;
	}
	
	protected boolean hasSuperscriptChild()
	{
		return children[LEFTSUPER] != null  ||  children[RIGHTSUPER] != null;
	}
	
	protected boolean hasSubscriptChild()
	{
		return children[LEFTSUB] != null  ||  children[RIGHTSUB] != null;
	}
	
	
	

	protected void updateRequisitionX()
	{
		LBox boxes[] = new LBox[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( i != MAIN )
			{
				boxes[i] = children[i] != null  ?  children[i].refreshRequisitionX().scaled( children[i], childScale )  :  null;
			}
			else
			{
				boxes[i] = children[i] != null  ?  children[i].refreshRequisitionX()  :  null;
			}
		}
		
		ScriptLayout.computeRequisitionX( layoutBox, columnBoxes, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], getSpacing(), getScriptSpacing() );
	}

	protected void updateRequisitionY()
	{
		LBox boxes[] = new LBox[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( i != MAIN )
			{
				boxes[i] = children[i] != null  ?  children[i].refreshRequisitionY().scaled( children[i], childScale )  :  null;
			}
			else
			{
				boxes[i] = children[i] != null  ?  children[i].refreshRequisitionY()  :  null;
			}
		}
		
		ScriptLayout.computeRequisitionY( layoutBox, rowBaselineY, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], getSpacing(), getScriptSpacing() );
	}
	

	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LBox boxes[] = new LBox[NUMCHILDREN];
		double prevChildWidths[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			boxes[i] = children[i] != null  ?  children[i].layoutBox  :  null;
			prevChildWidths[i] = children[i] != null  ?  children[i].layoutBox.getAllocationX()  :  0.0;
		}
		
		ScriptLayout.allocateX( layoutBox, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], columnBoxes, getSpacing(), getScriptSpacing() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				children[i].refreshAllocationX( prevChildWidths[i] );
			}
		}
	}

	
	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LBox boxes[] = new LBox[NUMCHILDREN];
		double prevChildHeights[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			boxes[i] = children[i] != null  ?  children[i].layoutBox  :  null;
			prevChildHeights[i] = children[i] != null  ?  children[i].layoutBox.getAllocationY()  :  0.0;
		}
		
		ScriptLayout.allocateY( layoutBox, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], rowBaselineY, getSpacing(), getScriptSpacing() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			if ( children[i] != null )
			{
				children[i].refreshAllocationY( prevChildHeights[i] );
			}
		}
	}
	

	
	
	
	private ArrayList<DPWidget> getChildrenInSlots(int[] slots)
	{
		ArrayList<DPWidget> entries = new ArrayList<DPWidget>();
		for (int slot: slots)
		{
			if ( children[slot] != null )
			{
				entries.add( children[slot] );
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
			DPWidget child = column.get( i );
			
			double edgeX = 0.0;
			if ( bRightEdge )
			{
				edgeX = child.getPositionInParentSpace().x + child.getAllocationInParentSpace().x;
			}
			else
			{
				edgeX = child.getPositionInParentSpace().x;
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
		if ( registeredChildren.size() == 0 )
		{
			// No children
			return null;
		}
		else if ( registeredChildren.size() == 1 )
		{
			// Only 1 child
			return registeredChildren.get( 0 );
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
	
	protected List<DPWidget> horizontalNavigationList()
	{
		ArrayList<DPWidget> xs = new ArrayList<DPWidget>();
		
		for (DPWidget x: children)
		{
			if ( x != null )
			{
				xs.add( x );
			}
		}
		
		return xs;
	}
	
	
	
	
	//
	// STYLESHEET METHODS
	//
	
	protected double getSpacing()
	{
		return ((ScriptStyleSheet)styleSheet).getSpacing();
	}

	protected double getScriptSpacing()
	{
		return ((ScriptStyleSheet)styleSheet).getScriptSpacing();
	}
}
