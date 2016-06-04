package simulation;

import petrineteditor.Transition;
import java.util.ArrayList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import petrinet.PetriNet;
import petrineteditor.TokenManager;
import petrineteditor.Place;
import java.util.Random;

public class Simulation {
	
	private PetriNet net;
	private Pane layout;
	private boolean prioritySimulation = false;		//simulation uses transition priorities if true
	
	private int numFirings;							//number of firings in simulation
	
	public Simulation(PetriNet petrinet, Pane pane){
		net = petrinet;
		layout = pane;
		numFirings = 1;
	}
	
	
	//display menu to initiate simulation
	public void displayMenu(Stage primaryStage){
		Stage simMenu = new Stage();
		simMenu.initOwner(primaryStage);
		simMenu.setTitle("Simulation");
		BorderPane pane = new BorderPane();
		pane.setPrefWidth(400.0);
		Button ok = new Button("OK");
		Button cancel = new Button("Cancel");
		
		HBox firingInput = new HBox(5, new Label("Number of Firings: "), new TextField("" + numFirings));
		((TextField)firingInput.getChildren().get(1)).setPrefColumnCount(5);
		firingInput.setAlignment(Pos.CENTER);
		BorderPane.setMargin(firingInput, new Insets(20.0, 0.0, 20.0, 0.0));
		HBox menuButtons = new HBox(10, ok, cancel);
		menuButtons.setAlignment(Pos.CENTER);
		
		VBox prioritySettings = new VBox(10);
		prioritySettings.setAlignment(Pos.TOP_CENTER);
		
		CheckBox priorityEnabler = new CheckBox("Enable Priority Firing");
		priorityEnabler.setSelected(false);
		
		ArrayList<Transition> transitions = net.getTransitions();
		
		Stage priorityMenu = getPriorityStage(transitions, simMenu);
		
		priorityEnabler.setOnAction(e -> {
			if(priorityEnabler.isSelected()){
				priorityMenu.show();
				prioritySimulation = true;
			}
			else{
				priorityMenu.close();
				prioritySimulation = false;
			}
		});
		
		prioritySettings.getChildren().add(priorityEnabler);
		
		pane.setTop(firingInput);
		pane.setCenter(prioritySettings);
		pane.setBottom(menuButtons);
		BorderPane.setMargin(menuButtons, new Insets(20.0, 0.0, 0.0, 0.0));
		
		ok.setOnAction(n ->{
			
			try{
				TextField firingsInput = ((TextField)((HBox)pane.getTop()).getChildren().get(1));
			
				numFirings = Integer.parseInt(firingsInput.getText());
					
					if(numFirings<1){
						numFirings = 1;
					}
					
					//start random simulation or priority simulation
					if(!prioritySimulation)
						this.start();
					else
						this.startPriority();
					
			}
			catch(NumberFormatException x){
				
				//show error, invalid firings input
				Stage firingError = new Stage();
				firingError.initOwner(simMenu);
				firingError.setTitle("Invalid Number of Firings");
				BorderPane errorPane = new BorderPane();
				errorPane.setPrefWidth(500.0);
				
				Button errorOK = new Button("OK");
				
				errorOK.setOnAction(t -> {
					firingError.close();
				});
				
				errorPane.setTop(new Label("Firings must be set to integer greater than 0."));
				errorPane.setBottom(errorOK);
				BorderPane.setAlignment(errorOK, Pos.CENTER);
				BorderPane.setMargin(errorPane, new Insets(20, 0, 20, 0));
	
				firingError.setScene(new Scene(errorPane));
				firingError.show();
			}
		});
	
		cancel.setOnAction(n->{
			simMenu.close();
		});
		
		simMenu.setScene(new Scene(pane));
		simMenu.show();
	}
	
