package BritefuryJ.DocLayout;

import BritefuryJ.Text.TextVisual;

public class DocLayoutNodeText extends DocLayoutNode
{
	private TextVisual textVisual;
	
	
	public DocLayoutNodeText(TextVisual textVisual)
	{
		this.textVisual = textVisual;
	}
	
	
	void setMetrics(TextVisual textVisual)
	{
		this.textVisual = textVisual;
		requestRelayout();
	}
	
	
	protected HMetrics computeMinimumHMetrics()
	{
		return textVisual.computeHMetrics();
	}

	protected HMetrics computePreferredHMetrics()
	{
		return textVisual.computeHMetrics();
	}


	protected VMetrics computeMinimumVMetrics()
	{
		return textVisual.computeVMetrics();
	}

	protected VMetrics computePreferredVMetrics()
	{
		return textVisual.computeVMetrics();
	}
}
