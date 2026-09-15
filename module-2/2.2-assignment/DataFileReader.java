/*
 * Author:      Zak Nizam
 * Date:        September 15, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 2.2 Programming Assignment
 *
 * Purpose:
 *  Read and display every record in the binary file written by
 *  RandomDataWriter. Each record contains five integers and five doubles.
 */

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class DataFileReader {
    private static final String FILE_NAME = "Zak Nizam datafile.dat";
    private static final int ARRAY_SIZE = 5;
    private static final int RECORD_BYTES = ARRAY_SIZE * (Integer.BYTES + Double.BYTES);

    public static void main(String[] args) {
        Path file = Path.of(FILE_NAME);

        if (!Files.exists(file)) {
            System.out.println("No data file found. Run RandomDataWriter first.");
            return;
        }

        try {
            long fileSize = Files.size(file);
            if (fileSize % RECORD_BYTES != 0) {
                throw new IOException("The file contains an incomplete data record.");
            }

            try (DataInputStream input = new DataInputStream(new FileInputStream(FILE_NAME))) {
                long recordCount = fileSize / RECORD_BYTES;
                System.out.println("Reading " + FILE_NAME);

                for (long record = 1; record <= recordCount; record++) {
                    int[] integers = new int[ARRAY_SIZE];
                    double[] doubles = new double[ARRAY_SIZE];

                    // Read values in the same order used by the writer.
                    for (int index = 0; index < ARRAY_SIZE; index++) {
                        integers[index] = input.readInt();
                    }
                    for (int index = 0; index < ARRAY_SIZE; index++) {
                        doubles[index] = input.readDouble();
                    }

                    System.out.println("Record " + record);
                    System.out.println("Integers: " + Arrays.toString(integers));
                    System.out.println("Doubles:  " + Arrays.toString(doubles));
                }

                System.out.println("Records read: " + recordCount);
            }
        } catch (IOException exception) {
            System.err.println("Unable to read data: " + exception.getMessage());
            System.exit(1);
        }
    }
}
