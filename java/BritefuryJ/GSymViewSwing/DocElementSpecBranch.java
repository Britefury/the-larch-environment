package BritefuryJ.GSymViewSwing;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

import BritefuryJ.GSymViewSwing.ElementViewFactories.ElementViewFactory;

public class DocElementSpecBranch extends DocElementSpec
{
	private DocElementSpec[] children;
	private AttributeSet attribs;
	private ElementViewFactory viewFactory;
	
	
	public DocElementSpecBranch(DocElementSpec[] children, AttributeSet attribs, ElementViewFactory viewFactory)
	{
		this.children = children;
		this.attribs = attribs;
		this.viewFactory = viewFactory;
	}
	

	String getText()
	{
		StringBuffer b = new StringBuffer();
		
		for (DocElementSpec e: children)
		{
			b.append( e.getText() );
		}
		
		return b.toString();
	}

	
	Element createElementSubtree(GSymViewDocument doc, Element parent, int offset)
	{
		Element[] childElements = new Element[children.length];
		
		AbstractDocument.BranchElement branch = doc.createGSymBranchElement( parent, attribs, viewFactory );
		
		for (int childI = 0; childI < children.length; childI++)
		{
			Element e = children[childI].createElementSubtree( doc, branch, offset );
			offset = e.getEndOffset();
			childElements[childI] = e;
		}
		
		branch.replace( 0, 0, childElements );
		
		return branch;
	}
}
