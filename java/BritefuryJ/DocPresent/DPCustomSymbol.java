package BritefuryJ.DocPresent;

import java.awt.Color;
import java.awt.Graphics2D;

import BritefuryJ.DocPresent.Metrics.HMetrics;
import BritefuryJ.DocPresent.Metrics.VMetrics;
import BritefuryJ.DocPresent.StyleSheets.CustomSymbolStyleSheet;


public class DPCustomSymbol extends DPWidget
{
	public interface SymbolInterface
	{
		public HMetrics computeHMetrics();
		public VMetrics computeVMetrics();
		public void draw(Graphics2D graphics);
	}
	
	
	protected SymbolInterface symbol;
	
	
	
	public DPCustomSymbol(SymbolInterface symbol)
	{
		this( CustomSymbolStyleSheet.defaultStyleSheet, symbol );
	}
	
	public DPCustomSymbol(CustomSymbolStyleSheet styleSheet, SymbolInterface symbol)
	{
		super( styleSheet );
		
		this.symbol = symbol;
		
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
	
	
	protected void draw(Graphics2D graphics)
	{
		super.draw( graphics );
		
		graphics.setColor( getColour() );
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
	
	
	protected Color getColour()
	{
		return ((CustomSymbolStyleSheet)styleSheet).getColour();
	}
}


