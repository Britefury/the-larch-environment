package BritefuryJ.DocPresent;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;


public class DPBin extends DPContainer implements ContentInterface
{
	protected DPWidget child;
	protected double childScale;
	
	
	
	public DPBin()
	{
		this( ContainerStyleSheet.defaultStyleSheet );
	}

	public DPBin(ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		childScale = 1.0;
	}
	
	
	
	public DPWidget getChild()
	{
		return child;
	}
	
	public void setChild(DPWidget child)
	{
		if ( child != this.child )
		{
			if ( child != null )
			{
				child.unparent();
			}
			
			DPWidget prevChild = this.child;
			
			if ( prevChild != null )
			{
				ChildEntry entry = childToEntry.get( prevChild );
				unregisterChildEntry( entry );
				childEntries.remove( entry );
			}
			
			this.child = child;
			
			if ( this.child != null )
			{
				ChildEntry entry = new ChildEntry( this.child );
				childEntries.add( entry );
				registerChildEntry( entry );				
			}
			
			queueResize();
		}
	}
	
	
	public double getChildScale()
	{
		return childScale;
	}
	
	public void setChildScale(double scale)
	{
		childScale = scale;
		queueResize();
	}
	
	
	protected void removeChild(DPWidget child)
	{
		assert child == this.child;
		setChild( null );
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
			return child.refreshPreferredHMetrics();
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
			return child.refreshMinimumVMetrics();
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
			return child.refreshPreferredVMetrics();
		}
		else
		{
			return new VMetrics();
		}
	}
	
	
	
	
	protected void allocateContentsX(double width)
	{
		if ( child != null )
		{
			allocateChildX( child, 0.0, width );
		}
	}

	protected void allocateContentsY(double height)
	{
		if ( child != null )
		{
			allocateChildY( child, 0.0, height );
		}
	}
	
	
	
	
	//
	// Focus navigation methods
	//
	
	protected List<DPWidget> horizontalNavigationList()
	{
		if ( child != null )
		{
			DPWidget[] navList = { child };
			return Arrays.asList( navList );
		}
		else
		{
			return null;
		}
	}
	
	
	
	//
	//
	// CONTENT METHODS
	//
	//

	public String getContent()
	{
		if ( child != null )
		{
			ContentInterface childContent = child.getContentInterface();
			if ( childContent != null )
			{
				return childContent.getContent();
			}
		}
		return "";
	}

	public int getContentLength()
	{
		if ( child != null )
		{
			ContentInterface childContent = child.getContentInterface();
			if ( childContent != null )
			{
				return childContent.getContentLength();
			}
		}
		return 0;
	}
	
	
	public ContentInterface getContentInterface()
	{
		return this;
	}
}
