import com.jcraft.jsch.*;
import jdk.jshell.spi.ExecutionControlProvider;

import java.io.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;

import static java.lang.System.out;

class Client {
  private Scanner scanner = new Scanner(System.in);
  private static final int TIMEOUT = 10000;
  private User user;
  private JSch jsch;
  private Session session;
  private ChannelSftp cSftp;

  /**
   * Class constructor
   */
  public Client() {
    user = new User();
    jsch = new JSch();
    session = null;
    cSftp = new ChannelSftp();
  }

  /**
   * Prompts for connection information
   */
  void promptConnectionInfo() {
    user.getUsername();
    user.getPassword();
    user.getHostname();
  }

  /**
   * Initiates connection
   */
  void connect() throws JSchException {
    session = jsch.getSession(user.username, user.hostname, 22);
    session.setPassword(user.password);
    Properties config = new Properties();
    config.put("StrictHostKeyChecking", "no");
    session.setConfig(config);

    out.println("Establishing Connection...");
    session.connect(TIMEOUT);

    Channel channel = session.openChannel("sftp");
    channel.setInputStream(null);
    channel.connect(TIMEOUT);
    cSftp = (ChannelSftp) channel;

    out.println("Successful SFTP connection");
  }

  /**
   * Terminates connection
   */
  void disconnect() {
    cSftp.exit();
    session.disconnect();
  }

  /**
   * Lists all directories and files on the user's local machine (from the current directory).
   */
  int displayLocalFiles() {
    File dir = new File(cSftp.lpwd());
    printLocalWorkingDir();
    File[] files = dir.listFiles();
    if (files != null) {
      int count = 0;
      for (File file : files) {
        if (count == 5) {
          count = 0;
          out.println();
        }
        out.print(file.getName() + "    ");
        ++count;
      }
      out.println("\n");
    }
    return 1;
  }

  /**
   * Lists all directories and files on the user's remote machine.
   */
  void displayRemoteFiles() throws SftpException {
    printRemoteWorkingDir();
    Vector remoteDir = cSftp.ls(cSftp.pwd());
    if (remoteDir != null) {
      int count = 0;
      for (int i = 0; i < remoteDir.size(); ++i) {
        if (count == 5) {
          count = 0;
          out.println();
        }
        Object dirEntry = remoteDir.elementAt(i);
        if (dirEntry instanceof ChannelSftp.LsEntry)
          out.print(((ChannelSftp.LsEntry) dirEntry).getFilename() + "    ");
        ++count;
      }
      out.println("\n");
    }
  }

  /**
   * Create a directory on the user's remote machine.
   */
  void createRemoteDir() throws SftpException {
    out.println("Enter the name of the new directory: ");
    String newDir = scanner.next();
    cSftp.mkdir(newDir);
  }

  /**
   * Print current working local path
   */
  void printLocalWorkingDir() {
    String lpwd = cSftp.lpwd();
    out.println("This is your current local working directory: " + lpwd + "\n");
  }

  /**
   * Print current working remote path
   */
  void printRemoteWorkingDir() throws SftpException {
    String pwd = cSftp.pwd();
    out.println("This is your current remote working directory: " + pwd + "\n");
  }

  /**
   * Change current working local path
   */
  void changeLocalWorkingDir() throws SftpException {
    String newDir;
    String lpwd = cSftp.lpwd();
    out.println("This is your current local working directory: " + lpwd + "\n");
    out.println("Enter the path of the directory you'd like to change to: ");
    newDir = scanner.next();
    cSftp.lcd(newDir);
    lpwd = cSftp.lpwd();
    out.println("This is your new current local working directory: " + lpwd + "\n");
  }

  /**
   * Change current working remote path
   */
  void changeRemoteWorkingDir() throws SftpException {
    String newDir;
    String pwd = cSftp.pwd();
    out.println("This is your current local working directory: " + pwd + "\n");
    out.println("Enter the path of the directory you'd like to change to: ");
    newDir = scanner.next();
    cSftp.cd(newDir);
    pwd = cSftp.pwd();
    out.println("This is your new current local working directory: " + pwd + "\n");
  }

  /**
   * Uploads file(s) to the current working remote directory from the current working local directory.
   *
   * @param filename -- The string containing the name(s) of the file(s) you wish to work with.
   * @throws SftpException -- General errors/exceptions
   */
  public void uploadFile(String filename) throws SftpException {
    if (filename.contains(",")) {
      //multiple files are wanted.

      //take the string and separate out the files.
      String removeWhitespace = filename.replaceAll("\\s", "");
      String[] arr = removeWhitespace.split(",");
      String output = new String();
      String pwd = cSftp.pwd();
      for (String file : arr) {
        cSftp.put(file, file);
        output += file + " has been uploaded to: " + pwd + "\n";
      }
      out.println(output);
    } else {
      cSftp.put(filename, filename);
      String pwd = cSftp.pwd();
      out.println("The file has been uploaded to: " + pwd);
    }
  }

  /**
   * Downloads file(s) from the current working remote directory to the current working local directory.
   *
   * @param filename
   * @throws SftpException
   */
  public void downloadFile(String filename) throws SftpException {
    if (filename.contains(",")) {
      //multiple files are wanted.

      //take the string and separate out the files.
      String removeWhitespace = filename.replaceAll("\\s", "");
      String[] arr = removeWhitespace.split(",");
      String output = new String();
      String lpwd = cSftp.lpwd();
      for (String file : arr) {
        cSftp.get(file, file);
        output += file + " has been downloaded to: " + lpwd + "\n";
      }
      out.println(output);
    } else {
      cSftp.get(filename, filename);
      String lpwd = cSftp.lpwd();
      out.println("The file has been downloaded to: " + lpwd);
    }
  }

  /**
   * Executes a command on the remote server.
   *
   * @param command -- The text command that you'd like to execute. (Ex: "ls -a" or "cd mydirectory")
   */
  void remoteExec(String command) {
    try {
      Channel channel = session.openChannel("Exec");
      ((ChannelExec) channel).setCommand(command);
      channel.setInputStream(null);
      ((ChannelExec) channel).setErrStream(System.err);


      channel.connect();
      InputStream input = channel.getInputStream();
      try {
        InputStreamReader inputReader = new InputStreamReader(input);
        BufferedReader bufferedReader = new BufferedReader(inputReader);
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
          System.out.println(line);
        }
        bufferedReader.close();
        inputReader.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }

      channel.disconnect();
      session.disconnect();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**
   * Create a directory on the user's local machine.
   */
  void createLocalDir() {
    out.println("Enter the name of the new directory: ");
    String dirName = scanner.next();
    String path = cSftp.lpwd() + "/" + dirName;
    File newDir = new File(path);
    if (!newDir.mkdir())
      out.println("Error creating local directory.");
  }

  void deleteRemoteFile(String files){
    String pwd = new String();
    if (files.contains(",")) {
      //multiple files are wanted.
      //take the string and separate out the files.
      String removeWhitespace = files.replaceAll("\\s", "");
      String[] arr = removeWhitespace.split(",");
      String output = new String();
      try {
        pwd = cSftp.pwd();

      for (String file : arr) {
        cSftp.rm(file);
        output += file + " has been deleted from: " + pwd + "\n";
      }
      }catch(Exception e){}
      out.println(output);
    } else {
      try {
        cSftp.rm(files);
        pwd = cSftp.pwd();
      }catch(Exception e){}
      out.println("The file has been deleted from: " + pwd);
    }
  }

}