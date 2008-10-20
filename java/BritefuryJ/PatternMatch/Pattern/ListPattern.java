//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch.Pattern;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ListPattern extends Pattern
{
	public static class OnlyOneRepeatAllowedException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	
	public static class Repeat extends Pattern implements RepeatInterface
	{
		private Pattern pattern;
		private int minLength, maxLength;
		
		
		public Repeat(int minLength, int maxLength, Pattern pattern)
		{
			this.minLength = minLength;
			this.maxLength = maxLength;
			this.pattern = pattern;
		}

	
		@SuppressWarnings("unchecked")
		public boolean test(Object x, Map<String, Object> bindings)
		{
			if ( x instanceof List )
			{
				List<Object> xs = (List<Object>)x;
				
				if ( xs.size() >= minLength  &&  ( maxLength == -1  ||  xs.size() <= maxLength ) )
				{
					for (Object a: xs)
					{
						if ( !pattern.test( a, bindings ) )
						{
							return false;
						}
					}
					
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}


		protected RepeatInterface getRepeatInterface()
		{
			return this;
		}


		public int getMinRepetitions()
		{
			return minLength;
		}

		public int getMaxRepetitions()
		{
			return maxLength;
		}


		public boolean equals(Object x)
		{
			if ( x instanceof Repeat )
			{
				Repeat r = (Repeat)x;
				return minLength == r.minLength  &&  maxLength == r.maxLength  &&  pattern.equals( r.pattern );
			}
			else
			{
				return false;
			}
		}
	}
	
	
	
	public static class ZeroOrMore extends Repeat
	{
		public ZeroOrMore(Pattern pattern)
		{
			super( 0, -1, pattern );
		}
	}
	
	
	
	public static class OneOrMore extends Repeat
	{
		public OneOrMore(Pattern pattern)
		{
			super( 1, -1, pattern );
		}
	}
	
	
	
	
	
	
	private Pattern[] begin, end;
	private Pattern repeat;
	private int minLength, maxLength;
	
	
	public ListPattern(Pattern[] xs) throws OnlyOneRepeatAllowedException
	{
		this( Arrays.asList( xs ) );
	}
	
	public ListPattern(List<Pattern> xs) throws OnlyOneRepeatAllowedException
	{
		int repeatIndex = -1;
		
		for (int i = 0; i < xs.size(); i++)
		{
			Pattern x = xs.get( i );
			RepeatInterface r = x.getRepeatInterface();
			if ( r != null )
			{
				if ( repeat != null )
				{
					throw new OnlyOneRepeatAllowedException();
				}
				repeat = x;
				repeatIndex = i;
			}
		}
		
		if ( repeat != null )
		{
			RepeatInterface r = repeat.getRepeatInterface();
			minLength = r.getMinRepetitions();
			maxLength = r.getMaxRepetitions();
			
			if ( repeatIndex > 0 )
			{
				List<Pattern> beginXs = xs.subList( 0, repeatIndex );
				begin = new Pattern[beginXs.size()];
				begin = beginXs.toArray( begin );
				minLength += begin.length;
				if ( maxLength != -1 )
				{
					maxLength += begin.length;
				}
			}
		
			if ( repeatIndex < ( xs.size() - 1 ) )
			{
				List<Pattern> endXs = xs.subList( repeatIndex + 1, xs.size() );
				end = new Pattern[endXs.size()];
				end = endXs.toArray( end );
				minLength += end.length;
				if ( maxLength != -1 )
				{
					maxLength += end.length;
				}
			}
		}
		else
		{
			begin = (Pattern[])xs.toArray();
			minLength = maxLength = begin.length;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean test(Object x, Map<String, Object> bindings)
	{
		if ( x instanceof List )
		{
			List<Object> xs = (List<Object>)x;
			
			int size = xs.size();
			if ( repeat != null )
			{
				if ( size >= minLength  &&  ( maxLength == -1  ||  size <= maxLength ) )
				{
					int beginEnd = 0;
					if ( begin != null )
					{
						beginEnd = begin.length;
						for (int i = 0; i < beginEnd; i++)
						{
							if ( !begin[i].test( xs.get( i ), bindings ) )
							{
								return false;
							}
						}
					}

					int endStart;
					if ( end != null )
					{
						endStart = size - end.length;
						for (int i = 0; i < end.length; i++)
						{
							if ( !end[i].test( xs.get( endStart + i ), bindings ) )
							{
								return false;
							}
						}
					}
					else
					{
						endStart = size;
					}
					
					
					return repeat.test( xs.subList( beginEnd, endStart ), bindings );			
				}
				else
				{
					return false;
				}
			}
			else
			{
				if ( size == begin.length )
				{
					for (int i = 0; i < size; i++)
					{
						if ( !begin[i].test( xs.get( i ), bindings ) )
						{
							return false;
						}
					}
					
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
	}



	private boolean compareObjects(Object x, Object y)
	{
		if ( x != null  &&  y != null )
		{
			return x.equals( y );
		}
		else if ( x == null  &&  y == null )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean compareArrays(Object[] x, Object[] y)
	{
		if ( x != null  &&  y != null )
		{
			return Arrays.asList( x ).equals( Arrays.asList( y ) );
		}
		else if ( x == null  &&  y == null )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean equals(Object x)
	{
		if ( x instanceof ListPattern )
		{
			ListPattern l = (ListPattern)x;
			return compareObjects( repeat, l.repeat )  &&  compareArrays( begin, l.begin )  &&  compareArrays( end, l.end );
		}
		else
		{
			return false;
		}
	}
}
