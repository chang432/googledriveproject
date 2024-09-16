# Google Drive Desktop Application

A simple desktop application to interact with Google Drive files. Allows users to view, upload, download, and delete 
files hosted on Google Drive. 

## Setup
- Run the project first to authenticate with OAuth 2.0 or else tests may fail
- Part of the ui testing may require permissions for your IDE or Java to control the computer. 
  - For example working with intellij on macOS, you need to go to System Preferences > Security & Privacy > Privacy > Accessibility 
  and add your IDE or Java.

## Deployment
Make sure working directory is the project root directory

To run the application: '**./gradlew run**' (Includes building but not testing)

To build the application: '**./gradlew build**' (Includes testing)

## Testing
Make sure working directory is the project root directory

To test the application: '**./gradlew test**'

## General
- To reauthenticate with another google account, remove the tokens/StoredCredential file and rerun the project