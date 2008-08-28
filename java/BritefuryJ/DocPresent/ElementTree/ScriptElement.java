package BritefuryJ.DocPresent.ElementTree;

import java.util.List;
import java.util.Vector;

import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ScriptStyleSheet;

public class ScriptElement extends BranchElement
{
	public static int LEFTSUPER = DPScript.LEFTSUPER;
	public static int LEFTSUB = DPScript.LEFTSUB;
	public static int MAIN = DPScript.MAIN;
	public static int RIGHTSUPER = DPScript.RIGHTSUPER;
	public static int RIGHTSUB = DPScript.RIGHTSUB;
	
	public static int NUMCHILDREN = DPScript.NUMCHILDREN;
	
	
	protected Element[] children;
	
	
	
	public ScriptElement()
	{
		this( ScriptStyleSheet.defaultStyleSheet );
	}
	
	public ScriptElement(ScriptStyleSheet styleSheet)
	{
		super( new DPScript( styleSheet ) );
		
		children = new Element[NUMCHILDREN];
	}




	public Element getChild(int slot)
	{
		return children[slot];
	}
	
	public void setChild(int slot, Element child)
	{
		Element existingChild = children[slot];
		if ( child != existingChild )
		{
			if ( existingChild != null )
			{
				existingChild.setParent( null );
				existingChild.setElementTree( null );
			}
			
			children[slot] = child;
			DPWidget childWidget = null;
			if ( child != null )
			{
				childWidget = child.getWidget();
			}
			getWidget().setChild( slot, childWidget );
			
			
			if ( child != null )
			{
				child.setParent( this );
				child.setElementTree( tree );
			}
		}
	}
	
	
	
	
	public Element getMainChild()
	{
		return getChild( MAIN );
	}
	
	public Element getLeftSuperscriptChild()
	{
		return getChild( LEFTSUPER );
	}
	
	public Element getLeftSubscriptChild()
	{
		return getChild( LEFTSUB );
	}
	
	public Element getRightSuperscriptChild()
	{
		return getChild( RIGHTSUPER );
	}
	
	public Element getRightSubscriptChild()
	{
		return getChild( RIGHTSUB );
	}
	
	
	public void setMainChild(Element child)
	{
		setChild( MAIN, child );
	}
	
	public void setLeftSuperscriptChild(Element child)
	{
		setChild( LEFTSUPER, child );
	}
	
	public void setLeftSubscriptChild(Element child)
	{
		setChild( LEFTSUB, child );
	}
	
	public void setRightSuperscriptChild(Element child)
	{
		setChild( RIGHTSUPER, child );
	}
	
	public void setRightSubscriptChild(Element child)
	{
		setChild( RIGHTSUB, child );
	}



	public DPScript getWidget()
	{
		return (DPScript)widget;
	}


	protected List<Element> getChildren()
	{
		Vector<Element> xs = new Vector<Element>();
		
		for (int slot = 0; slot < NUMCHILDREN; slot++)
		{
			if ( children[slot] != null )
			{
				xs.add( children[slot] );
			}
		}
		
		return xs;
	}



	public String getContent()
	{
		return getWidget().getContent();
	}
	
	public int getContentLength()
	{
		return getWidget().getContentLength();
	}
}
