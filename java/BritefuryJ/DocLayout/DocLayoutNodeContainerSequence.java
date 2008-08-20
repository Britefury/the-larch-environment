package BritefuryJ.DocLayout;

import java.util.List;
import java.util.Vector;

public abstract class DocLayoutNodeContainerSequence extends DocLayoutNodeContainer
{
	protected Vector<DocLayoutNode> children;
	
	
	DocLayoutNodeContainerSequence()
	{
		children = new Vector<DocLayoutNode>();
	}
	
	
	public void setChildren(DocLayoutNode[] children)
	{
		for (DocLayoutNode child: this.children)
		{
			child.setParent( null );
		}
		
		this.children = new Vector<DocLayoutNode>( children.length );
		for (DocLayoutNode child: children)
		{
			this.children.add( child );
		}
		
		for (DocLayoutNode child: this.children)
		{
			child.setParent( this );
		}

		requestRelayout();
	}
	
	
	public void setChildren(List<DocLayoutNode> children)
	{
		for (DocLayoutNode child: this.children)
		{
			child.setParent( null );
		}
		
		this.children = new Vector<DocLayoutNode>( children );
		
		for (DocLayoutNode child: this.children)
		{
			child.setParent( this );
		}

		requestRelayout();
	}
	
	
	
	
	HMetrics[] getChildrenRefreshedMinimumHMetrics(List<DocLayoutNode> nodes)
	{
		HMetrics[] chm = new HMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).refreshMinimumHMetrics();
		}
		return chm;
	}

	HMetrics[] getChildrenRefreshedMinimumHMetrics()
	{
		return getChildrenRefreshedMinimumHMetrics( children );
	}

	
	HMetrics[] getChildrenRefreshedPreferredHMetrics(List<DocLayoutNode> nodes)
	{
		HMetrics[] chm = new HMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).refreshPreferredHMetrics();
		}
		return chm;
	}
	
	HMetrics[] getChildrenRefreshedPreferredHMetrics()
	{
		return getChildrenRefreshedPreferredHMetrics( children );
	}
	
	
	
	VMetrics[] getChildrenRefreshedMinimumVMetrics(List<DocLayoutNode> nodes)
	{
		VMetrics[] chm = new VMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).refreshMinimumVMetrics();
		}
		return chm;
	}

	VMetrics[] getChildrenRefreshedMinimumVMetrics()
	{
		return getChildrenRefreshedMinimumVMetrics( children );
	}

	
	VMetrics[] getChildrenRefreshedPreferredVMetrics(List<DocLayoutNode> nodes)
	{
		VMetrics[] chm = new VMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).refreshPreferredVMetrics();
		}
		return chm;
	}

	VMetrics[] getChildrenRefreshedPreferredVMetrics()
	{
		return getChildrenRefreshedPreferredVMetrics( children );
	}


	
	
	
	HMetrics[] getChildrenMinimumHMetrics(List<DocLayoutNode> nodes)
	{
		HMetrics[] chm = new HMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).minH;
		}
		return chm;
	}

	HMetrics[] getChildrenMinimumHMetrics()
	{
		return getChildrenMinimumHMetrics( children );
	}

	
	HMetrics[] getChildrenPreferredHMetrics(List<DocLayoutNode> nodes)
	{
		HMetrics[] chm = new HMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).prefH;
		}
		return chm;
	}
	
	HMetrics[] getChildrenPreferredHMetrics()
	{
		return getChildrenPreferredHMetrics();
	}
	
	
	
	VMetrics[] getChildrenMinimumVMetrics(List<DocLayoutNode> nodes)
	{
		VMetrics[] chm = new VMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).minV;
		}
		return chm;
	}

	VMetrics[] getChildrenMinimumVMetrics()
	{
		return getChildrenMinimumVMetrics( children );
	}

	
	VMetrics[] getChildrenPreferredVMetrics(List<DocLayoutNode> nodes)
	{
		VMetrics[] chm = new VMetrics[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			chm[i] = nodes.get( i ).prefV;
		}
		return chm;
	}

	VMetrics[] getChildrenPreferredVMetrics()
	{
		return getChildrenPreferredVMetrics( children );
	}
}
