package BritefuryJ.DocLayout;

public class DocLayoutNodeLineBreak extends DocLayoutNodeBin
{
	private int lineBreakPriority;
	
	
	public DocLayoutNodeLineBreak()
	{
		this( 0 );
	}
	
	public DocLayoutNodeLineBreak(int lineBreakPriority)
	{
		super();
		this.lineBreakPriority = lineBreakPriority;
	}
	
	
	void setLineBreakPriority(int lineBreakPriority)
	{
		this.lineBreakPriority = lineBreakPriority;
		requestRelayout();
	}
	
	
	public int getLineBreakPriority()
	{
		return lineBreakPriority;
	}
}
