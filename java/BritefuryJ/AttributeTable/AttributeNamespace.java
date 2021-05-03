//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.AttributeTable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.python.core.Py;

import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.Graphics.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBox;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.StyleSheet.StyleSheet;


public class AttributeNamespace implements Presentable
{
	protected static Pattern validNamePattern = Pattern.compile( "[a-zA-Z_][a-zA-Z0-9_]*" );

	
	public static class InvalidAttributeNamespaceNameException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public InvalidAttributeNamespaceNameException(String message)
		{
			super( message );
		}
	}

	
	public static class NamespaceAlreadyExistsException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		public NamespaceAlreadyExistsException(String message)
		{
			super( message );
		}
	}
	
	
	private String name;
	private HashMap<String, AttributeBase> attributes = new HashMap<String, AttributeBase>();
	
	
	
	
	public AttributeNamespace(String name)
	{
		if ( !validNamePattern.matcher( name ).matches() )
		{
			throw new InvalidAttributeNamespaceNameException( "Invalid attribute namespace name '" + name + "'; name should be an identifier" );
		}
		this.name = name;
		GlobalAttributeRegistry.registerNamespace( this );
	}
	
	
	
	public String getName()
	{
		return name;
	}
	
	
	protected void registerAttribute(AttributeBase attribute)
	{
		String attrName = attribute.getName();
		if ( attributes.containsKey( attrName ) )
		{
			throw new AttributeBase.AttributeAlreadyExistsException( "The namespace " + name + " already contains an attribute under the name " + attrName );
		}
		attributes.put( attrName, attribute );
		GlobalAttributeRegistry.registerAttribute( attribute );
	}
	
	
	public AttributeBase get(String name)
	{
		return attributes.get( name );
	}
	
	public AttributeBase __getitem__(String name)
	{
		AttributeBase attr = get( name );
		if ( attr == null )
		{
			throw Py.KeyError( "No attribute named " + name );
		}
		return attr;
	}
	
	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		initStyles();
		
		ArrayList<String> attrNames = new ArrayList<String>();
		attrNames.addAll( attributes.keySet() );
		Collections.sort( attrNames );

		Pres title = new Row( new Object[] { namespaceStyle.applyTo( new Label( "Name: " ) ), namespaceNameStyle.applyTo( new Label( name ) ) } );
		
		Object tableContents[][] = new Object[attrNames.size()+1][];
		tableContents[0] = new Object[] { headingStyle.applyTo( new Label( "Attribute name" ) ), headingStyle.applyTo( new Label( "Default value" ) ) };
		for (int i = 0; i < attrNames.size(); i++)
		{
			String name = attrNames.get( i );
			tableContents[i+1] = new Object[] { attrNameStyle.applyTo( new Label( name ) ), attributes.get( name ).getDefaultValue() };
		}
		Pres table = tableStyle.applyTo( new Table( tableContents ) );
		
		return new ObjectBox( "Attribute namespace", new Column( new Object[] { title, table } ) );
	}



	public String toString()
	{
		return name;
	}
	
	
	private static void initStyles()
	{
		if ( namespaceStyle == null  ||  namespaceNameStyle == null  ||  headingStyle == null  ||  attrNameStyle == null  ||  tableStyle == null )
		{
			namespaceStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.3f, 0.3f, 0.4f ) ) );
			namespaceNameStyle = StyleSheet.style( Primitive.fontSize.as( 14 ), Primitive.fontBold.as( true ), Primitive.foreground.as( new Color( 0.0f, 0.0f, 0.4f ) ) );
			headingStyle = StyleSheet.style( Primitive.fontSize.as( 14 ), Primitive.fontBold.as( true ) );
			attrNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.4f, 0.4f ) ) );
			tableStyle = StyleSheet.style( Primitive.tableCellBoundaryPaint.as( new Color( 0.3f, 0.3f, 0.3f ) ), Primitive.tableBorder.as( new SolidBorder() ) );
		}
	}
	
	
	private static StyleSheet namespaceStyle = null, namespaceNameStyle = null, headingStyle = null, attrNameStyle = null, tableStyle = null;
}
