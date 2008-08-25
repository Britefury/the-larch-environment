package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class VBoxElement extends SequenceBranchElement
{
	public VBoxElement(VBoxStyleSheet styleSheet)
	{
		super( new DPVBox( styleSheet ) );
	}


	public DPVBox getWidget()
	{
		return (DPVBox)widget;
	}
}
