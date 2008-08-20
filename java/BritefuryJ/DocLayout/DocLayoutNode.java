package BritefuryJ.DocLayout;

import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class DocLayoutNode
{
	protected Point2 positionInParent;
	protected Vector2 size;
	protected HMetrics minH, prefH;
	protected VMetrics minV, prefV;
	protected DocLayoutNodeContainer parent;
	
	
	public DocLayoutNode()
	{
		positionInParent = new Point2();
		size = new Vector2();
		minH = new HMetrics();
		prefH = new HMetrics();
		minV = new VMetrics();
		prefV = new VMetrics();
	}
	
	
	public Point2 getPositionInParent()
	{
		return positionInParent;
	}
	
	public Vector2 getSize()
	{
		return size;
	}
	
	
	public DocLayoutNodeContainer getParent()
	{
		return parent;
	}
	
	
	
	public HMetrics refreshMinimumHMetrics()
	{
		minH = computeMinimumHMetrics();
		return minH;
	}
	
	public HMetrics refreshPreferredHMetrics()
	{
		prefH = computePreferredHMetrics();
		return prefH;
	}
	

	public VMetrics refreshMinimumVMetrics()
	{
		minV = computeMinimumVMetrics();
		return minV;
	}
	
	public VMetrics refreshPreferredVMetrics()
	{
		prefV = computePreferredVMetrics();
		return prefV;
	}
	

	abstract protected HMetrics computeMinimumHMetrics();
	abstract protected HMetrics computePreferredHMetrics();
	abstract protected VMetrics computeMinimumVMetrics();
	abstract protected VMetrics computePreferredVMetrics();
	
	
	
	protected void allocateContentsX(double allocation)
	{
	}
	
	protected void allocateContentsY(double allocation)
	{
	}
	
	
	public void allocateX(double x, double width)
	{
		positionInParent.x = x;
		size.x = width;
		allocateContentsX( width );
	}
	
	public void allocateY(double y, double height)
	{
		positionInParent.y = y;
		size.y = height;
		allocateContentsY( height );
	}
	
	
	
	
	protected void requestRelayout()
	{
		if ( parent != null )
		{
			parent.childRequestRelayout( this );
		}
	}
	
	
	protected void setParent(DocLayoutNodeContainer parent)
	{
		this.parent = parent;
	}





	public int getLineBreakPriority()
	{
		return -1;
	}
	
	public boolean isLineBreak()
	{
		return getLineBreakPriority() >= 0;
	}
}
