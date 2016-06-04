package petrinet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;
import javafx.stage.Stage;
import petrineteditor.Place;
import petrineteditor.Arc;
import petrineteditor.Transition;
import petrineteditor.Token;
import petrineteditor.TokenManager;

public class PetriNet {
	
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	
	//change in marking for place P and transition T when transition T is fired
	//P's index in outer ArrayList is equal to its index in places  
	private ArrayList<ArrayList<Integer>> inputTokens = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> outputTokens = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> incidence = new ArrayList<ArrayList<Integer>>();
	
	private Pane layout;
	private File saved;		//File Petri Net is saved to
	
	public PetriNet(Pane pane){
		layout = pane;
	}
	
	public void load(File source) throws IOException{
		clear();
		Scanner in = new Scanner(source);
		
		while(in.hasNext()){
			
			String type = in.next();
						
			if(type.equals("Place")){
				Place newPlace = new Place(in.nextDouble(), in.nextDouble());
				addPlace(newPlace);
				newPlace.setNumTokens(in.nextInt());
				newPlace.setName(in.next());
			}
			else if (type.equals("Transition")){
				Transition newTransition = new Transition(in.nextDouble(), in.nextDouble());
				addTransition(newTransition);
				newTransition.setPriority(in.nextInt());
				newTransition.setName(in.next());
			}
			else if(type.equals("Output")){
				Place startNode = places.get(in.nextInt());
				Transition endNode = transitions.get(in.nextInt());
				int weight = in.nextInt();
				int numPoints = in.nextInt();
				
				Arc newArc = new Arc(startNode, endNode);
				for(int i = 0; i < numPoints; i++)
					newArc.getPoints().add(in.nextDouble());
				
				addArc(newArc);
				setArcWeight(newArc, weight);
				newArc.initArcLabel();
			}
			else if(type.equals("Input")){
				Place endNode = places.get(in.nextInt());
				Transition startNode = transitions.get(in.nextInt());
				int weight = in.nextInt();
				int numPoints = in.nextInt();
				
				Arc newArc = new Arc(startNode, endNode);
				for(int i = 0; i < numPoints; i++)
					newArc.getPoints().add(in.nextDouble());
				addArc(newArc);
				setArcWeight(newArc, weight);
				newArc.initArcLabel();
			}
		}
		
		int[] marking = new int[places.size()];
		
		for(int i = 0; i < marking.length; i++)
			marking[i] = places.get(i).getNumTokens();
		
		TokenManager manager = new TokenManager(layout, this);
		manager.setMarking(marking);
		
		saved = source;
		in.close();
	}
	
	public void saveAs(File dest)throws IOException{
		try{
			dest.createNewFile();
			saved = dest;
		}
		catch(IOException e){
			Stage fileError = new Stage();
			fileError.setTitle("File Creation Error");
			BorderPane errorPane = new BorderPane();
			errorPane.setPrefWidth(500.0);
			
			Button errorOK = new Button("OK");
			
			errorOK.setOnAction(t -> {
				fileError.close();
			});
			
			errorPane.setTop(new Label("The specified file could not be created."));
			errorPane.setBottom(errorOK);
			BorderPane.setAlignment(errorOK, Pos.CENTER);
			BorderPane.setMargin(errorPane, new Insets(20, 0, 20, 0));

			fileError.setScene(new Scene(errorPane));
			fileError.show();
		}
		catch(NullPointerException e){
			Stage fileError = new Stage();
			fileError.setTitle("Invalid File Name");
			BorderPane errorPane = new BorderPane();
			errorPane.setPrefWidth(500.0);
			
			Button errorOK = new Button("OK");
			
			errorOK.setOnAction(t -> {
				fileError.close();
			});
			
			errorPane.setTop(new Label("The specified file could not be created."));
			errorPane.setBottom(errorOK);
			BorderPane.setAlignment(errorOK, Pos.CENTER);
			BorderPane.setMargin(errorPane, new Insets(20, 0, 20, 0));

			fileError.setScene(new Scene(errorPane));
			fileError.show();
		}
		save();
	}
	
	public boolean currentFileExists(){
		return saved!=null;
	}
	
