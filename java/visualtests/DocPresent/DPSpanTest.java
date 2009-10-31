//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWidget;

public class DPSpanTest extends DocPresentTestBase
{
	protected DPSpan span;
	
	
	protected DPWidget createWidget()
	{
		span = new DPSpan( null );
		span.append( new DPText( null, "first" ) );
		
		DPParagraph para = new DPParagraph( null );
		para.append( span );
		
		return para;
	}

	
	private DPSpanTest()
	{
		JFrame frame = new JFrame( "Span test" );
		initFrame( frame );
		

		
		AbstractAction addElementAction = new AbstractAction( "Add element to span" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event)
			{
				DPSpan s2 = new DPSpan( null );
				s2.append( new DPText( null, "second" ) );
				span.append( s2 );
			}
		};

		
		// Menu
		JMenu actionMenu = new JMenu( "Action" );
		actionMenu.add( addElementAction );
		
		menuBar.add( actionMenu );
	}
	
	public static void main(String[] args)
	{
		new DPSpanTest();
	}
}
