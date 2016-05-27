package petrineteditor;

import javafx.scene.control.Button;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

public abstract class PNode extends Button {

	protected double xCenter, yCenter;
	protected String name;
	protected Text nameLabel = new Text();
	
	public PNode(Shape shape){
		super("", shape);
		
		this.setOnMousePressed(e ->{
			e.consume();
		});
	}
	
	public double getX(){
		return xCenter;
	}
	
	public double getY(){
		return yCenter;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String newName){
		name = newName;
		nameLabel.setText(newName);
		nameLabel.setX( nameLabel.getX() -  nameLabel.getLayoutBounds().getWidth() / 3 );
	}
	
	public Text getNameLabel(){
		return nameLabel;
	}
}