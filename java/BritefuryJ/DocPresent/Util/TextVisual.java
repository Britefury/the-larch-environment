package BritefuryJ.DocPresent.Util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;

import javax.swing.JComponent;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.Metrics.VMetricsTypeset;
import BritefuryJ.Math.Point2;



public class TextVisual
{
	public static interface TextVisualListener
	{
		public void textVisualRequestResize(TextVisual t);
		public void textVisualRequestRedraw(TextVisual t);
	}
	
	
	private TextLayout layout;
	private String text;
	private Font font;
	private Color colour;
	private TextVisualListener listener;
	private JComponent component;
	
	
	public TextVisual(String text, Font font, Color colour, TextVisualListener listener)
	{
		assert font != null;
		
		this.text = text;
		this.font = font;
		this.colour = colour;
		this.listener = listener;
	}
	
	
	
	public String getText()
	{
		return text;
	}
	
	public void setText(String text)
	{
		if ( !text.equals( this.text ) )
		{
			this.text = text;
			layoutChanged();
		}
	}
	
	
	
	public Font getFont()
	{
		return font;
	}
	
	public void setFont(Font font)
	{
		if ( font != this.font )
		{
			this.font = font;
			layoutChanged();
		}
	}
	
	
	
	public Color getColour()
	{
		return colour;
	}
	
	public void setColour(Color colour)
	{
		if ( !colour.equals( this.colour ) )
		{
			this.colour = colour;
			requestRedraw();
		}
	}
	
	
	
	public void realise(DPPresentationArea a)
	{
		component = a.getComponent();
	}
	
	public void realise(JComponent component)
	{
		this.component = component;
	}
	
	
	public void unrealise()
	{
		component = null;
		layout = null;
	}
	
	
	
	
	public HMetrics computeHMetrics()
	{
		refreshLayout();
		double width = layout.getBounds().getWidth();
		return new HMetrics( width, layout.getAdvance() - width );
	}
	
	public VMetrics computeVMetrics()
	{
		refreshLayout();
		return new VMetricsTypeset( layout.getAscent(), layout.getDescent(), layout.getLeading() );
	}
	
	
	
	public void draw(Graphics2D graphics)
	{
		refreshLayout();
		graphics.setColor( colour );
		layout.draw( graphics, 0.0f, layout.getAscent() );
	}
	
	
	
	public TextHitInfo hitTest(Point2 pos)
	{
		refreshLayout();
		return layout.hitTestChar( (float)pos.x, (float)pos.y );
	}
	
	
	
	
	
	
	private void layoutChanged()
	{
		layout = null;
		requestResize();
	}
	
	private void requestResize()
	{
		if ( listener != null )
		{
			listener.textVisualRequestResize( this );
		}
	}
	
	private void requestRedraw()
	{
		if ( listener != null )
		{
			listener.textVisualRequestRedraw( this );
		}
	}
	
	private void refreshLayout()
	{
		assert component != null;
		if ( layout == null )
		{
			Graphics2D graphics = (Graphics2D)component.getGraphics();
			FontRenderContext frc = graphics.getFontRenderContext();
			layout = new TextLayout( text, font, frc );
		}
	}
}
