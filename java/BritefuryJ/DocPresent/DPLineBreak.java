package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class DPLineBreak extends DPBin implements LineBreakInterface
{
	private int lineBreakPriority;
	
	
	public DPLineBreak()
	{
		this( 0 );
	}
	
	public DPLineBreak(ContainerStyleSheet styleSheet)
	{
		this( styleSheet, 0 );
	}
	
	public DPLineBreak(int lineBreakPriority)
	{
		this( ContainerStyleSheet.defaultStyleSheet, lineBreakPriority );
	}
	
	public DPLineBreak(ContainerStyleSheet styleSheet, int lineBreakPriority)
	{
		super( styleSheet );
		this.lineBreakPriority = lineBreakPriority;
	}
	
	
	void setLineBreakPriority(int lineBreakPriority)
	{
		this.lineBreakPriority = lineBreakPriority;
		queueResize();
	}
	
	
	public int getLineBreakPriority()
	{
		return lineBreakPriority;
	}
	
	
	
	public LineBreakInterface getLineBreakInterface()
	{
		return this;
	}
}
