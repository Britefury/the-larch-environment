//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import java.util.HashMap;

import BritefuryJ.Graphics.FilledBorder;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleValues;
import BritefuryJ.Util.HashUtils;

public class Pad extends Pres
{
	//
	//
	// Padding
	//
	//
	
	private static class PaddingKey
	{
		private double leftPad, rightPad, topPad, bottomPad;
		private int hash;
		
		
		public PaddingKey(double leftPad, double rightPad, double topPad, double bottomPad)
		{
			this.leftPad = leftPad;
			this.rightPad = rightPad;
			this.topPad = topPad;
			this.bottomPad = bottomPad;
			hash = HashUtils.quadHash( new Double( leftPad ).hashCode(), new Double( rightPad ).hashCode(), new Double( topPad ).hashCode(), new Double( bottomPad ).hashCode() );
		}
		
		
		public int hashCode()
		{
			return hash;
		}
		
		
		public boolean equals(Object x)
		{
			if ( this == x )
			{
				return true;
			}
			
			
			if ( x instanceof PaddingKey )
			{
				PaddingKey k = (PaddingKey)x;
				
				return leftPad == k.leftPad  &&  rightPad == k.rightPad  &&  topPad == k.topPad  &&  bottomPad == k.bottomPad;
			}
			else
			{
				return false;
			}
		}
	}
	

	private static HashMap<PaddingKey, FilledBorder> paddingBorders = new HashMap<PaddingKey, FilledBorder>();
	
	
	private FilledBorder padBorder;
	private Pres child;

	
	
	public Pad(Pres child, double leftPad, double rightPad, double topPad, double bottomPad)
	{
		if ( leftPad == 0.0  &&  rightPad == 0.0  &&  topPad == 0.0  &&  bottomPad == 0.0 )
		{
			padBorder = null;
			this.child = child;
		}
		else
		{
			PaddingKey key = new PaddingKey( leftPad, rightPad, topPad, bottomPad );
			padBorder = paddingBorders.get( key );
			
			if ( padBorder == null )
			{
				padBorder = new FilledBorder( leftPad, rightPad, topPad, bottomPad );
				paddingBorders.put( key, padBorder );
			}

			this.child = child;
		}
	}

	
	
	//
	// Padding methods
	//
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement childElem = child.present( ctx, style );
		childElem = childElem.layoutWrap( style.get( Primitive.hAlign, HAlignment.class ), style.get( Primitive.vAlign, VAlignment.class ) );
		if ( padBorder != null )
		{
			return new LSBorder( padBorder, Primitive.getContainerStyleParams( style ), childElem );
		}
		else
		{
			return childElem;
		}
	}
}
