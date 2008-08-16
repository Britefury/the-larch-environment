package BritefuryJ.GSymViewSwing.ElementViewFactories;

import javax.swing.text.Element;
import javax.swing.text.GlyphView;
import javax.swing.text.View;

public class DocStructureTextViewFactory implements ElementViewFactory
{
	public static DocStructureTextViewFactory viewFactory = new DocStructureTextViewFactory();
	
	public View createView(Element e)
	{
		return new GlyphView( e );
	}
}
