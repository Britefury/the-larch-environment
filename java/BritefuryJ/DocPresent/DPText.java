package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.TextHitInfo;

import BritefuryJ.DocPresent.Caret.Caret;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class DPText extends DPContentLeaf implements TextVisual.TextVisualListener
{
	protected TextVisual visual;
	protected Point2 textPos;
	
	
	public DPText(String text, Font font, Color colour)
	{
		super( text );
		assert font != null;
		
		visual = new TextVisual( text, font, colour, this );
		
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

	
	public Font getFont()
	{
		return visual.getFont();
	}
	
	public void setFont(Font font)
	{
		assert font != null;

		visual.setFont( font );
	}
	
	
	public Color getColour()
	{
		return visual.getColour();
	}
	
	public void setColour(Color colour)
	{
		visual.setColour( colour );
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
		return info.getInsertionIndex();
	}
	
	
	protected boolean onKeyPress(Caret caret, KeyEvent event)
	{
		insertContent( caret.getMarker().getIndex(), String.valueOf( event.getKeyChar() ) );
		return true;
	}
}
