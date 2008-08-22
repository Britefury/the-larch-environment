package BritefuryJ.DocPresent;

public class DPLineBreak extends DPBin implements LineBreakInterface
{
	private int lineBreakPriority;
	
	
	public DPLineBreak()
	{
		this( 0 );
	}
	
	public DPLineBreak(int lineBreakPriority)
	{
		super();
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
