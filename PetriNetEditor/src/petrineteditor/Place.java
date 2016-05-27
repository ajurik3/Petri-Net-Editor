package petrineteditor;

import java.util.ArrayList;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Place extends PNode {
	
	private int numTokens;
	private ArrayList<Token> tokens = new ArrayList<Token>();
	
	public Place(double x, double y){
		super(placeShape());
		xCenter = x;
		yCenter = y;
		this.relocate(xCenter, yCenter);
		
		nameLabel.relocate(xCenter + 3.0, yCenter + 30.0);
		
		this.setStyle(	"-fx-min-width: 20px; " +
						"-fx-min-height: 20px; " +
						"-fx-max-width: 20px; " +
                		"-fx-max-height: 20px;");
		
	}
	
	private static Circle placeShape(){
		Circle placeShape = new Circle(20.0);
		placeShape.setFill(Color.WHITE);
		placeShape.setStroke(Color.BLACK);
		placeShape.setStrokeWidth(1.5);
		
		return placeShape;
	}
	
	public static ContextMenu getPlaceMenu(){
		ContextMenu menu = new ContextMenu();
		
		MenuItem edit = new MenuItem("Edit");
		
		edit.setOnAction(e->{
			System.out.println("Hello");
		});
		
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(e ->{
			
		});
		
		return menu;
	}
	
	public void setNumTokens(int newNum){
		numTokens = newNum;
	}
	
	public int getNumTokens(){
		return numTokens;
	}
	
	public ArrayList<Token> getTokens(){
		return tokens;
	}
	
	public void addTokens(Token tokenGraphic){
			tokens.add(tokenGraphic);
	}
	
	public void clearTokens(){
		if(tokens!=null){
			for(Token token: tokens){
				tokens.remove(token);
			}
		}
	}
	
	@Override
	public String getTypeSelector(){
		return "Place";

	}
}