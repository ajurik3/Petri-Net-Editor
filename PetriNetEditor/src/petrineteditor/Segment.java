package petrineteditor;
import javafx.scene.shape.Line;

public class Segment extends Line{

	private PNode startNode;
	
	public Segment(PNode start){
		super(start.getX(), start.getY(), start.getX(), start.getY());
		
		if(start.getTypeSelector()=="Place"){
			this.setStartX(start.getX()+10.0);
			this.setStartY(start.getY()+10.0);
		}
		else{
			this.setStartX(start.getX()+5.0);
			this.setStartY(start.getY()+20.0);
		}
		
		startNode = start;
	}
	public Segment(PNode start, double x, double y){
		super(x, y, x, y);
		startNode = start;
	}
	public PNode getStartNode(){
		return startNode;
	}
}