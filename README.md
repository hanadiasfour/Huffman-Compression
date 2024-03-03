# Huffman Compression Project

## Description:
Huffman coding is a lossless data compression algorithm which assigns variable-length codes to characters based on their frequencies, optimizing compression. The program utilizes a custom priority queue and binary tree to implement this algorithm. Integrated with JavaFX, the program offers a graphical user interface (GUI) that includes options to view the generated Huffman codes for each read byte, as well as the contents and size of the tree header used for decompression.

## Summary of Processing:

The process involves reading a specified file, counting byte frequencies, creating a Huffman coding tree, displaying encoding tables, and compressing the file. The compressed data is written to a file, accompanied by a header containing Huffman codes. The program can then read the compressed file, decode it, and output the decompressed file, ensuring fidelity to the original.



This project seamlessly integrates file compression and decompression functionalities through the implementation of the Huffman Coding algorithm, providing an efficient solution for lossless data compression.
