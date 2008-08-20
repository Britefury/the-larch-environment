package BritefuryJ.DocLayout;

import java.util.List;
import java.util.Vector;


public class DocLayoutNodeParagraph extends DocLayoutNodeContainerSequence
{
	public enum Alignment { TOP, CENTRE, BOTTOM, EXPAND, BASELINES };
	
	
	
	private static class Line
	{
		public List<DocLayoutNode> children;
		public VMetrics minV, prefV;
		
		
		public Line(List<DocLayoutNode> children)
		{
			this.children = children;
		}
	}
	
	
	private double spacing, padding;
	private Alignment alignment;
	private Vector<Line> lines;
	
	
	
	public DocLayoutNodeParagraph()
	{
		this( Alignment.BASELINES, 0.0, 0.0 );
	}
	
	public DocLayoutNodeParagraph(Alignment alignment, double spacing, double padding)
	{
		super();
		this.alignment = alignment;
		this.spacing = spacing;
		this.padding = padding;
		lines = new Vector<Line>();
	}
	


	private HMetrics combineHMetricsHorizontally(HMetrics[] childHMetrics)
	{
		if ( childHMetrics.length == 0 )
		{
			return new HMetrics();
		}
		else
		{
			// Accumulate the width required for all the children
			double width = 0.0;
			double x = 0.0;
			for (int i = 0; i < childHMetrics.length; i++)
			{
				HMetrics chm = childHMetrics[i];
				
				if ( i != childHMetrics.length - 1)
				{
					chm = chm.minSpacing( spacing );
				}
				
				width = x + chm.width  +  padding * 2.0;
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
			boolean bTypeset = false;
			double height = 0.0, ascent = 0.0, descent = 0.0;
			double advance = 0.0;
			for (int i = 0; i < childVMetrics.length; i++)
			{
				VMetrics chm = childVMetrics[i];
				double chAdvance = chm.height + chm.vspacing;
				height = Math.max( height, chm.height );
				if ( chm.isTypeset() )
				{
					VMetricsTypeset tchm = (VMetricsTypeset)chm;
					ascent = Math.max( ascent, tchm.ascent );
					descent = Math.max( descent, tchm.descent );
					bTypeset = true;
				}
				advance = Math.max( advance, chAdvance );
			}
			
			
			if ( bTypeset )
			{
				double typesetHeight = ascent + descent;
				// (typesetHeight can never be > height)
				if ( height > typesetHeight )
				{
					double extraHeight = height - typesetHeight;
					ascent += extraHeight * 0.5;
					descent += extraHeight * 0.5;
				}
				return new VMetricsTypeset( ascent, descent, advance - height );
			}
			else
			{
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
		if ( children.size() == 0 )
		{
			return new HMetrics();
		}
		else
		{
			// To compute the minimum required h-metrics, assume all line breaks are used.
			
			// Overall width and advance
			double width = 0.0;
			double advance = 0.0;
			
			// Width and advance for a line
			double lineWidth = 0.0;
			double lineX = 0.0;
			
			for (int i = 0; i < children.size(); i++)
			{
				DocLayoutNode child = children.get( i );
				if ( child.isLineBreak() )
				{
					width = Math.max( width, lineWidth );
					advance = Math.max( advance, lineX );
					
					// new line
					lineWidth = 0.0;
					lineX = 0.0;
				}
				else
				{
					HMetrics chm = child.refreshMinimumHMetrics();
					
					// Take spacing into account
					if ( i != children.size() - 1)
					{
						// Spacing not appended to last child
						
						if ( !children.get( i+1 ).isLineBreak() )
						{
							// Spacing not applied before a line break
							chm = chm.minSpacing( spacing );
						}
					}
					
					lineWidth = lineX + chm.width  +  padding * 2.0;
					lineX = lineWidth + chm.hspacing;
				}
			}
			
			return new HMetrics( width, advance - width );
		}
	}

	protected HMetrics computePreferredHMetrics()
	{
		return combineHMetricsHorizontally( getChildrenRefreshedPreferredHMetrics() );
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
		// Width and advance for a line
		int lineStartIndex = 0;
		double lineWidth = 0.0;
		double lineX = 0.0;
		DocLayoutNode lineBestBreak = null;
		int lineBestBreakIndex = -1;
		
		for (int i = 0; i < children.size(); i++)
		{
			// Get the child
			DocLayoutNode child = children.get( i );
			if ( child.isLineBreak() )
			{
				// Note the line break down
				if ( lineBestBreak == null  ||  lineBestBreak.getLineBreakPriority()  <=  child.getLineBreakPriority() )
				{
					lineBestBreak = child;
					lineBestBreakIndex = i;
				}
			}
			
			
			// Accumulate width; use preferred size
			HMetrics chm = child.prefH;
			
			// Take spacing into account
			if ( i != children.size() - 1)
			{
				// Spacing not applied before a line break
				chm = chm.minSpacing( spacing );
			}
			
			lineWidth = lineX + chm.width  +  padding * 2.0;
			lineX = lineWidth + chm.hspacing;
			
			
			// A line break is required if the @lineWidth has gone over @allocation
			if ( lineWidth > allocation  &&  lineBestBreak != null )
			{
				if ( lineBestBreakIndex > lineStartIndex )
				{
					// Build a new line
					lines.add( new Line( children.subList( lineStartIndex, lineBestBreakIndex ) ) );
				}
				
				// We want the for-loop to return to the break position
				i = lineBestBreakIndex;		// @i will be @lineBestBreakIndex at the beginning of the next loop

				// Start the next line
				lineStartIndex = lineBestBreakIndex + 1;
				lineWidth = 0.0;
				lineX = 0.0;
				lineBestBreak = null;
				lineBestBreakIndex = -1;
			}
		}
	
		if ( children.size() > lineStartIndex )
		{
			// Build a new line
			lines.add( new Line( children.subList( lineStartIndex, children.size() ) ) );
		}
	}
	
	
	
	private void allocateLineX(Line line, double allocation)
	{
		Metrics[] allocated = HMetrics.allocateSpacePacked( getChildrenMinimumHMetrics( line.children ), getChildrenPreferredHMetrics( line.children ), allocation );
		
		double width = 0.0;
		double x = 0.0;
		for (int i = 0; i < allocated.length; i++)
		{
			HMetrics chm = (HMetrics)allocated[i];
			
			if ( i != allocated.length - 1)
			{
				chm = chm.minSpacing( spacing );
			}

			double childX = x + padding;
			
			line.children.get( i ).allocateX( childX, chm.width );

			width = x + chm.width + padding * 2.0;
			x = width + chm.hspacing;
		}
	}

	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );
		
		
		// Stage 1:
		// Split the list of child nodes into lines
		lines.clear();
		splitIntoLines( allocation );
		
		
		// Stage 2:
		// Allocate each line
		for (Line line: lines)
		{
			allocateLineX( line, allocation );
		}
	}

	
	
	private void allocateLineY(Line line, double lineY, double lineAllocation)
	{
		if ( alignment == Alignment.BASELINES )
		{
			if ( line.prefV.isTypeset() )
			{
				VMetricsTypeset vmt = (VMetricsTypeset)line.prefV;
				
				double delta = lineAllocation - vmt.height;
				double ascent = vmt.ascent + delta * 0.5;
				
				for (DocLayoutNode child: line.children)
				{
					if ( child.prefV.isTypeset())
					{
						// Typeset child; align baselines
						VMetricsTypeset chmt = (VMetricsTypeset)child.prefV;
						double childY = lineY + Math.max( ascent - chmt.ascent, 0.0 );
						double childHeight = Math.min( chmt.height, lineAllocation );
						child.allocateY( childY, childHeight );
					}
					else
					{
						// Non-typeset child; centre alignment
						double childHeight = Math.min( child.prefV.height, lineAllocation );
						child.allocateY( lineY + ( lineAllocation - childHeight ) * 0.5, childHeight );
					}
				}
			}
			else
			{
				// No typeset children; default to centre alignment
				for (DocLayoutNode child: line.children)
				{
					double childHeight = Math.min( child.prefV.height, lineAllocation );
					child.allocateY( lineY + ( lineAllocation - childHeight ) * 0.5, childHeight );
				}
			}
		}
		else
		{
			for (DocLayoutNode child: line.children)
			{
				double childHeight = Math.min( child.prefV.height, lineAllocation );
				if ( alignment == Alignment.TOP )
				{
					child.allocateY( lineY, childHeight );
				}
				else if ( alignment == Alignment.CENTRE )
				{
					child.allocateY( lineY + ( lineAllocation - childHeight ) * 0.5, childHeight );
				}
				else if ( alignment == Alignment.BOTTOM )
				{
					child.allocateY( lineY + lineAllocation - childHeight, childHeight );
				}
				else if ( alignment == Alignment.EXPAND )
				{
					child.allocateY( lineY, lineAllocation );
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
		
		Metrics[] allocated = VMetrics.allocateSpacePacked( linesMinV, linesPrefV, allocation );
		
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
}
