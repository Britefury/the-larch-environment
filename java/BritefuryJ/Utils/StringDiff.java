//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Utils;

import java.util.ArrayList;

public class StringDiff
{
	public static class Operation
	{
		public enum OpCode
		{
			INSERT,
			DELETE,
			REPLACE,
			EQUAL
		}
		
		
		public OpCode opcode;
		public int aBegin, aEnd, bBegin, bEnd;
		
		
		public Operation(OpCode opcode, int aBegin, int aEnd, int bBegin, int bEnd)
		{
			this.opcode = opcode;
			this.aBegin = aBegin;
			this.aEnd = aEnd;
			this.bBegin = bBegin;
			this.bEnd = bEnd;
		}
		
		
		
		public boolean equals(Object x)
		{
			if ( x == this )
			{
				return true;
			}
			
			if ( x instanceof Operation )
			{
				Operation ox = (Operation)x;
				
				return opcode == ox.opcode  &&  aBegin == ox.aBegin  &&  aEnd == ox.aEnd  &&  bBegin == ox.bBegin  &&  bEnd == ox.bEnd;
			}
			else
			{
				return false;
			}
		}
		
		
		public String toString()
		{
			String range = "a[" + aBegin + ":" + aEnd + "] <-> b[" + bBegin + ":" + bEnd + "]";
			return opcode + "( " + range + " )";
		}

	
	
		public void offset(int offset)
		{
			aBegin += offset;
			aEnd += offset;
			bBegin += offset;
			bEnd += offset;
		}
	}
	
	
	
	private static class OperationLinked extends Operation
	{
		private OperationLinked prev;
		private int cost, length;
		
		
		
		private OperationLinked(OpCode opcode, int aBegin, int aEnd, int bBegin, int bEnd, OperationLinked prev, int cost)
		{
			super( opcode, aBegin, aEnd, bBegin, bEnd );
			this.prev = prev;
			this.cost = cost;
			if ( prev != null )
			{
				length = prev.length + 1;
			}
			else
			{
				length = 1;
			}
		}
		
		
		
		private Operation operation()
		{
			return new Operation( opcode, aBegin, aEnd, bBegin, bEnd );
		}
		
		
		
		private static OperationLinked insertChar(String a, String b, int i, int j, int opCost, OperationLinked prev)
		{
			int cost = prev != null  ?  prev.cost + opCost  :  opCost;
			if ( prev != null  &&  prev.opcode == OpCode.INSERT )
			{
				return new OperationLinked( OpCode.INSERT, prev.aBegin, prev.aEnd, prev.bBegin, j, prev.prev, cost );
			}
			else
			{
				return new OperationLinked( OpCode.INSERT, i, i, j-1, j, prev, cost );
			}
		}

		private static OperationLinked deleteChar(String a, String b, int i, int j, int opCost, OperationLinked prev)
		{
			int cost = prev != null  ?  prev.cost + opCost  :  opCost;
			if ( prev != null  &&  prev.opcode == OpCode.DELETE )
			{
				return new OperationLinked( OpCode.DELETE, prev.aBegin, i, prev.bBegin, prev.bEnd, prev.prev, cost );
			}
			else
			{
				return new OperationLinked( OpCode.DELETE, i-1, i, j, j, prev, cost );
			}
		}

		private static OperationLinked replaceChar(String a, String b, int i, int j, int opCost, OperationLinked prev)
		{
			int cost = prev != null  ?  prev.cost + opCost  :  opCost;
			OpCode opcode = a.charAt( i - 1 )  ==  b.charAt( j - 1 )  ?  OpCode.EQUAL  :  OpCode.REPLACE;
			if ( prev != null  &&  prev.opcode == opcode )
			{
				return new OperationLinked( opcode, prev.aBegin, i, prev.bBegin, j, prev.prev, cost );
			}
			else
			{
				return new OperationLinked( opcode, i-1, i, j-1, j, prev, cost );
			}
		}
	
	
		public String toString()
		{
			String opString = super.toString();
			String prevString = prev != null  ?  ", " + prev.toString()  :  "";
			return opString + prevString;
		}
	}
	
	
	
