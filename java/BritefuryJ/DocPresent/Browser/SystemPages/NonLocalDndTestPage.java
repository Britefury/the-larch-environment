//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.Math.Point2;

public class NonLocalDndTestPage extends SystemPage
{
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet textStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 18 ) );
	
	private static PrimitiveStyleSheet placeHolderStyle = styleSheet.withBackground( new FillPainter( new Color( 1.0f, 0.9f, 0.75f  ) ) );
	
	
	protected NonLocalDndTestPage()
	{
		register( "tests.nonlocaldnd" );
	}
	
	
	public String getTitle()
	{
		return "Non-local Drag and Drop test";
	}
	
	protected String getDescription()
	{
		return "Receive non-local drops.";
	}
	
	
	protected DPProxy makeDest()
	{
		return styleSheet.proxy( placeHolderStyle.box( styleSheet.staticText( " " ).pad( 8.0, 8.0 ) ) );
	}
	
	
	protected DPElement makeReceiver(DPElement dest, String title)
	{
		DPElement titleElement = textStyle.staticText( title );
		
		return styleSheet.withHBoxSpacing( 20.0 ).hbox( Arrays.asList( new DPElement[] { titleElement, dest } ) );
	}
	
	protected DPElement makeFileReceiver()
	{
		final DPProxy dest = makeDest(); 

		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			@SuppressWarnings("unchecked")
			public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data)
			{
				List<Object> fileList = (List<Object>)data;
				
				ArrayList<DPElement> elements = new ArrayList<DPElement>();
				for (Object x: fileList)
				{
					elements.add( styleSheet.staticText( x.toString() ) );
				}
				DPElement e = styleSheet.vbox( elements );
				
				dest.setChild( e );
				
				return true;
			}
		};
		
		dest.addNonLocalDropDest( DataFlavor.javaFileListFlavor, dropFn );

			
		return makeReceiver( dest, "File:" );
	}
	
	
	
	
	protected DPElement createContents()
	{
		DPElement fileReceiver = makeFileReceiver();
		
		return styleSheet.withVBoxSpacing( 20.0 ).vbox( Arrays.asList( new DPElement[] { fileReceiver.alignHExpand() } ) ).alignHExpand();
	}
}
