import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

/* class to demonstrate use of Drive files list API */
public class DriveMain {
  private static final String APPLICATION_NAME = "Google Drive Application";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();  // Global instance of the JSON factory.
  private static final String TOKENS_DIRECTORY_PATH = "tokens";  // Directory to store authorization tokens for this application.
  private static final int PAGE_SIZE = 10;    // default number of files displayed
  private static Drive service;
  private static String pageToken;

  private static final List<String> SCOPES =
      Collections.singletonList(DriveScopes.DRIVE);
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

  /** MIMETYPE returned by java api mapped to working MIMETYPE */
  private static final Map<String, String> MIMETYPE_MAP = new HashMap<>();
  static {
    MIMETYPE_MAP.put("application/vnd.google-apps.document", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    MIMETYPE_MAP.put("application/vnd.google-apps.spreadsheet", "text/csv");
    MIMETYPE_MAP.put("application/octet-stream", "text/plain");
  }

  /** Working MIMETYPE mapped to file extension */
  private static final Map<String, String> MIMETYPE_EXTENSIONS_MAP = new HashMap<>();
  static {
    MIMETYPE_EXTENSIONS_MAP.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
    MIMETYPE_EXTENSIONS_MAP.put("text/csv", ".csv");
    MIMETYPE_EXTENSIONS_MAP.put("application/pdf", ".pdf");
    MIMETYPE_EXTENSIONS_MAP.put("text/plain", ".txt");
  }

  /** File extension mapped to working MIMETYPE */
  private static final Map<String, String> EXTENSION_MIMETYPE_MAP = new HashMap<>();
  static {
    EXTENSION_MIMETYPE_MAP.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    EXTENSION_MIMETYPE_MAP.put(".csv", "text/csv");
    EXTENSION_MIMETYPE_MAP.put(".pdf", "application/pdf");
    EXTENSION_MIMETYPE_MAP.put(".txt", "text/plain");
  }

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
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
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  /**
   * Initialization logic required for Google Drive API
   */
  public static void initService() throws IOException, GeneralSecurityException {
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build();
    pageToken = null;
  }

  /**
   * Queries a batch of Google Drive files. Subsequent calls returns a new batch of files
   *
   * @param startOver start query from the beginning when set to true
   * @return List of Google Drive files represented as DriveFile objects
   */
  public static List<DriveFile> getFiles(boolean startOver) throws IOException {
    if (startOver) {
      pageToken = "";
    }

    List<DriveFile> res = new ArrayList<>();

    FileList result = service.files().list()
            .setFields("nextPageToken, files(modifiedTime,id,name,mimeType)")
            .setPageSize(PAGE_SIZE)
            .setPageToken(pageToken)
            .execute();
    List<File> files = result.getFiles();

    pageToken = result.getNextPageToken();

    if (files == null || files.isEmpty()) {
      System.out.println("No files found.");
    } else {
      for (File file : files) {
        DriveFile fileDrive = new DriveFile(file.getId(), file.getName(), file.getMimeType(), file.getModifiedTime());
        res.add(fileDrive);
      }
    }
    return res;
  }

  /**
   * Downloads a file from Google Drive
   *
   * @param file The desired Google Drive file to be downloaded represented as a DriveFile object
   * @param destination The desired local target directory for the file to be downloaded to
   */
  public static void downloadFile(DriveFile file, String destination) throws IOException {
    boolean isDocsDownload = false;

    String mimetype = file.Type;

    // Figure out what kind of download to do
    if (file.Type.contains("vnd.google-apps")) {
      isDocsDownload = true;
    }

    // Use valid mimetype if the given one is google api default
    if (MIMETYPE_MAP.containsKey(file.Type)) {
      mimetype = MIMETYPE_MAP.get(file.Type);
    }

    String extension = MIMETYPE_EXTENSIONS_MAP.get(mimetype);

    String destFilePath = destination + "/" + file.Name.replace(" ","-") + extension;
    System.out.println(destFilePath + ", " + mimetype);
    OutputStream outputStream = new FileOutputStream(destFilePath);
    if (isDocsDownload) {
      service.files().export(file.Id, mimetype).executeMediaAndDownloadTo(outputStream);
    } else {
      service.files().get(file.Id).executeMediaAndDownloadTo(outputStream);
    }
    outputStream.close();
  }

  /**
   * Upload a file from local system to Google Drive
   *
   * @param source The path of the desired file to be uploaded to Google Drive
   * @return The uploaded file represented as a DriveFile object
   */
  public static DriveFile uploadFile(String source) throws IOException {
    String extension = source.substring(source.lastIndexOf("."), source.length());

    FileContent mediaContent = new FileContent(EXTENSION_MIMETYPE_MAP.get(extension), new java.io.File(source));

    File fileMetadata = new File();
    fileMetadata.setName(source.substring(source.lastIndexOf("/")+1,source.lastIndexOf(".")));

    System.out.println(mediaContent + ", " + fileMetadata);
    File file = service.files().create(fileMetadata, mediaContent)
            .execute();
    return new DriveFile(file.getId(), file.getName(), file.getMimeType(), file.getModifiedTime());
  }

  /**
   * Delete a file from Google Drive
   *
   * @param file The desired file to be deleted represented as a DriveFile object
   */
  public static void deleteFile(DriveFile file) throws IOException {
    service.files().delete(file.Id).execute();
    System.out.println("File deleted successfully");
  }

  public static void main(String... args) throws IOException, GeneralSecurityException {
    // Build a new authorized API client service.
    initService();
    List<DriveFile> test = getFiles(false);
    test.forEach(i -> System.out.println(i.toString()));
    System.out.println("-----");
    List<DriveFile> test1 = getFiles(false);
    test1.forEach(i -> System.out.println(i.toString()));
//    System.out.println(test.get(0).toString());
//    DriveFile testDoc = new DriveFile("1QRqhhwP9v6KeHoQ_T_3OVyT_l7uvekWHBpCCu5eLOcA", "TESTDOC", "application/vnd.google-apps.document", null);
//    DriveFile testTest = new DriveFile("16VVWywDmoLuTHbL2C9CZT1XSux4NDpcA", "test_file", "application/pdf", null);
//    DriveFile textSheet = new DriveFile("1DSsH27NQ11qy0EWkOO2mVAjUHqBLhrz4uFo-gUO_BLk", "Japan 2025", "application/vnd.google-apps.spreadsheet", null);
//    DriveFile n = uploadFile("./hello.txt");
//    downloadFile(testDoc,"./src");
//    System.out.println(n);
//    deleteFile(testDoc);
  }
}