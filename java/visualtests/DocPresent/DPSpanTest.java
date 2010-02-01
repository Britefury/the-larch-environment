//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.DPLink;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.StyleSheets.HBoxStyleSheet;

public class DPSpanTest extends DocPresentTestBase
{
	protected DPSpan span;
	protected DPParagraph para;
	protected DPVBox vbox;
	
	
	protected DPWidget createWidget()
	{
		DPLink.LinkListener addSpanElementToSpanAction = new DPLink.LinkListener()
		{
			public boolean onLinkClicked(DPLink link, PointerButtonEvent buttonEvent)
			{
				System.out.println( "Adding span containing extra text element to outer span" );
				DPSpan s2 = new DPSpan( null );
				s2.append( new DPText( null, " text_in_innerspan_in_outerspan_in_para " ) );
				span.append( s2 );
				return false;
			}
		};

		DPLink.LinkListener addTextElementToSpanAction = new DPLink.LinkListener()
		{
			public boolean onLinkClicked(DPLink link, PointerButtonEvent buttonEvent)
			{
				System.out.println( "Adding text element to outer span" );
 				span.append( new DPText( null, " text_in_outerspan_in_para " ) );
				return false;
			}
		};

		DPLink.LinkListener addSpanElementToParaAction = new DPLink.LinkListener()
		{
			public boolean onLinkClicked(DPLink link, PointerButtonEvent buttonEvent)
			{
				System.out.println( "Adding span containing extra text element to para" );
				DPSpan s2 = new DPSpan( null );
				s2.append( new DPText( null, " text_in_innerspan_in_para " ) );
				para.append( s2 );
				return false;
			}
		};

		DPLink.LinkListener addTextElementToParaAction = new DPLink.LinkListener()
		{
			public boolean onLinkClicked(DPLink link, PointerButtonEvent buttonEvent)
			{
				System.out.println( "Adding text  element to para" );
				para.append( new DPText( null, " text_in_para " ) );
				return false;
			}
		};

		
		DPLink addSpanElementToSpanLink = new DPLink( null, "Add span element to span", addSpanElementToSpanAction );
		DPLink addTextElementToSpanLink = new DPLink( null, "Add text element to span", addTextElementToSpanAction );
		DPLink addSpanElementToParaLink = new DPLink( null, "Add span element to paragraph", addSpanElementToParaAction );
		DPLink addTextElementToParaLink = new DPLink( null, "Add text element to paragraph", addTextElementToParaAction );
		
		DPHBox linkBox = new DPHBox( null, new HBoxStyleSheet( 30.0 ) );
		linkBox.append( addSpanElementToSpanLink );
		linkBox.append( addTextElementToSpanLink );
		linkBox.append( addSpanElementToParaLink );
		linkBox.append( addTextElementToParaLink );

		
		
		
		span = new DPSpan( null );
		span.append( new DPText( null, " text_in_span_in_para " ) );
		
		para = new DPParagraph( null );
		para.append( span );
		
		vbox = new DPVBox( null );
		vbox.append( linkBox );
		vbox.append( para );
		
		return vbox;
	}

	
	private DPSpanTest()
	{
		JFrame frame = new JFrame( "Span test" );
		initFrame( frame );
	}
	
	public static void main(String[] args)
	{
		new DPSpanTest();
	}
}
