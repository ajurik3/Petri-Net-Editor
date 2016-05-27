package petrineteditor;

import javafx.geometry.Point2D;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;

public class Arc extends Polyline{
	
	private PNode startNode;
	private PNode endNode;
	private int weight;
	private Text weightLabel = new Text();
	
	public Arc(PNode start, PNode end){
		super();
		this.setStrokeWidth(3.5);
		startNode = start;
		endNode = end;
		weight = 1;
	}
	
	public void setWeight(int w){
		weight = w;
		weightLabel.setText("" + weight);
	}
	
	public int getWeight(){
		return weight;
	}
	
	public PNode getStartNode(){
		return startNode;
	}
	
	public PNode getEndNode(){
		return endNode;
	}
	
	public Text getWeightLabel(){
		return weightLabel;
	}
	
	public void initArcLabel(){
		weightLabel.setText("" + weight);
		int numPoints = this.getPoints().size();
		
		/*System.out.println(startNode.getName() + " to " + endNode.getName());
		
		for(double point : this.getPoints()){
			System.out.print(point + " ");
		}
		
		System.out.println("");*/
		
		
		Point2D intersection = new Point2D(this.getPoints().get(numPoints-4),      //arrow head x
										   this.getPoints().get(numPoints-3));     //arrow head y
		
		Point2D beginLastSegment = new Point2D(this.getPoints().get(numPoints-12),      //beginning of last segment x
				   							   this.getPoints().get(numPoints-11)); 	//beginning of last segment y
		
		Point2D labelLocation = intersection.midpoint(beginLastSegment);
		
		weightLabel.relocate(labelLocation.getX() + 2.5, labelLocation.getY() + 2.5);
	}
}