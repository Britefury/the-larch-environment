//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import BritefuryJ.Controls.Checkbox;
import BritefuryJ.Controls.ListSelect;
import BritefuryJ.Controls.RadioButton;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Heading2;

public class ListSelectTestPage extends TestPage
{
    protected ListSelectTestPage()
    {
    }


    public String getTitle()
    {
        return "List select test";
    }

    protected String getDescription()
    {
        return "List select control: choose item from list";
    }


    protected Pres createContents()
    {
        LiveValue state = new LiveValue( "one" );
        String labelTexts[] = {"One", "Two", "Three", "Four"};
        Object values[] = {"one", "two", "three", "four"};
        ListSelect sel = ListSelect.listSelectWithLabels(labelTexts, values, state);
        Pres listSelectSectionContents = new Row( new Object[] { sel.padX( 5.0 ), state } );

        return new Body( new Pres[] { new Heading2( "List select" ), listSelectSectionContents } );
    }
}
