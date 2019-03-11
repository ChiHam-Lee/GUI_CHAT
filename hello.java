package hello;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;

import java.awt.Frame;

import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedWriter;

import java.io.ByteArrayOutputStream;

import java.io.FileInputStream;

import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.*;

/*
class Receive extends Thread {
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(9999);
			DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
			while (true) {
				socket.receive(packet);
				String ip = packet.getAddress().getHostAddress();
				byte[] arr = packet.getData();
				int len = packet.getLength();
				System.out.println("IP:" + ip + "  " + new String(arr, 0, len));
			}
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
}

class Send extends Thread {
	public void run() {

		try {
			Scanner sc = new Scanner(System.in);
			DatagramSocket socket = new DatagramSocket();

			while (true) {
				String line = sc.nextLine();
				if ("exit".equals(line)) {
					break;
				}
				DatagramPacket packet = new DatagramPacket(line.getBytes(), line.getBytes().length,
						InetAddress.getByName("127.0.0.1"), 9999);
				socket.send(packet);
			}
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
*/
class GUI extends Frame {
	private DatagramSocket socket;
	private TextField iptext;
	private Button send;
	private Button record;
	private Button clear;
	private Button shake;
	private TextArea chattext;
	private TextArea sendtext;
	private BufferedWriter writer;

	public GUI() {
		window();
		downpanel();
		middlepanel();
		even();
	}

	private class receive extends Thread {
		public void run() {
			try {
				DatagramSocket socket = new DatagramSocket(9999);
				DatagramPacket packet = new DatagramPacket(new byte[8192], 8192);
				while (true) {
					socket.receive(packet);
					byte[] arr = packet.getData();
					int len = packet.getLength();
					if (arr[0] == -1 && len == 1) {
						shake();
					} else {
						String messenger = new String(arr, 0, len);
						String time = getTime();
						String ip = packet.getAddress().getHostAddress();
						String string = time + "\t" + ip + "说:\n" + messenger + "\n\n";
						chattext.append(string);
						writer.write(string);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private String getTime() {
			// TODO Auto-generated method stub
			Date d = new Date();
			SimpleDateFormat sd = new SimpleDateFormat("yyyy年mm月dd日hh:mm:ss");
			return sd.format(d);
		}
	}

	public void even() {
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					socket.close();
					writer.close();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});

		// --------------------------------------------------Send----------------------------------------------------------------------
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					send();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});

		sendtext.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendtext.append("\r");
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isShiftDown()) {
					try {
						send();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		// --------------------------------------------------Record----------------------------------------------------------------------
		record.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					recordFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});
		// --------------------------------------------------Clear----------------------------------------------------------------------
		clear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				chattext.setText("");
			}
		});

		// --------------------------------------------------Shake----------------------------------------------------------------------
		shake.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					send(new byte[] { -1 }, iptext.getText());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});

	}

	// ------------------SendMethod------------------------------------------------------------
	public void send(byte[] arr, String ip) throws IOException {
		DatagramPacket packet = new DatagramPacket(arr, arr.length, InetAddress.getByName(ip), 9999);
		socket.send(packet);
	}

	private void send() throws IOException {
		// TODO Auto-generated method stub
		String messenger = sendtext.getText();// 获取发送框内容
		String ip = iptext.getText();// 获取ip地址内容
		ip = ip.trim().length() == 0?"255.255.255.255":ip;//如果ip地址为空,默认向所有人广播
		send(messenger.getBytes(), ip);
		String time = getTime();
		String string = time + "\t对" + (ip.equals("255.255.255.255")?"所有人":ip )+ "说:\n" + messenger + "\n\n";
		chattext.append(string);
		writer.write(string);// 将信息写到文件中
		sendtext.setText("");// 发送后清空发送区
	}

	private String getTime() {
		// TODO Auto-generated method stub
		Date d = new Date();
		SimpleDateFormat sd = new SimpleDateFormat("yyyy年mm月dd日HH:mm:ss");
		return sd.format(d);
	}

	// ------------------ShakeMethod------------------------------------------------------------
	private void shake() throws InterruptedException {
		int x = this.getLocation().x;// 获取x坐标
		int y = this.getLocation().y;// 获取y坐标

		for (int i = 0; i <= 10; i++) {
			this.setLocation(x + 50 * (int) Math.pow(-1, i), y + 50 * (int) Math.pow(-1, i));// 用40+(-1)的i次幂来实现转换窗口坐标
			Thread.sleep(10);
		}
		this.setLocation(x, y);
	}

	// ------------------RecordMethod------------------------------------------------------------
	private void recordFile() throws IOException {
		writer.flush();
		FileInputStream input = new FileInputStream("info.txt");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		int i;
		byte[] arr = new byte[8192];
		while ((i = input.read(arr)) != -1) {
			output.write(arr, 0, i);
		}
		String str = output.toString();
		chattext.setText(str);

		input.close();

	}

	public void middlepanel() {
		Panel middle = new Panel();
		chattext = new TextArea();// 聊天文本区
		chattext.setEditable(false);// 设置浏览文本区不可编辑
		chattext.setBackground(Color.white);// 设置背景为白,不设置则为灰
		sendtext = new TextArea();// 发送文本区
		middle.setLayout(new BorderLayout());
		middle.add(chattext, BorderLayout.CENTER);
		middle.add(sendtext, BorderLayout.SOUTH);
		this.add(middle, BorderLayout.CENTER);
	}

	public void downpanel() {
		Panel down = new Panel();
		iptext = new TextField(30);
		iptext.setText("127.0.0.1");
		send = new Button("Send");
		clear = new Button("Clear");
		record = new Button("Record");
		shake = new Button("Shake");
		down.add(iptext);
		down.add(send);
		down.add(clear);
		down.add(record);
		down.add(shake);

		this.add(down, BorderLayout.SOUTH);
	}

	public void window() {
		this.setLocation(500, 500);
		this.setSize(600, 700);
		try {
			socket = new DatagramSocket();// 新建socket对象
			writer = new BufferedWriter(new FileWriter("info.txt", true));// 新建bufferedwriter对象,绑定info.txt文件储存信息,并在尾部append不是覆盖
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new receive().start();
		this.setVisible(true);
	}

}

class hello {

	public static void main(String[] args) throws IOException, InterruptedException {
		new GUI();
	}
}
