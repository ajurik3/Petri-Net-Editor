package petrineteditor;

import java.util.ArrayList;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import petrinet.PetriNet;

public class TokenManager {

	
	private Pane layout;
	private PetriNet net;
	private final int MAX_TOKENS = 99;
	
	public TokenManager(Pane pane, PetriNet petrinet){
		layout = pane;
		this.net = petrinet;
	}
	
	public void setMarking(int[] tokens){
		
		ArrayList<Place> places = net.getPlaces();
		
		for(int i = 0; i < tokens.length; i++){
			if(tokens[i]>MAX_TOKENS){
				setTokens(places.get(i), MAX_TOKENS, false);
				places.get(i).setNumTokens(MAX_TOKENS);
			}
			else{
				setTokens(places.get(i), tokens[i], false);
				places.get(i).setNumTokens(tokens[i]);
			}
		}
		updateTransitions();
	}
	
	public void changeMarking(int[] tokens){
		
		ArrayList<Place> places = net.getPlaces();
		
		for(int i = 0; i < tokens.length; i++){
			if(tokens[i]+places.get(i).getNumTokens()>MAX_TOKENS){
				setTokens(places.get(i), MAX_TOKENS, false);
				places.get(i).setNumTokens(MAX_TOKENS);
			}
			else{
				//System.out.println("Before: "+ places.get(i).getNumTokens() + ", After: " + (tokens[i]+places.get(i).getNumTokens()));
				setTokens(places.get(i), tokens[i]+places.get(i).getNumTokens(), false);
			}
		}
		
		updateTransitions();
	}
	
	//removes all tokens of target from pane and adds those which represent the new marking
	public void setTokens(Place target, int num, boolean singleChange){
		if(num>=0&&num<=MAX_TOKENS){
			
			if(target.getTokens().size()==0){
				//place does not have Token markings container initialized
				initialize(target);
			}
			
			ArrayList<Token> tokenList = target.getTokens();
		
			if(tokenList!=null){
				for( Token token : tokenList){
					layout.getChildren().remove(token.getShape());
				}
			}
		
			if(num==1){
				layout.getChildren().addAll(tokenList.get(4).getShape());
			}
			else if(num==2){
				layout.getChildren().addAll(tokenList.get(5).getShape());
				layout.getChildren().addAll(tokenList.get(6).getShape());
			}
			else if(num==3){
				layout.getChildren().addAll(tokenList.get(7).getShape());
				layout.getChildren().addAll(tokenList.get(8).getShape());
				layout.getChildren().addAll(tokenList.get(9).getShape());
			}
			else if(num==4){
				layout.getChildren().addAll(tokenList.get(0).getShape());
				layout.getChildren().addAll(tokenList.get(1).getShape());
				layout.getChildren().addAll(tokenList.get(2).getShape());
				layout.getChildren().addAll(tokenList.get(3).getShape());
			}
			else if(num==5){
				layout.getChildren().addAll(tokenList.get(0).getShape());
				layout.getChildren().addAll(tokenList.get(1).getShape());
				layout.getChildren().addAll(tokenList.get(2).getShape());
				layout.getChildren().addAll(tokenList.get(3).getShape());
				layout.getChildren().addAll(tokenList.get(4).getShape());
			}
			else if(num>5&&num<10){
				((Text)tokenList.get(10).getShape()).setText("" + num);
				layout.getChildren().addAll(tokenList.get(10).getShape());
			}
			else if (num>9){
				((Text)tokenList.get(11).getShape()).setText("" + num);
				layout.getChildren().addAll(tokenList.get(11).getShape());
			}
		
			target.setNumTokens(num);
		}
		
		//only update transitions via this function if called for an individual marking change
		if(singleChange)
			updateTransitions();
	}
	
	public void updateTransitions(){
		
		ArrayList<ArrayList<Integer>> outputTokens = net.getOutputTokens();
		ArrayList<Place> places = net.getPlaces();
		ArrayList<Transition> transitions = net.getTransitions();
		
		for(Transition transition : transitions){
			transition.setEnabled(true);
		}
		
		//set status to not enabled for all transitions whose input places have insufficient tokens
		for(int i = 0; i < places.size(); i++){
			for(int j = 0; j < transitions.size(); j++){
				if(places.get(i).getNumTokens()<outputTokens.get(i).get(j))
					transitions.get(j).setEnabled(false);
			}
		}
		
		//printTransitionStatus(transitions);
	}
	
	//adds all Tokens to parameter target which may be needed to represent its number of tokens
	private void initialize(Place target){
		
		//1, 4, and 5 token arrangement
		target.getTokens().add(new Token(new Circle(target.getX() + 2.0, target.getY() + 18.0, 4.0, Color.BLACK)));
		target.getTokens().add(new Token(new Circle(target.getX() + 18.0, target.getY() + 18.0, 4.0, Color.BLACK)));
		target.getTokens().add(new Token(new Circle(target.getX() + 2.0, target.getY() + 2.0, 4.0, Color.BLACK)));
		target.getTokens().add(new Token(new Circle(target.getX() + 18.0, target.getY() + 2.0, 4.0, Color.BLACK)));		
		target.getTokens().add(new Token(new Circle(target.getX()+10.0, target.getY()+10.0, 4.0, Color.BLACK)));
		
		//two token arrangement
		target.getTokens().add(new Token(new Circle(target.getX() + 2.5, target.getY() + 10.0, 4.0, Color.BLACK)));
		target.getTokens().add(new Token(new Circle(target.getX() + 17.5, target.getY() + 10.0, 4.0, Color.BLACK)));
		
		//three token arrangement
		target.getTokens().add(new Token(new Circle(target.getX() + 2.5, target.getY() + 17.5, 4.0, Color.BLACK)));
		target.getTokens().add(new Token(new Circle(target.getX() + 17.5, target.getY() + 17.5, 4.0, Color.BLACK)));
		target.getTokens().add(new Token(new Circle(target.getX() + 10.0, target.getY() + 2.5, 4.0, Color.BLACK)));
		
		//single digit representation
		Text singleNumShape = new Text(target.getX()+4.0, target.getY()+17.0, ""+6);
		singleNumShape.setFont(new Font(20.0));
		target.addTokens(new Token(singleNumShape));
		
		//double digit representation
		Text doubleNumShape = new Text(target.getX()-1.0, target.getY()+17.0, ""+10);
		doubleNumShape.setFont(new Font(20.0));
		target.addTokens(new Token(doubleNumShape));
	}
	
	//testing
	private void printTransitionStatus(ArrayList<Transition> transitions){
		for(Transition transition : transitions){
			if(transition.isEnabled())
				System.out.println(transition.getName() + ": Enabled");
			else
				System.out.println(transition.getName() + ": Disabled");
		}
	}
}