package petrineteditor;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Transition extends PNode {
	
	private boolean enabled;
	private int priority;
	
	public Transition(double x, double y){
		super(transitionShape());
		
		xCenter = x-5.0;
		yCenter = y-20.0;
		
		this.relocate(xCenter, yCenter);
		enabled = false;
		nameLabel.relocate(xCenter, yCenter + 40.0);
		
		this.setStyle(	"-fx-min-width: 10px; " +
						"-fx-min-height: 40px; " +
						"-fx-max-width: 10px; " +
                		"-fx-max-height: 40px;");
	}
	
	private static Rectangle transitionShape(){
		Rectangle transitionShape = new Rectangle(10.0, 40.0);
		transitionShape.setFill(Color.WHITE);
		transitionShape.setStroke(Color.BLACK);
		transitionShape.setStrokeWidth(1.5);
		return transitionShape;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void setEnabled(boolean status){
		enabled = status;
	}
		
	public void setPriority(int newPriority){
		priority = newPriority;
	}
	
	public int getPriority(){
		return priority;
	}
	
	@Override
	public String getTypeSelector(){
		return "Transition";

	}

}