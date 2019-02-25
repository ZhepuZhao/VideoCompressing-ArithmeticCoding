package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class PriorValueContextAdaptiveACDecodeVideoFile {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
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
			
			// pixel index for each frame
			int pixel_index = i % 4096; // range : 0 ~ 4095
			if (pixel_index == 0) {
				previous_frame = current_frame;
				current_frame = new int[64][64];
			}
			int row = pixel_index / 64;
			int col = pixel_index - row * 64;
			
			model = models[previous_frame[row][col]]; // intensity value of the prior framework 
			
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
