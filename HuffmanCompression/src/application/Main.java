package application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Main extends Application {

	private int numberOfLeafs = 0;
	private String[] headerRepresentation;

	@Override
	public void start(Stage primaryStage) {

		// creating stages and scenes
		MyPane rootPane = new MyPane();
		Scene scene = new Scene(rootPane, 800, 600);
		Scene headerPaneScene = new Scene(rootPane.getTheHeaderPane(), 700, 450);
		Scene tableScene = new Scene(rootPane.getTablePane(), 800, 600);
		Stage tableStage = new Stage();
		Stage headerStage = new Stage();
		headerStage.setScene(headerPaneScene);
		tableStage.setScene(tableScene);

		// to display the huffman nodes onto a table view
		ObservableList<HuffNode> list = FXCollections.observableArrayList();

		rootPane.getCompress().setOnAction(e -> {

			try {

				// Choosing file from desktop
				FileChooser fileChooser = new FileChooser();
				File selectedFile = fileChooser.showOpenDialog(new Stage());

				// Setting name and path to the GUI
				rootPane.getFileName().setText(selectedFile.getName());
				rootPane.getFilePath().setText(selectedFile.getAbsolutePath());
				rootPane.setSelectedFile(selectedFile);
				rootPane.getExtension().setText(".huff");

				//////////////////////////////////
				// Reading file for frequencies //
				//////////////////////////////////
				int bufferSize = 32;
				int notReadYet = (int) rootPane.getSelectedFile().length(); // Keep track of leftover data
				int appeared = 0; // Number of non-zero bytes
				int[] frequencies = new int[256]; // For counting the frequencies

				// buffered input to read the file
				InputStream input = new BufferedInputStream(new FileInputStream(rootPane.getSelectedFile()));

				while (notReadYet != 0) { // While there is information left in the file

					byte[] inputContainer; // Holds the 2048 byte or less of data from the stream

					if (notReadYet > bufferSize) { // If the information left can fit into the buffer
						inputContainer = new byte[bufferSize];
						input.read(inputContainer, 0, bufferSize);
						notReadYet = notReadYet - bufferSize; // Subtract the number of read items

					} else { // The amount of information left is less than the buffer size
						inputContainer = new byte[notReadYet];
						input.read(inputContainer, 0, notReadYet);
						notReadYet = 0; // No more items left to read
					}

					for (int i = 0; i < inputContainer.length; i++) { // Looping the input to count appearances
						if (frequencies[(inputContainer[i] + 256) % 256] == 0)
							appeared++; // To count the number of non-zero characters

						frequencies[(inputContainer[i] + 256) % 256]++; // Putting the range of byte as 0-255 only
					}
				}

				//////////////////////////
				// adding nodes to heap //
				//////////////////////////
				Heap heap = new Heap(appeared);// making heap with the size of non-zero entries
				HuffNode[] huffNodes = new HuffNode[appeared];// making array to hold huffman nodes

				int index = 0;// to keep track of huffman nodes array index
				for (int i = 0; i < frequencies.length; i++) // creating the HuffNodes to add to the heap

					if (frequencies[i] != 0) { // appeared in the read text
						HuffNode node = new HuffNode((char) i, frequencies[i], i);
						heap.insertMinHeap(node);// insert to heap
						huffNodes[index] = node;// add node to array
						index++;
					}

				///////////////////////////
				// creating huffman tree //
				//////////////////////////
				while (heap.size() > 1) {// repeat deleting the two minimum nodes in heap then adding their addition
											// until one node is left in the heap (n.logn)

					HuffNode min1 = heap.removeMin();// log n
					HuffNode min2 = heap.removeMin();// log n
					HuffNode combination = min1.shareParent(min2);// with frequency of min1 + min2

					// setting left and right of the combination node
					combination.setLeft(min1);
					combination.setRight(min2);

					heap.insertMinHeap(combination);// log n
				}

				list.clear();// clear observable list to insert new objects
				calculateHuffmanSequences(heap.getMin(), "", list);// filling sequences of huffman
				rootPane.getHuffmanTable().setItems(list);
				HuffNode.treeCode = new ByteNode();// clearing tree code to be written in the header
				getTreeEncoded(heap.getMin());// to encode the tree structure

				// compressing the file
				compress(huffNodes, rootPane.getSelectedFile(), getNewFile(rootPane.getSelectedFile().getName(),
						rootPane.getSelectedFile().getAbsolutePath(), "huff"));

				rootPane.getWarning().setText("Compression Complete");
				rootPane.getFileName().setText(rootPane.getSelectedFile().getName());
				rootPane.getFilePath().setText(rootPane.getSelectedFile().getAbsolutePath());
				rootPane.getHeaderContents().setText(getTheTextAreaContent(headerRepresentation));

			} catch (NullPointerException | NoSuchElementException | IOException e1) {
				e1.printStackTrace();
				rootPane.getWarning().setText("Error Reading File For Compression");
				rootPane.getFileName().setText("-No File-");
				rootPane.getFilePath().setText("-No Path-");
				rootPane.getHeaderContents().setText("-NO DETAILS YET-");

			}

		});

		rootPane.getDecompress().setOnAction(e -> {

			primaryStage.setScene(scene);
			primaryStage.setTitle("-Huffman Compression And Decompression-");

			try {

				// opening file chooser
				FileChooser fileChooser = new FileChooser();

				// Create a file filter for the huffman extension
				ExtensionFilter filter = new ExtensionFilter("Huff Files (*.huff)", "*.huff");
				fileChooser.getExtensionFilters().add(filter);
				File selectedFile = fileChooser.showOpenDialog(new Stage());

				rootPane.setSelectedFile(selectedFile);// saving selected file

				if (selectedFile != null && !selectedFile.getName().toLowerCase().endsWith(".huff"))
					throw new NoSuchElementException();// not the correct huff file

				// Setting name and path to the GUI
				rootPane.getFileName().setText(selectedFile.getName());
				rootPane.getFilePath().setText(selectedFile.getAbsolutePath());
				rootPane.setSelectedFile(selectedFile);
				rootPane.getExtension().setText("");
				// reading the correct file selection using input stream
				InputStream input = new FileInputStream(rootPane.getSelectedFile());

				/////////////////////
				// reading header //
				////////////////////
				byte[] size = new byte[2];// first two bytes represent the header size
				input.read(size, 0, size.length);
				int headerSize = (size[0] << 8) | (size[1]);// first byte set to the higher significance

				// reading the content of the header into buffer array
				byte[] header = new byte[headerSize];
				input.read(header, 0, headerSize);

				// depending on the length of the extension provided within the header
				// the loop will run and fill in the extension name
				String extension = "";
				for (int i = 1; i <= header[0]; i++)// header[0] = extension length
					extension += (char) header[i];

				// filling the tree code by looping from when the extension stops + 1 until the
				// length of the tree code provided in that slot
				HuffNode.treeCode = new ByteNode();// initializing the tree code byte linked list
				for (int i = 1 + header[0]; i < (header.length) - 1; i++)
					ByteNode.insert((header[i] + 256) % 256);

				// number last bits to read in the last byte
				int lastBits = header[(header.length) - 1];

				HuffNode rootNode = new HuffNode();// initializing the head of the huffman tree
				numberOfLeafs = 0;// keep track of number of nonzero entries

				// building huffman tree from tree code
				buildHuffmanTreeFromHeader(HuffNode.treeCode.getNext(), rootNode);

				// to store the leaf huffman nodes in the tree
				ObservableList<HuffNode> decompressedItems = FXCollections.observableArrayList();

				// calculate the huffman codes from the huffman tree just decoded
				calculateHuffmanSequences(rootNode, "", decompressedItems);
				rootPane.getHuffmanTable().setItems(decompressedItems);
				rootPane.getWarning().setText("Decompression Complete!");

				////////////////////////////////
				// invoking the Decompression //
				///////////////////////////////
				File newFile = getNewFile(rootPane.getSelectedFile().getName(),
						rootPane.getSelectedFile().getAbsolutePath(), extension);
				decompress(rootNode, input, rootPane.getSelectedFile(), newFile, extension, lastBits, headerSize + 2);

				// to fill the text area with header information
				String[] info = new String[5];
				info[0] = headerSize + "";
				info[1] = header[0] + "";
				info[2] = extension;
				info[3] = HuffNode.treeCode.getString(HuffNode.treeCode);
				info[4] = lastBits + "";

				rootPane.getHeaderContents().setText(getTheTextAreaContent(info));
				rootPane.getWarning().setText("Decompression Complete!");
				rootPane.getFileName().setText(newFile.getName());
				rootPane.getFilePath().setText(newFile.getAbsolutePath());

			} catch (NullPointerException | NoSuchElementException | IOException e1) {
				rootPane.getWarning().setText("Error Reading File For Decompression");
				rootPane.getFileName().setText("-No File-");
				rootPane.getFilePath().setText("-No Path-");
				rootPane.getHeaderContents().setText("-NO DETAILS YET-");
				e1.printStackTrace();

			}

		});

		rootPane.getHeader().setOnAction(e -> {

			headerStage.show();

		});

		rootPane.getViewTable().setOnAction(e -> {

			rootPane.getHuffmanTable().refresh();
			tableStage.show();

		});

		fillTable(list, rootPane.getHuffmanTable());
		primaryStage.setScene(scene);
		primaryStage.setTitle("-Huffman Compression And Decompression-");
		primaryStage.show();

	}

	/////////////////////////////////////////////////////////////////////
	//////////////////////////// MAIN METHOD ///////////////////////////
	////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		launch(args);

	}

	/*
	 * This method reads through a buffered stream the input file and translates
	 * each byte to its corresponding huffman sequence, which is written to a new
	 * compressed file.
	 */
	public void compress(HuffNode[] nodes, File file, File newFile) throws IOException {

		try (InputStream input = new FileInputStream(file);
				OutputStream output = new BufferedOutputStream(new FileOutputStream(newFile))) {

			int bitsLeft = getNumberOfBitsToBeLeft(nodes);// number of important bits in the last byte

			headerRepresentation = writeHeader(HuffNode.treeCode, file, output, bitsLeft);

			int inputBufferSize = 16;
			int outputBufferSize = 16;
			int notReadYet = (int) file.length();// number of bytes to read
			int byteCount = 0;// keep track of the byte position
			int bit = 0;// keep track of the bit position
			byte[] outputContainer = new byte[outputBufferSize];// store output

			while (notReadYet != 0) {// while there is information still not read in the file

				byte[] inputContainer;// holds the data from the stream

				if (notReadYet > inputBufferSize) {// if the information can fit into the buffer
					inputContainer = new byte[inputBufferSize];
					input.read(inputContainer, 0, inputBufferSize);
					notReadYet = notReadYet - inputBufferSize;// subtract the number of read items

				} else {// the amount of information left is less than the buffer size
					inputContainer = new byte[notReadYet];
					input.read(inputContainer, 0, notReadYet);
					notReadYet = 0;// no more items left to read

				}

				for (int i = 0; i < inputContainer.length; i++) {// looping the input bytes

					String code = getHuffmanCode((inputContainer[i] + 256) % 256, nodes);// getting huffman code of byte

					for (int k = 0; k < code.length(); k++) {// looping the huffman code

						if (code.charAt(k) == '1')// insert the one at the right position
							outputContainer[byteCount] |= (1 << (7 - bit));
						bit++;

						if (bit == 8) {// when a whole byte is complete
							byteCount++;// next byte
							bit = 0;// reset bit number
						}

						if (byteCount == outputBufferSize) {// when byte reached out of bound of container
							output.write(outputContainer, 0, outputBufferSize);// write to output
							outputContainer = new byte[outputBufferSize];// clear contents
							byteCount = 0;// reset byte position
							bit = 0;// reset bit number
						}
					}
				}
			}

			if (byteCount > 0) {// left over bytes in the output container

				if (bitsLeft != 0)// we need to include the last extra byte
					byteCount++;

				output.write(outputContainer, 0, byteCount);

			}

		}
	}

	public void decompress(HuffNode rootNode, InputStream input, File oldFile, File newFile, String extension,
			int numberOfBitsAtEnd, int headerSize) throws IOException {

		int inputBufferSize = 16;
		int outputBufferSize = 16;

		OutputStream output = new BufferedOutputStream(new FileOutputStream(newFile), outputBufferSize);
		InputStream bufferedInput = new BufferedInputStream(input, inputBufferSize);

		int notReadYet = ((int) oldFile.length()) - headerSize;// number of bytes to read
		int byteCount = 0;// keep track of the byte position
		byte[] outputContainer = new byte[outputBufferSize];// store output
		String sequence = "";// to store the Huffman sequence to be searched

		while (notReadYet != 0) {// while there is information still not read in the file

			byte[] inputContainer;// holds the 4096 byte or less of data from the stream

			if (notReadYet > inputBufferSize) {// if the information can fit into the buffer

				inputContainer = new byte[inputBufferSize];
				bufferedInput.read(inputContainer, 0, inputBufferSize);
				notReadYet = notReadYet - inputBufferSize;// subtract the number of read items

			} else {// the amount of information left is less than the buffer size

				inputContainer = new byte[notReadYet];
				bufferedInput.read(inputContainer, 0, notReadYet);
				notReadYet = 0;// no more items left to read
			}

			for (int i = 0; i < inputContainer.length; i++) {

				int theByte = (inputContainer[i] + 256) % 256;

				int bitLimit = 0;
				// on the last input session and last byte
				if (notReadYet == 0 && i == (inputContainer.length - 1) && numberOfBitsAtEnd != 0)
					bitLimit = 8 - numberOfBitsAtEnd;

				// iterate through each bit of the byte
				for (int bit = 7; bit >= bitLimit; bit--) {

					if (byteCount == outputBufferSize) {// when the container is full
						output.write(outputContainer, 0, byteCount);
						byteCount = 0;// reset index
					}

					// shift the bits to the right to get the current bit
					int currentBit = (theByte >> bit) & 1;

					// adding the bit to the sequence
					sequence += currentBit;

					// searching the Huffman tree for the byte
					int s = getByteFromTree(rootNode, sequence);

					if (s != -1) {// the Huffman code was found
						outputContainer[byteCount] = (byte) s;// add to the output buffer container
						byteCount++;// next position

						// clear the string for the next Huffman sequence
						sequence = "";
					}
				}
			}

			if (byteCount != 0) {
				output.write(outputContainer, 0, byteCount);
				byteCount = 0;// reset index
			}
		}

		output.close();
		bufferedInput.close();
	}

	/*
	 * This method takes in the byte linked list and the head of the huffman tree
	 * node it recursively loops through the byte node encountering the byte 0 means
	 * that the node encoded was an enternal node however, encountering a 1 means
	 * this node is a leaf node which contains the byte value with this logic we can
	 * rebuild the tree from left to right
	 * 
	 */
	public void buildHuffmanTreeFromHeader(ByteNode byteNode, HuffNode huffNode) {

		if (byteNode != null) {

			if (byteNode.getByteValue() == 0) {// internal node

				if (huffNode.getLeft() == null) {// placed on the left
					System.out.println(" 0 left is null");
					HuffNode temp = new HuffNode();
					temp.setParent(huffNode);
					huffNode.setLeft(temp);
					buildHuffmanTreeFromHeader(byteNode.getNext(), temp);

				} else if (!huffNode.getLeft().isImportant()) {// keep moving left since its not fully occupied
					System.out.println(" 0 left is not null and not important");
					buildHuffmanTreeFromHeader(byteNode.getNext(), huffNode.getLeft());// recursive to
																						// that internal
					// node

				} else {// left is already occupied fully by leaf nodes so we now check the right side
						// in the same manner

					if (huffNode.getRight() == null) {// the right is available to expand to (with internal node)
						System.out.println(" 0 left important and right is null");
						HuffNode temp = new HuffNode();
						temp.setParent(huffNode);
						huffNode.setRight(temp);
						buildHuffmanTreeFromHeader(byteNode.getNext(), temp);// recursive on that new
																				// internal node

					} else if (!huffNode.getRight().isImportant()) {// right side already has an internal node with no
																	// leafs (important values)
						System.out.println(" 0 left important and right is not");
						buildHuffmanTreeFromHeader(byteNode.getNext(), huffNode.getRight());

					} else {// right is completely full so we must go up the tree to find the next position

						System.out.println(" 0 left and right are important");
						huffNode.setImportant(true);// mark as full
						HuffNode parent = huffNode.getParent();

						// loop until we find a parent with an empty right side
						while (parent != null && parent.getRight() != null) {
							parent.setImportant(true);
							parent = parent.getParent();
						}

						HuffNode temp = new HuffNode();
						temp.setParent(parent);
						parent.setRight(temp);
						buildHuffmanTreeFromHeader(byteNode.getNext(), temp);
					}
				}

			} else if (byteNode.getByteValue() == 1) {// leaf node (important Values)

				if (huffNode.getLeft() == null) {// place this leaf on the left since its empty
					System.out.println(" 1 left is null: value" + byteNode.getNext().getByteValue());
					HuffNode temp = new HuffNode();
					temp.setAsciiValue(byteNode.getNext().getByteValue());
					temp.setImportant(true);
					huffNode.setLeft(temp);
					numberOfLeafs++;
					buildHuffmanTreeFromHeader(byteNode.getNext().getNext(), huffNode);

				} else if (!huffNode.getLeft().isImportant()) {// move to the left internal node
					System.out.println(" 1 left is not null and not important");
					buildHuffmanTreeFromHeader(byteNode.getNext(), huffNode.getLeft());

				} else {// left is already occupied fully by leaf nodes so we now check the right side
					// in the same manner

					if (huffNode.getRight() == null) {// place leaf on the right
						System.out.println(
								" 1 left important and right is null: value" + byteNode.getNext().getByteValue());

						HuffNode temp = new HuffNode();
						temp.setAsciiValue(byteNode.getNext().getByteValue());
						temp.setParent(huffNode);
						temp.setImportant(true);
						huffNode.setRight(temp);
						numberOfLeafs++;
						buildHuffmanTreeFromHeader(byteNode.getNext().getNext(), huffNode);

					} else {// right is full (important) already (must go up some levels)

						System.out
								.println(" 1 left and right are important: value" + byteNode.getNext().getByteValue());

						HuffNode temp = new HuffNode();
						temp.setAsciiValue(byteNode.getNext().getByteValue());
						temp.setImportant(true);
						huffNode.setImportant(true);// mark it as full
						HuffNode parent = huffNode.getParent();

						// loops up the nodes parent until finding a null right to place the leaf
						while (parent != null && parent.getRight() != null) {
							parent.setImportant(true);
							parent = parent.getParent();
						}

						temp.setParent(parent);
						parent.setRight(temp);
						numberOfLeafs++;
						buildHuffmanTreeFromHeader(byteNode.getNext().getNext(), parent);

					}
				}
			}
		}
	}

	// recursively walks through the tree of huffman nodes generating the huffman
	// code for each leaf node
	public void calculateHuffmanSequences(HuffNode node, String huffmanCode, ObservableList list) {

		if (node != null) {

			if (node.getLeft() != null)// go left and add a 0 to sequence
				calculateHuffmanSequences(node.getLeft(), huffmanCode + "0", list);

			if (node.isLeaf()) {// not internal node
				node.setHuffmanString(huffmanCode);// set the current sequence as the huffman code
				if (list != null)
					list.add(node);

			}

			if (node.getRight() != null)// go right and add a 1 to sequence
				calculateHuffmanSequences(node.getRight(), huffmanCode + "1", list);

		}

	}

	// encodes the tree structure as 1 indicates a leaf with a char and 0 is an
	// internal node (this way the tree can be reconstructed when de-compressing
	public void getTreeEncoded(HuffNode node) {

		if (node != null) {// node not empty(base case)

			if (node.getLeft() != null) {// left not empty
				if (node.getLeft().isLeaf()) {// left is leaf and has char (add 1 along with the char)
					ByteNode.insert(1);
					ByteNode.insert(node.getLeft().getAsciiValue());

				} else
					ByteNode.insert(0);// left is not leaf(add 0 an move on)
			}
			getTreeEncoded(node.getLeft());

			if (node.getRight() != null) {

				if (node.getRight().isLeaf()) {// right is leaf and has char (add 1 along with the char)
					ByteNode.insert((byte) 1);
					ByteNode.insert(node.getRight().getAsciiValue());

				} else
					ByteNode.insert(0);// right is not leaf(add 0 an move on)

				getTreeEncoded(node.getRight());
			}
		}
	}

	/*
	 * This method returns the byte number after traversing through the tree using
	 * the huffman code provided
	 * 
	 */
	public int getByteFromTree(HuffNode node, String code) {

		if (!code.isEmpty() && node != null) {

			if (code.charAt(0) == '0')
				return getByteFromTree(node.getLeft(), code.substring(1));

			if (node.isLeaf()) {
				return node.getAsciiValue();

			}

			if (code.charAt(0) == '1')
				return getByteFromTree(node.getRight(), code.substring(1));

		}

		if (node != null && node.isLeaf())
			return node.getAsciiValue();

		return -1;

	}

	public String[] writeHeader(ByteNode treeStructure, File file, OutputStream output, int bitsLeft)
			throws IOException {

		String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
		int extensionSize = extension.length();
		int treeSize = treeStructure.getLength();
		// header size will be represented in 4 bytes
		int headerSize = 1 + extensionSize + treeSize + 1;// 1 -> extension size, extension, 2 -> treeSize, tree,

		// 1 -> how many bits of the last byte is data
		byte[] treeInBytes = treeStructure.getBytesArray(treeStructure);

		DataOutputStream kk = new DataOutputStream(output);

		kk.writeShort((short) headerSize);// writing the header size in 2 bytes (short)
		output.write(extensionSize);// one byte representing the size(length) of the extension

		for (int i = 0; i < extension.length(); i++)// writing the extension byte by byte
			output.write(extension.charAt(i));

		output.write(treeInBytes, 0, treeInBytes.length);// writing the tree representation byte by byte
		output.write(bitsLeft);// how many bits to encode in the last byte);

		String[] headerInAString = new String[5];
		headerInAString[0] = headerSize + "";
		headerInAString[1] = extensionSize + "";
		headerInAString[2] = extension + "";
		headerInAString[3] = treeStructure.getString(treeStructure);
		headerInAString[4] = bitsLeft + "";
		return headerInAString;

	}

	public String getTheTextAreaContent(String[] string) {

		String ac = "";

		ac += "Header Size : " + string[0];
		ac += "\nExtension Size: " + string[1];
		ac += "\nExtension: " + string[2];
		ac += "\nTree Encoded: " + string[3];
		ac += "\nNumber of Important Bits In The Last Byte: " + string[4];

		return ac;

	}

	// given the byte, the huffman code is found from the nodes available
	public String getHuffmanCode(int b, HuffNode[] nodes) {

		for (int i = 0; i < nodes.length; i++)

			if (nodes[i].getCh() == (char) b)
				return nodes[i].getHuffmanString();

		return null;

	}

	public void fillTable(ObservableList<HuffNode> list, TableView<HuffNode> table) {

		TableColumn<HuffNode, Character> ch = new TableColumn<HuffNode, Character>("Character");
		TableColumn<HuffNode, String> code = new TableColumn<HuffNode, String>("Huffman Code");
		TableColumn<HuffNode, Integer> length = new TableColumn<HuffNode, Integer>("Length");
		TableColumn<HuffNode, Integer> frequency = new TableColumn<HuffNode, Integer>("Frequency");
		TableColumn<HuffNode, Integer> ascii = new TableColumn<HuffNode, Integer>("ASCII Value");

		ascii.setCellValueFactory(new PropertyValueFactory<HuffNode, Integer>("asciiValue"));
		ch.setCellValueFactory(new PropertyValueFactory<HuffNode, Character>("ch"));
		code.setCellValueFactory(new PropertyValueFactory<HuffNode, String>("huffmanString"));
		length.setCellValueFactory(new PropertyValueFactory<HuffNode, Integer>("length"));
		frequency.setCellValueFactory(new PropertyValueFactory<HuffNode, Integer>("frequency"));

		ascii.setStyle("-fx-alignment: CENTER;-fx-pref-width: 150;-fx-font-size: 15;");
		ch.setStyle("-fx-alignment: CENTER;-fx-pref-width: 150;-fx-font-size: 15;");
		code.setStyle("-fx-alignment: CENTER;-fx-pref-width: 170;-fx-font-size: 15;");
		length.setStyle("-fx-alignment: CENTER;-fx-pref-width: 160;-fx-font-size: 15;");
		frequency.setStyle("-fx-alignment: CENTER;-fx-pref-width: 160;-fx-font-size: 15;");

		// preventing automatic sorting
		ch.setSortable(false);
		code.setSortable(false);
		ascii.setSortable(false);

		// to prevent moving the columns
		ch.setReorderable(false);
		ascii.setReorderable(false);
		code.setReorderable(false);
		length.setReorderable(false);
		frequency.setReorderable(false);

		// setting columns for each table
		table.getColumns().addAll(ch, ascii, code, length, frequency);

		table.setPrefWidth(550);
		table.setMinHeight(300);

	}

	// this method returns a new file with the same path as the old
	public File getNewFile(String name, String absolutePath, String extension) throws IOException {

		name = name.substring(0, name.lastIndexOf("."));// getting file Name

		// in the same path as the old file with a new name
		String newFilePath = absolutePath.substring(0, absolutePath.lastIndexOf("\\") + 1) + name + "." + extension;

		// creating the new file
		File newFile = new File(newFilePath);
		newFile.createNewFile();

		return newFile;

	}

	// returns the number of last bits that wont make a full byte
	public int getNumberOfBitsToBeLeft(HuffNode[] nodes) {

		int totNumberOfBits = 0;

		// counting total bits in the file
		for (int i = 0; i < nodes.length; i++)
			totNumberOfBits += nodes[i].getFrequency() * nodes[i].getLength();

		return totNumberOfBits % 8;// returning the number of bits that don't add up to a full byte

	}

}
