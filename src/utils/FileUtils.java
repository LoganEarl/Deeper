package utils;

import java.io.*;

public class FileUtils {
    public static void copyFile(String fromFilePath, String toFilePath){
        File fin = new File(fromFilePath);
        File fout = new File(toFilePath);

        try {
            if (fin.exists()) {
                if (!fout.exists()) {
                    fout.getParentFile().mkdirs();
                    fout.mkdirs();
                }
            }

            FileInputStream in = new FileInputStream(fin);
            FileOutputStream out = new FileOutputStream(fout);
            while(in.available() > 0){
                byte[] buff = new byte[in.available()];
                in.read(buff);
                out.write(buff);
            }

            in.close();
            out.close();
        }catch (IOException ignored){}
    }
}
