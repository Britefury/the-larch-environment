//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.SimpleDndHandler;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class DndTestPage extends SystemPage
{
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet textStyle = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 16 ) );
	private static PrimitiveStyleSheet sourceStyle = styleSheet.withBackground( new FillPainter( new Color( 0.75f, 0.85f, 1.0f ) ) );
	private static PrimitiveStyleSheet destStyle = styleSheet.withBackground( new FillPainter( new Color( 1.0f, 0.9f, 0.75f  ) ) );
	private static PrimitiveStyleSheet tableStyle = styleSheet.withTableColumnSpacing( 25.0 ).withTableRowSpacing( 25.0 );
	
	
	protected DndTestPage()
	{
		register( "tests.dnd" );
	}
	
	
	public String getTitle()
	{
		return "Drag and Drop test";
	}
	
	protected String getDescription()
	{
		return "Text can be dragged from the sources to the destinations.";
	}
	
	
	
	protected DPWidget makeSourceElement(String title, final String dragData)
	{
		final DPText textElement = textStyle.text( title );
		DPWidget source = sourceStyle.box( textElement.pad( 10.0, 10.0 ) );
		
		SimpleDndHandler.SourceDataFn sourceDataFn = new SimpleDndHandler.SourceDataFn()
		{
			public Object createSourceData(PointerInputElement sourceElement)
			{
				return dragData;
			}
		};
		
		SimpleDndHandler sourceDndHandler = new SimpleDndHandler();
		sourceDndHandler.registerSource( "text", sourceDataFn );

		source.enableDnd( sourceDndHandler );

		return source;
	}
	
	protected DPWidget makeDestElement(String title)
	{
		final DPText textElement = textStyle.text( title );
		DPWidget dest = destStyle.box( textElement.pad( 10.0, 10.0 ) );

		SimpleDndHandler.DropFn dropFn = new SimpleDndHandler.DropFn()
		{
			public boolean acceptDrop(PointerInputElement destElement, Object data)
			{
				String text = (String)data;
				textElement.setText( text );
				return true;
			}
		};
		
		SimpleDndHandler destDndHandler = new SimpleDndHandler();
		destDndHandler.registerDest( "text", dropFn );


		dest.enableDnd( destDndHandler );

		return dest;
	}
	

	
	protected DPWidget createContents()
	{
		DPWidget source0 = makeSourceElement( "abc", "abc" );
		DPWidget source1 = makeSourceElement( "xyz", "xyz" );

		DPWidget dest0 = makeDestElement( "abc" );
		DPWidget dest1 = makeDestElement( "xyz" );
		
		return tableStyle.table( new DPWidget[][] { { styleSheet.text( "Source:" ), source0, source1 },
				{ styleSheet.text( "Destination:" ), dest0, dest1 } } ).padY( 10.0 );
	}
}
