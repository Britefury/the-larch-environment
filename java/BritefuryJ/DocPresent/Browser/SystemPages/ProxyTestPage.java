//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPStaticText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.StaticTextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class ProxyTestPage extends SystemPage
{
	protected ProxyTestPage()
	{
		register( "tests.proxy" );
	}
	
	
	public String getTitle()
	{
		return "Proxy test";
	}
	
	protected String getDescription()
	{
		return "Proxy element: contains one child, can act as a collatelable element or as a single child branch element.";
	}

	
	
	StaticTextStyleSheet blackText = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), Color.black );
	StaticTextStyleSheet redText = new StaticTextStyleSheet( new Font( "Sans serif", Font.PLAIN, 14 ), Color.red );
	
	SolidBorder _greyBorder = new SolidBorder( 2.0, 3.0, new Color( 0.5f, 0.5f, 0.5f ), null );
	
	
	protected DPWidget greyBorder(DPWidget child)
	{
		DPBorder border = new DPBorder( getContext(), _greyBorder );
		border.setChild( child );
		return border;
	}

	protected DPWidget proxy(DPWidget child)
	{
		DPProxy proxy = new DPProxy( getContext() );
		proxy.setChild( child );
		return proxy;
	}
	
	protected DPWidget paragraph(DPWidget children[])
	{
		DPParagraph para = new DPParagraph( getContext() );
		para.setChildren( children );
		return para;
	}
	
	protected DPWidget span(DPWidget children[])
	{
		DPSpan span = new DPSpan( getContext() );
		span.setChildren( children );
		return span;
	}
	
	protected DPWidget staticText(String text)
	{
		return new DPStaticText( getContext(), text );
	}
	
	protected DPWidget content(DPWidget header, DPWidget body)
	{
		DPVBox vbox = new DPVBox( getContext() );
		vbox.append( header );
		vbox.append( body );
		return vbox;
	}

	
	protected DPWidget createContents()
	{
		DPWidget header0 = createTextParagraph( "Border( Proxy( Border( \"Text\" )))" );
		DPWidget body0 = greyBorder( proxy( greyBorder( staticText( "Text" ) ) ) );
		DPWidget content0 = content( header0, body0 );
		
		DPWidget header1 = createTextParagraph( "Paragraph( \"Before\" Proxy( \"TEXT\" ) \"After\")" );
		DPWidget body1 = paragraph( new DPWidget[] { staticText( "Before" ), proxy( staticText( "Text" ) ), staticText( "After" ) } );
		DPWidget content1 = content( header1, body1 );
		
		DPWidget header2 = createTextParagraph( "Paragraph( \"Before\" Proxy( Span( \"One\" \"Two\" \"Three\" ) ) \"After\")" );
		DPWidget body2 = paragraph( new DPWidget[] { staticText( "Before" ), proxy( span( new DPWidget[] { staticText( "One" ), staticText( "Two" ), staticText( "Three" ) } ) ), staticText( "After" ) } );
		DPWidget content2 = content( header2, body2 );
		
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, 30.0 );
		DPVBox box = new DPVBox( getContext(), boxs );
		box.extend( new DPWidget[] { content0, content1, content2 } );

		return box;
	}
}
