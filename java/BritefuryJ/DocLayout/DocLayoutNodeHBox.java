package BritefuryJ.DocLayout;

public class DocLayoutNodeHBox extends DocLayoutNodeContainerSequence
{
	public enum Alignment { TOP, CENTRE, BOTTOM, EXPAND, BASELINES };
	
	
	
	private double spacing, padding;
	private Alignment alignment;
	
	
	
	public DocLayoutNodeHBox()
	{
		this( Alignment.BASELINES, 0.0, 0.0 );
	}
	
	public DocLayoutNodeHBox(Alignment alignment, double spacing, double padding)
	{
		super();
		this.alignment = alignment;
		this.spacing = spacing;
		this.padding = padding;
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
				
				if ( i != childHMetrics.length - 1 )
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
	

	
	

	protected HMetrics computeMinimumHMetrics()
	{
		return combineHMetricsHorizontally( getChildrenRefreshedMinimumHMetrics() );
	}

	protected HMetrics computePreferredHMetrics()
	{
		return combineHMetricsHorizontally( getChildrenRefreshedPreferredHMetrics() );
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return combineVMetricsHorizontally( getChildrenRefreshedMinimumVMetrics() );
	}

	protected VMetrics computePreferredVMetrics()
	{
		return combineVMetricsHorizontally( getChildrenRefreshedPreferredVMetrics() );
	}

	
	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );
		
		Metrics[] allocated = HMetrics.allocateSpacePacked( getChildrenMinimumHMetrics( children ), getChildrenPreferredHMetrics( children ), allocation );
		
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
			
			children.get( i ).allocateX( childX, chm.width );

			width = x + chm.width + padding * 2.0;
			x = width + chm.hspacing;
		}
	}

	
	
	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		if ( alignment == Alignment.BASELINES )
		{
			if ( prefV.isTypeset() )
			{
				VMetricsTypeset vmt = (VMetricsTypeset)prefV;
				
				double delta = allocation - vmt.height;
				double ascent = vmt.ascent + delta * 0.5;
				
				for (DocLayoutNode child: children)
				{
					if ( child.prefV.isTypeset())
					{
						// Typeset child; align baselines
						VMetricsTypeset chmt = (VMetricsTypeset)child.prefV;
						double childY = Math.max( ascent - chmt.ascent, 0.0 );
						double childHeight = Math.min( chmt.height, allocation );
						child.allocateY( childY, childHeight );
					}
					else
					{
						// Non-typeset child; centre alignment
						double childHeight = Math.min( child.prefV.height, allocation );
						child.allocateY( ( allocation - childHeight ) * 0.5, childHeight );
					}
				}
			}
			else
			{
				// No typeset children; default to centre alignment
				for (DocLayoutNode child: children)
				{
					double childHeight = Math.min( child.prefV.height, allocation );
					child.allocateY( ( allocation - childHeight ) * 0.5, childHeight );
				}
			}
		}
		else
		{
			for (DocLayoutNode child: children)
			{
				double childHeight = Math.min( child.prefV.height, allocation );
				if ( alignment == Alignment.TOP )
				{
					child.allocateY( 0.0, childHeight );
				}
				else if ( alignment == Alignment.CENTRE )
				{
					child.allocateY( ( allocation - childHeight ) * 0.5, childHeight );
				}
				else if ( alignment == Alignment.BOTTOM )
				{
					child.allocateY( allocation - childHeight, childHeight );
				}
				else if ( alignment == Alignment.EXPAND )
				{
					child.allocateY( 0.0, allocation );
				}
			}
		}
	}
}
