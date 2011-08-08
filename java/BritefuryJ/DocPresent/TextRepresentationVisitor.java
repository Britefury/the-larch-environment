//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent;



class TextRepresentationVisitor extends ElementTreeVisitor
{
	private StringBuilder builder = new StringBuilder();
	private ElementValueCache<String> cache;
	private int stack[] = new int[4*2];
	private int stackIndex = 0;
	private int elementCount = 0, cachedElements = 0;
	
	
	
	protected TextRepresentationVisitor(ElementValueCache<String> cache)
	{
		this.cache = cache;
	}
	
	
	public String getValue()
	{
		return builder.toString();
	}
	
	
	private void push(int elementCount, int position)
	{
		if ( stackIndex*2 == stack.length )
		{
			int newStack[] = new int[stack.length * 2];
			System.arraycopy( stack, 0, newStack, 0, stack.length );
			stack = newStack;
		}
		stack[stackIndex*2+0] = elementCount;
		stack[stackIndex*2+1] = position;
		stackIndex += 2;
	}
	
	private void pop(int elementCountAndPos[])
	{
		stackIndex -= 2;
		elementCountAndPos[0] = stack[stackIndex*2+0];
		elementCountAndPos[1] = stack[stackIndex*2+1];
	}
	
	
	@Override
	protected void preOrderVisitElement(DPElement e, boolean complete)
	{
		if ( complete )
		{
			push( elementCount, builder.length() );
		}
	}

	@Override
	protected void inOrderCompletelyVisitElement(DPElement e)
	{
		String leafText = e.getLeafTextRepresentation();
		if ( leafText != null )
		{
			builder.append( leafText );
		}
		else
		{
			String value = cache.get( e );
			if ( value != null )
			{
				builder.append( value );
			}
		}
		elementCount++;
	}

	@Override
	protected void postOrderVisitElement(DPElement e, boolean complete)
	{
		if ( complete )
		{
			int elementCountAndPos[] = { 0, 0 };
			pop( elementCountAndPos );
			int de = elementCount - elementCountAndPos[0] - cachedElements;
			if ( de  >  128 )
			{
				String value = builder.substring( elementCountAndPos[1] );
				cache.put( e, value );
				cachedElements += de;
			}
		}
	}

	@Override
	protected void inOrderVisitPartialContentLeafEditable(DPContentLeafEditable e, int startIndex, int endIndex)
	{
		builder.append( e.getLeafTextRepresentation().substring( startIndex, endIndex ) );
	}

	@Override
	protected boolean shouldVisitChildrenOfElement(DPElement e, boolean completeVisit)
	{
		return !cache.containsKey( e );
	}
}
