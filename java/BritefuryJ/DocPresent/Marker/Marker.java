package BritefuryJ.DocPresent.Marker;

import BritefuryJ.DocPresent.DPContentLeaf;

public class Marker
{
	public enum Bias { START, END };
	
	
	protected DPContentLeaf widget;
	protected int position;
	protected Bias bias;
	protected MarkerListener listener;
	
	
	
	public Marker()
	{
		this.widget = null;
		this.position = 0;
		this.bias = Bias.START;
	}
	
	public Marker(DPContentLeaf widget, int position, Bias bias)
	{
		this.widget = widget;
		this.position = position;
		this.bias = bias;
	}
	
	
	public void setMarkerListener(MarkerListener listener)
	{
		this.listener = listener;
	}
	
	
	
	public DPContentLeaf getWidget()
	{
		return widget;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public Bias getBias()
	{
		return bias;
	}
	
	
	public void setPosition(int position)
	{
		this.position = position;
		changed();
	}
	
	public void setPositionAndBias(int position, Bias bias)
	{
		this.position = position;
		this.bias = bias;
		changed();
	}
	
	public void set(DPContentLeaf widget, int position, Bias bias)
	{
		this.widget = widget;
		this.position = position;
		this.bias = bias;
		changed();
	}
	
	
	public int getIndex()
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	public static int getIndex(int position, Bias bias)
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	
	
	public boolean isValid()
	{
		return widget != null;
	}
	
	
	
	protected void changed()
	{
		if ( listener != null )
		{
			listener.markerChanged( this );
		}
	}




	public Marker clone()
	{
		try {
			return (Marker)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
}
