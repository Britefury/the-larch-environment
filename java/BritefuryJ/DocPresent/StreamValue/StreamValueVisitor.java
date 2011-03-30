//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StreamValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPContentLeafEditable;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementValueFunction;
import BritefuryJ.DocPresent.Marker.Marker;
import BritefuryJ.DocPresent.Selection.TextSelection;

public class StreamValueVisitor
{
	protected static class State
	{
		protected StreamValueBuilder builder;
		protected Stack<DPElement> elementStack = new Stack<DPElement>();
		

		protected State(DPElement root, StreamValueBuilder builder)
		{
			elementStack.push( root );
			this.builder = builder;
		}
	}
	
	
	protected static class ElementModification
	{
		private static int FLAG_PREFIX = 0x1;
		private static int FLAG_SUFFIX = 0x2;
		private static int FLAG_IGNORE_VALUE_FUNCTION = 0x4;
		private static int FLAG_FIXEDVALUE_MASK = 0x18;
		private static int FIXEDVALUE_IDENTITY = 0x0;
		private static int FIXEDVALUE_IGNORE = 0x08;
		private static int FIXEDVALUE_VALUE = 0x10;
		
		
		private int flags = 0;
		
		private Object prefix = null, suffix = null, value = null;
		
		
		
		public ElementModification()
		{
		}
		
		
		public void setPrefix(Object prefix)
		{
			setFlag( FLAG_PREFIX );
			this.prefix = prefix; 
		}
		
		public void clearPrefix()
		{
			clearFlag( FLAG_PREFIX );
			this.prefix = null; 
		}
		
		
		public void setSuffix(Object suffix)
		{
			setFlag( FLAG_SUFFIX );
			this.suffix = suffix; 
		}
		
		public void clearSuffix()
		{
			clearFlag( FLAG_SUFFIX );
			this.suffix = null; 
		}
		
		
		public void enableIgnoreValueFunction()
		{
			setFlag( FLAG_IGNORE_VALUE_FUNCTION );
		}
		
		public void disableIgnoreValueFunction()
		{
			clearFlag( FLAG_IGNORE_VALUE_FUNCTION );
		}
		
		
		public void preserveFixedValue()
		{
			setFlagField( FLAG_FIXEDVALUE_MASK, FIXEDVALUE_IDENTITY );
			this.value = null;
		}
		
		public void ignoreFixedValue()
		{
			setFlagField( FLAG_FIXEDVALUE_MASK, FIXEDVALUE_IGNORE );
			this.value = null;
		}
		
		public void setFixedValue(Object value)
		{
			setFlagField( FLAG_FIXEDVALUE_MASK, FIXEDVALUE_VALUE );
			this.value = value;
		}
		
		
		
		protected void clearFlag(int flag)
		{
			flags &= ~flag;
		}
		
		protected void setFlag(int flag)
		{
			flags |= flag;
		}
		
		protected void setFlagField(int mask, int value)
		{
			flags = ( flags & ~mask )  |  value;
		}
		
		protected boolean testFlag(int flag)
		{
			return ( flags & flag )  !=  0;
		}

		
		protected static void addPrefix(ElementModification mod, StreamValueBuilder builder, DPElement element)
		{
			if ( mod != null  &&  mod.testFlag( FLAG_PREFIX ) )
			{
				builder.append( mod.prefix );
			}
			else
			{
				if ( !( mod != null  &&  mod.testFlag( FLAG_IGNORE_VALUE_FUNCTION ) ) )
				{
					ElementValueFunction fn = element.getValueFunction();  
					if ( fn != null )
					{
						fn.addStreamValuePrefixToStream( builder, element );
					}
				}
			}
		}

		protected static void addSuffix(ElementModification mod, StreamValueBuilder builder, DPElement element)
		{
			if ( mod != null  &&  mod.testFlag( FLAG_SUFFIX ) )
			{
				builder.append( mod.suffix );
			}
			else
			{
				if ( !( mod != null  &&  mod.testFlag( FLAG_IGNORE_VALUE_FUNCTION ) ) )
				{
					ElementValueFunction fn = element.getValueFunction();  
					if ( fn != null )
					{
						fn.addStreamValueSuffixToStream( builder, element );
					}
				}
			}
		}
		
		
		protected static boolean addFixedValue(ElementModification mod, StreamValueBuilder builder, DPElement element)
		{
			if ( mod != null )
			{
				int f = mod.flags & FLAG_FIXEDVALUE_MASK;
				
				if ( f == FIXEDVALUE_VALUE )
				{
					builder.append( mod.value );
					return true;
				}
				else if ( f == FIXEDVALUE_IGNORE )
				{
					return false;
				}
				else if ( f == FIXEDVALUE_IDENTITY )
				{
					if ( element.hasFixedValue() )
					{
						builder.append( element.getFixedValue() );
						return true;
					}
					else
					{
						return false;
					}
				}
				else
				{
					throw new RuntimeException( "Invalid fixed value mode" );
				}
			}
			else
			{
				if ( element.hasFixedValue() )
				{
					builder.append( element.getFixedValue() );
					return true;
				}
				else
				{
					return false;
				}
			}
		}

