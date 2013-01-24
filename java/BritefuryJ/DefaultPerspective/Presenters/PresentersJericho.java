//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DefaultPerspective.Presenters;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import BritefuryJ.Pres.RichText.NormalText;
import net.htmlparser.jericho.*;
import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.ObjectPres.ObjectBorder;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.LineBreakCostSpan;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.ParagraphIndentMatchSpan;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class PresentersJericho extends ObjectPresenterRegistry
{
	public PresentersJericho()
	{
		registerJavaObjectPresenter( Source.class, presenter_Source );
		registerJavaObjectPresenter( Element.class, presenter_Element );
	}

	
	public static final ObjectPresenter presenter_Source = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Source source = (Source)x;
			
			Pres header = htmlSourceStyle.applyTo( new Label( "HTML Source" ) ).pad( 2.0, 2.0 ).alignHExpand();
			
			List<Element> children = source.getChildElements();
			Pres childPres = new Column( children.toArray() );
			
			return new ObjectBorder( new Column( new Pres[] { header, childPres.padX( 15.0, 0.0 ) } ) );
		}
	};


	public static final ObjectPresenter presenter_Element = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			Element element = (Element)x;
			
			Pres tag = tagStyle.applyTo( new Label( element.getName() ) );
			List<Object> headerElements = new ArrayList<Object>();
			headerElements.add( openAngleBracket );
			headerElements.add( tag );
			Attributes attrs = element.getAttributes();
			if ( attrs != null )
			{
				headerElements.add( new Label( " " ) );
				List<Object> attrElements = new ArrayList<Object>();
				boolean first = true;
				for (Attribute attr: attrs)
				{
					if ( !first )
					{
						attrElements.add( new Label( " " ) );
						attrElements.add( new LineBreak() );
					}
					presentAttribute( attrElements, attr, fragment, inheritedState );
					first = false;
				}
				headerElements.add( new ParagraphIndentMatchSpan( attrElements ) );
			}
			headerElements.add( closeAngleBracket );
			Pres header = new Paragraph( headerElements );
			
			Segment content = element.getContent();
			List<Element> childElements = element.getChildElements();

			ArrayList<Object> children = new ArrayList<Object>();
			int textStart = content.getBegin();
			Source source = element.getSource();
			for (Element e: element.getChildElements())
			{
				addCData( children, source, textStart, e.getBegin() );
				children.add( e );

				textStart = e.getEnd();
			}

			addCData( children, source, textStart, content.getEnd() );

			Pres childPres = new Column( children.toArray() );

			return new Column( new Pres[] { header, childPres.padX( 15.0, 0.0 ) } );
		}
	};


	private static void addCData(ArrayList<Object> children, Source source, int begin, int end)
	{
		if ( end > begin )
		{
			String text =  source.subSequence( begin, end ).toString();
			children.add( new NormalText( text ) );
		}
	}
	
	
	private static final Pattern whitespacePattern = Pattern.compile( "[ ]+" );
	
	private static void presentAttribute(List<Object> p, Attribute attr, FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres name = attrNameStyle.applyTo( new Label( attr.getKey() ) );
		Pres eq = attrPunctuationStyle.applyTo( new Label( "=" ) );
		String valueStrings[] = whitespacePattern.split( attr.getValue() );
		ArrayList<Pres> valueElements = new ArrayList<Pres>();
		boolean first = true;
		for (String v: valueStrings)
		{
			if ( !first )
			{
				valueElements.add( new Label( " " ) );
				valueElements.add( new LineBreak() );
			}
			valueElements.add( new Label( v ) );
			first = false;
		}
		Pres value = attrValueStyle.applyTo( new LineBreakCostSpan( valueElements.toArray() ) );
		p.add( name );
		p.add( eq );
		p.add( value );
	}


	private static final StyleSheet htmlSourceStyle = StyleSheet.style( Primitive.fontBold.as( true ), Primitive.foreground.as( new Color( 0.2f, 0.4f, 0.6f ) ), Primitive.background.as( new FillPainter( new Color( 0.85f, 0.85f, 0.85f ) ) ) );
	private static final StyleSheet angleBracketStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.5f, 0.5f, 0.5f, 0.5f ) ) );
	private static final StyleSheet tagStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.0f, 0.2f, 0.5f ) ) );
	private static final StyleSheet attrNameStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.4f, 0.4f, 0.4f ) ) );
	private static final StyleSheet attrPunctuationStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.3f, 0.4f, 0.3f ) ) );
	private static final StyleSheet attrValueStyle = StyleSheet.style( Primitive.foreground.as( new Color( 0.2f, 0.4f, 0.2f ) ) );
	private static final Pres openAngleBracket = angleBracketStyle.applyTo( new Label( "<" ) );
	private static final Pres closeAngleBracket = angleBracketStyle.applyTo( new Label( ">" ) );
}
