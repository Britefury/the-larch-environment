//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Browser.TestPages;

import java.util.ArrayList;
import java.util.Collections;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Controls.Hyperlink;
import BritefuryJ.DefaultPerspective.Presentable;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.RichText.Body;
import BritefuryJ.Pres.RichText.Head;
import BritefuryJ.Pres.RichText.LinkHeaderBar;
import BritefuryJ.Pres.RichText.Page;
import BritefuryJ.Pres.RichText.TitleBar;
import BritefuryJ.Pres.UI.SectionHeading2;
import BritefuryJ.Projection.TransientSubject;
import BritefuryJ.Projection.Subject;

public class TestsRootPage extends AbstractTestPage implements Presentable
{
	private static class TestSubject extends TransientSubject
	{
		AbstractTestPage page;
		
		public TestSubject(AbstractTestPage page)
		{
			super( null );
			
			this.page = page;
		}

		@Override
		public Object getFocus()
		{
			return page;
		}

		@Override
		public String getTitle()
		{
			return page.getTitle();
		}
	}
	
	
	
	private TestsRootPage()
	{
	}
	
	
	
	public String getTitle()
	{
		return "Tests";
	}
	

	

	
	public static int LINKHEADER_SYSTEMPAGE = 0x2;
	
	public static Pres createLinkHeader(int linkHeaderFlags)
	{
		ArrayList<Object> links = new ArrayList<Object>();
		
		if ( ( linkHeaderFlags & LINKHEADER_SYSTEMPAGE )  !=  0 )
		{
			links.add( new Hyperlink( "TESTS PAGE", subjectFor( TestsRootPage.instance ) ) );
		}
		
		return new LinkHeaderBar( links );
	}
	
	
	private static Subject subjectFor(AbstractTestPage page)
	{
		return new TestSubject( page );
	}
	
	
	
	
	
	protected Pres createTestsBox(String title, Hyperlink links[])
	{
		ArrayList<Object> testBoxChildren = new ArrayList<Object>();
		
		Pres heading = new SectionHeading2( title ).pad( 0.0, 0.0, 5.0, 15.0 );
		testBoxChildren.add( heading );

		Collections.addAll( testBoxChildren, links );
		
		return new Column( testBoxChildren ).pad( 10.0, 10.0 ).alignVRefY();
	}
	
	protected Pres createTestsContents()
	{
		ArrayList<Object> testBoxes = new ArrayList<Object>();
		testBoxes.add( createTestsBox( "Primitive elements", primitiveLinks ).pad( 25.0, 5.0 ) );
		testBoxes.add( createTestsBox( "Controls", controlsLinks ).pad( 25.0, 5.0 ) );

		return new Body( new Pres[] { new Row( testBoxes ).alignHPack().alignVTop() } );
	}

	
	
	
	public static final AlignmentTestPage alignment = new AlignmentTestPage();
	public static final BorderTestPage border = new BorderTestPage();
	public static final ColumnTestPage column = new ColumnTestPage();
	public static final ClipboardTestPage clipboard = new ClipboardTestPage();
	public static final DndTestPage dnd = new DndTestPage();
	public static final FlowGridTestPage flowGrid = new FlowGridTestPage();
	public static final FractionTestPage fraction = new FractionTestPage();
	public static final GridTestPage grid = new GridTestPage();
	public static final ImageTestPage image = new ImageTestPage();
	public static final MathRootTestPage mathRoot = new MathRootTestPage();
	public static final NonLocalDndTestPage nonLocalDnd = new NonLocalDndTestPage();
	public static final ParagraphTestPage paragraph = new ParagraphTestPage();
	public static final ParagraphWithSpanTestPage paragraphWithSpan = new ParagraphWithSpanTestPage();
	public static final ProxyAndSpanTestPage proxyAndSpan = new ProxyAndSpanTestPage();
	public static final RowTestPage row = new RowTestPage();
	public static final ScriptTestPage script = new ScriptTestPage();
	public static final SegmentTestPage segment = new SegmentTestPage();
	public static final SequenceViewTestPage sequenceView = new SequenceViewTestPage();
	public static final ShapeTestPage shape = new ShapeTestPage();
	public static final TableTestPage table = new TableTestPage();
	public static final TextTestPage text = new TextTestPage();
	public static final ViewportTestPage viewport = new ViewportTestPage();
	
