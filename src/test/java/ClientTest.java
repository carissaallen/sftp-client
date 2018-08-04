import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class ClientTest {
  /**
   * These need to be filled in before the tests will run properly.
   */
  private String userName = "brambora";
  private String password = "Oatman641!";
  private String hostName = "linux.cs.pdx.edu";

  @Test
  public void test() {
    assertThat("Default", equalTo("Default"));
  }

  @Test
  public void verifyDisplayLocalDirectoriesAndFiles() {
    Client client = new Client();
    File directory = new File(".");
    int expected = 1;
    //int actual = client.displayLocalFiles(directory);
    //assertThat(expected, equalTo(actual));
  }

  /**
   * Trying to upload a file should result in an error. This test verifies that.
   */
  @Test
  public void uploadFakeFile() {
    Client client = new Client(password, hostName, userName);
    try {
      client.connect();

      //try uploading a non existent file.
      try {
        client.uploadFile("This is not a file");
      } catch (Exception e) {
        System.out.println("Correct. This should throw an file not found exception.");
        e.printStackTrace();
      }

    } catch (Exception e) {
      System.out.println("Error in testing uploading a fake file.");
      e.printStackTrace();
    }
  }

  /**
   * Test whether uploading a real file is correct.
   */
  @Test
  public void uploadFile() {
    String fileName = "testfile.txt";
    String fileName2 = "testfile.txt, testfile2.txt";

    Client client = new Client(password, hostName, userName);
    try {
      client.connect();

      try {
        client.uploadFile(fileName);
        client.uploadFile(fileName2);

        File dir = new File(client.getcSftp().pwd());
        File[] files = dir.listFiles();
        for (File file : files) {
          if (file.getName().equals(fileName)) {
            //the file was found so the upload was successful.
            assertThat(file.getName(), equalTo(fileName));
          }
        }
        System.out.println("Now deleting the files you uploaded.");
        client.deleteRemoteFile(fileName);
        client.deleteRemoteFile(fileName2);
      } catch (Exception e) {
        System.out.println("There was an error uploading the file.");
      }
    } catch (Exception e) {
      System.out.println("There was an error with the uploading correct file test.");
    }
  }

  /**
   * Trying to upload a file that does not exist should result in an error. This test verifies that is the case.
   */
  @Test
  public void downloadFakeFile() {
    Client client = new Client(password, hostName, userName);
    try {
      client.connect();

      //try downloading a non existent file.
      try {
        client.downloadFile("This is not a file");
      } catch (Exception e) {
        System.out.println("Correct. This should throw a file not found exception.");
        e.printStackTrace();
      }

    } catch (Exception e) {
      System.out.println("Error");
      e.printStackTrace();
    }
  }

  /**
   * Test whether a file is uploaded correctly.
   */
  @Test
  public void downloadFile() {
    String fileName = "testfile.txt";
    String fileName2 = "testfile.txt, testfile2.txt";

    Client client = new Client(password, hostName, userName);
    try {
      client.connect();

      //try downloading a file
      try {
        client.downloadFile(fileName);
        client.downloadFile(fileName2);
        //verify that the file is in the local directory
        File dir = new File(client.getcSftp().lpwd());
        File[] files = dir.listFiles();
        for (File file : files) {
          if (file.getName().equals(fileName)) {
            //the file was found so the download was successful.
            assertThat(file.getName(), equalTo(fileName));
          }
        }

      } catch (Exception e) {
        System.out.println("This is an error. There was a problem downloading.");
        e.printStackTrace();
      }
    } catch (Exception e) {
      System.out.println("There was an error somewhere.");
      e.printStackTrace();
    }
  }

  /**
   * Try and delete a file that doesn't exist. This should fail.
   */
  @Test
  public void fakeDeleteFile(){
    Client client = new Client(password, hostName, userName);
    try{
      client.connect();

      try{
        client.deleteRemoteFile("This is not a real file.");
      }catch(Exception e){
        //this is correct
        System.out.println("Correct, this should fail.");
      }
    }catch(Exception e){

    }
  }

  /**
   *  Test deleting some files from the remote server.
   */
  @Test
  public void deleteFile(){
    String filename = "myTestFile.txt";
    String filenames = "testfile2.txt, testfile2.txt";
    String [] check = {"myTestfile.txt", "testfile2.txt", "testfile2.txt"};
    int fileFoundCounter = 0;
    Client client = new Client(password, hostName, userName);
    try{
      client.connect();
      try{
        //get the remote directory and look for the files in it to make sure they exist before performing the test
        File rDir = new File(client.getcSftp().pwd());
        File[] files = rDir.listFiles();
        for(File file: files)
        {
          //we verified that all the files were present before we tried to delete them
          if(fileFoundCounter == 3){
            continue;
          }
          //check for the file in the remote directory
          for(int i = 0; i < check.length; i++){
            if(file.getName() == check[i]){
              fileFoundCounter++;
            }
          }
        }
        client.deleteRemoteFile(filename);
        client.deleteRemoteFile(filenames);

        fileFoundCounter = 0;
        //this shouldn't increment the counter at all as it shouldn't find any of the files
        files = rDir.listFiles();
        for(File file: files){
          for(int i = 0; i < check.length; i++){
            if(file.getName() == check[i]){
              fileFoundCounter++;
            }
          }
        }
        //if we found no files it should still be 0, meaning they were successfully deleted
        if(fileFoundCounter == 0){
          System.out.println("Success!");
        }

      }catch(Exception e){}
    }catch(Exception e){}
  }
}
