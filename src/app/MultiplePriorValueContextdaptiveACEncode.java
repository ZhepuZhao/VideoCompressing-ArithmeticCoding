package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class MultiplePriorValueContextdaptiveACEncode {

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
			
			int next_symbol = fis.read();
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
			frames[2][row][col] = next_symbol;
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
