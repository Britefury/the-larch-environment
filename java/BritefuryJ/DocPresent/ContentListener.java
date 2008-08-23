package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Marker.Marker;

public interface ContentListener
{
	public void contentInserted(Marker m, String x);
	public void contentRemoved(Marker m, int length);
	public void contentReplaced(Marker m, int length, String x);
}
