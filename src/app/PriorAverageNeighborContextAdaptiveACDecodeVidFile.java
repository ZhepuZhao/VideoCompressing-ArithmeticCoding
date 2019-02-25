package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class PriorAverageNeighborContextAdaptiveACDecodeVidFile {

	public static void main(String[] args) throws IOException, InsufficientBitsLeftException {
		long start = System.currentTimeMillis();

		String input_file_name = "data/compressed.dat";
		String output_file_name = "data/uncompressed.dat";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] symbols = new Integer[256];
		
		for (int i=0; i<256; i++) {
			symbols[i] = i;
		}

		// Create 256 models. Model chosen depends on value of symbol prior to 
		// symbol being encoded.
		
		FreqCountIntegerSymbolModel[] models = new FreqCountIntegerSymbolModel[256];
		
		for (int i=0; i<256; i++) {
			// Create new model with default count of 1 for all symbols
			models[i] = new FreqCountIntegerSymbolModel(symbols);
		}
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];
		int[][] previous_frame = new int[64][64];
		int[][] current_frame = new int[64][64];
		
		for (int i=0; i<num_symbols; i++) {
			
			int total = 0; // total intensity value of neighbors
			// pixel index for each frame
			int pixel_index = i % 4096; // range : 0 ~ 4095
			if (pixel_index == 0) {
				previous_frame = current_frame;
				current_frame = new int[64][64];
			}
			int row = pixel_index / 64;
			int col = pixel_index - row * 64;
//			int up = -1, left = -1;
			int up = -1, left = -1, right = -1, down = -1;
			if (row > 0) up = row - 1;
			if (col > 0) left = col - 1;
			if (col < 63) right = col + 1;
			if (row < 63) down = row + 1; 
			
			// set number of neighbors
			int neighbor_num = 0;
			// set total intensity value
			/*
			 * xox
			 * o*x   neighbor like this (* is the current one, o is the neighbor)
			 * xxx
			 */
			if (up != -1) {
				total += previous_frame[up][col];
				neighbor_num++;
			}
			if (left != -1) {
				total += previous_frame[row][left];
				neighbor_num++;
			}
//			if (right != -1) {
//				total += previous_frame[row][right];
//				neighbor_num++;
//			}
//			if (down != -1) {
//				total += previous_frame[down][col];
//				neighbor_num++;
//			}
			
			// average intensity value of neighbors
			int average_intensity = (neighbor_num == 0) ? 0 : (total / neighbor_num);
			
			// choose the model associated with the average intensity value
			model = models[average_intensity];
			int sym = decoder.decode(model, bit_source);
			current_frame[row][col] = sym;
			fos.write(sym);
			
			// Update model used
			model.addToCount(sym);
			
		}
		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
		long end = System.currentTimeMillis();
		System.out.println("running time: " + (end - start));
	}

}
