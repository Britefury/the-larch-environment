//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.Metrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.Math.Point2;



public class DPParagraph extends DPContainerSequence
{
	public enum Alignment { TOP, CENTRE, BOTTOM, EXPAND, BASELINES };

	
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}


	protected static class ParagraphParentPacking extends ParentPacking
	{
		public double padding;
		
		public ParagraphParentPacking(double padding)
		{
			this.padding = padding;
		}
	}

	
	private static class Line
	{
		public DPWidget children[];
		public VMetrics minV, prefV;
		public double posY, sizeY;
		
		
		public Line(DPWidget children[])
		{
			this.children = children;
		}

	
		
		private void allocateX(DPParagraph paragraph, double lineX, double allocation)
		{
			double spacing = paragraph.getSpacing();

			Metrics[] allocated = HMetrics.allocateSpacePacked( getChildrenMinimumHMetrics( Arrays.asList( children ) ), getChildrenPreferredHMetrics( Arrays.asList( children ) ), null, allocation );
			
			double width = 0.0;
			double x = lineX;
			for (int i = 0; i < allocated.length; i++)
			{
				HMetrics chm = (HMetrics)allocated[i];
				
				if ( i != allocated.length - 1)
				{
					chm = chm.minSpacing( spacing );
				}
				
				DPWidget child = children[i];
				ParagraphParentPacking packing = (ParagraphParentPacking)child.getParentPacking();

				double childX = x + packing.padding;
				
				paragraph.allocateChildX( child, childX, chm.width );

				width = x + chm.width + packing.padding * 2.0;
				x = width + chm.hspacing;
			}
		}

		private void allocateY(DPParagraph paragraph, double lineY, double lineAllocation)
		{
			Alignment alignment = paragraph.getAlignment();
			
			posY = lineY;
			sizeY = lineAllocation;
			
			if ( alignment == Alignment.BASELINES )
			{
				VMetricsTypeset vmt = (VMetricsTypeset)prefV;
				
				double delta = lineAllocation - vmt.height;
				double y = lineY + vmt.ascent + delta * 0.5;
				
				for (DPWidget child: children)
				{
					double chAscent;
					VMetrics chm = child.prefV;
					if ( chm.isTypeset() )
					{
						VMetricsTypeset tchm = (VMetricsTypeset)chm;
						chAscent = tchm.ascent;
					}
					else
					{
						chAscent = chm.height * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
					}

					double childY = Math.max( y - chAscent, 0.0 );
					double childHeight = Math.min( chm.height, lineAllocation );
					paragraph.allocateChildY( child, childY, childHeight );
				}
			}
			else
			{
				for (DPWidget child: children)
				{
					double childHeight = Math.min( child.prefV.height, lineAllocation );
					if ( alignment == Alignment.TOP )
					{
						paragraph.allocateChildY( child, 0.0, childHeight );
					}
					else if ( alignment == Alignment.CENTRE )
					{
						paragraph.allocateChildY( child, ( lineAllocation - childHeight ) * 0.5, childHeight );
					}
					else if ( alignment == Alignment.BOTTOM )
					{
						paragraph.allocateChildY( child, lineAllocation - childHeight, childHeight );
					}
					else if ( alignment == Alignment.EXPAND )
					{
						paragraph.allocateChildY( child, 0.0, lineAllocation );
					}
				}
			}
		}
		
		

		private DPWidget getChildClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
		{
			if ( children.length == 0 )
			{
				return null;
			}
			else if ( children.length == 1 )
			{
				return children[0];
			}
			else
			{
				DPWidget childI = children[0];
				for (int i = 0; i < children.length - 1; i++)
				{
					DPWidget childJ = children[i+1];
					double iUpperX = childI.getPositionInParentSpace().x + childI.getAllocationInParentSpace().x;
					double jLowerX = childJ.getPositionInParentSpace().x;
					
					double midX = ( iUpperX + jLowerX ) * 0.5;
					
					if ( localPos.x < midX )
					{
						return childI;
					}
					
					childI = childJ;
				}
				
				return children[children.length-1];
			}
		}
	}

	
	
	private Line lines[];

	
	
	public DPParagraph()
	{
		this( ParagraphStyleSheet.defaultStyleSheet );
	}

	public DPParagraph(ParagraphStyleSheet styleSheet)
	{
		super( styleSheet );
		
		lines = new Line[0];
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

	
	
	protected ParagraphParentPacking createParentPackingForChild(DPWidget child)
	{
		return new ParagraphParentPacking( getPadding() );
	}

	
	protected void childListModified()
	{
	}


	


	private HMetrics combineHMetricsHorizontally(List<DPWidget> childList, double initialX, HMetrics[] childHMetrics)
	{
		if ( childHMetrics.length == 0 )
		{
			return new HMetrics();
		}
		else
		{
			double spacing = getSpacing();
			
			if ( childHMetrics.length == 1  &&  spacing < childHMetrics[0].hspacing )
			{
				return childHMetrics[0];
			}
			else
			{
				// Accumulate the width required for all the children
				double width = 0.0;
				double x = initialX;
				for (int i = 0; i < childHMetrics.length; i++)
				{
					DPWidget child = childList.get( i );
					ParagraphParentPacking packing = (ParagraphParentPacking)child.getParentPacking();
					HMetrics chm = childHMetrics[i];
					
					if ( i != childHMetrics.length - 1)
					{
						chm = chm.minSpacing( spacing );
					}
					
					width = x + chm.width  +  packing.padding * 2.0;
					x = width + chm.hspacing;
				}
				
				return new HMetrics( width, x - width );
			}
		}
	}
	

	private VMetrics combineVMetricsHorizontally(VMetrics[] childVMetrics)
	{
		if ( childVMetrics.length == 0 )
		{
			return new VMetrics();
		}
		else if ( childVMetrics.length == 1 )
		{
			return childVMetrics[0];
		}
		else
		{
			Alignment alignment = getAlignment();
			if ( alignment == Alignment.BASELINES )
			{
				double ascent = 0.0, descent = 0.0;
				double descentAndSpacing = 0.0;
				for (int i = 0; i < childVMetrics.length; i++)
				{
					VMetrics chm = childVMetrics[i];
					double chAscent, chDescent;
					if ( chm.isTypeset() )
					{
						VMetricsTypeset tchm = (VMetricsTypeset)chm;
						chAscent = tchm.ascent;
						chDescent = tchm.descent;
					}
					else
					{
						chAscent = chm.height * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
						chDescent = chm.height * 0.5  +  NON_TYPESET_CHILD_BASELINE_OFFSET;
					}
					ascent = Math.max( ascent, chAscent );
					descent = Math.max( descent, chDescent );
					double chDescentAndSpacing = chDescent + chm.vspacing;
					descentAndSpacing = Math.max( descentAndSpacing, chDescentAndSpacing );
				}
				
				return new VMetricsTypeset( ascent, descent, descentAndSpacing - descent );
			}
			else
			{
				double height = 0.0;
				double advance = 0.0;
				for (int i = 0; i < childVMetrics.length; i++)
				{
					VMetrics chm = childVMetrics[i];
					double chAdvance = chm.height + chm.vspacing;
					height = Math.max( height, chm.height );
					advance = Math.max( advance, chAdvance );
				}
				
				
				return new VMetrics( height, advance - height );
			}
		}
	}
	
	private VMetrics combineVMetricsVertically(VMetrics[] childVMetrics)
	{
		if ( childVMetrics.length == 0 )
		{
			return new VMetrics();
		}
		else if ( childVMetrics.length == 1 )
		{
			return childVMetrics[0];
		}
		else
		{
			// Accumulate the height required for all the children
			double height = 0.0;
			double y = 0.0;
			for (int i = 0; i < childVMetrics.length; i++)
			{
				VMetrics chm = childVMetrics[i];
				
				height = y + chm.height;
				y = height + chm.vspacing;
			}
			
			return new VMetrics( height, y - height );
		}
	}

	
	
	protected HMetrics computeMinimumHMetrics()
	{
		if ( registeredChildren.size() == 0 )
		{
			return new HMetrics();
		}
		else
		{
			double spacing = getSpacing();
			double indentation = getIndentation();

			// To compute the minimum required h-metrics, assume all line breaks are used.
			
			// Overall width and advance
			double width = 0.0;
			double advance = 0.0;
			
			// Width and advance for a line
			double lineWidth = 0.0;
			double lineX = 0.0;
			
			for (int i = 0; i < registeredChildren.size(); i++)
			{
				DPWidget child = registeredChildren.get( i );
				ParagraphParentPacking packing = (ParagraphParentPacking)child.getParentPacking();
				if ( child.getLineBreakInterface() != null )
				{
					width = Math.max( width, lineWidth );
					advance = Math.max( advance, lineX );
					
					// new line; start X and indentation
					lineWidth = 0.0;
					lineX = indentation;
				}
				else
				{
					HMetrics chm = child.refreshMinimumHMetrics();
					
					// Take spacing into account
					if ( i != registeredChildren.size() - 1)
					{
						// Spacing not appended to last child
						
						if ( registeredChildren.get( i+1 ).getLineBreakInterface() != null )
						{
							// Spacing not applied before a line break
							chm = chm.minSpacing( spacing );
						}
					}
					
					lineWidth = lineX + chm.width  +  packing.padding * 2.0;
					lineX = lineWidth + chm.hspacing;
				}
			}
			
			return new HMetrics( width, advance - width );
		}
	}

	protected HMetrics computePreferredHMetrics()
	{
		return combineHMetricsHorizontally( registeredChildren, 0.0, getChildrenRefreshedPreferredHMetrics() );
	}
	
	
	
	
	protected VMetrics computeMinimumVMetrics()
	{
		VMetrics[] lineMetrics = new VMetrics[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			Line line = lines[i];
			line.minV = combineVMetricsHorizontally( getChildrenRefreshedMinimumVMetrics( Arrays.asList( line.children ) ) );
			lineMetrics[i] = line.minV;
		}
		return combineVMetricsVertically( lineMetrics );
	}

	protected VMetrics computePreferredVMetrics()
	{
		VMetrics[] lineMetrics = new VMetrics[lines.length];
		for (int i = 0; i < lines.length; i++)
		{
			Line line = lines[i];
			line.prefV = combineVMetricsHorizontally( getChildrenRefreshedPreferredVMetrics( Arrays.asList( line.children ) ) );
			lineMetrics[i] = line.prefV;
		}
		return combineVMetricsVertically( lineMetrics );
	}

	
	
	private void splitIntoLines(double allocation)
	{
		double spacing = getSpacing();
		double indentation = getIndentation();

		// Width and advance for a line
		int lineStartIndex = 0;
		double lineWidth = 0.0;
		double lineX = 0.0;
		DPWidget bestLineBreakWidget = null;
		LineBreakInterface bestLineBreakInterface = null;
		int bestLineBreakIndex = -1;
		
		ArrayList<Line> lineList = new ArrayList<Line>();
		
		for (int i = 0; i < registeredChildren.size(); i++)
		{
			// Get the child
			DPWidget child = registeredChildren.get( i );
			ParagraphParentPacking packing = (ParagraphParentPacking)child.getParentPacking();
			LineBreakInterface lineBreak = child.getLineBreakInterface();
			if ( lineBreak != null )
			{
				// Keep track of the best line break candidate
				if ( bestLineBreakWidget == null  ||  bestLineBreakInterface.getLineBreakPriority()  <=  lineBreak.getLineBreakPriority() )
				{
					bestLineBreakWidget = child;
					bestLineBreakInterface = lineBreak;
					bestLineBreakIndex = i;
				}
			}
			
			
			// Accumulate width; use preferred size
			HMetrics chm = child.prefH;
			
			// Take spacing into account
			if ( i != registeredChildren.size() - 1)
			{
				// Spacing not applied before a line break
				chm = chm.minSpacing( spacing );
			}
			
			lineWidth = lineX + chm.width  +  packing.padding * 2.0;
			lineX = lineWidth + chm.hspacing;
			
			
			// A line break is required if the @lineWidth has gone over @allocation
			if ( lineWidth > allocation  &&  bestLineBreakWidget != null )
			{
				if ( bestLineBreakIndex > lineStartIndex )
				{
					// Build a new line
					DPWidget lineChildren[] = new DPWidget[bestLineBreakIndex + 1 - lineStartIndex];
					lineChildren = registeredChildren.subList( lineStartIndex, bestLineBreakIndex +1 ).toArray( lineChildren );
					lineList.add( new Line( lineChildren ) );
				}
				
				// We want the for-loop to return to the break position
				i = bestLineBreakIndex;		// @i will be @lineBestBreakIndex at the beginning of the next loop

				// Start the next line
				lineStartIndex = bestLineBreakIndex + 1;
				lineWidth = 0.0;
				lineX = indentation;
				bestLineBreakWidget = null;
				bestLineBreakIndex = -1;
			}
		}
	
		if ( registeredChildren.size() > lineStartIndex )
		{
			// Build a new line
			DPWidget lineChildren[] = new DPWidget[registeredChildren.size() - lineStartIndex];
			lineChildren = registeredChildren.subList( lineStartIndex, registeredChildren.size() ).toArray( lineChildren );
			lineList.add( new Line( lineChildren ) );
		}
		
		lines = new Line[lineList.size()];
		lines = lineList.toArray( lines );
	}

	
	
	
	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );

		double indentation = getIndentation();
		
		
		// Stage 1:
		// Split the list of child nodes into lines
		splitIntoLines( allocation );
		
		
		// Stage 2:
		// Allocate each line
		boolean bFirst = true;
		for (Line line: lines)
		{
			line.allocateX( this, bFirst ? 0.0 : indentation, allocation );
			bFirst = false;
		}
	}
	
	
	

	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		// Allocate the lines, vertically
		VMetrics[] linesMinV = new VMetrics[lines.length];
		VMetrics[] linesPrefV = new VMetrics[lines.length];
		
		for (int i = 0; i < lines.length; i++)
		{
			Line line = lines[i];
			linesMinV[i] = line.minV;
			linesPrefV[i] = line.prefV;
		}
		
		Metrics[] allocated = VMetrics.allocateSpacePacked( linesMinV, linesPrefV, null, allocation );
		
		double height = 0.0;
		double y = 0.0;
		for (int i = 0; i < allocated.length; i++)
		{
			VMetrics chm = (VMetrics)allocated[i];
			
			lines[i].allocateY( this, y, chm.height );

			height = y + chm.height;
			y = height + chm.vspacing;
		}
	}
	
	
	
	private Line getLineClosestToLocalPoint(Point2 localPos)
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
			Line lineI = lines[0];
			for (int i = 0; i < lines.length - 1; i++)
			{
				Line lineJ = lines[i+1];
				double iUpperY = lineI.posY + lineI.sizeY;
				double jLowerY = lineJ.posY;
				
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

	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		Line line = getLineClosestToLocalPoint( localPos );
		
		if ( line != null )
		{
			DPWidget child = line.getChildClosestToLocalPoint( localPos, filter );
			
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

	public Alignment getAlignment()
	{
		return ((ParagraphStyleSheet)styleSheet).getAlignment();
	}

	public double getSpacing()
	{
		return ((ParagraphStyleSheet)styleSheet).getSpacing();
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
