package BritefuryJ.DocLayout;

public class DocLayoutNodeRoot extends DocLayoutNodeBin
{
	public static interface RootListener
	{
		public void onLayoutRootRelayoutRequest();
	}

	
	
	private RootListener listener;
	
	
	public DocLayoutNodeRoot()
	{
		super();
	}
	
	
	public void setListener(RootListener listener)
	{
		this.listener = listener;
	}
	
	
	public void requestRelayout()
	{
		super.requestRelayout();
		
		if ( listener != null )
		{
			listener.onLayoutRootRelayoutRequest();
		}
	}
}
