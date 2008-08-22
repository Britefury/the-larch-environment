package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;


public class DPCustomSymbol extends DPWidget
{
	public interface SymbolInterface
	{
		public HMetrics computeHMetrics();
		public VMetrics computeVMetrics();
		public void draw(Graphics2D graphics);
	}
	
	
	protected SymbolInterface symbol;
	protected Color colour;
	
	
	
	public DPCustomSymbol(SymbolInterface symbol, Color colour)
	{
		this.symbol = symbol;
		this.colour = colour;
		
		queueResize();
	}
	
	
	public SymbolInterface getSymbol()
	{
		return symbol;
	}
	
	public void setSymbol(SymbolInterface symbol)
	{
		this.symbol = symbol;
		queueResize();
	}
	
	
	public Color getColour()
	{
		return colour;
	}
	
	public void setColour(Color colour)
	{
		this.colour = colour;
		queueFullRedraw();
	}
	
	
	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		graphics.setColor( colour );
		symbol.draw( graphics );
	}
	
	
	
	protected HMetrics computeMinimumHMetrics()
	{
		return symbol.computeHMetrics();
	}
	
	protected HMetrics computePreferredHMetrics()
	{
		return symbol.computeHMetrics();
	}


	protected VMetrics computeMinimumVMetrics()
	{
		return symbol.computeVMetrics();
	}

	protected VMetrics computePreferredVMetrics()
	{
		return symbol.computeVMetrics();
	}
}


