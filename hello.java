package heima;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.SequenceInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;
import java.security.AllPermission;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.lang.model.element.Element;
import javax.sound.midi.Sequence;
import javax.swing.*;

import org.omg.CORBA.TRANSACTION_MODE;

import heima2.*;

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
/*
 * public static int fun(int i) { ArrayList<Integer> list = new ArrayList<>();
 * for (int a = 1; a <= i; a++) { list.add(a); } int count = 1; for (int a = 0;
 * list.size() != 1; a++) { if (a == list.size()) { a = 0; }
 * 
 * if (count % 3 == 0) { list.remove(a--); }
 * 
 * count++; } return list.get(0); }
 * 
 * public static long Length(File dir) {// 计算文件夹大小方法 long length = 0; File[]
 * files = dir.listFiles(); for (File file : files) { if (file.isFile()) {
 * length += file.length(); } else { length += Length(file);//
 * 如果是文件夹,则递归调用Length方法 } } return length; }
 * 
 * public static void Copy(File one, File two) throws IOException { File toDir =
 * new File(two, one.getName());
 * 
 * toDir.mkdir(); File[] files = one.listFiles(); for (File file : files) { if
 * (file.isFile()) { BufferedInputStream bis = new BufferedInputStream(new
 * FileInputStream(file)); BufferedOutputStream bos = new BufferedOutputStream(
 * new FileOutputStream(new File(toDir, file.getName()))); int a; if ((a =
 * bis.read()) != -1) { bos.write(a); } bis.close(); bos.close(); } else {
 * Copy(file, toDir); } } }
 * 
 * public static void Print(File dir, int a) { File[] files = dir.listFiles();
 * for (File file : files) { for (int i = 0; i <= a; i++) {
 * System.out.print("\t"); } System.out.println(file); if (file.isDirectory()) {
 * 
 * Print(file, a + 1); }
 * 
 * } }
 * 
 * public static File Dir() {// 获取文件夹路径方法 System.out.println("请输入文件夹路径:");
 * Scanner sc = new Scanner(System.in); String line = sc.nextLine(); File dir =
 * new File(line); while (true) { if (!dir.exists()) {
 * System.out.println("路径不存在,请重新输入: "); return null; } else if (dir.isFile()) {
 * System.out.println("请重新输入一个文件夹路径"); } else { return dir; } } }
 * 
 * public static void Ball(int arr[]) {
 * 
 * 
 * int temp, min, locat; for (int i = 0; i < arr.length; i++) { locat = i; for
 * (int j = i; j < arr.length; j++) { if (arr[locat] > arr[j]) { locat = j; }
 * 
 * } swtich(arr, locat, i); }
 * 
 * }
 * 
 * public static void swtich(int arr[], int i, int y) { int temp; temp = arr[i];
 * arr[i] = arr[y]; arr[y] = temp; }
 * 
 * }
 * 
 * class MYRunnale implements Runnable {
 * 
 * @Override public void run() { // TODO Auto-generated method stub for (int i =
 * 0; i < 100000; i++) { System.out.println("主线程"); }
 * 
 * }
 * 
 * }
 * 
 * class MYThread extends Thread { public void run() { for (int i = 0; i <
 * 100000; i++) { System.out.println("一"); } } }
 * 
 * 
 * class outer { public static Inter method() { return new Inter() { public void
 * show() { System.out.println("hello"); } };
 * 
 * 
 * return new Inter() { public void show() { System.out.println("hello"); } };
 * 
 * 
 * } }
 * 
 * interface Inter { void show(); }
 * 
 * interface Jump { public abstract void jump(); }
 * 
 * class Cat extends Animal implements Jump, Serializable {
 * 
 * public Cat() { }
 * 
 * public Cat(String name, int leg) { // TODO Auto-generated constructor stub
 * super(name, leg); }
 * 
 * public void eat() { // TODO Auto-generated method stub
 * System.out.println("猫吃鱼"); }
 * 
 * public void sleep() { // TODO Auto-generated method stub
 * System.out.println("猫睡觉"); }
 * 
 * public void jump() { // TODO Auto-generated method stub
 * System.out.println("猫跳高"); } }
 * 
 * class Dog extends Animal implements Jump, Serializable { public Dog() {
 * 
 * }
 * 
 * public Dog(String name, int leg) { super(name, leg); }
 * 
 * @Override public void eat() { // TODO Auto-generated method stub
 * System.out.println("狗吃骨头"); }
 * 
 * @Override public void sleep() { // TODO Auto-generated method stub
 * System.out.println("狗睡觉"); }
 * 
 * @Override public void jump() { // TODO Auto-generated method stub
 * System.out.println("狗跳高"); }
 * 
 * }
 */