package BritefuryJ.GSymViewSwing.ElementViewFactories;

import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.View;

public class ParagraphViewFactory implements ElementViewFactory
{
	public static ParagraphViewFactory viewFactory = new ParagraphViewFactory();

	public View createView(Element e)
	{
		return new ParagraphView( e );
	}

}