		protected static boolean addElementFunctionResult(ElementModification mod, StreamValueBuilder builder, DPElement element)
		{
			if ( mod != null  &&  mod.testFlag( FLAG_IGNORE_VALUE_FUNCTION ) )
			{
				return false;
			}
			else
			{
				ElementValueFunction fn = element.getValueFunction();  
				if ( fn != null )
				{
					builder.append( fn.computeElementValue( element ) );
					return true;
				}
				else
				{
					return false;
				}
			}
		}
	}
	
	
	protected HashMap<DPElement, ElementModification> modifications;
	
	
	
	
	
	
	public StreamValueVisitor()
	{
	}
	
	
	//
	// Element modificaiton methods
	//
	
	public void setElementPrefix(DPElement element, Object prefix)
	{
		addElementModification( element ).setPrefix( prefix );
	}
	
	public void clearElementPrefix(DPElement element)
	{
		addElementModification( element ).clearPrefix();
	}
	
	
	public void setElementSuffix(DPElement element, Object suffix)
	{
		addElementModification( element ).setSuffix( suffix );
	}
	
	public void clearElementSuffix(DPElement element)
	{
		addElementModification( element ).clearSuffix();
	}
	
	
	public void enableIgnoreElementValueFunction(DPElement element)
	{
		addElementModification( element ).enableIgnoreValueFunction();
	}
	
	public void disableIgnoreElementValueFunction(DPElement element)
	{
		addElementModification( element ).disableIgnoreValueFunction();
	}
	
	
	public void preserveElementFixedValue(DPElement element)
	{
		addElementModification( element ).preserveFixedValue();
	}
	
	public void ignoreElementFixedValue(DPElement element)
	{
		addElementModification( element ).ignoreFixedValue();
	}
	
	public void ignoreElementFixedValuesOnPathUpTo(DPElement element, DPElement rootElement)
	{
		if ( !element.isInSubtreeRootedAt( rootElement ) )
		{
			throw new RuntimeException( "@element is not in sub-tree rooted at @rootElement" );
		}
		
		while ( element != rootElement )
		{
			ignoreElementFixedValue( element );
			element = element.getParent();
		}
	}
	
	public void ignoreElementFixedValuesOnPath(DPElement element, DPElement rootElement)
	{
		ignoreElementFixedValuesOnPathUpTo( element, rootElement );
		ignoreElementFixedValue( rootElement );
	}
	
	public void setElementFixedValue(DPElement element, Object value)
	{
		addElementModification( element ).setFixedValue( value );
	}

	
	
	//
	// Stream value building
	//
	