	private static final Hyperlink primitiveLinks[] = {
		new Hyperlink( "Alignment", subjectFor( alignment ) ),
		new Hyperlink( "Border", subjectFor( border ) ),
		new Hyperlink( "Column", subjectFor( column ) ),
		new Hyperlink( "Clipboard", subjectFor( clipboard ) ),
		new Hyperlink( "Drag and drop", subjectFor( dnd ) ),
		new Hyperlink( "Flow grid", subjectFor( flowGrid ) ),
		new Hyperlink( "Fraction", subjectFor( fraction ) ),
		new Hyperlink( "Grid", subjectFor( grid ) ),
		new Hyperlink( "Image", subjectFor( image ) ),
		new Hyperlink( "Math root", subjectFor( mathRoot ) ),
		new Hyperlink( "Non-local drag and drop", subjectFor( nonLocalDnd ) ),
		new Hyperlink( "Paragraph", subjectFor( paragraph ) ),
		new Hyperlink( "Paragraph with span", subjectFor( paragraphWithSpan ) ),
		new Hyperlink( "Proxy and span", subjectFor( proxyAndSpan ) ),
		new Hyperlink( "Row", subjectFor( row ) ),
		new Hyperlink( "Script", subjectFor( script ) ),
		new Hyperlink( "Segment", subjectFor( segment ) ),
		new Hyperlink( "Sequence view", subjectFor( sequenceView ) ),
		new Hyperlink( "Shape", subjectFor( shape ) ),
		new Hyperlink( "Table", subjectFor( table ) ),
		new Hyperlink( "Text", subjectFor( text ) ),
		new Hyperlink( "Viewport", subjectFor( viewport ) ),
	};

	
	
	
	public static final BubblePopupTestPage bubblePopup = new BubblePopupTestPage();
	public static final ButtonTestPage button = new ButtonTestPage();
	public static final CheckboxTestPage checkbox = new CheckboxTestPage();
	public static final ColourPickerTestPage colourPicker = new ColourPickerTestPage();
	public static final CommandConsoleTestPage commandConsole = new CommandConsoleTestPage();
	public static final EditableLabelTestPage editableLabel = new EditableLabelTestPage();
	public static final ExpanderTestPage expander = new ExpanderTestPage();
	public static final HyperlinkTestPage hyperlink = new HyperlinkTestPage();
	public static final NumericLabelTestPage numLabel = new NumericLabelTestPage();
	public static final ObjectDropBoxTestPage objectDrop = new ObjectDropBoxTestPage();
	public static final OptionMenuTestPage optionMenu = new OptionMenuTestPage();
	public static final PopupTestPage popup = new PopupTestPage();
	public static final RadioButtonTestPage radioButton = new RadioButtonTestPage();
	public static final ScrollBarTestPage scrollBar = new ScrollBarTestPage();
	public static final ScrolledViewportTestPage scrolledViewport = new ScrolledViewportTestPage();
	public static final SliderTestPage slider = new SliderTestPage();
	public static final SpinEntryTestPage spinEntry = new SpinEntryTestPage();
	public static final TabbedBoxTestPage tabbedBox = new TabbedBoxTestPage();
	public static final TextAreaTestPage textArea = new TextAreaTestPage();
	public static final TextEntryTestPage textEntry = new TextEntryTestPage();
	
	private static final Hyperlink controlsLinks[] = {
		new Hyperlink( "Bubble popup", subjectFor( bubblePopup ) ),
		new Hyperlink( "Button", subjectFor( button ) ),
		new Hyperlink( "Check box", subjectFor( checkbox ) ),
		new Hyperlink( "Colour picker", subjectFor( colourPicker ) ),
		new Hyperlink( "Command console", subjectFor( commandConsole ) ),
		new Hyperlink( "Editable label", subjectFor( editableLabel ) ),
		new Hyperlink( "Expander", subjectFor( expander ) ),
		new Hyperlink( "Hyperlink", subjectFor( hyperlink ) ),
		new Hyperlink( "Numeric label", subjectFor( numLabel ) ),
		new Hyperlink( "Object drop box", subjectFor( objectDrop ) ),
		new Hyperlink( "Option menu", subjectFor( optionMenu ) ),
		new Hyperlink( "Popup", subjectFor( popup ) ),
		new Hyperlink("Radio button", subjectFor(radioButton)),
		new Hyperlink( "Scroll bar", subjectFor( scrollBar ) ),
		new Hyperlink( "Scrolled viewport", subjectFor( scrolledViewport ) ),
		new Hyperlink( "Slider", subjectFor( slider ) ),
		new Hyperlink( "Spin entry", subjectFor( spinEntry ) ),
		new Hyperlink( "Tabbed box", subjectFor( tabbedBox ) ),
		new Hyperlink( "Text area", subjectFor( textArea ) ),
		new Hyperlink( "Text entry", subjectFor( textEntry ) ),
	};
	

	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres title = new TitleBar( "Tests" );
		
		Pres head = new Head( new Pres[] { createLinkHeader( 0 ), title } );
		
		return new Page( new Pres[] { head, createTestsContents() } );
	}


	
	public static final TestsRootPage instance = new TestsRootPage();
	public static final Subject instanceSubject = subjectFor( instance );
}
