import javafx.scene.control.Button;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.matcher.control.LabeledMatchers;
import javafx.stage.Stage;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
class DriveApplicationTest {
    @Start
    private void start(Stage stage) throws IOException, GeneralSecurityException {
        DriveMain.initService();
        new DriveApplication().start(stage);
    }

    @Test
    void testButtonsExist() {
        FxAssert.verifyThat("#uploadButton", LabeledMatchers.hasText("Upload"));
        FxAssert.verifyThat("#downloadButton", LabeledMatchers.hasText("Download"));
        FxAssert.verifyThat("#refreshButton", LabeledMatchers.hasText("Refresh"));
        FxAssert.verifyThat("#deleteButton", LabeledMatchers.hasText("Delete"));
    }

    @Test
    void testRefreshButtonFunctionality(FxRobot robot) throws IOException {
        DriveApplication.loadMoreItems();
        robot.sleep(1000);
        int doubledTableSize = DriveApplication.table.getItems().size();
        robot.clickOn("#refreshButton").sleep(1000);
        int afterRefreshTableSize = DriveApplication.table.getItems().size();
        assertEquals(doubledTableSize / 2, afterRefreshTableSize);
    }

    @Test
    void testLoadingAdditionalItems() throws IOException {
        int initialTableSize = DriveApplication.table.getItems().size();
        DriveApplication.loadMoreItems();
        int updatedTableSize = DriveApplication.table.getItems().size();
        assertEquals(initialTableSize * 2, updatedTableSize);
    }
}