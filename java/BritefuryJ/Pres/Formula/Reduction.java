//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Formula;

import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Formula.Formula.FormulaStyle;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Script;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Reduction extends CompositePres
{
	private Pres reductionSymbol, expr, rangeInit, rangeEnd;
	
	
	public Reduction(Object reductionSymbol, Object expr, Object rangeInit, Object rangeEnd)
	{
		this.reductionSymbol = Pres.coerce( reductionSymbol );
		this.expr = Pres.coerce( expr );
		this.rangeInit = rangeInit != null  ?  Pres.coerce( rangeInit )  :  null;
		this.rangeEnd = rangeEnd != null  ?  Pres.coerce( rangeEnd )  :  null;
	}
	
	public Reduction(Object reductionSymbol, Object expr)
	{
		this( reductionSymbol, expr, null, null );
	}
	
	
	
	@Override
	public Pres pres(PresentationContext ctx, StyleValues style)
	{
		FormulaStyle st = style.get( Formula.formulaStyle, FormulaStyle.class );
		StyleSheet reductionStyle = Formula.reductionSymbolStyle.get( style );
		StyleSheet smallStyle = Formula.smallStyle.get( style );
		
		if ( st == FormulaStyle.STANDARD )
		{
			int numItems = 1;
			if ( rangeInit != null )
			{
				numItems++;
			}
			if ( rangeEnd != null )
			{
				numItems++;
			}
			Pres leftItems[] = new Pres[numItems];
			int i = 0;
			if ( rangeEnd != null )
			{
				leftItems[i++] = smallStyle.applyTo( rangeEnd ).alignHCentre();
			}
			int refPointIndex = i;
			leftItems[i++] = reductionStyle.applyTo( reductionSymbol );
			if ( rangeInit != null )
			{
				leftItems[i++] = smallStyle.applyTo( rangeInit ).alignHCentre();
			}
			
			Pres left = new Column( refPointIndex, leftItems );
			
			return new Row( new Pres[] { left, expr } );
		}
		else if ( st == FormulaStyle.INLINE )
		{
			return new Row( new Pres[] {
					new Script( reductionStyle.applyTo( reductionSymbol ), null, null, smallStyle.applyTo( rangeEnd ), smallStyle.applyTo( rangeInit ) ),
					expr } );
		}
		else
		{
			throw new RuntimeException( "Unknown FormulaStyle" );
		}
	}
	
	
	
	public static Reduction sum(Object expr, Object rangeInit, Object rangeEnd)
	{
		return new Reduction( new Label( "\u2211" ), expr, rangeInit, rangeEnd );
	}

	public static Reduction sum(Object expr)
	{
		return new Reduction( new Label( "\u2211" ), expr );
	}

	public static Reduction product(Object expr, Object rangeInit, Object rangeEnd)
	{
		return new Reduction( new Label( "\u2210" ), expr, rangeInit, rangeEnd );
	}

	public static Reduction product(Object expr)
	{
		return new Reduction( new Label( "\u2210" ), expr );
	}

	public static Reduction coproduct(Object expr, Object rangeInit, Object rangeEnd)
	{
		return new Reduction( new Label( "\u220f" ), expr, rangeInit, rangeEnd );
	}

	public static Reduction coproduct(Object expr)
	{
		return new Reduction( new Label( "\u220f" ), expr );
	}

	public static Reduction intersection(Object expr, Object rangeInit, Object rangeEnd)
	{
		return new Reduction( new Label( "\u2229" ), expr, rangeInit, rangeEnd );
	}

	public static Reduction intersection(Object expr)
	{
		return new Reduction( new Label( "\u2229" ), expr );
	}

	public static Reduction union(Object expr, Object rangeInit, Object rangeEnd)
	{
		return new Reduction( new Label( "\u222a" ), expr, rangeInit, rangeEnd );
	}

	public static Reduction union(Object expr)
	{
		return new Reduction( new Label( "\u222a" ), expr );
	}
}
