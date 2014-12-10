package soc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
    public static void write(String line) {
        try {
            File file = new File("./spark.log");
            FileWriter fw = new FileWriter(file, true);
            fw.write(line);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
