package application;

public class ByteNode {

	private static int count = 0;
	private int byteValue;
	private ByteNode next;

	public ByteNode() {
		count = 0;
	}

	public ByteNode(int b) {
		this.byteValue = b;

	}

	public int getLength() {

		return count;

	}

	public int getByteValue() {
		return byteValue;
	}

	public void setByteValue(int byteValue) {
		this.byteValue = byteValue;
	}

	public ByteNode getNext() {
		return next;
	}

	public void setNext(ByteNode next) {
		this.next = next;
	}

	public static void insert(int b) {

		if (count == 0) {

			HuffNode.treeCode.setNext(new ByteNode(b));
			count++;
			return;
		}

		ByteNode current = HuffNode.treeCode.getNext();

		for (int i = 0; i < count - 1; i++)
			current = current.getNext();

		current.setNext(new ByteNode(b));
		count++;

	}

	public static byte[] getBytesArray(ByteNode root) {

		byte[] array = new byte[root.getLength()];

		ByteNode current = root.getNext();// skip empty head

		for (int i = 0; i < root.getLength(); i++) {// looping the linked nodes for bytes
			array[i] = (byte) current.getByteValue();
			current = current.getNext();

		}

		return array;

	}

	public String getString(ByteNode root) {

		ByteNode current = root.getNext();

		String k = "";
		for (int i = 0; i < root.getLength(); i++) {
			k += (byte) current.getByteValue() + " ";
			current = current.getNext();

		}

		return k;

	}

}