	public StreamValue getStreamValue(DPElement root)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		buildStreamValue( root, builder );
		return builder.stream();
	}
	
	public StreamValue getStreamValueFromStartToMarker(DPElement root, Marker marker)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		DPContentLeafEditable leaf = marker.getElement();
		buildStreamValueFromStartOfRootToMarker( builder, leaf, marker, root );
		return builder.stream();
	}
	
	public StreamValue getStreamValueFromMarkerToEnd(DPElement root, Marker marker)
	{
		StreamValueBuilder builder = new StreamValueBuilder();
		DPContentLeafEditable leaf = marker.getElement();
		buildStreamValueFromMarkerToEndOfRoot( builder, leaf, marker, root );
		return builder.stream();
	}
	
	public StreamValue getStreamValueInTextSelection(TextSelection s)
	{
		DPContainer commonRoot = s.getCommonRootContainer();
		
		if ( commonRoot != null )
		{
			ArrayList<DPElement> startPath = s.getStartPathFromCommonRoot();
			ArrayList<DPElement> endPath = s.getEndPathFromCommonRoot();

			StreamValueBuilder builder = new StreamValueBuilder();

			buildStreamValueBetweenPaths( builder, commonRoot, s.getStartMarker(), startPath, s.getEndMarker(), endPath );
		
			return builder.stream();
		}
		else
		{
			Marker startMarker = s.getStartMarker();
			Marker endMarker = s.getEndMarker();
			DPContentLeafEditable leaf = startMarker.getElement();
			if ( endMarker.getElement() != leaf )
			{
				throw new RuntimeException( "No common root, but leaf elements do not match" );
			}
			
			StreamValueBuilder builder = new StreamValueBuilder();
			builder.appendTextValue( leaf.getTextRepresentation().substring( startMarker.getClampedIndex(), endMarker.getClampedIndex() ) );
			return builder.stream();
		}
	}
	

	
	protected void buildStreamValueFromStartOfRootToMarker(StreamValueBuilder builder, DPContentLeafEditable leaf, Marker marker, DPElement root)
	{
		DPContainer parent = leaf.getParent();
		if ( leaf != root  &&  parent != null )
		{
			buildStreamValueFromStartOfRootToMarkerFromChild( builder, parent, marker, root, leaf );
		}
		builder.appendTextValue( leaf.getTextRepresentation().substring( 0, marker.getClampedIndex() ) );
	}
	
	protected void buildStreamValueFromMarkerToEndOfRoot(StreamValueBuilder builder, DPContentLeafEditable leaf, Marker marker, DPElement root)
	{
		builder.appendTextValue( leaf.getTextRepresentation().substring( marker.getClampedIndex() ) );
		DPContainer parent = leaf.getParent();
		if ( leaf != root  &&  parent != null )
		{
			buildStreamValueFromMarkerToEndOfRootFromChild( builder, parent, marker, root, leaf );
		}
	}

	
	
	protected void buildStreamValueFromStartOfRootToMarkerFromChild(StreamValueBuilder builder, DPContainer subtree, Marker marker, DPElement root, DPElement fromChild)
	{
		DPContainer parent = subtree.getParent();
		if ( root != subtree  &&  parent != null )
		{
			buildStreamValueFromStartOfRootToMarkerFromChild( builder, parent, marker, root, subtree );
		}
		
		ElementModification mod = getElementModification( subtree );
		ElementModification.addPrefix( mod, builder, subtree );

		for (DPElement child: subtree.getStreamValueChildren())
		{
			if ( child != fromChild )
			{
				buildStreamValue( child, builder );
			}
			else
			{
				break;
			}
		}
	}
	
	protected void buildStreamValueFromMarkerToEndOfRootFromChild(StreamValueBuilder builder, DPContainer subtree, Marker marker, DPElement root, DPElement fromChild)
	{
		List<DPElement> children = subtree.getStreamValueChildren();
		int childIndex = children.indexOf( fromChild );
		
		if ( (childIndex + 1) < children.size() )
		{
			for (DPElement child: children.subList( childIndex + 1, children.size() ))
			{
				buildStreamValue( child, builder );
			}
		}

		ElementModification mod = getElementModification( subtree );
		ElementModification.addSuffix( mod, builder, subtree );

		DPContainer parent = subtree.getParent();
		if ( root != subtree  &&  parent != null )
		{
			buildStreamValueFromMarkerToEndOfRootFromChild( builder, parent, marker, root, subtree );
		}
	}
	
	
	protected void buildStreamValueBetweenPaths(StreamValueBuilder builder, DPContainer commonRoot, Marker startMarker, ArrayList<DPElement> startPath,
			Marker endMarker, ArrayList<DPElement> endPath)
	{
		List<DPElement> children = commonRoot.getStreamValueChildren();
		
	
		DPElement startChild = startPath.get( 1 );
		DPElement endChild = endPath.get( 1 );
		
		int startIndex = children.indexOf( startChild );
		int endIndex = children.indexOf( endChild );
	
	
		buildStreamValueFromMarkerToEndOfRoot( builder, startMarker.getElement(), startMarker, startChild );
		
		for (int i = startIndex + 1; i < endIndex; i++)
		{
			buildStreamValue( children.get( i ), builder );
		}

		buildStreamValueFromStartOfRootToMarker( builder, endMarker.getElement(), endMarker, endChild );
	}

	
	protected void buildStreamValue(DPElement root, StreamValueBuilder builder)
	{
		State state = new State( root, builder );
		while ( !state.elementStack.isEmpty() )
		{
			DPElement element = state.elementStack.pop();
			visit( state, element );
		}
	}
	
	protected void visit(State state, DPElement element)
	{
		ElementModification mod = getElementModification( element );
		
		ElementModification.addPrefix( mod, state.builder, element );
		
		if ( ElementModification.addFixedValue( mod, state.builder, element ) )
		{
			// Fixed value added
		}
		else
		{
			if ( ElementModification.addElementFunctionResult( mod, state.builder, element ) )
			{
				// Result of evaluating element function added
			}
			else
			{
				element.addToStreamValue( state.builder );
				List<DPElement> children = element.getStreamValueChildren();
				if ( children.size() > 0 )
				{
					state.elementStack.addAll( children );
					Collections.reverse( state.elementStack.subList( state.elementStack.size() - children.size(), state.elementStack.size() ) );
				}
			}
		}
	
		ElementModification.addSuffix( mod, state.builder, element );
	}
	
	
	
	
	//
	// Acquiring element modifications
	//
	
	private ElementModification getElementModification(DPElement element)
	{
		if ( modifications != null )
		{
			return modifications.get( element );
		}
		else
		{
			return null;
		}
	}
	
	private ElementModification addElementModification(DPElement element)
	{
		if ( modifications == null )
		{
			modifications = new HashMap<DPElement, ElementModification>();
		}
		
		ElementModification mod = modifications.get( element );
		if ( mod == null )
		{
			mod = new ElementModification();
			modifications.put( element, mod );
		}
		
		return mod;
	}
}
