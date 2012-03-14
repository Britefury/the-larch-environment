//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.util.IdentityHashMap;

import BritefuryJ.Util.RichString.RichStringBuilder;

public class SequentialRichStringVisitor extends AbstractRichStringVisitor
{
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

		
		protected static void addPrefix(ElementModification mod, RichStringBuilder builder, LSElement element)
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
						fn.addPrefixToRichString( builder, element );
					}
				}
			}
		}

		protected static void addSuffix(ElementModification mod, RichStringBuilder builder, LSElement element)
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
						fn.addSuffixToRichString( builder, element );
					}
				}
			}
		}
		
		
		protected static boolean addFixedValue(ElementModification mod, RichStringBuilder builder, LSElement element)
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

		protected static boolean addElementFunctionResult(ElementModification mod, RichStringBuilder builder, LSElement element)
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
		
		
		protected static boolean shouldVisitChildrenOfElement(ElementModification mod, LSElement element)
		{
			if ( mod != null )
			{
				int f = mod.flags & FLAG_FIXEDVALUE_MASK;
				
				if ( f == FIXEDVALUE_VALUE )
				{
					// We have a fixed value - no need to visit children
					return false;
				}
				else if ( f == FIXEDVALUE_IDENTITY )
				{
					// We leave the fixed value as it is
					if ( element.hasFixedValue() )
					{
						// We have a fixed value - no need to visit children
						return false;
					}
				}
				else if ( f == FIXEDVALUE_IGNORE )
				{
					// We are ignoring any fixed value - next test
				}
				else
				{
					throw new RuntimeException( "Invalid fixed value mode" );
				}
			}
			else
			{
				// No element modification
				if ( element.hasFixedValue() )
				{
					// We have a fixed value - no need to visit children
					return false;
				}
			}
			
			
			// Try value function
			if ( mod == null  ||  !mod.testFlag( FLAG_IGNORE_VALUE_FUNCTION ) )
			{
				// We have no modification
				// or
				// we are NOT ignoring any value function present
				ElementValueFunction fn = element.getValueFunction();  
				if ( fn != null )
				{
					// We have a function to compute the element value - no need to visit children
					return false;
				}
			}
			
			
			// We need to visit child elements
			return true;
		}
	}
	
	
	protected IdentityHashMap<LSElement, ElementModification> modifications;
	
	
	
	
	
	
	public SequentialRichStringVisitor()
	{
	}
	
	
	//
	// Element modificaiton methods
	//
	
	public void setElementPrefix(LSElement element, Object prefix)
	{
		addElementModification( element ).setPrefix( prefix );
	}
	
	public void clearElementPrefix(LSElement element)
	{
		addElementModification( element ).clearPrefix();
	}
	
	
	public void setElementSuffix(LSElement element, Object suffix)
	{
		addElementModification( element ).setSuffix( suffix );
	}
	
	public void clearElementSuffix(LSElement element)
	{
		addElementModification( element ).clearSuffix();
	}
	
	
	public void enableIgnoreElementValueFunction(LSElement element)
	{
		addElementModification( element ).enableIgnoreValueFunction();
	}
	
	public void disableIgnoreElementValueFunction(LSElement element)
	{
		addElementModification( element ).disableIgnoreValueFunction();
	}
	
	
	public void preserveElementFixedValue(LSElement element)
	{
		addElementModification( element ).preserveFixedValue();
	}
	
	public void ignoreElementFixedValue(LSElement element)
	{
		addElementModification( element ).ignoreFixedValue();
	}
	
	public void ignoreElementFixedValuesOnPathUpTo(LSElement element, LSElement rootElement)
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
	
	public void ignoreElementFixedValuesOnPath(LSElement element, LSElement rootElement)
	{
		ignoreElementFixedValuesOnPathUpTo( element, rootElement );
		ignoreElementFixedValue( rootElement );
	}
	
	public void setElementFixedValue(LSElement element, Object value)
	{
		addElementModification( element ).setFixedValue( value );
	}

	
	
	//
	// Acquiring element modifications
	//
	
	private ElementModification getElementModification(LSElement element)
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
	
	private ElementModification addElementModification(LSElement element)
	{
		if ( modifications == null )
		{
			modifications = new IdentityHashMap<LSElement, ElementModification>();
		}
		
		ElementModification mod = modifications.get( element );
		if ( mod == null )
		{
			mod = new ElementModification();
			modifications.put( element, mod );
		}
		
		return mod;
	}




	@Override
	protected void preOrderVisitElement(RichStringBuilder builder, LSElement e)
	{
		ElementModification mod = getElementModification( e );
		ElementModification.addPrefix( mod, builder, e );
	}


	@Override
	protected void postOrderVisitElement(RichStringBuilder builder, LSElement e)
	{
		ElementModification mod = getElementModification( e );
		ElementModification.addSuffix( mod, builder, e );
	}
	

	@Override
	protected void inOrderVisitElement(RichStringBuilder builder, LSElement e)
	{
		ElementModification mod = getElementModification( e );
		if ( !ElementModification.addFixedValue( mod, builder, e ) )
		{
			if ( !ElementModification.addElementFunctionResult( mod, builder, e ) )
			{
				e.addToRichString( builder );
			}
		}
	}

	protected void inOrderVisitPartialContentLeafEditable(RichStringBuilder builder, LSContentLeafEditable e, int startIndex, int endIndex)
	{
		builder.appendTextValue( e.getTextRepresentation().substring( startIndex, endIndex ) );
	}
	
	public boolean shouldVisitChildrenOfElement(LSElement e, boolean completeVisit)
	{
		if ( completeVisit )
		{
			ElementModification mod = getElementModification( e );
			return ElementModification.shouldVisitChildrenOfElement( mod, e );
		}
		else
		{
			return true;
		}
	}
}
