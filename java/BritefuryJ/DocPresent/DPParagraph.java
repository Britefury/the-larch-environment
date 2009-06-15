//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.Layout.BoxPackingParams;
import BritefuryJ.DocPresent.Layout.LBox;
import BritefuryJ.DocPresent.Layout.ParagraphLayout;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.Math.Point2;



public class DPParagraph extends DPContainerSequence
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}



	
	private ParagraphLayout.Line lines[];

	
	
	public DPParagraph()
	{
		this( ParagraphStyleSheet.defaultStyleSheet );
	}

	public DPParagraph(ParagraphStyleSheet styleSheet)
	{
		super( styleSheet );
		
		lines = new ParagraphLayout.Line[0];
	}


	
	

	public int getInsertIndex(Point2 localPos)
	{
		//Return the index at which an item could be inserted.
		// localPos is checked against the contents of the box in order to determine the insert index
		
		if ( size() == 0 )
		{
			return 0;
		}
	
		double pos = localPos.x;
		
		double[] midPoints = new double[registeredChildren.size()];
		
		for (int i = 0; i < midPoints.length; i++)
		{
			DPWidget child = registeredChildren.get( i );
			midPoints[i] = child.getPositionInParentSpace().x  +  child.getAllocationInParentSpace().x * 0.5;
		}
		
		if ( pos < midPoints[0] )
		{
			return size();
		}
		else if ( pos > midPoints[midPoints.length-1] )
		{
			return 0;
		}
		else
		{
			for (int i = 0; i < midPoints.length-1; i++)
			{
				double lower = midPoints[i];
				double upper = midPoints[i+1];
				if ( pos >= lower  &&  pos <= upper )
				{
					return i + 1;
				}
			}
			
			throw new CouldNotFindInsertionPointException();
		}
	}

	
	
	protected void updateRequisitionX()
	{
		LBox[] childBoxes = new LBox[registeredChildren.size()];
		BoxPackingParams[] packingParams = new BoxPackingParams[registeredChildren.size()];
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			childBoxes[i] = registeredChildren.get( i ).refreshRequisitionX();
			packingParams[i] = (BoxPackingParams)registeredChildren.get( i ).getParentPacking();
		}

		ParagraphLayout.computeRequisitionX( layoutBox, childBoxes, getIndentation(), getSpacing(), packingParams );
	}

	protected void updateRequisitionY()
	{
		for (int y = 0; y < lines.length; y++)
		{
			LBox[] lineChildren = lines[y].getChildBoxes();
			for (int x = 0; x < lineChildren.length; x++)
			{
				lineChildren[x].getElement().refreshRequisitionY();
			}
		}

		ParagraphLayout.computeRequisitionY( layoutBox, lines, getVSpacing(), getAlignment() );
	}
	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LBox childBoxes[] = getChildrenLayoutBoxes();
		double prevWidths[] = getChildrenAllocationX();
		BoxPackingParams packing[] = (BoxPackingParams[])getChildrenPackingParams( new BoxPackingParams[registeredChildren.size()] );
		
		lines = ParagraphLayout.allocateX( layoutBox, childBoxes, getIndentation(), getSpacing(), packing );
		
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
		
		double prevHeights[][] = new double[lines.length][];
		for (int y = 0; y < lines.length; y++)
		{
			LBox[] lineChildren = lines[y].getChildBoxes();
			prevHeights[y] = new double[lineChildren.length];
			for (int x = 0; x < lineChildren.length; x++)
			{
				prevHeights[y][x] = lineChildren[x].getAllocationY();
			}
		}
		
		ParagraphLayout.allocateY( layoutBox, lines, getSpacing(), getAlignment() );
		
		for (int y = 0; y < lines.length; y++)
		{
			LBox[] lineChildren = lines[y].getChildBoxes();
			for (int x = 0; x < lineChildren.length; x++)
			{
				lineChildren[x].getElement().refreshAllocationY( prevHeights[y][x] );
			}
		}
	}
	

	
	private ParagraphLayout.Line getLineClosestToLocalPoint(Point2 localPos)
	{
		if ( lines.length == 0 )
		{
			return null;
		}
		else if ( lines.length == 1 )
		{
			return lines[0];
		}
		else
		{
			ParagraphLayout.Line lineI = lines[0];
			for (int i = 0; i < lines.length - 1; i++)
			{
				ParagraphLayout.Line lineJ = lines[i+1];
				double iUpperY = lineI.getLineBox().getPositionInParentSpaceY() + lineI.getLineBox().getAllocationY();
				double jLowerY = lineJ.getLineBox().getPositionInParentSpaceY();
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					return lineI;
				}
				
				lineI = lineJ;
			}
			
			return lines[lines.length-1];
		}
	}

	private DPWidget getLineChildClosestToLocalPoint(ParagraphLayout.Line line, Point2 localPos)
	{
		LBox children[] = line.getChildBoxes();
		if ( children.length == 0 )
		{
			return null;
		}
		else if ( children.length == 1 )
		{
			return children[0].getElement();
		}
		else
		{
			LBox childI = children[0];
			for (int i = 0; i < children.length - 1; i++)
			{
				LBox childJ = children[i+1];
				double iUpperX = childI.getPositionInParentSpaceX() + childI.getAllocationX();
				double jLowerX = childJ.getPositionInParentSpaceX();
				
				double midX = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midX )
				{
					return childI.getElement();
				}
				
				childI = childJ;
			}
			
			return children[children.length-1].getElement();
		}
	}

	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		ParagraphLayout.Line line = getLineClosestToLocalPoint( localPos );
		
		if ( line != null )
		{
			DPWidget child = getLineChildClosestToLocalPoint( line, localPos );
			
			DPWidget c = getLeafClosestToLocalPointFromChild( child, localPos, filter );
			
			if ( c != null )
			{
				return c;
			}
			
			int index = registeredChildren.indexOf( child );
			
			DPWidget next = null;
			for (int j = index + 1; j < registeredChildren.size(); j++)
			{
				next = getLeafClosestToLocalPointFromChild( registeredChildren.get( j ), localPos, filter );
				if ( next != null )
				{
					break;
				}
			}

			DPWidget prev = null;
			for (int j = index - 1; j >= 0; j--)
			{
				prev = getLeafClosestToLocalPointFromChild( registeredChildren.get( j ), localPos, filter );
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
				double distToPrev = localPos.x - ( prev.getPositionInParentSpace().x + prev.getAllocationInParentSpace().x );
				double distToNext = next.getPositionInParentSpace().x - localPos.x;
				
				return distToPrev > distToNext  ?  prev  :  next;
			}
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
	// STYLESHEET METHODS
	//
	//

	public VAlignment getAlignment()
	{
		return ((ParagraphStyleSheet)styleSheet).getAlignment();
	}

	public double getSpacing()
	{
		return ((ParagraphStyleSheet)styleSheet).getSpacing();
	}

	public double getVSpacing()
	{
		return ((ParagraphStyleSheet)styleSheet).getVSpacing();
	}

	public double getPadding()
	{
		return ((ParagraphStyleSheet)styleSheet).getPadding();
	}

	public double getIndentation()
	{
		return ((ParagraphStyleSheet)styleSheet).getIndentation();
	}
}
