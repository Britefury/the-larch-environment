//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Input.ObjectDndHandler;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.StaticText;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class NonLocalDndTestPage extends SystemPage
{
	private static StyleSheet styleSheet = StyleSheet.instance;
	private static StyleSheet textStyle = styleSheet.withAttr( Primitive.fontSize, 18 );
	
	private static StyleSheet placeHolderStyle = styleSheet.withAttr( Primitive.background, new FillPainter( new Color( 1.0f, 0.9f, 0.75f  ) ) );
	
	
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
	
	
	protected Pres makeDest()
	{
		return new Proxy( placeHolderStyle.applyTo( new Bin( new StaticText( " " ).pad( 8.0, 8.0 ) ) ) );
	}
	
	
	protected Pres makeReceiver(Pres dest, String title)
	{
		Pres titleElem = textStyle.applyTo( new StaticText( title ) );
		
		return styleSheet.withAttr( Primitive.rowSpacing, 20.0 ).applyTo( new Row( new Pres[] { titleElem, dest } ) );
	}
	
	protected Pres makeFileReceiver()
	{
		Pres fileReceiverPres = new Pres()
		{
			@Override
			public DPElement present(final PresentationContext ctx, final StyleValues style)
			{
				Pres dest = makeDest(); 
				final DPElement element = dest.present( ctx, style );

				
				ObjectDndHandler.DropFn dropFn = new ObjectDndHandler.DropFn()
				{
					@SuppressWarnings("unchecked")
					public boolean acceptDrop(PointerInputElement destElement, Point2 targetPosition, Object data, int action)
					{
						List<Object> fileList = (List<Object>)data;
						
						ArrayList<Object> elements = new ArrayList<Object>();
						for (Object x: fileList)
						{
							elements.add( new StaticText( x.toString() ) );
						}
						DPElement e = new Column( elements ).present( ctx, style );
						
						((DPProxy)element).setChild( e );
						
						return true;
					}
				};
				
				element.addNonLocalDropDest( DataFlavor.javaFileListFlavor, dropFn );
				
				return element;
			}
		};
		
		
		return makeReceiver( fileReceiverPres, "File:" );
	}
	
	
	
	
	protected Pres createContents()
	{
		Pres fileReceiver = makeFileReceiver();
		
		return new Body( new Pres[] { fileReceiver } );
	}
}