	/*
	 * Saves Petri Net component's delimited by lines with each of the component's properties delimited by a space
	 * Components appear in the following order: places, transitions, output arcs, input arcs
	 * 
	 * Format for each component is as follows:
	 * 
	 * Place X-Coordinate Y-Coordinate NumberOfTokens Name
	 * Transition X-Coordinate Y-Coordinate Priority Name
	 * Output StartNode EndNode Weight [PointX-Coordinate PointY-Coordinate]+ 
	 * Input EndNode StartNode Weight [PointX-Coordinate PointY-Coordinate]+ 
	 * 
	 */
	public void save() throws IOException{
		
		PrintWriter out = new PrintWriter(saved);
		
		for(Place place : places){
			out.print("Place ");
			out.print(place.getX() + " " + place.getY() + " ");
			out.print(place.getNumTokens() + " ");
			out.println(place.getName());
		}
			
		for(Transition transition : transitions){
			out.print("Transition ");
			out.print((transition.getX()+5) + " " + (transition.getY()+20) + " ");
			out.print(transition.getPriority() + " ");
			out.println(transition.getName());
		}
						
		for(Arc arc : arcs){
			
			if(places.indexOf(arc.getStartNode())!=-1){
				out.print("Output ");
				out.print(places.indexOf(arc.getStartNode()) + " " + transitions.indexOf(arc.getEndNode()) + " ");
				out.print(arc.getWeight() + " ");
				out.print(arc.getPoints().size() + " ");
				for(double point: arc.getPoints()){
					out.print(point + " ");
				}
				out.println("");
			}
			else{
				out.print("Input ");
				out.print(places.indexOf(arc.getEndNode()) + " " + transitions.indexOf(arc.getStartNode()) + " ");
				out.print(arc.getWeight() + " ");
				out.print(arc.getPoints().size() + " ");
				for(double point: arc.getPoints()){
					out.print(point + " ");
				}
				out.println("");
			}
		}
		out.close();
	}
	
	public void addPlace(Place newPlace){
		places.add(newPlace);
		newPlace.setName("P" + (places.size()-1));
		
		ArrayList<Integer> placeInput = new ArrayList<Integer>(transitions.size());
		ArrayList<Integer> placeOutput = new ArrayList<Integer>(transitions.size());
		ArrayList<Integer> inc = new ArrayList<Integer>(transitions.size());
		
		for(int i = 0; i < transitions.size(); i++){
			placeInput.add(0);
			placeOutput.add(0);
			inc.add(0);
		}
		inputTokens.add(placeInput);
		outputTokens.add(placeOutput);
		incidence.add(inc);
		
		layout.getChildren().addAll(newPlace, newPlace.getNameLabel());
		//printMatrices();
	}
	
	//removes place, all arcs connected to it, and all tokens displayed for the place
	public void removePlace(Place target){
		
		ArrayList<Integer> remove = new ArrayList<Integer>();
		
		
		for(int i = 0; i < arcs.size(); i++){
			if(arcs.get(i).getStartNode()==target||arcs.get(i).getEndNode()==target){
				remove.add(0, i);
			}
		}
		
		for(int i = 0; i < remove.size(); i++){
			removeArc(arcs.get((remove.get(i))));
		}
		
		
		ArrayList<Token> removedTokens = target.getTokens();
		
		for(Token token: removedTokens){
			if(layout.getChildren().indexOf(token.getShape())!=-1)
				layout.getChildren().remove(token.getShape());
		}
		
		inputTokens.remove(places.indexOf(target));
		outputTokens.remove(places.indexOf(target));
		incidence.remove(places.indexOf(target));
		places.remove(target);
		layout.getChildren().remove(target.getNameLabel());
		layout.getChildren().remove(target);
		
	}
	
	public void addTransition(Transition newTransition){
		transitions.add(newTransition);
		newTransition.setPriority(transitions.size());
		newTransition.setName("T" + (transitions.size()-1));			
		
		for(int i = 0; i < places.size(); i++){
			inputTokens.get(i).add(0);
			outputTokens.get(i).add(0);
			incidence.get(i).add(0);
		}
		
		layout.getChildren().addAll(newTransition, newTransition.getNameLabel());
		//printMatrices();
	}
	
	//removes transition and all arcs connected to it
	public void removeTransition(Transition target){
		int index = transitions.indexOf(target);
		
		ArrayList<Integer> removedArcs = new ArrayList<Integer>();

		
		for(int i = 0; i < arcs.size(); i++){
			if(arcs.get(i).getStartNode()==target||arcs.get(i).getEndNode()==target){
				removedArcs.add(0, i);
			}
		}
		
		for(int i = 0; i < removedArcs.size(); i++){
			removeArc(arcs.get((removedArcs.get(i))));
		}
		
		for(int i = 0; i < places.size(); i++){
			inputTokens.get(i).remove(index);
			outputTokens.get(i).remove(index);
			incidence.get(i).remove(index);
		}
		
		transitions.remove(target);
		layout.getChildren().remove(target.getNameLabel());
		layout.getChildren().remove(target);
	}
	
	public void addArc(Arc newArc){
		
		int row = 0;
		int col = 0;
				
		if(places.indexOf(newArc.getStartNode())!=-1){
			row = places.indexOf(newArc.getStartNode());
			col = transitions.indexOf(newArc.getEndNode());

			outputTokens.get(row).set(col, outputTokens.get(row).get(col) + 1);
			
			if(outputTokens.get(row).get(col)==1){
				arcs.add(newArc);
				layout.getChildren().add(0, (Polyline)newArc);
			}
			else if(outputTokens.get(row).get(col)>1){
				Arc existingArc = findOutputArc(places.get(row), transitions.get(col));
				setArcWeight(existingArc, existingArc.getWeight()+1);
				if(layout.getChildren().indexOf(existingArc.getWeightLabel())==-1)
					layout.getChildren().add(existingArc.getWeightLabel());
			}
		}
		else{
			row = places.indexOf(newArc.getEndNode());
			col = transitions.indexOf(newArc.getStartNode());
			inputTokens.get(row).set(col, inputTokens.get(row).get(col) + 1);
			
			if(inputTokens.get(row).get(col)==1){
				arcs.add(newArc);
				layout.getChildren().add(0, (Polyline)newArc);
			}
			else if(inputTokens.get(row).get(col)>1){
				Arc existingArc = findInputArc(transitions.get(col), places.get(row));
				setArcWeight(existingArc, existingArc.getWeight()+1);
				if(layout.getChildren().indexOf(existingArc.getWeightLabel())==-1)
					layout.getChildren().add(existingArc.getWeightLabel());
			}
		}
		
		incidence.get(row).set(col, inputTokens.get(row).get(col)-outputTokens.get(row).get(col));
		
		//printMatrices();
	}
	
