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
		private Operation prev;
		private int length;
		
		
		public Operation(OpCode opcode, int aBegin, int aEnd, int bBegin, int bEnd)
		{
			this.opcode = opcode;
			this.aBegin = aBegin;
			this.aEnd = aEnd;
			this.bBegin = bBegin;
			this.bEnd = bEnd;
			prev = null;
			length = 0;
		}
		
		private Operation(OpCode opcode, int aBegin, int aEnd, int bBegin, int bEnd, Operation prev)
		{
			this.opcode = opcode;
			this.aBegin = aBegin;
			this.aEnd = aEnd;
			this.bBegin = bBegin;
			this.bEnd = bEnd;
			this.prev = prev;
			length = this.prev.length + 1;
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
		
		
		
		private static Operation insertChar(String a, String b, int i, int j, Operation prev)
		{
			if ( prev != null  &&  prev.opcode == OpCode.INSERT )
			{
				return new Operation( OpCode.INSERT, prev.aBegin, prev.aEnd, prev.bBegin, j, prev.prev );
			}
			else
			{
				return new Operation( OpCode.INSERT, i, i, j-1, j, prev.prev );
			}
		}

		private static Operation deleteChar(String a, String b, int i, int j, Operation prev)
		{
			if ( prev != null  &&  prev.opcode == OpCode.DELETE )
			{
				return new Operation( OpCode.DELETE, prev.aBegin, i, prev.bBegin, prev.bEnd, prev.prev );
			}
			else
			{
				return new Operation( OpCode.DELETE, i-1, i, j, j, prev.prev );
			}
		}

		private static Operation replaceChar(String a, String b, int i, int j, Operation prev)
		{
			OpCode opcode = a.charAt( i - 1 )  ==  b.charAt( j - 1 )  ?  OpCode.EQUAL  :  OpCode.REPLACE;
			if ( prev != null  &&  prev.opcode == opcode )
			{
				return new Operation( opcode, prev.aBegin, i, prev.bBegin, j, prev.prev );
			}
			else
			{
				return new Operation( opcode, i-1, i, j-1, j, prev.prev );
			}
		}
	}
	
	
	
	private static class Node
	{
		public int i, j;
		public int cost;
		public Node prev;
		
		
		public Node(int i, int j, int cost, Node prev)
		{
			this.i = i;
			this.j = j;
			this.cost = cost;
			this.prev = prev;
		}

		public Node(int i, int j, int cost)
		{
			this.i = i;
			this.j = j;
			this.cost = cost;
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
			Node prevRow[] = new Node[a.length()+1];
			Node curRow[] = new Node[a.length()+1];
			
			// Initialise first row
			int cost = 0;
			prevRow[0] = new Node( 0, 0, 0 );
			Node prevNode = prevRow[0];
			for (int i = 1; i <= a.length(); i++)
			{
				// Moving right through the graph; DELETE char from @a
				cost += deleteCost( a.charAt( i - 1 ) );
				Node node = new Node( i, 0, cost, prevNode );
				prevRow[i] = node;
				prevNode = node;
			}
			
			
			// For each subsequent row
			for (int j = 1; j <= b.length(); j++)
			{
				// Fill @cur row

				// Current row, first element:
				// Moving down through the graph; INSERT char from @b
				curRow[0] = new Node( 0, j, prevRow[0].cost + insertCost( b.charAt( j - 1 ) ), prevRow[0] );
				prevNode = curRow[0];
				// Current row, remaining elements
				for (int i = 1; i <= a.length(); i++)
				{
					// Moving right through the graph; DELETE char from @a
					int delCost = prevNode.cost + deleteCost( a.charAt( i - 1 ) );
					// Moving down through the graph; INSERT char from @b
					int insCost = prevRow[i].cost + insertCost( b.charAt( j - 1 ) );
					// Moving down-right through the graph; REPLACE char from @a with char from @b
					int replCost = prevRow[i-1].cost + replaceCost( a.charAt( i - 1 ), b.charAt( j - 1 ) );

					Node node;
					// Favour replace
					if ( replCost <= delCost  &&  replCost <= insCost )
					{
						node = new Node( i, j, replCost, prevRow[i-1] );
					}
					else
					{
						if ( insCost <= delCost )
						{
							node = new Node( i, j, insCost, prevRow[i] );
						}
						else
						{
							node = new Node( i, j, delCost, prevNode );
						}
					}
					
					curRow[i] = node;
					prevNode = node;
				}
				
				
				// Swap the rows over
				Node rowSwap[] = prevRow;
				prevRow = curRow;
				curRow = rowSwap;
			}
			
			
			// Now we have a node graph.
			// Get the final node.
			Node node = prevRow[a.length()];
			Operation currentOp = null;
			while ( node != null  &&  node.prev != null )
			{
				Operation.OpCode opcode;
				if ( node.i == node.prev.i )
				{
					// Moving down through the graph; INSERT char from @b
					opcode = Operation.OpCode.INSERT;
				}
				else if ( node.j == node.prev.j )
				{
					// Moving right through the graph; DELETE char from @a
					opcode = Operation.OpCode.DELETE;
				}
				else
				{
					// Moving down-right through the graph; REPLACE char from @a with char from @b
					if ( a.charAt( node.prev.i ) == b.charAt( node.prev.j ) )
					{
						// Characters are same; EQUAL
						opcode = Operation.OpCode.EQUAL;
					}
					else
					{
						opcode = Operation.OpCode.REPLACE;
					}
				}
				
				// Create the operation
				if ( currentOp != null  &&  currentOp.opcode == opcode )
				{
					// Extend the operation to cover this character
					currentOp.aBegin = node.prev.i;
					currentOp.bBegin = node.prev.j;
				}
				else
				{
					currentOp = new Operation( opcode, node.prev.i, node.i, node.prev.j, node.j );
					operations.add( currentOp );
				}

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
}
