import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DriveMainTest {
    @BeforeEach
    public void setUp() throws GeneralSecurityException, IOException {
        DriveMain.initService();
    }

    @Test
    void testUploadDownloadDeleteTextFile() throws Exception {
        Path filePath = Paths.get("./test_file.txt");
        String content = "test contents";

        Files.write(filePath, content.getBytes());
        DriveFile testFile = DriveMain.uploadFile(filePath.toString());

        Files.delete(filePath);
        System.out.println(testFile);
        DriveMain.downloadFile(testFile, ".");

        DriveMain.deleteFile(testFile);

        assertTrue(Files.exists(filePath));
        Files.delete(filePath);

    }

    @Test
    void testUploadDownloadDeleteWordDoc() throws Exception {
        Path filePath = Paths.get("./test_file.docx");
        String content = "test contents";

        Files.write(filePath, content.getBytes());
        DriveFile testFile = DriveMain.uploadFile(filePath.toString());

        Files.delete(filePath);
        System.out.println(testFile);
        DriveMain.downloadFile(testFile, ".");

        DriveMain.deleteFile(testFile);

        assertTrue(Files.exists(filePath));
        Files.delete(filePath);
    }

    @Test
    void testUploadDownloadDeletePdf() throws Exception {
        Path filePath = Paths.get("./test_file.pdf");
        String content = "test contents";

        Files.write(filePath, content.getBytes());
        DriveFile testFile = DriveMain.uploadFile(filePath.toString());

        Files.delete(filePath);
        System.out.println(testFile);
        DriveMain.downloadFile(testFile, ".");

        DriveMain.deleteFile(testFile);

        assertTrue(Files.exists(filePath));
        Files.delete(filePath);
    }

    @Test
    void testGetFilesBatch() throws IOException {
        List<DriveFile> files_batch1 = DriveMain.getFiles(true);
        List<DriveFile> files_batch2 = DriveMain.getFiles(true);

        boolean res = true;
        for (int i=0;i<files_batch1.size();i++) {
            if (!files_batch1.get(i).getName().equals(files_batch2.get(i).getName())) {
                res = false;
                break;
            }
        }
        assertTrue(res);
    }
}