package BritefuryJ.DocPresent.ElementTree;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class BinElement extends BranchElement
{
	protected Element child;
	
	
	public BinElement(ContainerStyleSheet styleSheet)
	{
		this( new DPBin( styleSheet ) );
	}

	protected BinElement(DPBin widget)
	{
		super( widget );
	}
	
	
	public void setChild(Element child)
	{
		if ( child != this.child )
		{
			if ( this.child != null )
			{
				this.child.setParent( null );
			}
			this.child = child;
			this.child.setParent( this );
			
			DPWidget childWidget = null;
			if ( child != null )
			{
				childWidget = child.getWidget();
			}
			getWidget().setChild( childWidget );
		}
	}
	

	
	public DPBin getWidget()
	{
		return (DPBin)widget;
	}
}