	//returns a window where user can set priority of each transition for the simulation
	//with a TextField
	private Stage getPriorityStage(ArrayList<Transition> transitions, Stage ownerStage){
		
		Stage prioritySettings = new Stage();
		prioritySettings.setTitle("Priority Settings");
		prioritySettings.initOwner(ownerStage);
		prioritySettings.setWidth(400.0);

		VBox priorityContainer = new VBox(20);
		priorityContainer.setAlignment(Pos.CENTER);
		
		GridPane transitionSettings = new GridPane();
		transitionSettings.setAlignment(Pos.CENTER);
		transitionSettings.setHgap(50.0);
		transitionSettings.setVgap(10.0);
		
		transitionSettings.addRow(0, new Text("Name"), new Text("Transition Priority"));
		
		
		//display rows with transition name a TextField for each transition
		for(int i = 0; i < transitions.size(); i++)
			transitionSettings.addRow(i+1, new Text(transitions.get(i).getName()),
								  new TextField("" + transitions.get(i).getPriority()));
		
		//center all nodes and set TextField sizes
		for(Node node : transitionSettings.getChildren()){
			GridPane.setHalignment(node, HPos.CENTER);
			
			if((GridPane.getColumnIndex(node)!=0)&&(GridPane.getRowIndex(node)!=0))
				//contains TextField
				((TextField)node).setPrefColumnCount(5);
		}
		
		Button okButton = new Button("OK");
		
		
		okButton.setOnAction(e ->{
			for(Node node : transitionSettings.getChildren()){
				if((GridPane.getColumnIndex(node)!=0)&&(GridPane.getRowIndex(node)!=0)){
					
					//index of transition
					int transNum = GridPane.getRowIndex(node) - 1;
					try{
						
						//set priority of current transition
						if((Integer.parseInt(((TextField)node).getText())) > 0)
							transitions.get(transNum).setPriority(Integer.parseInt(((TextField)node).getText()));
						else{
							//display error, invalid priority input entered
							
							prioritySimulation = false;
							
							Stage priorityError = new Stage();
							priorityError.initOwner(prioritySettings);
							priorityError.setTitle("Invalid Priority Number");
							BorderPane errorPane = new BorderPane();
							errorPane.setPrefWidth(600.0);
						
							Button errorOK = new Button("OK");
						
							errorOK.setOnAction(t -> {
								priorityError.close();
							});
							
							errorPane.setTop(new Label("Invalid priority input: Transition " + transitions.get(transNum).getName() + " priority not changed"));
							BorderPane.setAlignment(errorPane.getTop(), Pos.CENTER);
							errorPane.setCenter(new Label("Priority number must be set to integer between 1 and 10000."));
							errorPane.setBottom(errorOK);
							BorderPane.setAlignment(errorOK, Pos.CENTER);
							BorderPane.setMargin(errorPane.getCenter(), new Insets(20, 0, 20, 0));
			
							priorityError.setScene(new Scene(errorPane));
							priorityError.show();
						}
					}
					catch(NumberFormatException x)
					{
						//display error, invalid priority input entered
						
						prioritySimulation = false;
						
						Stage priorityError = new Stage();
						priorityError.initOwner(prioritySettings);
						priorityError.setTitle("Invalid Priority Number");
						BorderPane errorPane = new BorderPane();
						errorPane.setPrefWidth(600.0);
					
						Button errorOK = new Button("OK");
					
						errorOK.setOnAction(t -> {
							priorityError.close();
						});
						
						errorPane.setTop(new Label("Invalid priority input: Transition " + transitions.get(transNum).getName() + " priority not changed"));
						BorderPane.setAlignment(errorPane.getTop(), Pos.CENTER);
						errorPane.setCenter(new Label("Priority number must be set to integer greater than 0."));
						errorPane.setBottom(errorOK);
						BorderPane.setAlignment(errorOK, Pos.CENTER);
						BorderPane.setMargin(errorPane.getCenter(), new Insets(20, 0, 20, 0));
		
						priorityError.setScene(new Scene(errorPane));
						priorityError.show();
					}
				}
			}
			
			if(prioritySimulation)
				//simulation not postponed due to invalid input
				prioritySettings.close();
			else{
				//invalid input, restore previous settings to TextField text
				for(Node node : transitionSettings.getChildren()){
					GridPane.setHalignment(node, HPos.CENTER);
					if((GridPane.getColumnIndex(node)!=0)&&(GridPane.getRowIndex(node)!=0)){
						int transNum = GridPane.getRowIndex(node) - 1;
						
						try{
							if((Integer.parseInt(((TextField)node).getText())) < 1)
								((TextField)node).setText("" + transitions.get(transNum).getPriority());
						}
						catch(NumberFormatException x){
							((TextField)node).setText("" + transitions.get(transNum).getPriority());
						}
					}
				}
				
				prioritySimulation = true;
			}
		});
		
		priorityContainer.getChildren().addAll(transitionSettings, okButton);
		
		prioritySettings.setScene(new Scene(priorityContainer));
		
		return prioritySettings;
	}
	
