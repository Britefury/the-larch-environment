package BritefuryJ.DocLayout;

public abstract class DocLayoutNodeContainer extends DocLayoutNode
{
	protected void childRequestRelayout(DocLayoutNode child)
	{
		requestRelayout();
	}
}
