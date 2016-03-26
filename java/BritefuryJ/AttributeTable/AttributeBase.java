//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.AttributeTable;

import java.awt.Color;
import java.util.Comparator;
import java.util.regex.Pattern;

import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBoxWithFields;
import BritefuryJ.Pres.ObjectPres.VerticalField;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public abstract class AttributeBase implements Presentable
{
	protected static Pattern validNamePattern = Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]*" );

	
	public static class AttributeAlreadyExistsException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public AttributeAlreadyExistsException(String message)
		{
			super( message );
		}
	}
	

	
	public static class InvalidAttributeNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public InvalidAttributeNameException(String message)
		{
			super( message );
		}
	}

	
	
	public static class AttributeNameComparator implements Comparator<AttributeBase>
	{
		public int compare(AttributeBase o1, AttributeBase o2)
		{
			return o1.getFullName().compareTo( o2.getFullName() );
		}
	}
	
	
	protected AttributeNamespace namespace;
	protected String name;
	protected Class<?> valueClass;
	protected Object defaultValue;
	
	
	public AttributeBase(AttributeNamespace namespace, String name, Object defaultValue)
	{
		this( namespace, name, null, defaultValue );
	}
	
	public AttributeBase(AttributeNamespace namespace, String name, Class<?> valueClass, Object defaultValue)
	{
		if ( !validNamePattern.matcher( name ).matches() )
		{
			throw new InvalidAttributeNameException( "Invalid attribute name '" + name + "'; name should be an identifier" );
		}
		this.namespace = namespace;
		this.name = name;
		this.valueClass = valueClass;
		this.defaultValue = defaultValue;
		this.namespace.registerAttribute( this );
	}
	
	
	
	public AttributeNamespace getNamespace()
	{
		return namespace;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getFullName()
	{
		return namespace.getName() + "." + name;
	}
	
	public Class<?> getValueClass()
	{
		return valueClass;
	}
	
	public Object getDefaultValue()
	{
		return defaultValue;
	}
	
	
	
	public AttributeWithValue __call__(Object value)
	{
		return new AttributeWithValue( this, value );
	}
	
	public AttributeWithValue as(Object value)
	{
		return new AttributeWithValue( this, value );
	}
	
	
	
	abstract protected Object checkValue(Object value);
	
	
	protected AttributeTable use(AttributeTable attributeTable)
	{
		return attributeTable;
	}



	protected void notifyBadAttributeType(Object value, Class<?> expectedType)
	{
		System.err.println( "WARNING: attrib table \"" + getClass().getName() + "\": attribute '" + getFullName() + "' should have value of type '" + expectedType.getName() + "', has value '" + value + "'; type '" + value.getClass().getName() + "'" );
	}

	protected void notifyAttributeShouldNotBeNull(Class<?> expectedType)
	{
		if ( expectedType == null )
		{
			notifyAttributeShouldNotBeNull();
		}
		else
		{
			System.err.println( "WARNING: attrib table \"" + getClass().getName() + "\": attribute '" + getFullName() + "' should not have a null value; type='" + expectedType.getName() + "'" );
		}
	}

	protected void notifyAttributeShouldNotBeNull()
	{
		System.err.println( "WARNING: attrib table \"" + getClass().getName() + "\": attribute '" + getFullName() + "' should not have a null value" );
	}
	
	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		initStyles();
		
		return new ObjectBoxWithFields( "Attribute", new Object[] {
				nameStyle.applyTo( new Label( getFullName() ) ),
				new VerticalField( "Default", defaultValue ) } );
	}


	
	public String toString()
	{
		return namespace.toString() + "." + name;
	}
	
	
	
	private static void initStyles()
	{
		if ( nameStyle == null )
		{
			nameStyle = StyleSheet.style( Primitive.fontBold.as( true ), Primitive.foreground.as( new Color( 0.0f, 0.25f, 0.25f ) ) );
		}
	}
	
	
	private static StyleSheet nameStyle = null;
}
