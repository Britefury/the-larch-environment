package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextHitInfo;

import BritefuryJ.DocPresent.Util.TextVisual;
import BritefuryJ.Math.Point2;

public class DPText extends DPWidget implements TextVisual.TextVisualListener
{
	protected TextVisual visual;
	protected Point2 textPos;
	
	
	public DPText(String text, Font font, Color colour)
	{
		assert font != null;
		
		visual = new TextVisual( text, font, colour, this );
		
		textPos = new Point2();
	}
	
	
	
	public String getText()
	{
		return visual.getText();
	}
	
	public void setText(String text)
	{
		visual.setText( text );
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
	
	
	
	protected HMetrics computeRequiredHMetrics()
	{
		return visual.computeHMetrics();
	}
	
	protected VMetrics computeRequiredVMetrics()
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
}
