//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.RichText;

import java.awt.Paint;

import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Pres.CompositePres;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Rule extends CompositePres
{
	public enum Direction
	{
		HORIZONTAL,
		VERTICAL
	}
	
	private Direction dir;
	
	
	public Rule(Direction dir)
	{
		this.dir = dir;
	}
	
	
	public static Pres hrule()
	{
		return new Rule( Direction.HORIZONTAL );
	}
	
	public static Pres vrule()
	{
		return new Rule( Direction.VERTICAL );
	}
	
	
	@Override
	public Pres pres(PresentationContext ctx, StyleValues style)
	{
		double ruleThickness = style.get( RichText.ruleThickness, Double.class );
		double ruleInset = style.get( RichText.ruleInset, Double.class );
		double rulePadding = style.get( RichText.rulePadding, Double.class );
		StyleSheet ruleStyle = StyleSheet.style( Primitive.shapePainter.as( new FillPainter( style.get( RichText.rulePaint, Paint.class ) ) ) );
		Pres box = ruleStyle.applyTo( new Box( ruleThickness, ruleThickness) );
		
		if ( dir == Direction.HORIZONTAL )
		{
			return new Bin( box.pad( ruleInset, rulePadding ) ).alignHExpand().alignVTop();
		}
		else if ( dir == Direction.VERTICAL )
		{
			return new Bin( box.pad( ruleInset, rulePadding ) ).alignVExpand().alignHPack();
		}
		else
		{
			throw new RuntimeException( "Invalid rule direction" );
		}
	}
}
