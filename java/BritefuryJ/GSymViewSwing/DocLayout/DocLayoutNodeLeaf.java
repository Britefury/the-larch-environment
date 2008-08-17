package BritefuryJ.GSymViewSwing.DocLayout;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

import BritefuryJ.GSymViewSwing.ElementViewFactories.ElementViewFactory;

public class DocLayoutNodeLeaf extends DocLayoutNode
{
	private String text;
	private AttributeSet attribs;
	private ElementViewFactory viewFactory;
	
	
	public DocLayoutNodeLeaf(String text, AttributeSet attribs, ElementViewFactory viewFactory)
	{
		this.text = text;
		this.attribs = attribs;
		this.viewFactory = viewFactory;
	}
	
	public String getText()
	{
		return text;
	}

	public Element createElementSubtree(Element parent, int offset)
	{
		return docLayout.document.createGSymLeafElement( parent, attribs, offset, offset + text.length(), viewFactory );
	}
}
