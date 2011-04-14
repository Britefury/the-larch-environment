//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.Pres.ElementRef;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.LineBreak;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Span;
import BritefuryJ.Pres.Primitive.Text;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class ProxyAndSpanTestPage extends SystemPage
{
	protected ProxyAndSpanTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Proxy and span test";
	}
	
	protected String getDescription()
	{
		return "Proxy element: contains one child, can act as a collatelable element or as a single child branch element. Span element: contains multiple children.";
	}

	

	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet blackText = styleSheet.withAttr( Primitive.foreground, Color.black );
	private static StyleSheet redText = styleSheet.withAttr( Primitive.foreground, Color.red );
	private static StyleSheet greenText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.0f ) );
	private static StyleSheet seaGreenText = styleSheet.withAttr( Primitive.foreground, new Color( 0.0f, 0.5f, 0.5f ) );

	
	protected ArrayList<Object> makeTextNodes(String text)
	{
		String[] words = text.split( " " );
		ArrayList<Object> nodes = new ArrayList<Object>();
		for (int i = 0; i < words.length; i++)
		{
			nodes.add( new Text( words[i] ) );
			if ( i  != words.length -1 )
			{
				nodes.add( new Text( " " ) );
				nodes.add( new LineBreak() );
			}
		}
		return nodes;
	}
	
	
	private static String paragraphText = "The document presentation system, called DocPresent, provides a paragraph flow layout element. Line break elements give the paragraph element the "
		+ "opportunity to end lines in order to fit them in the given horizontal space. Lines are arranged vertically.";
	private static String spanParaText = "The DocPresent span element incorporates its children into the children of the parent sequence element - paragraphs, horizontal or vertical boxes, or other span elements "
		+ "which are in turn incorporated into parent elements until an element is reached that provides a specififc layout.";
	private static String spanContentText = "These elements reside within a span.";
	private static String spanInProxyContentText = "These elements reside within a span, within a proxy.";
	private static String spanSecondaryText = "These are replacement elements that reside within a span.";
	private static String proxySecondaryText = "These are replacement elements that reside within a span, within a proxy.";
	private static String spanParaPostText = "These elements are members of the paragraph after the span.";
	
	

	protected Pres createParagraph1()
	{
		return blackText.applyTo( new Paragraph( makeTextNodes( paragraphText ) ) );
	}
	
	protected Pres createParagraph2()
	{
		final ElementRef spanRef = redText.applyTo( new Span( makeTextNodes( spanContentText ) ) ).elementRef();
		
		ArrayList<Object> paragraph2Contents = makeTextNodes( spanParaText );
		paragraph2Contents.add( new Text( " " ) );
		paragraph2Contents.add( new LineBreak() );
		paragraph2Contents.add( spanRef );
		paragraph2Contents.add( new Text( " " ) );
		paragraph2Contents.add( new LineBreak() );
		paragraph2Contents.addAll( makeTextNodes( spanParaPostText ) );
		Pres paragraph = new Paragraph( paragraph2Contents );
		
		Hyperlink.LinkListener onModifySpanLink = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink.HyperlinkControl link, PointerButtonClickedEvent buttonEvent)
			{
				for (DPElement element: spanRef.getElements())
				{
					DPSpan span = (DPSpan)element;
					ArrayList<DPElement> children = new ArrayList<DPElement>();
					for (Object o: makeTextNodes( spanSecondaryText ))
					{
						Pres p = (Pres)o;
						children.add( greenText.applyTo( p ).present( spanRef.getContextForElement( element ), spanRef.getStyleForElement( element ) ) );
					}
					span.setChildren( children );
				}
			}
		};
		Hyperlink modifySpanLink = new Hyperlink( "Place secondary text into span", onModifySpanLink );
		
		return styleSheet.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( new Column( new Pres[] { paragraph, modifySpanLink } ) );
	}
	
	protected Pres createParagraph3()
	{
		final ElementRef spanRef = redText.applyTo( new Span( makeTextNodes( spanInProxyContentText ) ) ).elementRef();
		final ElementRef proxyRef = new Proxy( spanRef ).elementRef();
		
		ArrayList<Object> paragraph2Contents = makeTextNodes( spanParaText );
		paragraph2Contents.add( new Text( " " ) );
		paragraph2Contents.add( new LineBreak() );
		paragraph2Contents.add( proxyRef );
		paragraph2Contents.add( new Text( " " ) );
		paragraph2Contents.add( new LineBreak() );
		paragraph2Contents.addAll( makeTextNodes( spanParaPostText ) );
		Pres paragraph = new Paragraph( paragraph2Contents );
		
		Hyperlink.LinkListener onModifySpanLink = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink.HyperlinkControl link, PointerButtonClickedEvent buttonEvent)
			{
				for (DPElement element: spanRef.getElements())
				{
					DPSpan span = (DPSpan)element;
					ArrayList<DPElement> children = new ArrayList<DPElement>();
					for (Object o: makeTextNodes( spanSecondaryText ))
					{
						Pres p = (Pres)o;
						children.add( greenText.applyTo( p ).present( spanRef.getContextForElement( element ), spanRef.getStyleForElement( element ) ) );
					}
					span.setChildren( children );
				}
			}
		};
		Hyperlink modifySpanLink = new Hyperlink( "Place secondary text into span", onModifySpanLink );
		
		Hyperlink.LinkListener onModifyProxyLink = new Hyperlink.LinkListener()
		{
			public void onLinkClicked(Hyperlink.HyperlinkControl link, PointerButtonClickedEvent buttonEvent)
			{
				for (DPElement element: proxyRef.getElements())
				{
					DPProxy proxy = (DPProxy)element;
					Pres span = seaGreenText.applyTo( new Span( makeTextNodes( proxySecondaryText ) ) );
					proxy.setChild( span.present( proxyRef.getContextForElement( element ), proxyRef.getStyleForElement( element ) ) );
				}
			}
		};
		Hyperlink modifyProxyLink = new Hyperlink( "Place new span into proxy", onModifyProxyLink );

		return styleSheet.withAttr( Primitive.columnSpacing, 5.0 ).applyTo( new Column( new Pres[] { paragraph,
				styleSheet.withAttr( Primitive.rowSpacing, 15.0 ).applyTo( new Row( new Pres[] { modifySpanLink, modifyProxyLink } ) ) } ) );
	}
	
	protected Pres createContents()
	{
		return new Body( new Pres[] { createParagraph1(), createParagraph2(), createParagraph3() } );
	}
}