	private Arc findInputArc(Transition startNode, Place endNode){
		Arc foundArc = new Arc(startNode, endNode);   //variable must be initialized
		
		for(Arc arc: arcs){
			if(arc.getStartNode()==startNode&&arc.getEndNode()==endNode)
				foundArc = arc;
		}
		
		return foundArc;
	}
	
	private Arc findOutputArc(Place startNode, Transition endNode){
		Arc foundArc = new Arc(startNode, endNode);   //variable must be initialized
		
		for(Arc arc: arcs){
			if(arc.getStartNode()==startNode&&arc.getEndNode()==endNode)
				foundArc = arc;
		}
		
		return foundArc;
	}
	
	public void removeArc(Arc target){
		
		int row = 0;
		int col = 0;
		
		if(places.indexOf(target.getStartNode())!=-1){
			//update (P, T) arc
			row = places.indexOf(target.getStartNode());
			col = transitions.indexOf(target.getEndNode());
			outputTokens.get(row).set(col, 0);
		}
		else{
			//update (T, P) arc
			row = places.indexOf(target.getEndNode());
			col = transitions.indexOf(target.getStartNode());
			inputTokens.get(row).set(col, 0);
		}
		
		incidence.get(row).set(col, inputTokens.get(row).get(col)-outputTokens.get(row).get(col));
		arcs.remove(target);
		if(layout.getChildren().indexOf(target.getWeightLabel())!=-1)
			layout.getChildren().remove(target.getWeightLabel());
		layout.getChildren().remove((Polyline)target);
	}
	
	public void setArcWeight(Arc target, int weight){
		int row = 0;
		int col = 0;
		
		if(places.indexOf(target.getStartNode())!=-1){
			//update (P, T) arc
			row = places.indexOf(target.getStartNode());
			col = transitions.indexOf(target.getEndNode());
			outputTokens.get(row).set(col, weight);
		}
		else{
			//update (T, P) arc
			row = places.indexOf(target.getEndNode());
			col = transitions.indexOf(target.getStartNode());
			inputTokens.get(row).set(col, weight);
		}
		
		incidence.get(row).set(col, inputTokens.get(row).get(col)-outputTokens.get(row).get(col));
		target.setWeight(weight);
		
		//remove weight label if 0 or 1
		if(layout.getChildren().indexOf(target.getWeightLabel())!=-1){
			if(target.getWeight()<2)
				layout.getChildren().remove(target.getWeightLabel());
		}
		else{
			if(target.getWeight()>1)
				layout.getChildren().add(target.getWeightLabel());
		}
	}
	
	public ArrayList<Place> getPlaces(){
		return places;
	}
	
	public ArrayList<Transition> getTransitions(){
		return transitions;
	}
	
	public ArrayList<Arc> getArcs(){
		return arcs;
	}
	
	public ArrayList<ArrayList<Integer>> getInputTokens(){
		return inputTokens;
	}
	
	public ArrayList<ArrayList<Integer>> getOutputTokens(){
		return outputTokens;
	}
	
	public ArrayList<ArrayList<Integer>> getIncidence(){
		return incidence;
	}
	
	public void clear(){
		places.clear();
		transitions.clear();
		arcs.clear();
		inputTokens.clear();
		outputTokens.clear();
		incidence.clear();
		layout.getChildren().clear();
	}
	
	//testing
	private void printMatrices(){
		
		System.out.println("Input: ");
		
		for(int i = 0; i < inputTokens.size(); i++){
			for(int j = 0; j < inputTokens.get(i).size(); j++){
				System.out.print(inputTokens.get(i).get(j) + " ");
			}	
			System.out.println("");
		}
		
		System.out.println("Output: ");
		
		for(int i = 0; i < outputTokens.size(); i++){
			for(int j = 0; j < outputTokens.get(i).size(); j++){
				System.out.print(outputTokens.get(i).get(j) + " ");
			}	
			System.out.println("");
		}
		
		System.out.println("Incidence: ");
		
		for(int i = 0; i < incidence.size(); i++){
			for(int j = 0; j < incidence.get(i).size(); j++){
				System.out.print(incidence.get(i).get(j) + " ");
			}	
			System.out.println("");
		}
		
	}
}