//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import java.util.ArrayList;

public class ParagraphLayout
{
	public static class Line
	{
		public LBox lineBox;
		public LBox children[];
		
		
		private Line(LBox ch[], double indentation, double spacing, BoxPackingParams packingParams[], double allocation)
		{
			children = ch;
			
			lineBox = new LBox( null );
			HorizontalLayout.computeRequisitionX( lineBox, children, spacing, packingParams );
			lineBox.allocationX = allocation - indentation;
			HorizontalLayout.allocateX( lineBox, children, spacing, packingParams );
			for (LBox child: children)
			{
				child.positionInParentSpaceX += indentation;
			}
		}
		
		
		public LBox getLineBox()
		{
			return lineBox;
		}
		
		public LBox[] getChildBoxes()
		{
			return children;
		}
		
		
		private void computeRequisitionY(VAlignment vAlignment)
		{
			HorizontalLayout.computeRequisitionY( lineBox, children, vAlignment );
		}

		private void allocateY(VAlignment vAlignment)
		{
			HorizontalLayout.allocateY( lineBox, children, vAlignment );
			
			for (LBox child: children)
			{
				child.positionInParentSpaceY  +=  lineBox.positionInParentSpaceY;
			}
		}
	}
	
	

	public static void computeRequisitionX(LBox box, LBox children[], double indentation, double hSpacing, BoxPackingParams packingParams[])
	{
		// Accumulate the width required for all the children
		
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater
		
		double minWidth = 0.0, lineWidth = 0.0, prefWidth = 0.0;
		double minAdvance = 0.0, lineAdvance = 0.0, prefAdvance = 0.0;
		double minX = 0.0, lineX = 0.0, prefX = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LBox child = children[i];
			
			BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
			double padding = params != null  ?  params.padding  :  0.0;
			
			// Filter out any h-spacing that is within the amount of padding
			double prefChildSpacing = Math.max( child.prefHSpacing - padding, 0.0 );
			
			prefWidth = prefX + child.prefWidth  +  padding * 2.0;
			prefAdvance = prefWidth + prefChildSpacing;
			prefX = prefAdvance + hSpacing;

		
			if ( child.bLineBreak )
			{
				minWidth = Math.max( minWidth, lineWidth );
				minAdvance = Math.max( minAdvance, lineAdvance );
				minX = Math.max( minX, lineX );
				
				// New line
				lineWidth = 0.0;
				lineAdvance = 0.0;
				lineX = indentation;
			}
			else
			{
				double minChildSpacing = Math.max( child.minHSpacing - padding, 0.0 );
				
				lineWidth = lineX + child.minWidth  +  padding * 2.0;
				lineAdvance = lineWidth + minChildSpacing;
				lineX = lineAdvance + hSpacing;
			}
		}
		
		
		minWidth = Math.max( minWidth, lineWidth );
		minAdvance = Math.max( minAdvance, lineAdvance );
		minX = Math.max( minX, lineX );

