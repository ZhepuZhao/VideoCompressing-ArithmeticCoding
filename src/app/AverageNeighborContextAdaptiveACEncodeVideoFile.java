package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

/*
 * Size after compression: 894,196
 */
public class AverageNeighborContextAdaptiveACEncodeVideoFile {

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();

		String input_file_name = "data/out.dat";
		String output_file_name = "data/compressed.dat";

		int range_bit_width = 40;

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();
				
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

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// First 4 bytes are the number of symbols encoded
		bit_sink.write(num_symbols, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);

		// Now encode the input
		FileInputStream fis = new FileInputStream(input_file_name);
		
		// Use model 0 as initial model.
		FreqCountIntegerSymbolModel model = models[0];
		int[][] intensity_value = new int[64][64];
		
		for (int i=0; i<num_symbols; i++) {
			
			// pixel index for each frame
			int total = 0; // total intensity value of neighbors
			int pixel_index = i % 4096; // range : 0 ~ 4095
			if (pixel_index == 0) intensity_value = new int[64][64];
			int up = -1, left = -1;
			int row = pixel_index / 64;
			int col = pixel_index - row * 64;
			if (row > 0) up = row - 1;
			if (col > 0) left = col - 1;
			
			// set number of neighbors
			int neighbor_num = 0;
			
			// set total intensity value
			/*
			 * xox
			 * o*x   neighbor like this (* is the current one, o is the neighbor)
			 * xxx
			 */
			if (up != -1) {
				total += intensity_value[up][col];
				neighbor_num++;
			}
			if (left != -1) {
				total += intensity_value[row][left];
				neighbor_num++;
			}
			
			/*
			 * oox
			 * o*x   neighbor like this (* is the current one, o is the neighbor)
			 * xxx
			 */
//			if (up != -1 && left != -1) {
//				total += intensity_value[up][left];
//				neighbor_num++;
//			} else if (up != -1) {
//				total += intensity_value[up][col];
//				neighbor_num++;
//			} else if (left != -1) {
//				total += intensity_value[row][left];
//				neighbor_num++;
//			}
			
			int next_symbol = fis.read();
			
			// average intensity value of neighbors
			int average_intensity = (neighbor_num == 0) ? 0 : (total / neighbor_num);
			
			// choose the model associated with the average intensity value
			model = models[average_intensity];
			intensity_value[row][col] = next_symbol;
			encoder.encode(next_symbol, model, bit_sink);
			
			// Update model used
			model.addToCount(next_symbol);
			
		}
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
		long end = System.currentTimeMillis();
		System.out.println("running time: " + (end - start));
	}

}
