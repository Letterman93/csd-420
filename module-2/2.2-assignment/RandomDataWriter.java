/*
 * Author:      Zak Nizam
 * Date:        September 15, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 2.2 Programming Assignment
 *
 * Purpose:
 *  Generate five random integers and five random doubles and append them
 *  to a binary data file. Create the file if it does not already exist.
 */

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class RandomDataWriter {
    private static final String FILE_NAME = "Zak Nizam datafile.dat";
    private static final int ARRAY_SIZE = 5;

    public static void main(String[] args) {
        Random random = new Random();
        int[] integers = new int[ARRAY_SIZE];
        double[] doubles = new double[ARRAY_SIZE];

        for (int index = 0; index < ARRAY_SIZE; index++) {
            integers[index] = random.nextInt(100);
            doubles[index] = random.nextDouble() * 100;
        }

        // The true argument enables appending rather than replacing the file.
        try (DataOutputStream output = new DataOutputStream(
                new FileOutputStream(FILE_NAME, true))) {
            // Each record stores five integers followed by five doubles.
            for (int value : integers) {
                output.writeInt(value);
            }
            for (double value : doubles) {
                output.writeDouble(value);
            }

            System.out.println("Data appended to " + FILE_NAME);
            System.out.println("Integers: " + Arrays.toString(integers));
            System.out.println("Doubles:  " + Arrays.toString(doubles));
        } catch (IOException exception) {
            System.err.println("Unable to write data: " + exception.getMessage());
            System.exit(1);
        }
    }
}
