package BritefuryJ.DocPresent;

import java.awt.Graphics2D;
import java.awt.font.TextHitInfo;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class DPText extends DPContentLeafEditable implements TextVisual.TextVisualListener
{
	protected TextVisual visual;
	protected Point2 textPos;
	
	
	public DPText(String text)
	{
		this( TextStyleSheet.defaultStyleSheet, text );
	}
	
	public DPText(TextStyleSheet styleSheet, String text)
	{
		super( styleSheet, text );
		
		visual = new TextVisual( text, styleSheet, this );
		
		textPos = new Point2();
	}
	
	
	
	public String getText()
	{
		return getContent();
	}
	
	public void setText(String text)
	{
		setContent( text );
	}
	
	
	public void contentChanged()
	{
		visual.setText( getContent() );
	}

	
	public TextHitInfo hitTest(Point2 pos)
	{
		return visual.hitTest( pos );
	}
	
	
	
	protected void onRealise()
	{
		super.onRealise();
		visual.realise( presentationArea );
	}
	
	protected void onUnrealise()
	{
		super.onUnrealise();
		visual.unrealise();
	}
	
	
	
	protected void draw(Graphics2D graphics)
	{
		visual.draw( graphics );
	}
	
	
	public void drawCaret(Graphics2D graphics, Caret c)
	{
		visual.drawCaret( graphics, c.getMarker().getIndex() );
	}



	
	protected HMetrics computeMinimumHMetrics()
	{
		return visual.computeHMetrics();
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		return visual.computeHMetrics();
	}

	
	protected VMetrics computeMinimumVMetrics()
	{
		return visual.computeVMetrics();
	}

	protected VMetrics computePreferredVMetrics()
	{
		return visual.computeVMetrics();
	}

	
	public void textVisualRequestRedraw(TextVisual t)
	{
		queueFullRedraw();
	}


	public void textVisualRequestResize(TextVisual t)
	{
		queueResize();
	}



	public int getContentPositonForPoint(Point2 localPos)
	{
		TextHitInfo info = hitTest( localPos );
		if ( info != null )
		{
			return info.getInsertionIndex();
		}
		else
		{
			return 0;
		}
	}
}
