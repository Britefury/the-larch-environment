//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Input.ObjectDndHandler;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;

public class NonLocalDndTestPage extends TestPage
{
	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet textStyle = styleSheet.withValues( Primitive.fontSize.as( 18 ) );

	private static StyleSheet placeHolderStyle = styleSheet.withValues( Primitive.background.as( new FillPainter( new Color( 1.0f, 0.9f, 0.75f ) ) ) );
	
	
	protected NonLocalDndTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Non-local Drag and Drop test";
	}
	
	protected String getDescription()
	{
		return "Receive non-local drops. Drop a file onto the receiver titled 'File:'";
	}
	
	
	protected Pres makeDest(LiveValue live)
	{
		return new Proxy( placeHolderStyle.applyTo( new Bin( live.pad( 8.0, 8.0 ) ) ) );
	}
	
	
	protected Pres makeReceiver(Pres dest, String title)
	{
		Pres titleElem = textStyle.applyTo( new Label( title ) );
		
		return styleSheet.withValues( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Pres[] { titleElem, dest } ) );
	}
	
	protected Pres makeFileReceiver()
	{
		final LiveValue live = new LiveValue( new Label( " " ) );
		Pres dest = makeDest( live ); 

		
		ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
		{
			@SuppressWarnings("unchecked")
			public boolean acceptDrop(LSElement destElement, Point2 targetPosition, Object data, int action)
			{
				List<Object> fileList = (List<Object>)data;
				
				ArrayList<Object> elements = new ArrayList<Object>();
				for (Object x: fileList)
				{
					elements.add( new Label( x.toString() ) );
				}
				Pres r = new Column( elements );
				
				live.setLiteralValue( r );
				
				return true;
			}
		};
		
		Pres fileReceiverPres = dest.withNonLocalDropDest( DataFlavor.javaFileListFlavor, dropFn );
		
		
		return makeReceiver( fileReceiverPres, "File:" );
	}
	
	
	
	
	protected Pres createContents()
	{
		Pres fileReceiver = makeFileReceiver();
		
		return new Body( new Pres[] { fileReceiver } );
	}
}
