package BritefuryJ.GSymViewSwing.DocLayout;

import java.util.HashSet;
import java.util.Vector;

import javax.swing.text.Element;

import BritefuryJ.GSymViewSwing.GSymViewDocument;
import BritefuryJ.GSymViewSwing.ElementViewFactories.ParagraphViewFactory;

public class DocLayout
{
	private HashSet<DocLayoutNode> nodesToRefresh;
	protected GSymViewDocument document;
	private DocLayoutNodeBranch root;
	
	
	public DocLayout(GSymViewDocument document)
	{
		nodesToRefresh = new HashSet<DocLayoutNode>();
		this.document = document;
		root = new DocLayoutNodeBranch( null, ParagraphViewFactory.viewFactory );
		root.setDocLayout( this );
	}
	
	
	
	public DocLayoutNodeBranch getRoot()
	{
		return root;
	}
	
	
	public void refresh()
	{
		for (DocLayoutNode node: nodesToRefresh)
		{
			Element elementToReplace = node.getElement();
			document.elementReplace( elementToReplace, node );
		}
		
		nodesToRefresh.clear();
	}
	
	

	
	protected void nodeRefreshRequest(DocLayoutNode node)
	{
		// First, remove any nodes from @nodesToRefresh that are in the subtree rooted at @node
		Vector<DocLayoutNode> nodesToRemove = new Vector<DocLayoutNode>();
		
		for (DocLayoutNode n: nodesToRefresh)
		{
			if ( n != node  &&  n.isInSubtreeRootedAt( node ) )
			{
				nodesToRemove.add( n );
			}
		}
		
		for (DocLayoutNode n: nodesToRemove)
		{
			nodesToRefresh.remove( n );
		}
		
		// Now, add @node to @nodesToRefresh
		nodesToRefresh.add( node );
	}
}
