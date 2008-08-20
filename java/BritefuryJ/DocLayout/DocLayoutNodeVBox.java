package BritefuryJ.DocLayout;



public class DocLayoutNodeVBox extends DocLayoutNodeContainerSequence
{
	public enum Alignment { LEFT, CENTRE, RIGHT, EXPAND };
	
	
	private double spacing, padding;
	private Alignment alignment;
	
	
	
	public DocLayoutNodeVBox()
	{
		this( Alignment.LEFT, 0.0, 0.0 );
	}
	
	public DocLayoutNodeVBox(Alignment alignment, double spacing, double padding)
	{
		super();
		this.alignment = alignment;
		this.spacing = spacing;
		this.padding = padding;
	}
	


	private HMetrics combineHMetrics(HMetrics[] childHMetrics)
	{
		if ( childHMetrics.length == 0 )
		{
			return new HMetrics();
		}
		else
		{
			HMetrics hm = new HMetrics();
			double advance = 0.0;
			for (int i = 0; i < childHMetrics.length; i++)
			{
				HMetrics chm = childHMetrics[i];
				double chAdvance = chm.width + chm.hspacing;
				hm.width = Math.max( hm.width, chm.width );
				advance = Math.max( advance, chAdvance );
			}
			
			hm.hspacing = advance - hm.width;
			
			return hm;
		}
	}
	
	
	private VMetrics combineVMetrics(VMetrics[] childVMetrics)
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
				
				if ( i != childVMetrics.length - 1)
				{
					chm = chm.minSpacing( spacing );
				}
				
				height = y + chm.height  +  padding * 2.0;
				y = height + chm.vspacing;
			}
			
			return new VMetrics( height, y - height );
		}
	}
	

	
	protected HMetrics computeMinimumHMetrics()
	{
		return combineHMetrics( getChildrenRefreshedMinimumHMetrics() );
	}

	protected HMetrics computePreferredHMetrics()
	{
		return combineHMetrics( getChildrenRefreshedPreferredHMetrics() );
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return combineVMetrics( getChildrenRefreshedMinimumVMetrics() );
	}

	protected VMetrics computePreferredVMetrics()
	{
		return combineVMetrics( getChildrenRefreshedPreferredVMetrics() );
	}

	
	
	protected void allocateContentsX(double allocation)
	{
		super.allocateContentsX( allocation );
		
		for (DocLayoutNode child: children)
		{
			double childWidth = Math.min( child.prefH.width, allocation );
			if ( alignment == Alignment.LEFT )
			{
				child.allocateX( 0.0, childWidth );
			}
			else if ( alignment == Alignment.CENTRE )
			{
				child.allocateX( ( allocation - childWidth ) * 0.5, childWidth );
			}
			else if ( alignment == Alignment.RIGHT )
			{
				child.allocateX( allocation - childWidth, childWidth );
			}
			else if ( alignment == Alignment.EXPAND )
			{
				child.allocateX( 0.0, allocation );
			}
		}
	}

	protected void allocateContentsY(double allocation)
	{
		super.allocateContentsY( allocation );
		
		Metrics[] allocated = VMetrics.allocateSpacePacked( getChildrenMinimumVMetrics(), getChildrenPreferredVMetrics(), allocation );
		
		double height = 0.0;
		double y = 0.0;
		for (int i = 0; i < allocated.length; i++)
		{
			VMetrics chm = (VMetrics)allocated[i];
			
			if ( i != allocated.length - 1)
			{
				chm = chm.minSpacing( spacing );
			}

			double childY = y + padding;
			
			children.get( i ).allocateY( childY, chm.height );

			height = y + chm.height + padding * 2.0;
			y = height + chm.vspacing;
		}
	}
}
