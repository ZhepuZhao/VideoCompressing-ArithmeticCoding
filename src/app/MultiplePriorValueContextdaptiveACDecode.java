package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class MultiplePriorValueContextdaptiveACDecode {

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
		int[][][] frames = new int[3][64][64];

//		int[][] previous_frame = new int[64][64];
//		int[][] current_frame = new int[64][64];
		
		for (int i=0; i<num_symbols; i++) {
			
			// pixel index for each frame
			int pixel_index = i % 4096; // range : 0 ~ 4095
			if (pixel_index == 0) {
				for (int j = 0; j < 2; j++) {
					frames[j] = frames[j+1];
				}
				frames[2] = new int[64][64];
			}
			int row = pixel_index / 64;
			int col = pixel_index - row * 64;
			
			int null_frame_count = 0;
			int total_intensity = 0;
			for (int j = 0; j < 3; j++) {
				if (frames[j] != null) {
					null_frame_count++;
					total_intensity += frames[j][row][col];
				}
			}
			int average_intensity =  total_intensity / null_frame_count;
			model = models[average_intensity]; // intensity value of the prior framework 
			
			int sym = decoder.decode(model, bit_source);
			frames[2][row][col] = sym;
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
