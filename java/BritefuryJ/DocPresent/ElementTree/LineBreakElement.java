package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class LineBreakElement extends BinElement
{
	public LineBreakElement()
	{
		this( 0 );
	}
	
	public LineBreakElement(ContainerStyleSheet styleSheet)
	{
		this( styleSheet, 0 );
	}
	
	public LineBreakElement(int lineBreakPriority)
	{
		this( ContainerStyleSheet.defaultStyleSheet, lineBreakPriority );
	}
	
	public LineBreakElement(ContainerStyleSheet styleSheet, int lineBreakPriority)
	{
		super( new DPLineBreak( styleSheet, lineBreakPriority ) );
	}

	
	public DPLineBreak getWidget()
	{
		return (DPLineBreak)widget;
	}
}