	private static boolean isJunk(char c)
	{
		return c == ' '  ||  c == '\t';
	}
	
	private static int insertCost(char c)
	{
		return isJunk( c )  ?  0  :  1;
	}
	
	private static int deleteCost(char c)
	{
		return isJunk( c )  ?  0  :  1;
	}
	
	private static int replaceCost(char a, char b)
	{
		if ( isJunk( a ) )
		{
			return isJunk( b )  ?  0  :  1;
		}
		else
		{
			if ( isJunk( b ) )
			{
				return 1;
			}
			else
			{
				return a == b  ?  0  :  1;
			}
		}
	}
	
	
	
	
	public static ArrayList<Operation> levenshteinDiff(String a, String b)
	{
		ArrayList<Operation> operations = new ArrayList<Operation>();

		if ( a.length() > 0  &&  b.length() > 0 )
		{
			OperationLinked prevRow[] = new OperationLinked[a.length()+1];
			OperationLinked curRow[] = new OperationLinked[a.length()+1];
			
			// Initialise first row
			int cost = 0;
			prevRow[0] = null;
			OperationLinked prevNode = prevRow[0];
			for (int i = 1; i <= a.length(); i++)
			{
				// Moving right through the graph; consuming chars from @a, not consuming chars from @b; DELETE chars from @a
				cost += deleteCost( a.charAt( i - 1 ) );
				OperationLinked node = OperationLinked.deleteChar( a, b, i, 0, deleteCost( a.charAt( i - 1 ) ), prevNode );
				prevRow[i] = node;
				prevNode = node;
			}
			
			// For each subsequent row
			for (int j = 1; j <= b.length(); j++)
			{
				// Fill @cur row

				// Current row, first element:
				// Moving down through the graph; not consuming char from @a, consuming char from @b; INSERT char from @b
				curRow[0] = OperationLinked.insertChar( a, b, 0, j, insertCost( b.charAt( j - 1 ) ), prevRow[0] );
				prevNode = curRow[0];
				// Current row, remaining elements
				for (int i = 1; i <= a.length(); i++)
				{
					// Moving right through the graph; consume char from @a, don't consume char from @b; DELETE char from @a
					OperationLinked delOp = OperationLinked.deleteChar( a, b, i, j, deleteCost( a.charAt( i - 1 ) ), prevNode );
					// Moving down through the graph; don't consume char from @a, consume char from @b; INSERT char from @b
					OperationLinked insOp = OperationLinked.insertChar( a, b, i, j, insertCost( b.charAt( j - 1 ) ), prevRow[i] );
					// Moving down-right through the graph; consume char from @a, consume char from @b; REPLACE char from @a with char from @b (chars may be equal)
					OperationLinked replOp = OperationLinked.replaceChar( a, b, i, j, replaceCost( a.charAt( i - 1 ), b.charAt( j - 1 ) ), prevRow[i-1] );

					// Determine the best operation to use
					// Start with delete
					OperationLinked op = delOp;
					
					// Switch to insert if its better
					if ( insOp.cost < op.cost  ||  ( insOp.cost == op.cost  &&  insOp.length <= op.length  ) )
					{
						op = insOp;
					}

					// Switch to replace if its better
					if ( replOp.cost < op.cost  ||  ( replOp.cost == op.cost  &&  replOp.length <= op.length  ) )
					{
						op = replOp;
					}
					
					curRow[i] = op;
					prevNode = op;
				}
				
				// Swap the rows over
				OperationLinked rowSwap[] = prevRow;
				prevRow = curRow;
				curRow = rowSwap;
			}
			
			
			// Now we have a node graph.
			// Get the final node.
			OperationLinked node = prevRow[a.length()];
			while ( node != null )
			{
				operations.add( node.operation() );
				node = node.prev;
			}
		}
		else if ( a.length() > 0  &&  b.length() == 0 )
		{
			// Delete all of @a
			operations.add( new Operation( Operation.OpCode.DELETE, 0, a.length(), 0, 0 ) );
		}
		else if ( a.length() == 0  &&  b.length() > 0 )
		{
			// Insert all of @b
			operations.add( new Operation( Operation.OpCode.INSERT, 0, 0, 0, b.length() ) );
		}
		else if ( a.length() == 0  &&  b.length() == 0 )
		{
			// Nothing to do
		}
		
		return operations;
	}
	
	
	
	
	public static int getCommonPrefixLength(String a, String b)
	{
		for (int i = 0; i < Math.min( a.length(), b.length() ); i++)
		{
			if ( a.charAt( i ) != b.charAt( i ) )
			{
				return i;
			}
		}
		
		return Math.min( a.length(), b.length() );
	}

