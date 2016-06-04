package petrineteditor;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import petrinet.PetriNet;
import simulation.Simulation;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.io.IOException;
import java.lang.Math;

import javafx.collections.ObservableList;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class PetriNetEditor extends Application{
	
	private Pane layout = new Pane();
	
	Simulation sim;
	PetriNet net = new PetriNet(layout);
	ToggleGroup selectMode = new ToggleGroup();		//current mode of editor
	
	private Segment currentSegment = null;
	private Polyline potentialArc;					//previously processed Segments
	private Stage stage;
	private TokenManager tokens = new TokenManager(layout, net);
	

	public static void main(String[] args) {
		Application.launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		stage = primaryStage;
		primaryStage.setMaximized(true);
		primaryStage.setTitle("Petri Net Editor");
		
		VBox options = new VBox(10);
		MenuBar mainMenu = getMenuBar(primaryStage);
		HBox toolBar = getToolBar(primaryStage);
		options.getChildren().addAll(mainMenu, toolBar);
		
		BorderPane border = new BorderPane();
		border.setTop(options);
		border.setCenter(layout);
		BorderPane.setMargin(toolBar, new Insets(20, 0, 20, 0));
		
		layout.setOnMouseClicked( e ->{
			if(e.getButton()==MouseButton.PRIMARY){
				if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Place"){
					//create new place
					Place newPlace = new Place(e.getX(), e.getY());		
					newPlace.setContextMenu(getPlaceMenu(newPlace, primaryStage, tokens));
					newPlace.setOnMouseClicked(v -> {handlePlaceClick(v);});
					net.addPlace(newPlace);
				}
				else if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Transition"){
					//create new transition
					Transition newTransition = new Transition(e.getX(), e.getY());
					newTransition.setContextMenu(getTransitionMenu(newTransition, primaryStage));			
					newTransition.setOnMouseClicked(v -> {handleTransitionClick(v);});
					net.addTransition(newTransition);
				}
				else if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Arc"){
					//add segment to potentialArc and create new user controlled segment
					if(currentSegment!=null){
						addSegment(e.getX(), e.getY());
					}
				}
			}
		});
		
		//user selects new segment endpoint
		layout.setOnMouseMoved(e ->{
			if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Arc"){
				if(currentSegment!=null)
				{
					currentSegment.setEndX(e.getX());
					currentSegment.setEndY(e.getY());
				}
			}
		});

		primaryStage.setScene(new Scene(border));
		primaryStage.show();
	}
	
	//returns HBox containing ToggleButton for editor's modes
	private HBox getToolBar(Stage primaryStage){
		HBox toolBar = new HBox(10);
		toolBar.setAlignment(Pos.CENTER);

		toolBar.getChildren().addAll(//new ToggleButton("Select"), 
				new ToggleButton("Place"), new ToggleButton("Transition"), 
				new ToggleButton("Arc"), new ToggleButton("Add Token"), 
				new ToggleButton("Remove Token"), 
				new ToggleButton("Fire Transition"), new ToggleButton("Simulate"));
		
		for(Node t: toolBar.getChildren()){
			((ToggleButton)t).setToggleGroup(selectMode);
			((ToggleButton)t).setOnAction(e ->clearOldMode());
		}
		
		selectMode.getToggles().get(0).setSelected(true);		//set default value to selected
		
		//enable transition firing
		toolBar.getChildren().get(5).setOnMouseClicked( e ->{
			sim = new Simulation(net, layout);
		});
		
		toolBar.getChildren().get(6).setOnMouseClicked( e ->{
			sim = new Simulation(net, layout);
			sim.displayMenu(primaryStage);
		});
		
		return toolBar;
	}
	
	//returns application MenuBar
	private MenuBar getMenuBar(Stage primaryStage){
		Menu file = new Menu("File");
		MenuBar mainMenu = new MenuBar(file);
		
		MenuItem New = new MenuItem("New");
		MenuItem open = new MenuItem("Open");
		MenuItem save = new MenuItem("Save");
		MenuItem saveAs = new MenuItem("Save As");
		MenuItem exit = new MenuItem("Exit");
		
		file.getItems().addAll(New, open, save, saveAs, exit);
		
		file.setOnShown(e->{
			if(net.currentFileExists())
				save.setVisible(true);
			else
				save.setVisible(false);
		});
		
		FileChooser navigator = new FileChooser();
		navigator.getExtensionFilters().addAll(new ExtensionFilter("Petri Net", "*.pn"),
											new ExtensionFilter("All Files", "*.*"));
		
		New.setOnAction(e ->{
			net.clear();
			clearOldMode();
		});
		
		open.setOnAction(e ->{
			try{
				net.load(navigator.showOpenDialog(primaryStage));
			}
			catch(IOException x){
				Stage fileError = getFileOpenError();
				fileError.show();
			}
			
			//set context menu and event handler for all places
			for(Place place : net.getPlaces()){
				place.setContextMenu(getPlaceMenu(place, primaryStage, tokens));
				place.setOnMouseClicked(v -> {handlePlaceClick(v);});
			}
			
			//set context menu and event handler for all transitions
			for(Transition transition : net.getTransitions()){
				transition.setContextMenu(getTransitionMenu(transition, primaryStage));
				transition.setOnMouseClicked(v -> {handleTransitionClick(v);});
			}
			
			//set event handler for all arcs
			for(Arc arc : net.getArcs()){
				arc.setOnMouseClicked(v -> {handleArcClick(v);});
			}
			
		});
		
		save.setOnAction(e ->{
			try{
				net.save();
			}
			catch(IOException x){
				Stage fileError = getFileOpenError();
				fileError.show();
			}
		});
		
		saveAs.setOnAction(e ->{
			try{
				net.saveAs(navigator.showSaveDialog(primaryStage));
			}
			catch(IOException x){
				Stage fileError = getFileOpenError();
				fileError.show();
			}
		});
		
		
		exit.setOnAction(e -> primaryStage.close());
		
		return mainMenu;
	}
	
	//returns window displayed for file open error
	private Stage getFileOpenError(){
		Stage fileError = new Stage();
		fileError.setTitle("File Open Error");
		BorderPane errorPane = new BorderPane();
		errorPane.setPrefWidth(500.0);
		
		Button errorOK = new Button("OK");
		
		errorOK.setOnAction(t -> {
			fileError.close();
		});
		
		errorPane.setTop(new Label("The specified file could not be opened."));
		errorPane.setBottom(errorOK);
		BorderPane.setAlignment(errorOK, Pos.CENTER);
		BorderPane.setMargin(errorPane, new Insets(20, 0, 20, 0));

		fileError.setScene(new Scene(errorPane));
		return fileError;
	}
	
	private ContextMenu getPlaceMenu(Place newPlace, Stage primaryStage, TokenManager tokens){
		ContextMenu menu = new ContextMenu();
		
		MenuItem edit = new MenuItem("Edit");
		
		edit.setOnAction(v->{
			//display place edit menu
			
			Stage editMenu = new Stage();
			editMenu.initOwner(primaryStage);
			editMenu.setTitle("Edit Place");
			BorderPane pane = new BorderPane();
			pane.setPrefWidth(300.0);
			Button ok = new Button("OK");
			Button cancel = new Button("Cancel");
			
			HBox nameInput = new HBox(5, new Label("Name:   "), new TextField(newPlace.getName()));
			nameInput.setAlignment(Pos.CENTER);
			HBox tokenInput = new HBox(5, new Label("Tokens: "), new TextField("" + newPlace.getNumTokens()));					
			tokenInput.setAlignment(Pos.CENTER);
			HBox menuButtons = new HBox(10, ok, cancel);
			menuButtons.setAlignment(Pos.CENTER);
			
			pane.setTop(nameInput);
			pane.setCenter(tokenInput);
			pane.setBottom(menuButtons);
			
			ok.setOnAction(n ->{
				
				//set place name to first 20 non-space characters in first TextField
				String name = ((TextField)((HBox)pane.getChildren().get(0)).getChildren().get(1)).getText();
				name = name.replaceAll("\\s","");
				if(name.length()>20)
					name = name.substring(0,20);
				newPlace.setName(name);
				
				int numTokens = newPlace.getNumTokens();
				
				try{
						numTokens = Integer.parseInt(((TextField)((HBox)pane.getChildren().get(1)).getChildren().get(1)).getText());
						
						//if number of tokens is within bounds, set new number of tokens
						if(numTokens<0&&numTokens>99)
							tokens.setTokens(newPlace, numTokens, true);
						
						if(sim!=null)
							//disable transition firing
							sim = new Simulation(net, layout);
						editMenu.close();
				}
				catch(NumberFormatException x){
					//display error for invalid 
					
					Stage tokenError = new Stage();
					tokenError.initOwner(editMenu);
					tokenError.setTitle("Invalid Token Number");
					BorderPane errorPane = new BorderPane();
					errorPane.setPrefWidth(400.0);
					
					Button errorOK = new Button("OK");
					
					errorOK.setOnAction(t -> {
						tokenError.close();
					});
					
					errorPane.setTop(new Label("Tokens must be set to integer from 0 to 99."));
					BorderPane.setAlignment(errorPane.getTop(), Pos.CENTER);
					errorPane.setBottom(errorOK);
					BorderPane.setAlignment(errorOK, Pos.CENTER);
					BorderPane.setMargin(errorPane.getTop(), new Insets(20, 0, 20, 0));
		
					tokenError.setScene(new Scene(errorPane));
					tokenError.show();
				}
			});
		
			cancel.setOnAction(n->{
				editMenu.close();
			});
			
			editMenu.setScene(new Scene(pane));
			editMenu.show();
			
			if(sim!=null)
				//refresh sim references
				sim = new Simulation(net, layout);
		});
		
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(v ->{
				clearOldMode();
				net.removePlace(newPlace);
				tokens.updateTransitions();
				if(sim!=null)
					//refresh sim references
					sim = new Simulation(net, layout);
		});
		
		menu.getItems().addAll(edit, delete);
		
		return menu;
	}
	
	private ContextMenu getArcMenu(Arc newArc){
		ContextMenu menu = new ContextMenu();
		
		MenuItem edit = new MenuItem("Edit");
		
		edit.setOnAction(v->{
			//display arc edit menu
			
			Stage editMenu = new Stage();
			editMenu.setTitle("Edit Arc");
			BorderPane pane = new BorderPane();
			pane.setPrefWidth(300.0);
			Button ok = new Button("OK");
			Button cancel = new Button("Cancel");
			
			HBox weightInput = new HBox(5, new Label("Weight: "), new TextField("" + newArc.getWeight()));					
			weightInput.setAlignment(Pos.CENTER);
			HBox menuButtons = new HBox(10, ok, cancel);
			menuButtons.setAlignment(Pos.CENTER);
			
			pane.setCenter(weightInput);
			pane.setBottom(menuButtons);
			
			ok.setOnAction(n ->{
				
				int weight = newArc.getWeight();
				
				try{
					weight = Integer.parseInt(((TextField)((HBox)pane.getChildren().get(0)).getChildren().get(1)).getText());
						
						if(weight<1){
							weight = 1;
						}
						
						if(weight>99){
							weight = 99;
						}
						
						if(sim!=null)
							//refresh sim references
							sim = new Simulation(net, layout);
						editMenu.close();
				}
				catch(NumberFormatException x){
					//display error for invalid weight input
					
					Stage weightError = new Stage();
					weightError.initOwner(editMenu);
					weightError.setTitle("Invalid Weight Number");
					BorderPane errorPane = new BorderPane();
					errorPane.setPrefWidth(500.0);
					
					Button errorOK = new Button("OK");
					
					errorOK.setOnAction(t -> {
						weightError.close();
					});
					
					errorPane.setTop(new Label("Weight must be set to integer from 1 to 99."));
					errorPane.setBottom(errorOK);
					BorderPane.setAlignment(errorPane.getTop(), Pos.CENTER);
					BorderPane.setAlignment(errorOK, Pos.CENTER);
					BorderPane.setMargin(errorPane.getTop(), new Insets(20, 0, 20, 0));
		
					weightError.setScene(new Scene(errorPane));
					weightError.show();
				}
				
				net.setArcWeight(newArc, weight);
			});
		
			cancel.setOnAction(n->{
				editMenu.close();
			});
			
			editMenu.setScene(new Scene(pane));
			editMenu.show();
		});
		
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(v ->{
				net.removeArc(newArc);
				if(sim!=null)
					//refresh sim references
					sim = new Simulation(net, layout);
		});
		
		menu.getItems().addAll(edit, delete);
		
		return menu;
	}
	
	private ContextMenu getTransitionMenu(Transition newTransition, Stage primaryStage){
		ContextMenu menu = new ContextMenu();
		
		MenuItem edit = new MenuItem("Edit");
		
		edit.setOnAction(v->{
			//display transition edit menu
			
			Stage editMenu = new Stage();
			editMenu.initOwner(primaryStage);
			editMenu.setTitle("Edit Transition");
			BorderPane pane = new BorderPane();
			pane.setPrefWidth(300.0);
			Button ok = new Button("OK");
			Button cancel = new Button("Cancel");
			
			HBox nameInput = new HBox(5, new Label("Name:    "), new TextField(newTransition.getName()));
			nameInput.setAlignment(Pos.CENTER);
			HBox priorityInput = new HBox(5, new Label("Priority: "), new TextField("" + newTransition.getPriority()));					
			priorityInput.setAlignment(Pos.CENTER);
			HBox menuButtons = new HBox(10, ok, cancel);
			menuButtons.setAlignment(Pos.CENTER);
			
			pane.setTop(nameInput);
			pane.setCenter(priorityInput);
			pane.setBottom(menuButtons);
			
			ok.setOnAction(n ->{
				
				//set name to first 20 non-space characters in first TextField
				String name = ((TextField)((HBox)pane.getChildren().get(0)).getChildren().get(1)).getText();
				name = name.replaceAll("\\s","");
				if(name.length()>20)
					name = name.substring(0,20);
				newTransition.setName(name);
				
				int priority = newTransition.getPriority();
				
				try{
					priority = Integer.parseInt(((TextField)((HBox)pane.getChildren().get(1)).getChildren().get(1)).getText());
						
						if(priority<1){
							priority = 1;
						}
						
						if(priority>10000){
							priority = 10000;
						}
						
				newTransition.setPriority(priority);
						
						if(sim!=null)
							//refresh sim references
							sim = new Simulation(net, layout);
						editMenu.close();
				}
				catch(NumberFormatException x){
					//display error for invalid priority input
					
					Stage priorityError = new Stage();
					priorityError.initOwner(editMenu);
					priorityError.setTitle("Invalid Priority Number");
					BorderPane errorPane = new BorderPane();
					errorPane.setPrefWidth(500.0);
					
					Button errorOK = new Button("OK");
					
					errorOK.setOnAction(t -> {
						priorityError.close();
					});
					
					errorPane.setTop(new Label("Priority must be set to integer between 1 and 10000."));
					BorderPane.setAlignment(errorPane.getTop(), Pos.CENTER);
					errorPane.setBottom(errorOK);
					BorderPane.setAlignment(errorOK, Pos.CENTER);
					BorderPane.setMargin(errorPane.getTop(), new Insets(20, 0, 20, 0));
		
					priorityError.setScene(new Scene(errorPane));
					priorityError.show();
				}
			});
		
			cancel.setOnAction(n->{
				editMenu.close();
			});
			
			editMenu.setScene(new Scene(pane));
			editMenu.show();
		});
		
		MenuItem delete = new MenuItem("Delete");
		delete.setOnAction(v ->{
				clearOldMode();
				net.removeTransition(newTransition);
				if(sim!=null)
					//refresh sim references
					sim = new Simulation(net, layout);
		});
		
		menu.getItems().addAll(edit, delete);
		
		return menu;
	}
	
	//draws arrow head of arc
	private void addHead(PNode node){
		ObservableList<Double> points = potentialArc.getPoints();
		int length = potentialArc.getPoints().size();
		
		//last segment of arc
		Line last = new Line(points.get(length-4), //startX
				points.get(length-3),			   //startY
				points.get(length-2),  			   //endX
				points.get(length-1));  		   //endY
		
		//portion of last segment which intersects with arc end node (initialized for place end node)
		Shape intersectionShape = Shape.intersect(new Circle(node.getX()+10.0, node.getY()+10.0, 20.0), last);
		
		
		if(node.getTypeSelector()=="Transition"){
			//change intersection for transition end node
			intersectionShape = Shape.intersect(new Rectangle(node.getX(), node.getY(), 10.0, 40.0), last);
		}
		
		Bounds intersectionBounds = intersectionShape.getBoundsInParent(); 
		
		double changeX = last.getEndX() - last.getStartX();
		double changeY = last.getEndY() - last.getStartY();
		
		//get angle 
		Point2D changeVector = new Point2D(changeX, changeY);
		double angle = changeVector.angle(new Point2D(-Math.abs(changeVector.normalize().getX()), 0));
		angle *= Math.PI/180;
		
		int sign = 1;
		
		if(changeY < 0.0&&changeX > 0.0){
			
			angle = -angle;
		}
		else if (changeY < 0.0){
			angle = Math.abs(Math.PI - angle);
			sign = -1;
		}
		
		//set x coordinate to nearest point in intersection bounds
		double xCoord = 0.0;
			
		if(changeX>=0.0)
			xCoord = intersectionBounds.getMinX();
		else if(changeX<0.0){
			xCoord = intersectionBounds.getMaxX();
		}
		
		//set y coordinate to nearest point in intersection bounds
		double yCoord = 0.0;
			
		if(changeY >=0.0){
			yCoord = intersectionBounds.getMinY();
		}
		else if(changeY < 0.0){
			yCoord = intersectionBounds.getMaxY();
		}
			
		points.addAll(xCoord, yCoord,							//intersection with node
				xCoord + sign*10*Math.cos(Math.PI/4-angle), 	//head point 1
				yCoord + sign*10*Math.sin(Math.PI/4-angle),
				xCoord, yCoord,									//back to intersection
				xCoord + sign*10*Math.cos(Math.PI/4+angle), 
				yCoord - sign*10*Math.sin(Math.PI/4+angle));	//head point 2			

		layout.getChildren().removeAll(currentSegment, potentialArc);
		Arc newArc = new Arc(currentSegment.getStartNode(), node);
		for(double point : potentialArc.getPoints()){
			newArc.getPoints().add(point);
		}
		newArc.initArcLabel();
		newArc.setOnMouseClicked(e -> {handleArcClick(e);});
		net.addArc(newArc);
		tokens.updateTransitions();
		currentSegment = null;
		potentialArc = null;
	}
	
	//add beginning or ending segment of arc
	void procPotentialArc(PNode node){
		if(currentSegment==null){
			currentSegment = new Segment(node);
			potentialArc = new Polyline(currentSegment.getStartX(), currentSegment.getStartY());
			layout.getChildren().add(currentSegment);
		}
		else{
			if(currentSegment.getStartNode().getTypeSelector() != 
					node.getTypeSelector()){
				
				if(node.getTypeSelector() == "Place")
					addSegment(node.getX()+10.0, node.getY()+10.0);
				else
					addSegment(node.getX()+5.0, node.getY()+20.0);
				addHead(node);
			}
		}	
	}
	
	//add intermediary segment of arc
	void addSegment(double x, double y){
		if(currentSegment!=null){
			layout.getChildren().remove(currentSegment);
			
			//add segment to potentialArc
			layout.getChildren().remove(potentialArc);
			potentialArc.getPoints().addAll(x, y);
			
			layout.getChildren().add(potentialArc);
			
			//add new potential Segment
			currentSegment = new Segment(currentSegment.getStartNode(), x, y);
			layout.getChildren().add(currentSegment);
		}
	}
	
	//called when selectedToggle is changed
	private void clearOldMode(){
		
		//remove any incomplete arc
		if(currentSegment!=null){
			layout.getChildren().remove(currentSegment);
			currentSegment = null;
		}
		if(potentialArc!=null){
			layout.getChildren().remove(potentialArc);
			potentialArc = null;
		}
		
		//disable transition firing
		if(sim!=null){
			sim = null;
		}
	}
	
	//checks that a toggle is selected
	private boolean selected(){
		return ((ToggleButton)selectMode.getSelectedToggle())!= null;
	}
	
	private void handlePlaceClick(MouseEvent e){
		
		Place newPlace = ((Place)e.getSource());
		if(e.getButton()==MouseButton.PRIMARY){
			
			if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Arc"){
				procPotentialArc(newPlace);
			}
			else if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Add Token"){
				tokens.setTokens(newPlace, newPlace.getNumTokens()+1, true);
			}
			else if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Remove Token"){
				tokens.setTokens(newPlace, newPlace.getNumTokens()-1, true);
			}
		}
	}
	
	private void handleTransitionClick(MouseEvent e){
		Transition newTransition = ((Transition)e.getSource());
		
		if(e.getButton()==MouseButton.PRIMARY){
		
			if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Arc"){
				procPotentialArc(newTransition);
			}
			else if(selected()&&((ToggleButton)selectMode.getSelectedToggle()).getText()=="Fire Transition"){
				sim.fireTransition(newTransition);
			}
		}
	}
	
	private void handleArcClick(MouseEvent e){
		Arc arc = ((Arc)e.getSource());
				
		if(e.getButton()==MouseButton.SECONDARY)
		{
			ContextMenu arcContext = getArcMenu(arc);
			arcContext.show(stage, e.getSceneX(), e.getSceneY());
		}
	}
}
 