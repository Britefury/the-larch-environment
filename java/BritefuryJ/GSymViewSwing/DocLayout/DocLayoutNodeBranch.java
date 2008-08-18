package BritefuryJ.GSymViewSwing.DocLayout;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

import BritefuryJ.GSymViewSwing.ElementViewFactories.ElementViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.LabelViewFactory;

public class DocLayoutNodeBranch extends DocLayoutNode
{
	private DocLayoutNode[] children;
	private AttributeSet attribs;
	private ElementViewFactory viewFactory;
	
	
	public DocLayoutNodeBranch(AttributeSet attribs, ElementViewFactory viewFactory)
	{
		this.children = new DocLayoutNode[0];
		this.attribs = attribs;
		this.viewFactory = viewFactory;
	}
	
	
	protected void setDocLayout(DocLayout doc)
	{
		super.setDocLayout( doc );

		for (DocLayoutNode child: this.children)
		{
			child.setDocLayout( doc );
		}
	}
	

	public void setChildren(DocLayoutNode[] children)
	{
		for (DocLayoutNode child: this.children)
		{
			child.setDocLayout( null );
			child.setParent( null );
		}
		
		this.children = children;

		for (DocLayoutNode child: this.children)
		{
			child.setParent( parent );
			child.setDocLayout( docLayout );
		}
		
		requestRefresh();
	}
	

	public String getText()
	{
		StringBuffer b = new StringBuffer();
		
		for (DocLayoutNode e: children)
		{
			b.append( e.getText() );
		}
		
		return b.toString();
	}

	
	public Element createElementSubtree(Element parent, int offset)
	{
		assert docLayout != null;
		
		if ( children.length == 0 )
		{
			AbstractDocument.BranchElement branch = docLayout.document.createGSymBranchElement( parent, attribs, viewFactory );
			Element[] childElements = { docLayout.document.createGSymLeafElement( branch, attribs, offset, offset + 1, LabelViewFactory.viewFactory ) };
			branch.replace( 0, 0, childElements );
			return branch;
		}
		else
		{
			Element[] childElements = new Element[children.length];
			
			AbstractDocument.BranchElement branch = docLayout.document.createGSymBranchElement( parent, attribs, viewFactory );
			
			for (int childI = 0; childI < children.length; childI++)
			{
				Element e = children[childI].createElementSubtree( branch, offset );
				offset = e.getEndOffset();
				childElements[childI] = e;
			}
			
			branch.replace( 0, 0, childElements );
			
			return branch;
		}
	}
}
