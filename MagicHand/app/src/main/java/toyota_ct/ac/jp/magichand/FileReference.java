package toyota_ct.ac.jp.magichand;

import java.io.File;

/**
 * Created by daiki_000 on 2015/07/22.
 */

public class FileReference {
    public String[] name;
    int a=0,b=0;
     public void GetFileList(){
        String path = new File(".").getAbsoluteFile().getParent();
        File dir = new File(path);
        File[] files = dir.listFiles();
        for(int i=0; i<files.length; i++){
            File file = files[i];
            name[i] = files[i].getName();
            if(file.isDirectory()) {
                name[a++] = "Folder:  " +name[i];
            }else if(file.isFile()){
                name[a++] = "File:  " +name[i];
            }

        }
    }

}
