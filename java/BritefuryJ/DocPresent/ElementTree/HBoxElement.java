package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;

public class HBoxElement extends SequenceBranchElement
{
	public HBoxElement(HBoxStyleSheet styleSheet)
	{
		super( new DPHBox( styleSheet ) );
	}


	public DPHBox getWidget()
	{
		return (DPHBox)super.getWidget();
	}
}