	public static int getCommonSuffixLength(String a, String b)
	{
		int j = a.length() - 1;
		int k = b.length() - 1;
		for (int i = 0; i < Math.min( a.length(), b.length() ); i++)
		{
			if ( a.charAt( j ) != b.charAt( k ) )
			{
				return i;
			}
			
			j--;
			k--;
		}
		
		return Math.min( a.length(), b.length() );
	}
	
	
	
	
	public static ArrayList<Operation> diff(String a, String b)
	{
		ArrayList<Operation> operations = new ArrayList<Operation>();

		int prefixLen = getCommonPrefixLength( a, b );
		
		if ( prefixLen == a.length()  ||  prefixLen == b.length() )
		{
			// @a is a substring at the start of @b
			// OR
			// @b is a substring at the start of @a
			operations.add( new Operation( Operation.OpCode.EQUAL, 0, prefixLen, 0, prefixLen ) );
			if ( a.length() > b.length() )
			{
				// delete chars from end of @a
				operations.add( new Operation( Operation.OpCode.DELETE, prefixLen, a.length(), prefixLen, prefixLen ) );
			}
			else if ( b.length() > a.length() )
			{
				// insert chars from end of @b
				operations.add( new Operation( Operation.OpCode.INSERT, prefixLen, prefixLen, prefixLen, b.length() ) );
			}
			
			return operations;
		}
		
		
		int suffixLen = getCommonSuffixLength( a, b );

		if ( suffixLen == a.length()  ||  suffixLen == b.length() )
		{
			// @a is a substring at the end of @b
			// OR
			// @b is a substring at the end of @a
			int aOffset = 0, bOffset = 0;
			if ( a.length() > b.length() )
			{
				// delete chars from start of @a
				operations.add( new Operation( Operation.OpCode.DELETE, 0, a.length() - suffixLen, 0, 0 ) );
				aOffset = a.length() - suffixLen;
			}
			else if ( b.length() > a.length() )
			{
				// insert chars from start of @b
				operations.add( new Operation( Operation.OpCode.INSERT, 0, 0, 0, b.length() - suffixLen ) );
				bOffset = b.length() - suffixLen;
			}
			operations.add( new Operation( Operation.OpCode.EQUAL, aOffset, a.length(), bOffset, b.length() ) );

			return operations;
		}
		
		
		// Add an extra character to each end to give the differencing algorithm a little context to work with.
		prefixLen = Math.max( prefixLen - 1, 0 );
		suffixLen = Math.max( suffixLen - 1, 0 );
		
		
		if ( prefixLen > 0 )
		{
			operations.add( new Operation( Operation.OpCode.EQUAL, 0, prefixLen, 0, prefixLen ) );
		}
		
		
		// Use Levenshtein algorithm for diff
		ArrayList<Operation> levenshteinOps = levenshteinDiff( a.substring( prefixLen, a.length() - suffixLen ), b.substring( prefixLen, b.length() - suffixLen ) );
		for (Operation op: levenshteinOps)
		{
			op.offset( prefixLen );
		}
		operations.addAll( levenshteinOps );

		if ( suffixLen > 0 )
		{
			operations.add( new Operation( Operation.OpCode.EQUAL, a.length() - suffixLen, a.length(), b.length() - suffixLen, b.length() ) );
		}
		
		return operations;
	}
}
