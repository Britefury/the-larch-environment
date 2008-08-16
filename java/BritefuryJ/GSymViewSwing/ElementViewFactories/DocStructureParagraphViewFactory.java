package BritefuryJ.GSymViewSwing.ElementViewFactories;

import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.View;

public class DocStructureParagraphViewFactory implements ElementViewFactory
{
	public static DocStructureParagraphViewFactory viewFactory = new DocStructureParagraphViewFactory();
	
	public View createView(Element e)
	{
		return new ParagraphView( e );
	}
}
