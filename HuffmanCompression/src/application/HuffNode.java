package application;

public class HuffNode implements Comparable<HuffNode> {

	static ByteNode treeCode = new ByteNode();
	private HuffNode parent;
	private char ch;
	private int asciiValue, frequency, length;
	private String huffmanString;
	private HuffNode right, left;
	private boolean isImportant = false;

	public HuffNode(char ch, int frequency, int asciiValue) {
		this.ch = ch;
		this.frequency = frequency;
		this.asciiValue = asciiValue;
	}

	public HuffNode(int asciiValue) {
		this.asciiValue = asciiValue;
		this.ch = (char) asciiValue;
	}

	public HuffNode() {

	}

	public boolean isImportant() {

		return isImportant;

	}

	public HuffNode shareParent(HuffNode b) {

		int joinedFrq = this.frequency + b.frequency;
		HuffNode root = new HuffNode();
		root.setFrequency(joinedFrq);
		return root;

	}

	public boolean isLeaf() {
		if (this.right == null && this.left == null)
			return true;
		else
			return false;

	}

	public int getAsciiValue() {
		return asciiValue;
	}

	public void setAsciiValue(int asciiValue) {
		this.asciiValue = asciiValue;
		this.ch = (char) asciiValue;
	}

	public char getCh() {
		return ch;
	}

	public void setCh(char ch) {
		this.ch = ch;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public HuffNode getRight() {
		return right;
	}

	public void setRight(HuffNode right) {
		this.right = right;
	}

	public HuffNode getLeft() {
		return left;
	}

	public void setLeft(HuffNode left) {
		this.left = left;
	}

	public String getHuffmanString() {
		return huffmanString;
	}

	public void setHuffmanString(String huffmanString) {
		this.huffmanString = huffmanString;
		setLength(huffmanString.length());
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setImportant(boolean isImportant) {
		this.isImportant = isImportant;
	}

	public HuffNode getParent() {
		return parent;
	}

	public void setParent(HuffNode parent) {
		this.parent = parent;
	}

	@Override
	public int compareTo(HuffNode node) {
		if (this.frequency < node.frequency)
			return -1;

		else if (this.frequency > node.frequency)
			return 1;

		else
			return 0;

	}

	@Override
	public String toString() {
		return "[asssci " + asciiValue + " ] right and left is null? " + (this.getRight() == null)
				+ (this.getLeft() == null);
	}

}
