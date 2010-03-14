//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.Hyperlink;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class ProxyAndSpanTestPage extends SystemPage
{
	protected ProxyAndSpanTestPage()
	{
		register( "tests.proxyandspan" );
	}
	
	
	public String getTitle()
	{
		return "Proxy and span test";
	}
	
	protected String getDescription()
	{
		return "Proxy element: contains one child, can act as a collatelable element or as a single child branch element. Span element: contains multiple children.";
	}

	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet blackText = styleSheet.withForeground( Color.black );
	private static PrimitiveStyleSheet redText = styleSheet.withForeground( Color.red );
	private static PrimitiveStyleSheet greenText = styleSheet.withForeground( new Color( 0.0f, 0.5f, 0.0f ) );
	private static PrimitiveStyleSheet seaGreenText = styleSheet.withForeground( new Color( 0.0f, 0.5f, 0.5f ) );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	protected ArrayList<DPElement> makeTextNodes(String text, PrimitiveStyleSheet styleSheet)
	{
		String[] words = text.split( " " );
		ArrayList<DPElement> nodes = new ArrayList<DPElement>();
		for (int i = 0; i < words.length; i++)
		{
			nodes.add( styleSheet.text( words[i] ) );
			if ( i  != words.length -1 )
			{
				nodes.add( styleSheet.text( " " ) );
				nodes.add( styleSheet.lineBreak() );
			}
		}
		return nodes;
	}
	
	
	private static String paragraphText = "The gSym document presentation system, called DocPresent, provides a paragraph flow layout element. Line break elements give the paragraph element the "
		+ "opportunity to end lines in order to fit them in the given horizontal space. Lines are arranged vertically.";
	private static String spanParaText = "The DocPresent span element incorporates its children into the children of the parent sequence element - paragraphs, horizontal or vertical boxes, or other span elements "
		+ "which are in turn incorporated into parent elements until an element is reached that provides a specififc layout.";
	private static String spanContentText = "These elements reside within a span.";
	private static String spanInProxyContentText = "These elements reside within a span, within a proxy.";
	private static String spanSecondaryText = "These are replacement elements that reside within a span.";
	private static String proxySecondaryText = "These are replacement elements that reside within a span, within a proxy.";
	private static String spanParaPostText = "These elements are members of the paragraph after the span.";
	
	

	protected DPElement createParagraph1()
	{
		return styleSheet.paragraph( makeTextNodes( paragraphText, blackText ) );
	}
	
	protected DPElement createParagraph2()
	{
		final DPSpan span = styleSheet.span( makeTextNodes( spanContentText, redText ) );
		
		ArrayList<DPElement> paragraph2Contents = makeTextNodes( spanParaText, blackText );
		paragraph2Contents.add( styleSheet.text( " " ) );
		paragraph2Contents.add( styleSheet.lineBreak() );
		paragraph2Contents.add( span );
		paragraph2Contents.add( styleSheet.text( " " ) );
		paragraph2Contents.add( styleSheet.lineBreak() );
		paragraph2Contents.addAll( makeTextNodes( spanParaPostText, blackText ) );
		DPElement paragraph = styleSheet.paragraph( paragraph2Contents );
		
		Hyperlink.LinkListener onModifySpanLink = new Hyperlink.LinkListener()
		{
			public boolean onLinkClicked(Hyperlink link, PointerButtonEvent buttonEvent)
			{
				span.setChildren( makeTextNodes( spanSecondaryText, greenText ) );
				return true;
			}
		};
		Hyperlink modifySpanLink = controlsStyleSheet.link( "Place secondary text into span", onModifySpanLink );
		
		return styleSheet.withVBoxSpacing( 5.0 ).vbox( Arrays.asList( new DPElement[] { paragraph, modifySpanLink.getElement() } ) );
	}
	
	protected DPElement createParagraph3()
	{
		DPSpan span = styleSheet.span( makeTextNodes( spanInProxyContentText, redText ) );
		final DPProxy proxy = styleSheet.proxy( span );
		
		ArrayList<DPElement> paragraph2Contents = makeTextNodes( spanParaText, blackText );
		paragraph2Contents.add( styleSheet.text( " " ) );
		paragraph2Contents.add( styleSheet.lineBreak() );
		paragraph2Contents.add( proxy );
		paragraph2Contents.add( styleSheet.text( " " ) );
		paragraph2Contents.add( styleSheet.lineBreak() );
		paragraph2Contents.addAll( makeTextNodes( spanParaPostText, blackText ) );
		DPElement paragraph = styleSheet.paragraph( paragraph2Contents );
		
		Hyperlink.LinkListener onModifySpanLink = new Hyperlink.LinkListener()
		{
			public boolean onLinkClicked(Hyperlink link, PointerButtonEvent buttonEvent)
			{
				DPSpan span = (DPSpan)proxy.getChild();
				span.setChildren( makeTextNodes( spanSecondaryText, greenText ) );
				return true;
			}
		};
		Hyperlink modifySpanLink = controlsStyleSheet.link( "Place secondary text into span", onModifySpanLink );
		
		Hyperlink.LinkListener onModifyProxyLink = new Hyperlink.LinkListener()
		{
			public boolean onLinkClicked(Hyperlink link, PointerButtonEvent buttonEvent)
			{
				proxy.setChild( styleSheet.span( makeTextNodes( proxySecondaryText, seaGreenText ) ) );
				return true;
			}
		};
		Hyperlink modifyProxyLink = controlsStyleSheet.link( "Place new span into proxy", onModifyProxyLink );

		return styleSheet.withVBoxSpacing( 5.0 ).vbox( Arrays.asList( new DPElement[] { paragraph,
				styleSheet.withHBoxSpacing( 15.0 ).hbox( Arrays.asList( new DPElement[] { modifySpanLink.getElement(), modifyProxyLink.getElement() } ) ) } ) );
	}
	
	protected DPElement createContents()
	{
		return styleSheet.withVBoxSpacing( 30.0 ).vbox( Arrays.asList( new DPElement[] { createParagraph1(), createParagraph2(), createParagraph3() } ) );
	}
}
