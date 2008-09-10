//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.JythonInterface;

import org.python.core.PySlice;

public class JythonSlice
{
	public static class SetSliceWithStepLengthMismatchException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public static class SliceStepCannotBeZero extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	public static Object[] arrayGetSlice(Object[] in, PySlice slice)
	{
		// Based on Jython PyList source code
		int indices[] = slice.indicesEx( in.length );
		
		int start = indices[0];
		int stop = indices[1];
		int step = indices[2];
		
		return arrayGetSlice( in, start, stop, step );
	}
	
	
	public static Object[] arrayGetSlice(Object[] in, int start, int stop, int step)
	{
		if ( step == 0 )
		{
			throw new SliceStepCannotBeZero();
		}

		if ( step > 0  &&  stop < start )
		{
			stop = start;
		}
		
		int n = sliceLength( start, stop, step );
		
		Object[] out = new Object[n];
		if ( step == 1)
		{
			System.arraycopy( in, start, out, 0, stop - start );
			return out;
		}
		else
		{
			int j = 0;
			for (int i = start; j < n; i += step)
			{
				out[j] = in[i];
				j++;
			}
			
			return out;
		}
	}
	

	
	
	
	
	public static Object[] arraySetSlice(Object[] dest, PySlice slice, Object[] src)
	{
		// Based on Jython PyList source code
		int indices[] = slice.indicesEx( dest.length );
		
		int start = indices[0];
		int stop = indices[1];
		int step = indices[2];
		
		return arraySetSlice( dest, start, stop, step, src );
	}

	
	
	public static Object[] arraySetSlice(Object[] dest, int start, int stop, int step, Object[] src)
	{
		if ( step == 0 )
		{
			throw new SliceStepCannotBeZero();
		}

		if ( step > 0  &&  stop < start )
		{
			stop = start;
		}
		
		
		if ( step == 1 )
		{
			int size = start + src.length + dest.length - stop;
			Object[] result = new Object[size];
			System.arraycopy( dest, 0, result, 0, start );
			System.arraycopy( src, 0, result, start, src.length );
			System.arraycopy( dest, stop, result, start + src.length, dest.length - stop );
			return result;
		}
		else
		{
			int n = src.length;
			if ( n != sliceLength( start, stop, step ) )
			{
				throw new SetSliceWithStepLengthMismatchException();
			}
			
			Object[] result = new Object[dest.length];
			System.arraycopy( dest, 0, result, 0, dest.length );
			for (int i = 0, j = start; i < n; i++, j += step)
			{
				result[j] = src[i];
			}
			return result;
		}
	}
	
	
	    
	public static Object[] arrayDelSlice(Object[] dest, PySlice slice)
	{
		// Based on Jython PyList source code
		int indices[] = slice.indicesEx( dest.length );
		
		int start = indices[0];
		int stop = indices[1];
		int step = indices[2];
		
		return arrayDelSlice( dest, start, stop, step );
	}

	
	
	public static Object[] arrayDelSlice(Object[] dest, int start, int stop, int step)
	{
		if ( step == 0 )
		{
			throw new SliceStepCannotBeZero();
		}

		if ( step > 0  &&  stop < start )
		{
			stop = start;
		}
		
		
		if ( step == 1 )
		{
			int size = start + dest.length - stop;
			Object[] result = new Object[size];
			System.arraycopy( dest, 0, result, 0, start );
			System.arraycopy( dest, stop, result, start, dest.length - stop );
			return result;
		}
		else
		{
			int sliceLen = sliceLength( start, stop, step );
			int resultLen = dest.length - sliceLen;
			
			Object[] result = new Object[resultLen];
			
			if ( step < 0 )
			{
				int newStart = start + step * (sliceLen-1);
				int newStep = -step;
				int newStop = newStart + newStep * (sliceLen-1);
				start = newStart;
				stop = newStop;
				step = newStep;
			}
			
			for (int i = 0, j = 0, k = start; i < resultLen; i++, j++)
			{
				if ( j == k )
				{
					j++;
					k += step;
				}
				
				result[i] = dest[j]; 
			}
			return result;
		}
	}
	
	
	    
	public static final int sliceLength(int start, int stop, long step)
	{
		// Based on Jython PySequence source code
		long result;
		
		if ( step > 0 )
		{
			result = ((stop - start + step - 1) / step);
		}
		else
		{
			result = ((stop - start + step + 1) / step);
		}
		
		return (int)(result >= 0  ?  result  :  0);
	}
}
