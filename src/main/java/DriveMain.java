import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.StartPageToken;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

/* class to demonstrate use of Drive files list API */
public class DriveMain {
  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String TOKENS_DIRECTORY_PATH = "tokens";

  private static Drive service;

  /**
   * Global instance of the scopes required by this quickstart.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES =
      Collections.singletonList(DriveScopes.DRIVE);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /** MIMETYPE returned by java api mapped to working MIMETYPE and corresponding file extension */
  private static final Map<String, List<String>> MIMETYPE_MAP = new HashMap<>();
  static {
    MIMETYPE_MAP.put("application/vnd.google-apps.document", Arrays.asList("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"));
    MIMETYPE_MAP.put("application/vnd.google-apps.spreadsheet", Arrays.asList("text/csv", ".csv"));
  }

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = DriveMain.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;
  }

  private static List<DriveFile> getFiles() throws IOException {
    List<DriveFile> res = new ArrayList<>();

    // Print the names and IDs for up to 10 files
    String pageToken = null;
    int pageSize = 40;

    FileList result = service.files().list()
            .setPageSize(pageSize)
            .setPageToken(pageToken)
            .execute();
    List<File> files = result.getFiles();

//    System.out.println(result.getNextPageToken());

    if (files == null || files.isEmpty()) {
      System.out.println("No files found.");
    } else {
      for (File file : files) {
        System.out.println(file);
//        System.out.println(file.getName() + ", " + file.getId() + ", " + file.getSize());
        DriveFile fileDrive = new DriveFile(file.getId(), file.getName(), file.getMimeType(), file.getModifiedTime());
        res.add(fileDrive);
      }
    }
    return res;
  }

  private static void downloadFile(DriveFile file, String destination) throws IOException {
    String mimetype = MIMETYPE_MAP.get(file.Type).get(0);
    String extension = MIMETYPE_MAP.get(file.Type).get(1);

    String destFilePath = destination + "/" + file.Name.replace(" ","-") + extension;

    OutputStream outputStream = new FileOutputStream(destFilePath);
    service.files().export(file.Id, mimetype).executeMediaAndDownloadTo(outputStream);
    outputStream.close();

  }

  public static void main(String... args) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();

    List<DriveFile> test = getFiles();
//    System.out.println(test.get(0).toString());
    DriveFile testDoc = new DriveFile("1anjhzNnMWRmugY8UgyEJyKPhIJAW7bA2WdGhTVoUymM", "TESTDOC", "application/vnd.google-apps.document", null);
    DriveFile textSheet = new DriveFile("1DSsH27NQ11qy0EWkOO2mVAjUHqBLhrz4uFo-gUO_BLk", "Japan 2025", "application/vnd.google-apps.spreadsheet", null);
    downloadFile(textSheet, "./");
//    test.forEach(i -> System.out.println(i.toString()));
  }
}