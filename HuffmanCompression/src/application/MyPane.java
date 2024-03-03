package application;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MyPane extends BorderPane {

	private TableView<HuffNode> huffmanTable;
	private Button compress, decompress, viewTable, header, doneComp, doneDecomp;
	private Label warning, fileName, heading, filePath, extension;
	private TextField newName;
	private TextArea headerContents;
	private String path, name;
	private File selectedFile;

	public MyPane() {

		extension = new Label();
		doneComp = new Button("Done");
		doneDecomp = new Button("Done");
		huffmanTable = new TableView<>();
		compress = new Button("Compress");
		decompress = new Button("Decompress");
		viewTable = new Button("View Huffman Table");
		header = new Button("View Header Contents");
		warning = new Label("");
		fileName = new Label("-No File-");
		filePath = new Label("-No Path-");
		heading = new Label("Huffman Compression/Decompression");
		headerContents = new TextArea("-NO DETAILS YET-");

		setProperties();

		Separator separator0 = new Separator(Orientation.HORIZONTAL);
		separator0.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

		Separator separator1 = new Separator(Orientation.HORIZONTAL);
		separator1.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

		Separator separator2 = new Separator(Orientation.VERTICAL);
		separator2.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

		// creating the layout of the interface using Vertical and Horizontal Boxes
		HBox topButtons = new HBox(50);
		topButtons.getChildren().addAll(compress, separator2, decompress);
		topButtons.setAlignment(Pos.CENTER);

		VBox fileInfo = new VBox(20);
		fileInfo.getChildren().addAll(fileName, filePath);
		fileInfo.setPadding(new Insets(0, 0, 0, 30));
		fileInfo.setAlignment(Pos.CENTER_LEFT);

		HBox bottomButtons = new HBox(70);
		bottomButtons.getChildren().addAll(viewTable, header);
		bottomButtons.setAlignment(Pos.CENTER);

		VBox center = new VBox(30);
		center.getChildren().addAll(heading, topButtons, separator0, fileInfo, separator1, warning, bottomButtons);
		center.setAlignment(Pos.CENTER);

		setCenter(center);
		setPadding(new Insets(10, 10, 10, 20));
		setStyle("-fx-background-color:rgb(255,235,245);");

	}

	public BorderPane getNameChanger(int type) {

		Label title = new Label("Enter New Name:");
		title.setFont(Font.font("Times New Roman", FontWeight.BOLD, 25));
		extension.setFont(Font.font("Times New Roman", 25));
		fileName.setFont(Font.font("Times New Roman", 20));
		newName = new TextField();
		newName.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
		newName.setStyle("-fx-control-inner-background:rgb(255,235,245);-fx-border-color:black;-fx-border-width: 1;");
		newName.setPrefWidth(300);

		Label heading = new Label("Change File Name");
		heading.setPadding(new Insets(20, 0, 20, 0));
		heading.setFont(Font.font("Courier New", FontWeight.BOLD, 33));
		heading.setTextFill(Color.BLACK);

		HBox theName = new HBox(20);
		theName.getChildren().addAll(newName, extension);
		theName.setAlignment(Pos.CENTER);
		theName.setPadding(new Insets(10, 10, 30, 10));

		VBox center = new VBox(5);

		if (type == 1) {// for compression
			doneComp.setFont(Font.font("Times New Roman", 25));
			doneComp.setStyle("-fx-background-color: rgb(235, 167, 172);-fx-border-color:black;-fx-border-width: 1;");

			center.getChildren().addAll(title, theName, doneComp);

		} else {// for decompression
			doneDecomp.setFont(Font.font("Times New Roman", 25));
			doneDecomp.setStyle("-fx-background-color: rgb(235, 167, 172);-fx-border-color:black;-fx-border-width: 1;");
			center.getChildren().addAll(title, theName, doneDecomp);
		}

		center.setPadding(new Insets(30, 10, 10, 10));
		center.setAlignment(Pos.CENTER);

		BorderPane changerPane = new BorderPane();
		changerPane.setAlignment(heading, Pos.CENTER);
		changerPane.setCenter(center);
		changerPane.setTop(heading);
		changerPane.setStyle("-fx-background-color:rgb(255,235,245);");

		return changerPane;

	}

	// setting properties for the nodes and objects
	private void setProperties() {
		Image fileImage = new Image("file.png");
		ImageView fImg = new ImageView(fileImage);

		Image pathImage = new Image("path.png");
		ImageView pImg = new ImageView(pathImage);

		fImg.setScaleX(.7);
		fImg.setScaleY(.7);
		fileName.setGraphic(fImg);
		fileName.setContentDisplay(ContentDisplay.LEFT);
		filePath.setGraphic(pImg);
		filePath.setContentDisplay(ContentDisplay.LEFT);
		filePath.setGraphicTextGap(7);

		compress.setStyle("-fx-background-color: rgb(235, 167, 172);-fx-border-color:black;-fx-border-width: 1;");
		decompress.setStyle("-fx-background-color: rgb(235, 167, 172);-fx-border-color:black;-fx-border-width: 1;");
		viewTable.setStyle("-fx-background-color: rgb(235, 167, 172);-fx-border-color:black;-fx-border-width: 1;");
		header.setStyle("-fx-background-color: rgb(235, 167, 172);-fx-border-color:black;-fx-border-width: 1;");

		compress.setFont(Font.font("Times New Roman", 25));
		decompress.setFont(Font.font("Times New Roman", 25));
		viewTable.setFont(Font.font("Times New Roman", 22));
		header.setFont(Font.font("Times New Roman", 22));

		fileName.setFont(Font.font("Times New Roman", 22));
		filePath.setFont(Font.font("Times New Roman", 22));
		warning.setStyle("-fx-text-fill:red;");
		warning.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
		warning.setPadding(new Insets(15));

		heading.setPadding(new Insets(0, 0, 20, 0));
		heading.setFont(Font.font("Courier New", FontWeight.BOLD, 33));
		heading.setTextFill(Color.BLACK);

		huffmanTable.setStyle(
				"-fx-border-color:black;-fx-border-Width:3;-fx-background-color:rgb(255,235,245);-fx-control-inner-background:rgb(255,235,245);");

		headerContents.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
		headerContents.setStyle("-fx-control-inner-background:rgb(255,235,245);");
		headerContents.setWrapText(true);
		headerContents.setEditable(false);

	}

	// Pane for showing the huffman table contents
	public BorderPane getTablePane() {

		BorderPane thePane = new BorderPane();

		thePane.setCenter(huffmanTable);

		return thePane;

	}

	// Pane for showing the header contents
	public BorderPane getTheHeaderPane() {

		BorderPane thePane = new BorderPane();

		Label title = new Label("Header Representation");
		title.setPadding(new Insets(30, 0, 20, 0));
		title.setFont(Font.font("Courier New", FontWeight.BOLD, 33));
		title.setTextFill(Color.BLACK);
		title.setAlignment(Pos.CENTER);

		VBox center = new VBox(10);
		center.setAlignment(Pos.CENTER);
		center.getChildren().addAll(title, headerContents);

		thePane.setStyle("-fx-background-color: rgb(235, 167, 172);");
		thePane.setTop(center);

		return thePane;

	}

	// getters and setters
	public TableView<HuffNode> getHuffmanTable() {
		return huffmanTable;
	}

	public Button getCompress() {
		return compress;
	}

	public Button getDecompress() {
		return decompress;
	}

	public Button getViewTable() {
		return viewTable;
	}

	public Label getWarning() {
		return warning;
	}

	public Label getFileName() {
		return fileName;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Label getHeading() {
		return heading;
	}

	public Label getFilePath() {
		return filePath;
	}

	public Button getHeader() {
		return header;
	}

	public TextArea getHeaderContents() {
		return headerContents;
	}

	public Button getDoneComp() {
		return doneComp;
	}

	public Button getDoneDecomp() {
		return doneDecomp;
	}

	public TextField getNewName() {
		return newName;
	}

	public File getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
	}

	public Label getExtension() {
		return extension;
	}

}
