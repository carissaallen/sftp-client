import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;

import java.io.IOException;
import java.net.UnknownHostException;


class Client {

  void Sftp() {
    var jsch = new JSch();
    int option;
    var menu = new Menu();

    do{
      option = menu.mainMenu();
      if (option ==1) {
        try {
          var user = new User();
          String username = user.getUsername();
          String password = user.getPassword();
          String hostname = user.getHostname();
          Session session = jsch.getSession(username, hostname, 22);
          session.setPassword(password);
          session.setConfig("StrictHostKeyChecking", "no");
          System.out.println("Establishing Connection...");
          session.connect();

          Channel channel = session.openChannel("sftp");
          channel.connect();
          ChannelSftp cSftp = (ChannelSftp) channel;

          System.out.println("Successful SFTP connection");

          do {
            option = menu.workingMenu();
            switch (option) {
              case 1: //list directories: local and remote option
                option = menu.displayFilesMenu();
                if (option == 1) {
                  System.out.println("Listing remote directories...");
                }
                if (option == 2) {
                  System.out.println("Listing local directories and files...");
                  File currentDir = new File(".");
                  displayLocalFiles(currentDir);
                }
                break;

              case 2: //get file/files: which files, put where
                System.out.println("Getting files...");
                break;

              case 3: //put file/files: which files put where
                System.out.println("Putting Files...");
                break;

              case 4: //create directory: name?, where?
                System.out.println("Creating directories...");
                break;

              case 5: //delete file/directory
                System.out.println("Deleting directories...");
                break;

              case 6: //change permissions
                System.out.println("Changing permissions...");
                break;

              case 7: //copy directory
                System.out.println("Copying directories...");
                break;

              case 8: //rename file
                System.out.println("Renaming files...");
                break;

              case 9: //view log history
                System.out.println("Viewing log history...");
                break;

              case 10: //exit
                System.out.println("Closing connection...");
                cSftp.exit();
                session.disconnect();
                break;

              default:
                System.out.println("Try again");
                break;
            }
          } while (option != 10);
        } catch (Exception e) {
          if(e.getCause() instanceof UnknownHostException)
            System.out.println("Unknown host name");
          else if(e.getMessage().equals("Auth fail"))
            System.out.println("Authentication failed");
          else if(e.getCause() instanceof IOException)
            System.out.println("IOException detected");
          else
            e.printStackTrace();
        }
      }
    } while (option != 2);
    System.out.println("Goodbye");
  }

  public static void main(String[] args) {
    var connection = new Client();
    connection.Sftp();
  }

  /**
   * Lists all directories and files on the user's local machine (from the current directory).
   */
  public static int displayLocalFiles(File dir) {
    try {
      File[] files = dir.listFiles();
      for (File file : files) {
        if (file.isDirectory()) {
          System.out.println("Directory: " + file.getCanonicalPath());
          displayLocalFiles(file);
        } else {
          System.out.println("     File: " + file.getCanonicalPath());
        }
      }
      return 1;
    } catch (IOException e) {
      e.printStackTrace();
      return -1;
    }
  }
}

