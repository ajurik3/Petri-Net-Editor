package petrineteditor;

import javafx.scene.shape.Shape;

public class Token {
	
	private Shape tokenShape;
	
	Token(Shape shape){
		tokenShape = shape;
	}
	
	public Shape getShape(){
		return tokenShape;
	}
}