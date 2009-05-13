//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Metrics;

public abstract class Metrics
{
	public abstract double getLength();
	public abstract double getTotalLength();
	
	public abstract Metrics scaled(double scale);
	public abstract Metrics minSpacing(double spacing);
	public abstract Metrics offsetLength(double deltaLength);


	//
	//
	// NOTE:
	// For padding and spacing; subtract the amount of space required for these from @allocation, before passing that value in
	//
	//
	public static Metrics[] allocateSpacePacked(Metrics minimum[], Metrics[] preferred, double allocation)
	{
		return allocateSpacePacked( minimum, preferred, null, allocation );
	}

	//
	//
	// NOTE:
	// For padding and spacing; subtract the amount of space required for these from @allocation, before passing that value in
	//
	//
	public static Metrics[] allocateSpacePacked(Metrics minimum[], Metrics preferred[], int packFlags[], double allocation)
	{
		assert minimum.length == preferred.length;
		
		int numExpand = 0;
		
		if ( packFlags != null )
		{
			assert packFlags.length == minimum.length;
			for (int i = 0; i < packFlags.length; i++)
			{
				int f = packFlags[i];
				if ( testPackFlagExpand( f ) )
				{
					numExpand++;
				}
			}
		}
		
		
		double minSum = 0.0, prefSum = 0.0;
		for (int i = 0; i < minimum.length; i++)
		{
			if ( i == minimum.length - 1 )
			{
				minSum += minimum[i].getLength();
				prefSum += preferred[i].getLength();
			}
			else
			{
				minSum += minimum[i].getTotalLength();
				prefSum += preferred[i].getTotalLength();
			}
		}
		
		if ( allocation >= prefSum )
		{
			if ( allocation == prefSum  ||  numExpand == 0 )
			{
				return preferred;
			}
			else
			{
				double totalExpand = allocation - prefSum;
				double expandPerChild = totalExpand / (double)numExpand;
				
				Metrics[] childAlloc = new Metrics[preferred.length];
				
				for (int i = 0; i < preferred.length; i++)
				{
					if ( packFlags != null  &&  testPackFlagExpand( packFlags[i] ) )
					{
						childAlloc[i] = preferred[i].offsetLength( expandPerChild );
					}
					else
					{
						childAlloc[i] = preferred[i];
					}
				}
				
				return childAlloc;
			}
		}
		else if ( allocation <= minSum )
		{
			return minimum;
		}
		else
		{
			Metrics[] childAlloc = new Metrics[minimum.length];
			double deltaSum = prefSum - minSum;
			double allocToShare = allocation - minSum;
			double fraction = allocToShare / deltaSum;
			
			for (int i = 0; i < minimum.length; i++)
			{
				double delta = 0.0;
				if ( i == minimum.length - 1 )
				{
					delta = preferred[i].getLength() - minimum[i].getLength();
				}
				else
				{
					delta = preferred[i].getTotalLength() - minimum[i].getTotalLength();
				}
				
				childAlloc[i] = minimum[i].offsetLength( delta * fraction );
			}
			
			return childAlloc;
		}
	}
	
	
	
	
	
	private static int PACKFLAG_EXPAND = 1;
	
	
	public static int packFlags(boolean bExpand)
	{
		return ( bExpand ? PACKFLAG_EXPAND : 0 );
	}
	
	public static int combinePackFlags(int flags0, int flags1)
	{
		return flags0 | flags1;
	}
	
	public static boolean testPackFlagExpand(int packFlags)
	{
		return ( packFlags & PACKFLAG_EXPAND )  !=  0;
	}
}