		box.setRequisitionX( minWidth, prefWidth, minAdvance - minWidth, prefAdvance - prefWidth );
	}

	
	public static void computeRequisitionY(LBox box, Line lines[], double vSpacing, VAlignment vAlignment)
	{
		LBox lineBoxes[] = new LBox[lines.length];
		
		int i = 0;
		for (Line line: lines)
		{
			line.computeRequisitionY( vAlignment );
			lineBoxes[i++] = line.lineBox;
		}
		
		VerticalLayout.computeRequisitionY( box, lineBoxes, vSpacing, null );
	}




	public static Line[] allocateX(LBox box, LBox children[], double indentation, double hSpacing, BoxPackingParams packingParams[])
	{
		boolean bFirstLine = true;
		
		ArrayList<Line> lines = new ArrayList<Line>();
		int lineStartIndex = 0;
		
		double lineWidth = 0.0;
		double lineAdvance = 0.0;
		double lineX = 0.0;
		
		LBox bestLineBreak = null;
		int bestLineBreakIndex = -1;
		double xAtBestLineBreak = 0.0, xAfterBestLineBreak = 0.0;
		
		LBox lastLineBreak = null;
		int lastLineBreakIndex = -1;
		double xAfterLastLineBreak = 0.0;
		
		for (int i = 0; i < children.length; i++)
		{
			LBox child = children[i];
			
			BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
			double padding = params != null  ?  params.padding  :  0.0;
			
			// Filter out any h-spacing that is within the amount of padding
			double childSpacing = Math.max( child.prefHSpacing - padding, 0.0 );
			
			// Keep track of the best and most recent line break boxes
			if ( child.bLineBreak )
			{
				if ( bestLineBreak == null  ||  child.lineBreakCost <= bestLineBreak.lineBreakCost )
				{
					// Found a better line break
					bestLineBreak = child;
					bestLineBreakIndex = i;
					xAtBestLineBreak = lineX;
				}
				
				lastLineBreak = child;
				lastLineBreakIndex = i;
			}
			
			// Accumulate width, advance, and x
			lineWidth = lineX + child.prefWidth  +  padding * 2.0;
			lineAdvance = lineWidth + childSpacing;
			lineX = lineAdvance + hSpacing;
			
			// Note the x position after the best and most recent line breaks
			if ( child == bestLineBreak )
			{
				xAfterBestLineBreak = lineX;
			}
			
			if ( child == lastLineBreak )
			{
				xAfterLastLineBreak = lineX;
			}
			
			
			if ( lineWidth > box.allocationX  &&  bestLineBreak != null  &&  i > lineStartIndex )
			{
				// We need to start a new line
				
				// Pick a line break
				int lineBreakIndex;
				double xAfterLineBreak;
				if ( ( lineWidth - xAtBestLineBreak )  >  box.allocationX  &&  child != lastLineBreak  &&  lastLineBreak != null )
				{
					// We still go over the allocation limit even if we do split at the best line break.
					// In this case, choose the most recent line break instead
					lineBreakIndex = lastLineBreakIndex;
					xAfterLineBreak = xAfterLastLineBreak;
				}
				else
				{
					// Go with the best line break (the one with the least cost)
					lineBreakIndex = bestLineBreakIndex;
					xAfterLineBreak = xAfterBestLineBreak;
				}
				
				// Build a list of child boxes for the line
				int lineLength = lineBreakIndex - lineStartIndex;
				LBox lineChildren[] = new LBox[lineLength];
				System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
				BoxPackingParams linePackingParams[] =  null;
				if ( packingParams != null )
				{
					linePackingParams = new BoxPackingParams[lineLength];
					System.arraycopy( packingParams, lineStartIndex, linePackingParams, 0, lineLength );
				}
				lines.add( new Line( lineChildren, bFirstLine  ?  0.0  :  indentation, hSpacing, linePackingParams, box.allocationX ) );
				
				// Next line
				lineStartIndex = lineBreakIndex + 1;
				
				lineWidth -= xAfterLineBreak;
				lineAdvance -= xAfterLineBreak;
				lineX -= xAfterLineBreak;
				
				lineWidth += indentation;
				lineAdvance += indentation;
				lineX += indentation;
				
				bestLineBreak = null;
				bestLineBreakIndex = -1;
				xAtBestLineBreak = 0.0;
				xAfterBestLineBreak = 0.0;

				bFirstLine = false;
			}
		}
		
		if ( lineStartIndex < children.length )
		{
			// Create the last line
			int lineLength = children.length - lineStartIndex;
			LBox lineChildren[] = new LBox[lineLength];
			System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
			BoxPackingParams linePackingParams[] =  null;
			if ( packingParams != null )
			{
				linePackingParams = new BoxPackingParams[lineLength];
				System.arraycopy( packingParams, lineStartIndex, linePackingParams, 0, lineLength );
			}
			lines.add( new Line( lineChildren, bFirstLine  ?  0.0  :  indentation, hSpacing, linePackingParams, box.allocationX ) );
		}
		
		
		Line[] lineArray = new Line[lines.size()];
		return lines.toArray( lineArray );
	}





	public static void allocateY(LBox box, Line lines[], double vSpacing, VAlignment vAlignment)
	{
		LBox lineBoxes[] = new LBox[lines.length];
		
		int i = 0;
		for (Line line: lines)
		{
			lineBoxes[i++] = line.lineBox;
		}
		
		VerticalLayout.allocateY( box, lineBoxes, vSpacing, null );
		
		for (Line line: lines)
		{
			line.allocateY( vAlignment );
		}
	}

}
