package BritefuryJ.DocPresent;

import java.lang.Math;
import java.util.List;
import java.util.Vector;

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


	protected static class ParagraphChildEntry extends DPContainerSequence.ChildEntry
	{
		public double padding;
		
		public ParagraphChildEntry(DPWidget child, double padding)
		{
			super( child );
			
			this.padding = padding;
		}
	}

	
	private static class Line
	{
		public List<ChildEntry> children;
		public VMetrics minV, prefV;
		
		
		public Line(List<ChildEntry> children)
		{
			this.children = children;
		}
	}

	
	
	private Vector<Line> lines;

	
	
	public DPParagraph()
	{
		this( ParagraphStyleSheet.defaultStyleSheet );
	}

	public DPParagraph(ParagraphStyleSheet styleSheet)
	{
		super( styleSheet );
		
		lines = new Vector<Line>();
	}


	
	public void append(DPWidget child)
	{
		appendChildEntry( createChildEntryForChild( child ) );
	}

	public void extend(DPWidget[] children)
	{
		ChildEntry[] entries = new ChildEntry[children.length];
		
		for (int i = 0; i < children.length; i++)
		{
			entries[i] = createChildEntryForChild( children[i] );
		}
		
		extendChildEntries( entries );
	}

	public void extend(List<DPWidget> children)
	{
		ChildEntry[] entries = new ChildEntry[children.size()];
		
		for (int i = 0; i < children.size(); i++)
		{
			entries[i] = createChildEntryForChild( children.get( i ) );
		}
		
		extendChildEntries( entries );
	}
	
	public void insert(int index, DPWidget child)
	{
		insertChildEntry( index, createChildEntryForChild( child ) );
	}

	public void remove(DPWidget child)
	{
		removeChildEntry( childToEntry.get( child ) );
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
		
		double[] midPoints = new double[childEntries.size()];
		
		for (int i = 0; i < midPoints.length; i++)
		{
			ChildEntry entry = childEntries.get( i );
			midPoints[i] = entry.pos.x  +  entry.size.x * 0.5;
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

	
	
	protected ParagraphChildEntry createChildEntryForChild(DPWidget child)
	{
		return new ParagraphChildEntry( child, getPadding() );
	}

	
	protected void childListModified()
	{
	}


	


	private HMetrics combineHMetricsHorizontally(List<ChildEntry> entries, double initialX, HMetrics[] childHMetrics)
	{
		if ( childHMetrics.length == 0 )
		{
			return new HMetrics();
		}
		else
		{
			double spacing = getSpacing();
			// Accumulate the width required for all the children
			double width = 0.0;
			double x = initialX;
			for (int i = 0; i < childHMetrics.length; i++)
			{
				ParagraphChildEntry childEntry = (ParagraphChildEntry)entries.get( i );
				HMetrics chm = childHMetrics[i];
				
				if ( i != childHMetrics.length - 1)
				{
					chm = chm.minSpacing( spacing );
				}
				
				width = x + chm.width  +  childEntry.padding * 2.0;
				x = width + chm.hspacing;
			}
			
			return new HMetrics( width, x - width );
		}
	}
	

	private VMetrics combineVMetricsHorizontally(VMetrics[] childVMetrics)
	{
		if ( childVMetrics.length == 0 )
		{
			return new VMetrics();
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
		if ( childEntries.size() == 0 )
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
			
			for (int i = 0; i < childEntries.size(); i++)
			{
				ParagraphChildEntry childEntry = (ParagraphChildEntry)childEntries.get( i );
				DPWidget child = childEntry.child;
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
					if ( i != childEntries.size() - 1)
					{
						// Spacing not appended to last child
						
						if ( childEntries.get( i+1 ).child.getLineBreakInterface() != null )
						{
							// Spacing not applied before a line break
							chm = chm.minSpacing( spacing );
						}
					}
					
					lineWidth = lineX + chm.width  +  childEntry.padding * 2.0;
					lineX = lineWidth + chm.hspacing;
				}
			}
			
			return new HMetrics( width, advance - width );
		}
	}

	protected HMetrics computePreferredHMetrics()
	{
		return combineHMetricsHorizontally( childEntries, 0.0, getChildrenRefreshedPreferredHMetrics() );
	}
	
	
	
	
	protected VMetrics computeMinimumVMetrics()
	{
		VMetrics[] lineMetrics = new VMetrics[lines.size()];
		for (int i = 0; i < lines.size(); i++)
		{
			Line line = lines.get( i );
			line.minV = combineVMetricsHorizontally( getChildrenRefreshedMinimumVMetrics( line.children ) );
			lineMetrics[i] = line.minV;
		}
		return combineVMetricsVertically( lineMetrics );
	}

	protected VMetrics computePreferredVMetrics()
	{
		VMetrics[] lineMetrics = new VMetrics[lines.size()];
		for (int i = 0; i < lines.size(); i++)
		{
			Line line = lines.get( i );
			line.prefV = combineVMetricsHorizontally( getChildrenRefreshedPreferredVMetrics( line.children ) );
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
		
		for (int i = 0; i < childEntries.size(); i++)
		{
			// Get the child
			ParagraphChildEntry childEntry = (ParagraphChildEntry)childEntries.get( i );
			DPWidget child = childEntry.child;
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
			if ( i != childEntries.size() - 1)
			{
				// Spacing not applied before a line break
				chm = chm.minSpacing( spacing );
			}
			
			lineWidth = lineX + chm.width  +  childEntry.padding * 2.0;
			lineX = lineWidth + chm.hspacing;
			
			
			// A line break is required if the @lineWidth has gone over @allocation
			if ( lineWidth > allocation  &&  bestLineBreakWidget != null )
			{
				if ( bestLineBreakIndex > lineStartIndex )
				{
					// Build a new line
					lines.add( new Line( childEntries.subList( lineStartIndex, bestLineBreakIndex ) ) );
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
	
		if ( childEntries.size() > lineStartIndex )
		{
			// Build a new line
			lines.add( new Line( childEntries.subList( lineStartIndex, childEntries.size() ) ) );
		}
	}

	
	
	
	private void allocateLineX(Line line, double lineX, double allocation)
	{
		double spacing = getSpacing();

		Metrics[] allocated = HMetrics.allocateSpacePacked( getChildrenMinimumHMetrics( line.children ), getChildrenPreferredHMetrics( line.children ), null, allocation );
		
		double width = 0.0;
		double x = lineX;
		for (int i = 0; i < allocated.length; i++)
		{
			HMetrics chm = (HMetrics)allocated[i];
			
			if ( i != allocated.length - 1)
			{
				chm = chm.minSpacing( spacing );
			}
			
			ParagraphChildEntry entry = (ParagraphChildEntry)line.children.get( i ); 

			double childX = x + entry.padding;
			
			allocateChildX( entry.child, childX, chm.width );

			width = x + chm.width + entry.padding * 2.0;
			x = width + chm.hspacing;
		}
	}


	
	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );

		double indentation = getIndentation();
		
		
		// Stage 1:
		// Split the list of child nodes into lines
		lines.clear();
		splitIntoLines( allocation );
		
		
		// Stage 2:
		// Allocate each line
		boolean bFirst = true;
		for (Line line: lines)
		{
			allocateLineX( line, bFirst ? 0.0 : indentation, allocation );
			bFirst = false;
		}
	}
	
	
	

	private void allocateLineY(Line line, double lineY, double lineAllocation)
	{
		Alignment alignment = getAlignment();
		if ( alignment == Alignment.BASELINES )
		{
			VMetricsTypeset vmt = (VMetricsTypeset)line.prefV;
			
			double delta = lineAllocation - vmt.height;
			double y = lineY + vmt.ascent + delta * 0.5;
			
			for (ChildEntry entry: line.children)
			{
				ParagraphChildEntry paraEntry = (ParagraphChildEntry)entry;
				DPWidget child = paraEntry.child;
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
				allocateChildY( child, childY, childHeight );
			}
		}
		else
		{
			for (ChildEntry entry: line.children)
			{
				DPWidget child = entry.child;
				double childHeight = Math.min( child.prefV.height, lineAllocation );
				if ( alignment == Alignment.TOP )
				{
					allocateChildY( child, 0.0, childHeight );
				}
				else if ( alignment == Alignment.CENTRE )
				{
					allocateChildY( child, ( lineAllocation - childHeight ) * 0.5, childHeight );
				}
				else if ( alignment == Alignment.BOTTOM )
				{
					allocateChildY( child, lineAllocation - childHeight, childHeight );
				}
				else if ( alignment == Alignment.EXPAND )
				{
					allocateChildY( child, 0.0, lineAllocation );
				}
			}
		}
	}
	
	
	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		// Allocate the lines, vertically
		VMetrics[] linesMinV = new VMetrics[lines.size()];
		VMetrics[] linesPrefV = new VMetrics[lines.size()];
		
		for (int i = 0; i < lines.size(); i++)
		{
			Line line = lines.get( i );
			linesMinV[i] = line.minV;
			linesPrefV[i] = line.prefV;
		}
		
		Metrics[] allocated = VMetrics.allocateSpacePacked( linesMinV, linesPrefV, null, allocation );
		
		double height = 0.0;
		double y = 0.0;
		for (int i = 0; i < allocated.length; i++)
		{
			VMetrics chm = (VMetrics)allocated[i];
			
			allocateLineY( lines.get( i ), y, chm.height );

			height = y + chm.height;
			y = height + chm.vspacing;
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
