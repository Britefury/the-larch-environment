package BritefuryJ.DocPresent;

import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.ContentLeafStyleSheet;
import BritefuryJ.Math.Point2;

public class DPWhitespace extends DPContentLeaf
{
	protected double width;
	
	
	public DPWhitespace()
	{
		this( ContentLeafStyleSheet.defaultStyleSheet, "", 0.0 );
	}
	
	public DPWhitespace(ContentLeafStyleSheet styleSheet)
	{
		this( styleSheet, "", 0.0 );
	}
	
	public DPWhitespace(String content, double width)
	{
		this( ContentLeafStyleSheet.defaultStyleSheet, content, width );
	}

	public DPWhitespace(ContentLeafStyleSheet styleSheet, String content, double width)
	{
		super( styleSheet, content );
		this.width = width;
	}

	
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
	}

	public int getContentPositonForPoint(Point2 localPos)
	{
		if ( localPos.x >= width * 0.5 )
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}

	protected HMetrics computeMinimumHMetrics()
	{
		return new HMetrics( width );
	}

	protected HMetrics computePreferredHMetrics()
	{
		return new HMetrics( width );
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return new VMetrics();
	}
	
	protected VMetrics computePreferredVMetrics()
	{
		return new VMetrics();
	}



	public boolean isWhitespace()
	{
		return true;
	}
}
