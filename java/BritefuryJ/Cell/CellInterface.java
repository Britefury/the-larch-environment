package BritefuryJ.Cell;


public interface CellInterface {
	public CellEvaluator getEvaluator();
	public void setEvaluator(CellEvaluator eval);
	
	public Object getLiteralValue();
	public void setLiteralValue(Object value);
	public boolean isLiteral();
	
	public Object getValue();
	public boolean isValid();
}
