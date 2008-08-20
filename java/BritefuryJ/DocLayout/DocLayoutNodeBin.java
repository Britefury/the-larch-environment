package BritefuryJ.DocLayout;

public class DocLayoutNodeBin extends DocLayoutNodeContainer
{
	private DocLayoutNode child;
	
	
	public DocLayoutNodeBin()
	{
		super();
	}
	
	
	public void setChild(DocLayoutNode child)
	{
		if ( this.child != null )
		{
			this.child.setParent( null );
		}
		
		this.child = child;
		
		if ( this.child != null )
		{
			this.child.setParent( this );
		}
		
		requestRelayout();
	}
	
	
	
	protected HMetrics computeMinimumHMetrics()
	{
		if ( child != null )
		{
			return child.refreshMinimumHMetrics();
		}
		else
		{
			return new HMetrics();
		}
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		if ( child != null )
		{
			return child.computePreferredHMetrics();
		}
		else
		{
			return new HMetrics();
		}
	}
	
	protected VMetrics computeMinimumVMetrics()
	{
		if ( child != null )
		{
			return child.computeMinimumVMetrics();
		}
		else
		{
			return new VMetrics();
		}
	}

	protected VMetrics computePreferredVMetrics()
	{
		if ( child != null )
		{
			return child.computePreferredVMetrics();
		}
		else
		{
			return new VMetrics();
		}
	}
	
	
	
	protected void allocateContentsX(double allocation)
	{
		if ( child != null )
		{
			child.allocateX( 0.0, allocation );
		}
	}
	
	protected void allocateContentsY(double allocation)
	{
		if ( child != null )
		{
			child.allocateY( 0.0, allocation );
		}
	}
}
