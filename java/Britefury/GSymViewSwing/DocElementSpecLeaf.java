package BritefuryJ.GSymViewSwing;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;

import BritefuryJ.GSymViewSwing.ElementViewFactories.ElementViewFactory;

public class DocElementSpecLeaf extends DocElementSpec
{
	private String text;
	private AttributeSet attribs;
	private ElementViewFactory viewFactory;
	
	
	public DocElementSpecLeaf(String text, AttributeSet attribs, ElementViewFactory viewFactory)
	{
		this.text = text;
		this.attribs = attribs;
		this.viewFactory = viewFactory;
	}
	
	public String getText()
	{
		return text;
	}

	public Element createElementSubtree(GSymViewDocument doc, Element parent, int offset)
	{
		return doc.createGSymLeafElement( parent, attribs, offset, offset + text.length(), viewFactory );
	}
}
