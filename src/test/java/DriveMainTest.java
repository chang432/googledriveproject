import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

public class DriveMainTest {

    @Test
    void testUploadDownloadDeleteMethod() {
        Path filePath = Paths.get("./test_file.txt");
        String content = "test contents";
        try {
            DriveMain.initService();

            Files.write(filePath, content.getBytes());
            DriveFile testFile = DriveMain.uploadFile(filePath.toString());

            Files.delete(filePath);
            System.out.println(testFile);
            DriveMain.downloadFile(testFile, ".");

            DriveMain.deleteFile(testFile);

            assertTrue(Files.exists(filePath));
            Files.delete(filePath);
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testAnotherMethod() {
        // Add more test methods as needed
    }
}