package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPHBoxTypeset;
import BritefuryJ.DocPresent.DPFlow;
import BritefuryJ.DocPresent.DPWidget;

public class DPFlowTest_call_getattr
{
	protected static DPWidget makeGetAttr(DPWidget target, String attrName)
	{
		DPFlow f = new DPFlow();
		DPHBoxTypeset h = new DPHBoxTypeset();
		h.append( target );
		h.append( new DPText( ".", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK ) );
		f.append( h );
		f.append( new DPText( attrName, new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLUE ) );
		return f;
	}
	
	protected static DPWidget makeCall(DPWidget target)
	{
		DPHBoxTypeset h = new DPHBoxTypeset();
		h.append( target );
		h.append( new DPText( "(", new Font( "Sans serif", Font.BOLD, 12 ), new Color( 0.0f, 0.5f, 0.0f ) ) );

		DPFlow f = new DPFlow();
		DPHBoxTypeset argBox0 = new DPHBoxTypeset();
		argBox0.append( new DPText( "aaaaaa", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK ) );
		argBox0.append( new DPText( ",", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK ) );
		DPHBoxTypeset argBox1 = new DPHBoxTypeset();
		argBox1.append( new DPText( "bbbbbb", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK ) );
		argBox1.append( new DPText( ",", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK ) );
		DPHBoxTypeset argBox2 = new DPHBoxTypeset();
		argBox2.append( new DPText( "cccccc", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK ) );
		argBox2.append( new DPText( ",", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK ) );
		DPHBoxTypeset argBox3 = new DPHBoxTypeset();
		argBox3.append( new DPText( "dddddd", new Font( "Sans serif", Font.PLAIN, 12 ), Color.BLACK ) );
		f.append( argBox0 );
		f.append( argBox1 );
		f.append( argBox2 );
		f.append( argBox3 );

		h.append( f );
		h.append( new DPText( ")", new Font( "Sans serif", Font.BOLD, 12 ), new Color( 0.0f, 0.5f, 0.0f ) ) );
		
		return h;
	}
	
	
	protected static DPWidget makeGetAttrChain(DPWidget target, int level)
	{
		if ( level == 0 )
		{
			return target;
		}
		else
		{
			return makeGetAttrChain( makeGetAttr( target, "abcdefghi" + String.valueOf( level ) ), level - 1 );
		}
	}
	
	protected static DPWidget makeCallChain(DPWidget target, int level)
	{
		if ( level == 0 )
		{
			return target;
		}
		else
		{
			return makeCallChain( makeCall( target ), level - 1 );
		}
	}
	
	public static void main(final String[] args) {
		JFrame frame = new JFrame( "HBox typeset test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
		
		DPText t = new DPText( "this", new Font( "Sans serif", Font.BOLD, 12 ), Color.BLACK );
		DPWidget contents = makeCall( makeGetAttrChain( makeCall( makeGetAttrChain( makeCall( makeGetAttrChain( t, 4) ), 4 ) ), 4 ) );
		
     
	     
		area.setChild( contents );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
}
