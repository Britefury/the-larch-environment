//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import BritefuryJ.Controls.Checkbox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.Combinators.ElementRef;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.Column;
import BritefuryJ.DocPresent.Combinators.RichText.Body;
import BritefuryJ.DocPresent.Combinators.RichText.Heading2;

public class CheckboxTestPage extends SystemPage
{
	protected CheckboxTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Checkbox test";
	}
	
	protected String getDescription()
	{
		return "Checkbox element: can be toggled";
	}
	
	
	private class CheckboxTextChanger implements Checkbox.CheckboxListener
	{
		private ElementRef textElements;
		
		
		public CheckboxTextChanger(ElementRef textElements)
		{
			this.textElements = textElements;
		}


		public void onCheckboxToggled(Checkbox.CheckboxControl checkbox, boolean state)
		{
			for (DPElement element: textElements.getElements())
			{
				DPText textElement = (DPText)element;
				textElement.setText( String.valueOf( state ) );
			}
		}
	}

	

	protected Pres createContents()
	{
		ElementRef stateTextRef = new StaticText( "false" ).elementRef();
		Checkbox checkbox = Checkbox.checkboxWithLabel( "State", false, new CheckboxTextChanger( stateTextRef ) );
		Pres checkboxSectionContents = new Column( new Pres[] { stateTextRef, checkbox.padX( 5.0 ) } );
		
		return new Body( new Pres[] { new Heading2( "Checkbox" ), checkboxSectionContents } );
	}
}