	private void start(){
		
		ArrayList<Place> places = net.getPlaces();
		int numPlaces = places.size();
		
		int maxTokens[] = new int[numPlaces];
		double averageTokens[] = new double[numPlaces];
		int marking[] = new int[numPlaces];
		
		//initialize arrays with initial marking of places
		for(int i = 0; i < numPlaces; i++){
			marking[i] = places.get(i).getNumTokens();
			averageTokens[i] = marking[i];
			maxTokens[i] = marking[i];
		}
		
		ArrayList<Transition> transitions = net.getTransitions();
		ArrayList<Integer> enabled = new ArrayList<Integer>();		//indices of enabled transitions
		
		updateTransitions(enabled, marking, places, transitions);
		int numEnabled = enabled.size();
				
		if(numEnabled>0){
		
			Random selector = new Random(System.currentTimeMillis());
			ArrayList<ArrayList<Integer>> incidence = net.getIncidence();
			int fireNum = 0;
			
			while((numEnabled>0)&&(fireNum<numFirings)){
				//enabled transition exists and specified number of firings not completed
				
				//index of randomly chosen enabled transition
				int col = Math.abs(selector.nextInt())%numEnabled;
				
				
				for(int i = 0; i < marking.length; i++){
					marking[i] += incidence.get(i).get(enabled.get(col));	//update marking
					averageTokens[i] += marking[i];							//add marking to average
					
					if(marking[i] > maxTokens[i])
						maxTokens[i] = marking[i];							//update any new maximums
				}
			
				//round results
				for(int i = 0; i < numPlaces; i++){
				averageTokens[i] /= (fireNum+1);
				
				averageTokens[i] *= 10000;
				averageTokens[i] = (int)averageTokens[i];
				averageTokens[i] /= 10000;
				}
			
			displayResults(fireNum, places, averageTokens, maxTokens);
			}
		}
		else{
			//no transition enabled
			displayResults(0, places, averageTokens, maxTokens);
		}
	}
	
	private void startPriority(){
		ArrayList<Place> places = net.getPlaces();
		int numPlaces = places.size();
		
		int maxTokens[] = new int[numPlaces];
		double averageTokens[] = new double[numPlaces];
		int marking[] = new int[numPlaces];
		
		//initialize arrays with initial marking of places
		for(int i = 0; i < numPlaces; i++){
			marking[i] = places.get(i).getNumTokens();
			averageTokens[i] = marking[i];
			maxTokens[i] = marking[i];
		}
		
		ArrayList<Transition> transitions = net.getTransitions();
		ArrayList<Integer> enabled = new ArrayList<Integer>();		//indices of enabled transitions
		
		//initialize enabled transitions
		updateTransitionsPriority(enabled, marking, places, transitions);
		int numEnabled = enabled.size();
				
		if(numEnabled>0){
		
			Random selector = new Random(System.currentTimeMillis());
			ArrayList<ArrayList<Integer>> incidence = net.getIncidence();
			int fireNum = 0;
			
			while((numEnabled>0)&&(fireNum<numFirings)){
				//enabled transition exists and specified number of firings not completed
				
				//index of randomly chosen enabled transition
				int col = Math.abs(selector.nextInt())%numEnabled;
				
				
				for(int i = 0; i < marking.length; i++){
					marking[i] += incidence.get(i).get(enabled.get(col));	//update marking
					averageTokens[i] += marking[i];							//add marking to average
					
					if(marking[i] > maxTokens[i])
						maxTokens[i] = marking[i];							//update any new maximums
				}

				updateTransitionsPriority(enabled, marking, places, transitions);
				numEnabled = enabled.size();
				fireNum++;
			}
			
			//round results
			for(int i = 0; i < numPlaces; i++){
				averageTokens[i] /= (fireNum+1);
				
				averageTokens[i] *= 10000;
				averageTokens[i] = (int)averageTokens[i];
				averageTokens[i] /= 10000;
			}
			
			displayResults(fireNum, places, averageTokens, maxTokens);
		}
		else{
			//no transitions enabled
			displayResults(0, places, averageTokens, maxTokens);
		}
	}
	
	
	private void displayResults(int fireNum, ArrayList<Place> places, double[] averageTokens, int[] maxTokens){
		Stage results = new Stage();
		results.setTitle("Simulation Results");
		BorderPane pane = new BorderPane();
		pane.setPrefWidth(400.0);
		
		VBox resultHeader = new VBox();
		
		Text title = new Text("RESULTS");
		title.setFont(new Font(30));
		
		Text fireNumResults = new Text("Number of Firings: " + fireNum);
		
		resultHeader.getChildren().addAll(title, fireNumResults);
		
		Button ok = new Button("OK");
		
		ok.setOnAction(e ->{
			results.close();
		});
		
		pane.setTop(resultHeader);
		resultHeader.setAlignment(Pos.CENTER);

		pane.setBottom(ok);
		BorderPane.setAlignment(ok, Pos.CENTER);
		
		GridPane resultTable = new GridPane();
		resultTable.setAlignment(Pos.CENTER);
		resultTable.setHgap(50.0);
		resultTable.setVgap(10.0);
		
		resultTable.addRow(0, new Text("Name"), new Text("Average Tokens"), new Text("Max Tokens"));
		
		
		for(int i = 0; i < places.size(); i++)
			resultTable.addRow(i+1, new Text(places.get(i).getName()),
								  new Text("" + averageTokens[i]),
								  new Text("" + maxTokens[i]));
		
		for(Node node : resultTable.getChildren())
			if(GridPane.getColumnIndex(node)!=0)
				GridPane.setHalignment(node, HPos.CENTER);
		
		pane.setCenter(resultTable);
		BorderPane.setMargin(resultTable, new Insets(20.0, 0.0, 30.0, 0.0));

		results.setScene(new Scene(pane));
		results.show();
	}
	
