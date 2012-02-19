//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell.Presenters;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JColorChooser;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Cell.CellEditPerspective;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.PushElementInteractor;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.ObjectPresentation.ObjectPresenter;
import BritefuryJ.ObjectPresentation.ObjectPresenterRegistry;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class PresentersAWT extends ObjectPresenterRegistry
{
	public PresentersAWT()
	{
		registerJavaObjectPresenter( Color.class,  presenter_Color );
	}


	public static final ObjectPresenter presenter_Color = new ObjectPresenter()
	{
		public Pres presentObject(Object x, FragmentView fragment, SimpleAttributeTable inheritedState)
		{
			final Color colour = (Color)x;
			
			PushElementInteractor interactor = new PushElementInteractor()
			{
				
				@Override
				public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
				{
					DPElement cellElement = (DPElement)element;
					
					Color newColour = JColorChooser.showDialog( cellElement.getRootElement().getComponent(), "Choose colour", colour );
					
					if ( newColour != null )
					{
						CellEditPerspective.notifySetCellValue( cellElement, newColour );
					}
				}
				
				@Override
				public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
				{
					return event.getButton() == 1;
				}
			};

			StyleSheet swatchStyle = StyleSheet.style( Primitive.shapePainter.as( new FillPainter( colour ) ), Primitive.cursor.as( new Cursor( Cursor.HAND_CURSOR ) ) );
			Pres swatch = swatchStyle.applyTo( new Box( 25.0, 10.0 ) );
			return swatch.withElementInteractor( interactor ).alignVRefYExpand();
		}
	};
}
