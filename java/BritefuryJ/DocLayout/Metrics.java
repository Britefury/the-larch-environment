package BritefuryJ.DocLayout;

public abstract class Metrics
{
	public abstract double getLength();
	public abstract double getTotalLength();
	
	public abstract Metrics scaled(double scale);
	public abstract Metrics minSpacing(double spacing);
	public abstract Metrics offsetLength(double deltaLength);


	public static Metrics[] allocateSpacePacked(Metrics[] minimum, Metrics[] preferred, double allocation)
	{
		assert minimum.length == preferred.length;
		
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
			return preferred;
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


}