	//modifies enabled to contain the indices of all enabled transitions with the marking specified by
	//the marking parameter during a random simulation
	private void updateTransitions(ArrayList<Integer> enabled, int[] marking,
								   ArrayList<Place> places, ArrayList<Transition> transitions){
		
		ArrayList<ArrayList<Integer>> outputTokens = net.getOutputTokens();
		
		enabled.clear();
		
		for(int i = 0; i < transitions.size(); i++){
			enabled.add(i);
		}
		
		for(int i = 0; i < places.size(); i++){
			for(int j = 0; j < transitions.size(); j++){
				if(marking[i]<outputTokens.get(i).get(j))
					enabled.remove(new Integer(j));
			}
		}
	}
	
	//modifies enabled to contain the indices of all enabled transitions with the marking specified by
	//the marking parameter during a priority simulation
	private void updateTransitionsPriority(ArrayList<Integer> enabled, int[] marking,
										   ArrayList<Place> places, ArrayList<Transition> transitions){
		
		ArrayList<ArrayList<Integer>> outputTokens = net.getOutputTokens();
		
		enabled.clear();
		
		for(int i = 0; i < transitions.size(); i++){
			enabled.add(i);
		}
		
		for(int i = 0; i < places.size(); i++){
			for(int j = 0; j < transitions.size(); j++){
				if(marking[i]<outputTokens.get(i).get(j)){
					enabled.remove(new Integer(j));
				}
			}
		}
		
		int maxPriority = Integer.MAX_VALUE;
		//finds maximum priority of enabled transitions
		for(int i = 0; i < enabled.size(); i++){
			if(transitions.get(enabled.get(i)).getPriority() < maxPriority)
				maxPriority = transitions.get(enabled.get(i)).getPriority();
		}
		
		//remove all enabled transitions with less than maximum priority
		for(int i = enabled.size()-1; i >= 0; i--){
			if(transitions.get(enabled.get(i)).getPriority() > maxPriority)
				enabled.remove(new Integer(enabled.get(i)));
		}
	}
	
	//fires transition by changing token marking and updating pane
	public void fireTransition(Transition fired){
		if(fired.isEnabled()){
			
			ArrayList<ArrayList<Integer>> incidence = net.getIncidence();
			int[] markingChange = new int[incidence.size()];
			
			int col = net.getTransitions().indexOf(fired);
			
			for(int i = 0; i < markingChange.length; i++){
				markingChange[i] = incidence.get(i).get(col);
			}
			
			TokenManager manager = new TokenManager(layout, net);
			manager.changeMarking(markingChange);
		}
	}	
}