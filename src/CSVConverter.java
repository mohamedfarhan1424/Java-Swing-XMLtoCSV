
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.opencsv.CSVWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class CSVConverter extends JFrame {
	JButton button, convert, open;
	File selectedFile = null;
	JLabel label;
	String outPath="C:";

	public CSVConverter() {
		button = new JButton("Choose file");
		convert = new JButton("convert");
		open = new JButton("open");
		label = new JLabel("Choose file");
		button.setBounds(50, 50, 700, 80);
		button.setFocusable(false);
		button.setBorder(BorderFactory.createDashedBorder(Color.BLACK, 10, 10));
//		button.setBackground(Color.CYAN);
		convert.setBounds(350, 150, 100, 50);
		label.setBounds(50, 250, 400, 50);
		open.setBounds(50, 300, 80, 40);
//		label.setForeground(Color.RED);
		label.setFont(new Font("serif", 10, 20));
		convert.setEnabled(false);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				open.setVisible(false);
				label.setForeground(Color.ORANGE);
				label.setText("Choosing...");
				JFileChooser fileChooser = new JFileChooser();
				int i = fileChooser.showOpenDialog(null);
				if (i == JFileChooser.APPROVE_OPTION) {
					File f = fileChooser.getSelectedFile();
//					File f=new File("c://input.xml");
					try {
						if (f.getName().split("\\.")[1].equals("xml")) {
							selectedFile = f;
							convert.setEnabled(true);
							button.setText(f.getName());
							label.setText("File chosen :" + f.getName());
							label.setForeground(Color.ORANGE);
						} else {
							JOptionPane.showMessageDialog(null, "Invalid File");
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
					}
				} else {
					label.setForeground(Color.BLACK);
					label.setText("Choose File");
				}
			}
		});
		open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Runtime r = Runtime.getRuntime();
				try {
					r.exec("cmd /c "+outPath+"//output.csv");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		convert.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					label.setText("Extracting...");
					
					Object values[] = getXMLContents();
					
					HashMap<Integer, ArrayList<String>> grants = (HashMap<Integer, ArrayList<String>>) values[0];
					HashSet<String> columnHead = (HashSet<String>) values[1];
					HashMap<Integer, String> names = (HashMap<Integer, String>) values[2];
//					System.out.println(grants);
//					System.out.println(columnHead);
//					System.out.println(names);
					label.setText("Converting...");
					toCSV(columnHead, grants, names);
					label.setText("Converted. Saved in:"+outPath);
					label.setForeground(Color.GREEN);
					open.setVisible(true);
				} catch (Exception e1) {

					label.setText("Error occured. Try again");
					label.setForeground(Color.RED);
					e1.printStackTrace();
				}
			}
		});
		open.setVisible(false);
		add(button);
		add(convert);
		add(label);
		add(open);

		setLocation(200, 100);
		setSize(800, 500);
		setTitle("XML - CSV converter");
		setLayout(null);
		setVisible(true);
		getContentPane().setBackground(Color.WHITE);
	}

	private void toCSV(HashSet<String> columnHead, HashMap<Integer, ArrayList<String>> grants,
			HashMap<Integer, String> names) throws Exception {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select the folder to save");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = fileChooser.showOpenDialog(this);
		
		if (option == JFileChooser.APPROVE_OPTION) {
			outPath= fileChooser.getSelectedFile().getPath();
		} 
		
		File file = new File(outPath+"//output.csv");
		try {
			FileWriter outputfile = new FileWriter(file);
			CSVWriter writer = new CSVWriter(outputfile);
			String[] header = new String[columnHead.size() + 1];
			header[0] = "title";
			int ind = 1;
			for (String s : columnHead)
				header[ind++] = s;
			writer.writeNext(header);
			for (Map.Entry<Integer, String> e : names.entrySet()) {
				String data[] = new String[columnHead.size() + 1];
				data[0] = e.getValue();
				int i = 1;
				ArrayList<String> grant = grants.get(e.getKey());
				for (String s : columnHead) {
					if (grant != null && grant.contains(s)) {
						data[i++] = "Yes";
					} else {
						data[i++] = "No";
					}
				}
				writer.writeNext(data);
			}
			writer.close();
		} catch (Exception e) {
			throw e;
		}
	}

	private Object[] getXMLContents() throws Exception {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(selectedFile);
			doc.getDocumentElement().normalize();
			
			NodeList itemList = doc.getElementsByTagName("item");
			NodeList nodeList = doc.getElementsByTagName("group");
			
			HashMap<Integer, String> names = new HashMap<Integer, String>();
			HashMap<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();
			HashSet<String> head = new HashSet<String>();
			
			for (int i = 0; i < itemList.getLength(); i++) {
				Node node = itemList.item(i);
				int key = Integer.valueOf(node.getAttributes().item(0).getTextContent());
				String name = node.getChildNodes().item(1).getTextContent();
				names.put(key, name);
			}
			
			for (int itr = 0; itr < nodeList.getLength(); itr++) {
				Node node = nodeList.item(itr);
				NodeList grantList = node.getChildNodes();
				String desc = grantList.item(1).getFirstChild().getTextContent();
				head.add(desc);
				
				for (int i = 2; i < grantList.getLength(); i++) {
					Node innerNode = grantList.item(i);

					if (innerNode.getNodeName().equals("grant")) {
						int key = Integer.valueOf(innerNode.getAttributes().item(0).getTextContent());
						ArrayList<String> al = map.getOrDefault(key, new ArrayList<String>());
						al.add(desc);
						map.put(key, al);
					}
				}
			}

			return new Object[] { map, head, names };
		} catch (Exception e) {
			throw e;
		}
	}

	public static void main(String args[]) {

		new CSVConverter();
	}
}
