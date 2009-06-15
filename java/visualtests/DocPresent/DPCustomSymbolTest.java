//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package visualtests.DocPresent;


import java.awt.geom.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPCustomSymbol;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.StyleSheets.CustomSymbolStyleSheet;



public class DPCustomSymbolTest
{
	public static void main(final String[] args)
	{
		JFrame frame = new JFrame( "Custom symbol test" );
		
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		DPPresentationArea area = new DPPresentationArea();
		
		
		DPCustomSymbol.SymbolInterface symbol = new DPCustomSymbol.SymbolInterface()
		{
			public void draw(Graphics2D graphics)
			{
				graphics.draw( new Line2D.Double( 0.0, 0.0, 10.0, 15.0 ) );
				graphics.draw( new Line2D.Double( 10.0, 15.0, 20.0, 0.0 ) );
				graphics.draw( new Line2D.Double( 0.0, 20.0, 10.0, 15.0 ) );
				graphics.draw( new Line2D.Double( 10.0, 15.0, 20.0, 20.0 ) );
			}
			
			public double getWidth()
			{
				return 20.0;
			}
			
			public double getHeight()
			{
				return 22.0;
			}
			
			public void setBoxRequisitionX(LReqBox box)
			{
				box.setRequisitionX( 20.0, 0.0 );
			}
		
			public void setBoxRequisitionY(LReqBox box)
			{
				box.setRequisitionY( 20.0, 2.0, 0.0 );
			}
		};
     
		CustomSymbolStyleSheet styleSheet = new CustomSymbolStyleSheet( Color.blue );
		DPCustomSymbol customSym = new DPCustomSymbol( styleSheet, symbol );
		     
		area.setChild( customSym );
		     
		     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
