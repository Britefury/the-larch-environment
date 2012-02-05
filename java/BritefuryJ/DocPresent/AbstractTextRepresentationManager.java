//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

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
			return !completeVisit  ||  !cache.containsKey( e );
		}
	}
	
	
	
	protected static class FoundException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	protected class FindLeafAtPositionVisitor extends ElementTreeVisitor
	{
		private int queryPosition;
		private DPContentLeaf leaf = null;

		protected int position = 0;
		
		
		
		
		public FindLeafAtPositionVisitor(int queryPosition)
		{
			this.queryPosition = queryPosition;
		}
		

		public DPContentLeaf getLeaf()
		{
			return leaf;
		}
		
	
		
		//
		// VISITOR METHODS
		//
		
		private void append(int length)
		{
			if ( length != -1 )
			{
				position += length;
			}
		}
		
		@Override
		protected void preOrderVisitElement(DPElement e, boolean complete)
		{
			append( getElementPrefixLength( e, complete ) );
		}

		@Override
		protected void inOrderCompletelyVisitElement(DPElement e)
		{
		}

		@Override
		protected void postOrderVisitElement(DPElement e, boolean complete)
		{
			if ( complete )
			{
				// Perform in-order visit as a first step of the post-order, otherwise we muck up the position,
				// preventing shouldVisitChildrenOfElement() from working properly
				int length = getElementContentLength( e );
				if ( length != -1 )
				{
					position += length;
				}
				else
				{
					String value = cache.get( e );
					if ( value != null )
					{
						// If the query position is within the bounds of the cached value,
						// we must visit the children, since the element we are looking for resides
						// within the subtree rooted at @e
						// Otherwise we can hop over them.
						length = value.length();
						int end = position + length;
						if ( queryPosition > end )
						{
							position = end;
						}
					}
				}
			}
			
			
			
			append( getElementSuffixLength( e, complete ) );
			if ( queryPosition < position  &&  e instanceof DPContentLeaf )
			{
				leaf = (DPContentLeaf)e;
				throw new FoundException();
			}
		}

		@Override
		protected void inOrderVisitPartialContentLeafEditable(DPContentLeafEditable e, int startIndex, int endIndex)
		{
			append( getPartialEditableLeafContentLength( e, startIndex, endIndex ) );
		}

		@Override
		protected boolean shouldVisitChildrenOfElement(DPElement e, boolean completeVisit)
		{
			if ( !completeVisit )
			{
				return true;
			}
			else
			{
				String value = cache.get( e );
				if ( value != null )
				{
					// If the query position is within the bounds of the cached value,
					// we must visit the children, since the element we are looking for resides
					// within the subtree rooted at @e
					// Otherwise we can hop over them.
					int end = position + value.length();
					return queryPosition <= end;
				}
				else
				{
					// No cached value - must visit children
					return true;
				}
			}
		}

		@Override
		public void visitTextSelection(TextSelection s)
		{
			try
			{
				super.visitTextSelection( s );
			}
			catch (FoundException e)
			{
			}
		}

		@Override
		public void visitFromStartOfRootToMarker(Marker marker, DPElement root)
		{
			try
			{
				super.visitFromStartOfRootToMarker( marker, root );
			}
			catch (FoundException e)
			{
			}
		}

		@Override
		public void visitFromMarkerToEndOfRoot(Marker marker, DPElement root)
		{
			try
			{
				super.visitFromMarkerToEndOfRoot( marker, root );
			}
			catch (FoundException e)
			{
			}
		}

		@Override
		public void visitSubtree(DPElement root)
		{
			try
			{
				super.visitSubtree( root );
			}
			catch (FoundException e)
			{
			}
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
	
	
	
	
	public String getTextRepresentationOf(DPElement subtreeRoot)
	{
		BuilderVisitor visitor = new BuilderVisitor();
		visitor.visitSubtree( subtreeRoot );
		return visitor.getValue();
	}

	public String getTextRepresentationFromStartToMarker(Marker marker, DPElement subtreeRoot)
	{
		BuilderVisitor visitor = new BuilderVisitor();
		visitor.visitFromStartOfRootToMarker( marker, subtreeRoot );
		return visitor.getValue();
	}

	public String getTextRepresentationFromMarkerToEnd(Marker marker, DPElement subtreeRoot)
	{
		BuilderVisitor visitor = new BuilderVisitor();
		visitor.visitFromMarkerToEndOfRoot( marker, subtreeRoot );
		return visitor.getValue();
	}

	public String getTextRepresentationInTextSelection(TextSelection selection)
	{
		BuilderVisitor visitor = new BuilderVisitor();
		visitor.visitTextSelection( selection );
		return visitor.getValue();
	}
	
	
	
	public DPContentLeaf getLeafAtPositionInSubtree(DPElement subtreeRoot, int position)
	{
		FindLeafAtPositionVisitor visitor = new FindLeafAtPositionVisitor( position );
		visitor.visitSubtree( subtreeRoot );
		DPContentLeaf leaf = visitor.getLeaf( );
		if ( leaf == null )
		{
			System.out.println( "getLeafAtPositionInSubtree: NULL, position=" + position + ", length=" + getTextRepresentationOf( subtreeRoot ).length() );
		}
		return leaf;
	}
	
	public int getPositionOfElementInSubtree(DPContainer subtreeRoot, DPElement e)
	{
		int position = 0;
		
		while ( e != subtreeRoot )
		{
			DPContainer parent = e.getParent();
			
			if ( parent == null )
			{
				throw new DPElement.IsNotInSubtreeException();
			}
			
			List<DPElement> children = parent.getChildrenInSequentialOrder();
			int index = children.indexOf( e );
			
			if ( index == -1 )
			{
				throw new RuntimeException( "ERROR (should not happen): Could not find @e in list of children" );
			}
			
			for (int i = index - 1; i >= 0; i--)
			{
				position += getTextRepresentationOf( children.get( i ) ).length();
			}
			
			e = parent;
		}
		
		return position;
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
	
	
	
	protected int getElementPrefixLength(DPElement e, boolean complete)
	{
		String prefix = getElementPrefix( e, complete );
		return prefix != null  ?  prefix.length()  :  -1;
	}

	protected int getElementSuffixLength(DPElement e, boolean complete)
	{
		String prefix = getElementSuffix( e, complete );
		return prefix != null  ?  prefix.length()  :  -1;
	}

	protected int getElementContentLength(DPElement e)
	{
		String prefix = getElementContent( e );
		return prefix != null  ?  prefix.length()  :  -1;
	}
	
	protected int getPartialEditableLeafContentLength(DPContentLeafEditable e, int startIndex, int endIndex)
	{
		return endIndex - startIndex;
	}
}