package network;

import java.net.Socket;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class NetworkMain {
	private String ip = "0.0.0.0";// "192.168.31.31";
	private int port = 51043;
	private String name;

	private static final Logger LOGGER = Logger.getLogger(NetworkMain.class.getName());

	public Socket socket = null;

	private BufferedWriter out;
	private BufferedReader in;
	private InputStream in1;

	public NetworkMain(String ip, int port, String name) {
		this.ip = ip;
		this.port = port;
		this.name = name;
	}

	public void connect() throws Exception {
		if (socket == null) {
			LOGGER.info("Initiating Connection with " + name + "...");
			socket = new Socket(ip, port);
			if (socket == null) {
				LOGGER.info("Failed to create socket.\n");
			}
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			in1 = socket.getInputStream();

			LOGGER.info("Connection with " + name + " established! " + socket);
			return;
		} else {
			LOGGER.info("Already connected with " + name + ". " + socket);
			return;
		}
	}

	public void sendMessage(String message) {
		if (socket == null) {
			LOGGER.warning("Not connected to " + name);
			return;
		} else {
			try {
				LOGGER.info("Sending message to " + name + "...");
				out.write(message);
				out.newLine();
				out.flush();
				LOGGER.info("Message sent: " + message);
				return;
			} catch (Exception e) {
				LOGGER.info("Sending Message Failed.\n" + e.toString());
				e.printStackTrace();
				return;
			}
		}
	}

	public String receiveImage() {
		while (true) {
			try {
				byte[] sizeAr = new byte[4];
				in1.read(sizeAr);
				int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

				byte[] imageAr = new byte[size];
				in1.read(imageAr);

				BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
				ImageIO.write(image, "jpg", new File("takenImage.jpg"));
				Thread.sleep(3000);
				String ID = readFile();
				return ID;
			} catch (Exception ex) {
				System.out.println("Not connected.\n");
			}
		}
	}

	public String readFile() {
		try {
			File myObj = new File("takenID.txt");
			Scanner myReader = new Scanner(myObj);
			String data = myReader.nextLine();
			myReader.close();
			return data;
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return null;
	}

	public String receiveMessage() {
		if (socket == null) {
			LOGGER.warning("Not connected to " + name);
			return null;
		} else {
			try {
				LOGGER.info("Receiving message from " + name + "...");
				String message = null;
				while (message == null || message.isEmpty()) {
					message = in.readLine();
					if (message != null && !message.isEmpty()) {
						LOGGER.info("Message received: " + message);
						return message;
					}
				}
			} catch (Exception e) {
				LOGGER.info("Receiving Message Failed.\n" + e.toString());
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public void disconnect() {
		LOGGER.info("Disconnecting from " + name + "...");
		if (socket == null) {
			LOGGER.warning("Not connected to " + name + ".");
			return;
		} else {
			try {
				socket.close();
				out.close();
				in.close();
				socket = null;
				return;
			} catch (Exception e) {
				LOGGER.warning("Disconnecting from " + name + " failed: " + e.toString());
				e.printStackTrace();
				return;
			}
		}
	}

	public static void main(String[] args) {
		NetworkMain test = new NetworkMain("0.0.0.0", 12345, "ImageRec");
		NetworkMain test2 = new NetworkMain("0.0.0.0", 5005, "RPI");
		try {
			test2.connect();
			test.connect();
		} catch (UnknownHostException e) {
			LOGGER.warning("Connection Failed: UnknownHostException\n" + e.toString());
			return;
		} catch (IOException e) {
			LOGGER.warning("Connection Failed: IOException\n" + e.toString());
			return;
		} catch (Exception e) {
			LOGGER.warning("Connection Failed!\n" + e.toString());
			e.printStackTrace();
			return;
		}
		test.sendMessage("Hello");
		System.out.println(test.receiveMessage());
		while (true)
			;
		// disconnect();
	}
}
