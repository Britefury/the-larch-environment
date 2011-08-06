//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.Controls.IntSpinEntry;
import BritefuryJ.Controls.RealSpinEntry;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.SpaceBin;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;
import BritefuryJ.StyleSheet.StyleSheet;

public class SpinEntryTestPage extends SystemPage
{
	protected SpinEntryTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Spin entry test";
	}
	
	protected String getDescription()
	{
		return "Spin entry control: edit a numeric value";
	}
	
	
	private static class RealSpinEntryTextChanger implements RealSpinEntry.RealSpinEntryListener
	{
		private DPText textElement;
		
		
		public RealSpinEntryTextChanger(DPText textElement)
		{
			this.textElement = textElement;
		}


		public void onSpinEntryValueChanged(RealSpinEntry.RealSpinEntryControl spinEntry, double value)
		{
			textElement.setText( String.valueOf( value ) );
		}
	}

	

	private static class IntSpinEntryTextChanger implements IntSpinEntry.IntSpinEntryListener
	{
		private DPText textElement;
		
		
		public IntSpinEntryTextChanger(DPText textElement)
		{
			this.textElement = textElement;
		}


		public void onSpinEntryValueChanged(IntSpinEntry.IntSpinEntryControl spinEntry, int value)
		{
			textElement.setText( String.valueOf( value ) );
		}
	}

	

	protected Pres createContents()
	{
		Pres realValueTextPres = new Label( "0.0" );
		DPText realValueText = (DPText)realValueTextPres.present();
		Pres intValueTextPres = new Label( "0" );
		DPText intValueText = (DPText)intValueTextPres.present();
		RealSpinEntryTextChanger realListener = new RealSpinEntryTextChanger( realValueText );
		IntSpinEntryTextChanger intListener = new IntSpinEntryTextChanger( intValueText );
		RealSpinEntry realSpinEntry = new RealSpinEntry( 0.0, -100.0, 100.0, 1.0, 10.0, realListener );
		IntSpinEntry intSpinEntry = new IntSpinEntry( 0, -100, 100, 1, 10, intListener );
		Pres realLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Real number: " ),
			    new SpaceBin( realSpinEntry.alignHExpand(), 100.0, -1.0 ), realValueText } ).padX( 5.0 ) );
		Pres intLine = StyleSheet.style( Primitive.rowSpacing.as( 20.0 ) ).applyTo( new Row( new Object[] { new Label( "Integer: " ),
			    new SpaceBin( intSpinEntry.alignHExpand(), 100.0, -1.0 ), intValueText } ).padX( 5.0 ) );
		Pres spinEntrySectionContents = new Column( new Pres[] { realLine, intLine } );
		
		return new Body( new Pres[] { new Heading2( "Spin entries" ), spinEntrySectionContents } );
	}
}
