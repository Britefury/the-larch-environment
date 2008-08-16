package BritefuryJ.GSymViewSwing.ElementViewFactories;

import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.View;

public class GlyphViewFactory implements ElementViewFactory
{
	public static GlyphViewFactory viewFactory = new GlyphViewFactory();

	public View createView(Element e)
	{
		return new GlyphView( e );
	}

}
