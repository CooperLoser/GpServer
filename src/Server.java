import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
	ServerSocket serverSocket;
	ArrayList<Socket> list = new ArrayList();

	public Server() throws IOException {
		serverSocket = new ServerSocket(12345);

		while (true) {
			Socket clientSocket = serverSocket.accept();

			Thread t = new Thread(() -> {
				synchronized (list) {
					list.add(clientSocket);
				}

				try {
					serve(clientSocket);
				} catch (IOException e) {
				}

				synchronized (list) {
					list.remove(clientSocket);
				}
			});
			t.start();
		}
	}

	private void handleChatMessage(DataInputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int len = in.readInt();
		in.read(buffer, 0, len);
		System.out.println(new String(buffer, 0, len));

		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {

				try {
					Socket s = list.get(i);
					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					out.writeInt(0); // send the message type for chat message
					out.writeInt(len);
					out.write(buffer, 0, len);
					out.flush();
				} catch (IOException e) {
					System.out.println("The client is disconnected already!");
				}

			}
		}
	}

	private void handlePixelMessage(DataInputStream in) throws IOException {
		int color = in.readInt();
		int x = in.readInt();
		int y = in.readInt();

		synchronized (list) {
			for (int i = 0; i < list.size(); i++) {
				try {
					Socket s = list.get(i);
					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					out.writeInt(1); // type of message is pixel message
					out.writeInt(color);
					out.writeInt(x);
					out.writeInt(y);
					out.flush();
				} catch (IOException ex) {
					// if we go here, it means the client is disconnected
				}
			}
		}
	}

	public void serve(Socket clientSocket) throws IOException {
		DataInputStream in = new DataInputStream(clientSocket.getInputStream());

		while (true) {
			int type = in.readInt(); // get the message type

			switch (type) {
			case 0:
				handleChatMessage(in); // chat message
				break;
			case 1:
				handlePixelMessage(in);
				break;
			case 2:
				// TODO: other message?
				break;
			default:
				// something else?
			}

		}
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		new Server();
	}

}
