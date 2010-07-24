//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import java.util.HashMap;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Border.FilledBorder;
import BritefuryJ.Utils.HashUtils;

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
			hash = HashUtils.nHash( new int[] { new Double( leftPad ).hashCode(), new Double( rightPad ).hashCode(), new Double( topPad ).hashCode(), new Double( bottomPad ).hashCode() } );
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
	public DPElement present(PresentationContext ctx)
	{
		DPBorder element = new DPBorder( padBorder );
		DPElement childElem = child.present( ctx );
		element.setChild( childElem.layoutWrap() );
		element.copyAlignmentFlagsFrom( childElem );
		return element;
	}
}
