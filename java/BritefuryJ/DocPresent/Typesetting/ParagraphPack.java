//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;

import java.util.ArrayList;
import java.util.List;

public class ParagraphPack
{
	public static class Line
	{
		public TSBox lineBox;
		public TSBox children[];
		
		
		private Line(TSBox ch[], double indentation, double spacing, double childPadding[], double allocation)
		{
			children = ch;
			
			lineBox = new TSBox();
			HorizontalPack.computeRequisitionX( lineBox, children, spacing, childPadding );
			lineBox.allocationX = allocation - indentation;
			HorizontalPack.allocateX( lineBox, children, spacing, childPadding, null );
			for (TSBox child: children)
			{
				child.positionInParentSpaceX += indentation;
			}
		}
		
		
		public TSBox getLineBox()
		{
			return lineBox;
		}
		
		public TSBox[] getChildBoxes()
		{
			return children;
		}
		
		
		private void computeRequisitionY(VAlignment vAlignment)
		{
			HorizontalPack.computeRequisitionY( lineBox, children, vAlignment );
		}

		private void allocateY(VAlignment vAlignment)
		{
			HorizontalPack.allocateY( lineBox, children, vAlignment );
		}
	}
	
	

	public static void computeRequisitionX(TSBox box, TSBox children[], double indentation, double hSpacing, double childPadding[])
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
			TSBox child = children[i];
			
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
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

	
	public static void computeRequisitionY(TSBox box, List<Line> lines, double vSpacing, VAlignment vAlignment)
	{
		TSBox lineBoxes[] = new TSBox[lines.size()];
		
		int i = 0;
		for (Line line: lines)
		{
			line.computeRequisitionY( vAlignment );
			lineBoxes[i++] = line.lineBox;
		}
		
		VerticalPack.computeRequisitionY( box, lineBoxes, vSpacing, null );
	}




	public static ArrayList<Line> allocateX(TSBox box, TSBox children[], double indentation, double hSpacing, double childPadding[])
	{
		boolean bFirstLine = true;
		
		ArrayList<Line> lines = new ArrayList<Line>();
		int lineStartIndex = 0;
		
		double lineWidth = 0.0;
		double lineAdvance = 0.0;
		double lineX = 0.0;
		
		TSBox bestLineBreak = null;
		int bestLineBreakIndex = -1;
		double xAtBestLineBreak = 0.0, xAfterBestLineBreak = 0.0;
		
		TSBox lastLineBreak = null;
		int lastLineBreakIndex = -1;
		double xAfterLastLineBreak = 0.0;
		
		for (int i = 0; i < children.length; i++)
		{
			TSBox child = children[i];
			
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
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
				if ( ( lineWidth - xAtBestLineBreak )  >  box.allocationX  &&  child != lastLineBreak )
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
				TSBox lineChildren[] = new TSBox[lineLength];
				System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
				double linePadding[] =  null;
				if ( childPadding != null )
				{
					linePadding= new double[lineLength];
					System.arraycopy( childPadding, lineStartIndex, linePadding, 0, lineLength );
				}
				lines.add( new Line( lineChildren, bFirstLine  ?  0.0  :  indentation, hSpacing, linePadding, box.allocationX ) );
				
				// Next line
				lineStartIndex = lineBreakIndex + 1;
				
				lineWidth -= xAfterLineBreak;
				lineAdvance -= xAfterLineBreak;
				lineX -= xAfterLineBreak;
				
				lineWidth += indentation;
				lineAdvance += indentation;
				lineX += indentation;
				
				bFirstLine = false;
			}
		}
		
		if ( lineStartIndex < children.length )
		{
			// Create the last line
			int lineLength = children.length - lineStartIndex;
			TSBox lineChildren[] = new TSBox[lineLength];
			System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
			double linePadding[] =  null;
			if ( childPadding != null )
			{
				linePadding= new double[lineLength];
				System.arraycopy( childPadding, lineStartIndex, linePadding, 0, lineLength );
			}
			lines.add( new Line( lineChildren, bFirstLine  ?  0.0  :  indentation, hSpacing, linePadding, box.allocationX ) );
		}
		
		
		return lines;
	}





	public static void allocateY(TSBox box, List<Line> lines, double vSpacing, VAlignment vAlignment)
	{
		TSBox lineBoxes[] = new TSBox[lines.size()];
		
		int i = 0;
		for (Line line: lines)
		{
			lineBoxes[i++] = line.lineBox;
		}
		
		VerticalPack.allocateY( box, lineBoxes, vSpacing, null, null );
		
		for (Line line: lines)
		{
			line.allocateY( vAlignment );
		}
	}

}
