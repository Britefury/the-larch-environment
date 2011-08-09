//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;

public abstract class AbstractTextRepresentationManager
{
	protected class BuilderVisitor extends ElementTreeVisitor
	{
		private StringBuilder builder = new StringBuilder();
		private int stack[] = new int[4*2];
		private int stackIndex = 0;
		private int elementCount = 0;
		private int cachedElements = 0;
		
		
	
		
		public String getValue()
		{
			return builder.toString();
		}

		
		
		
		
		//
		// STACK METHODS
		//
		
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
		
		

		//
		// VISITOR METHODS
		//
		
		@Override
		protected void preOrderVisitElement(DPElement e, boolean complete)
		{
			if ( complete )
			{
				push( elementCount, builder.length() );
			}
			
			String prefix = getElementPrefix( e, complete );
			if ( prefix != null )
			{
				builder.append( prefix );
			}
		}

		@Override
		protected void inOrderCompletelyVisitElement(DPElement e)
		{
			String content = getElementContent( e );
			if ( content != null )
			{
				builder.append( content );
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
			String suffix = getElementSuffix( e, complete );
			if ( suffix != null )
			{
				builder.append( suffix );
			}

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
			String content = getPartialEditableLeafContent( e, startIndex, endIndex );
			if ( content != null )
			{
				builder.append( content );
			}
		}

		@Override
		protected boolean shouldVisitChildrenOfElement(DPElement e, boolean completeVisit)
		{
			return !cache.containsKey( e );
		}
	}
	
	
	
	protected ElementValueCache<String> cache;

	
	
	public AbstractTextRepresentationManager()
	{
		this( new ElementValueCache<String>() );
	}
	
	public AbstractTextRepresentationManager(ElementValueCache<String> cache)
	{
		super();

		this.cache = cache;
	}
	
	
	
	
	public String getTextRepresentationOf(DPElement root)
	{
		BuilderVisitor builder = new BuilderVisitor();
		builder.visitSubtree( root );
		return builder.getValue();
	}

	public String getTextRepresentationFromStartToMarker(Marker marker, DPElement root)
	{
		BuilderVisitor builder = new BuilderVisitor();
		builder.visitFromStartOfRootToMarker( marker, root );
		return builder.getValue();
	}

	public String getTextRepresentationFromMarkerToEnd(Marker marker, DPElement root)
	{
		BuilderVisitor builder = new BuilderVisitor();
		builder.visitFromMarkerToEndOfRoot( marker, root );
		return builder.getValue();
	}

	public String getTextRepresentationInTextSelection(TextSelection selection)
	{
		BuilderVisitor builder = new BuilderVisitor();
		builder.visitTextSelection( selection );
		return builder.getValue();
	}

	
	
	//
	// CONTENT HANDLING METHODS: OVERRIDE
	//
	
	protected String getElementPrefix(DPElement e, boolean complete)
	{
		return null;
	}

	protected String getElementSuffix(DPElement e, boolean complete)
	{
		return null;
	}

	protected abstract String getElementContent(DPElement e);		// Return true if element content added, else children will be visited
	
	protected final String getPartialEditableLeafContent(DPContentLeafEditable e, int startIndex, int endIndex)
	{
		return e.getLeafTextRepresentation().substring( startIndex, endIndex );
	}
}