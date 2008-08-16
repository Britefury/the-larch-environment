package BritefuryJ.Cell;


public class CellEvaluatorLiteral implements CellEvaluator {
	private Object value;
	
		
	
	public CellEvaluatorLiteral(Object value) {
		super();
		this.value = value;
	}


	public Object evaluate() {
		return value;
	}

}
