package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;

public class ParagraphElement extends SequenceBranchElement
{
	public ParagraphElement(ParagraphStyleSheet styleSheet)
	{
		super( new DPParagraph( styleSheet ) );
	}


	public DPParagraph getWidget()
	{
		return (DPParagraph)widget;
	}
}
