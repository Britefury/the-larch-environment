package BritefuryJ.GSymViewSwing;

import javax.swing.text.Element;

public abstract class DocElementSpec
{
	abstract String getText();
	abstract Element createElementSubtree(GSymViewDocument doc, Element parent, int offset);
}
