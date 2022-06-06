import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.imageio.ImageIO;
// �޽��� �ڽ�
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


class KOSPI extends JPanel implements Runnable {
	private JTextField searchField;
	private JButton searchButton;
	private JTable kospiTable;
	private JScrollPane scroll;
	private DefaultTableModel dtm;
	
	// ���� ���� 
	final String KOSPI_URL = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=0&page=";	// �ڽ��� �ð��Ѿ׼�
	final String ITEM_URL = "https://finance.naver.com/";											// ���� �� ���� ��ũ 1
	static String[] urlArray = new String[100];														// ���� �� ���� ��ũ 2 (�ڿ� �ջ�� ��)
	static String header[] = {"�����", "���簡", "���Ϻ�", "�����", "�ܱ��κ���"};
	static String contents[][] = new String[100][5];												// ���̺�� �����ϱ� ���� 2���� �迭

	static int lines = 0;																			// 1������ 50��, 51������ 100���� �������� �ٸ��⶧���� �̾ �����ϱ� ����          
	// ����
	String faceURL = "";
	String tailURL = "";
	URL imageURL;
	Image image;	
	JLabel chartImage;
	//
	public KOSPI() {
		setLayout(null);

		Init(KOSPI_URL + "1");							// �ڽ��� ���� 1�� ~ 50��
		Init(KOSPI_URL + "2");							// �ڽ��� ���� 51�� ~ 100��
		
		searchField = new JTextField();
		// ����Ű�� �˻��ϱ�
		searchField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchTitle = searchField.getText();
		    	for(int i = 0; i < urlArray.length; i++) {
		    		if(searchTitle.equals(contents[i][0])) {
		    			floatFrame(i, searchTitle);
		    			break;
		    		}
		    		else if(i == 99){
		    			JOptionPane.showMessageDialog(null, "�ش�Ǵ� ������ ��Ʈ�� �������� �ʽ��ϴ�.","�ȳ�", JOptionPane.ERROR_MESSAGE);
		    		}
		    	}
			}
		});
		searchField.setBounds(10, 20, 380, 20);
		
		searchButton = new JButton("�˻�");
		// ��ưŬ������ �˻��ϱ�
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		    	String searchTitle = searchField.getText();
		    	for(int i = 0; i < urlArray.length; i++) {
		    		if(searchTitle.equals(contents[i][0])) {
		    			floatFrame(i, searchTitle);
		    			break;
		    		}
		    		else if(i == 99){
		    			JOptionPane.showMessageDialog(null, "�ش�Ǵ� ������ ��Ʈ�� �������� �ʽ��ϴ�.","�ȳ�", JOptionPane.ERROR_MESSAGE);
		    		}
		    	}
		    }
		});
		searchButton.setBounds(410, 20, 60, 20);
		dtm = new DefaultTableModel(contents, header) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;	// �ֽ� ���̺� ����ڰ� ���� �Ұ�
			}
		};
		kospiTable = new JTable(dtm) {
			@Override
		    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) 
		    {
		        Component c = super.prepareRenderer(renderer, row, col);
		        if(col == 1 || col == 2 || col == 3) {
			        if(this.getValueAt(row,  3).toString().substring(0, 1).equals("+")) { // Ư���� ���� ���� ���� ã�Ƽ� �� ���� �������� �����Ѵ�
			                c.setForeground(Color.red);
			        } 
			        else if (this.getValueAt(row,  3).toString().substring(0, 1).equals("-")){
			            c.setForeground(Color.blue);
			        }
		        }
		        else {
		        	c.setForeground(Color.black);
		        }
		        return c;
		    }
			
		};
		
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(kospiTable.getModel());
		kospiTable.setRowSorter(rowSorter);
		searchField.getDocument().addDocumentListener(new DocumentListener()
				{

					@Override
					public void insertUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
		                String text = searchField.getText();

		                if (text.trim().length() == 0) {
		                    rowSorter.setRowFilter(null);
		                } else {
		                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
		                }
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						String text = searchField.getText();

		                if (text.trim().length() == 0) {
		                    rowSorter.setRowFilter(null);
		                } else {
		                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
		                }
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						  throw new UnsupportedOperationException("Not supported yet.");
					}
			
				});
		
		kospiTable.getTableHeader().setReorderingAllowed(false);				// ���� ���� �Ұ�
		kospiTable.getTableHeader().setResizingAllowed(false);					// ������ ���� �Ұ�
		kospiTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);	// �ڵ� Ű�Է� ����
		kospiTable.addMouseListener(new MouseAdapter() {	
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {	// ���̺� �ι�Ŭ�� �� ���ο� �����ӿ� ������ ���
					int itemIndex = kospiTable.getSelectedRow();
					if(itemIndex >= 0)
						floatFrame(itemIndex, contents[itemIndex][0]);
				}
			}
		});
		
		scroll = new JScrollPane(kospiTable);
		scroll.setBounds(10, 50, 560, 650);
		
		add(scroll);
		add(searchField);
		add(searchButton);
	}
	
	void Init(String url) {												// �������� �������� �Լ�
		try {
			Document kospi = Jsoup.connect(url).get();
			Elements standard = new Elements(kospi.select("a.tltle"));	// ������ ��(���� ��)
			Elements number = new Elements(kospi.select("td.number"));	// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			
			Iterator<Element> title_list = standard.listIterator();
			Iterator<Element> number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				String urls = tmp.select("a").attr("href");				// Ŭ�� �� ������ ��ũ
				String title = tmp.text();								// �����
				String price = number_list.next().text();				// ���簡
				String yprice = number_list.next().text();				// ���Ϻ�
				String percent = number_list.next().text();				// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				String foreign = number_list.next().text();				// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				
				contents[lines][0] = title;
				contents[lines][1] = price;
				contents[lines][2] = yprice;
				contents[lines][3] = percent;
				contents[lines][4] = foreign;
				urlArray[lines] = ITEM_URL + urls;
				lines++;
			}
		} catch(IOException e) { System.out.println("class KOSPI : ��ȣ���� ���� �ּ��Դϴ�"); }
	}
	
	void floatFrame(int index, String title) {							// �ֽ� ������ �������� ���� �Լ�
		// ������
		JFrame tmpFrame = new JFrame(title);
		JButton[] btn = new JButton[10];
		JLabel[] labels = new JLabel[8];
		chartImage = new JLabel();
		LineBorder border = new LineBorder(Color.BLACK, 1, true);
		chartImage.setBorder(border);
		// ��Ʈ
		Font head = new Font("Dialog", Font.BOLD, 30);
		Font sub = new Font("Dialog", Font.BOLD, 20);
		Font etc = new Font("Dialog", Font.BOLD, 13);
		// ����Ʈ ��ư
		btn[0] = new JButton("1��");
		btn[1] = new JButton("1����");
		btn[2] = new JButton("3����");
		btn[3] = new JButton("1��");
		btn[4] = new JButton("3��");
		btn[5] = new JButton("5��");
		btn[6] = new JButton("10��");
		// ����Ʈ ��ư
		btn[7] = new JButton("�Ϻ�");
		btn[8] = new JButton("�ֺ�");
		btn[9] = new JButton("����");
		
		try {
			
			Document doc2 = Jsoup.connect(urlArray[index]).get();
			Iterator<Element> items = new Elements(doc2).select("div.chart").listIterator();
			Iterator<Element> infos = new Elements(doc2).select("div.rate_info").listIterator();
			
			while(items.hasNext()) {
				Element item = items.next();
				Element info = infos.next();	
				// ���� �ڽ�
				String today = info.select("div.today").text();
				String[] today_info = today.split(" ");
				
				if(today_info[3].equals("�϶�")) {
					labels[0] = new JLabel(title + " " + today_info[0] + "��");																								// ���簡
					labels[0].setForeground(Color.blue);
					labels[0].setFont(head);
					labels[1] = new JLabel("���ϴ�� " + today_info[3] + " " + today_info[4] + " | " + today_info[7] + today_info[9] + today_info[10]);	// ���� ���
					labels[1].setForeground(Color.blue);
					labels[1].setFont(sub);
				}
				else if(today_info[3].equals("���")) {
					labels[0] = new JLabel(title + " " + today_info[0] + "��");																								// ���簡
					labels[0].setForeground(Color.red);
					labels[0].setFont(head);
					labels[1] = new JLabel("���ϴ�� " + today_info[3] + " " + today_info[4] + " | " + today_info[7] + today_info[9] + today_info[10]);	// ���� ���
					labels[1].setForeground(Color.red);
					labels[1].setFont(sub);
				}
				else if(today_info[3].equals("����")) {
					labels[0] = new JLabel(title + " " + today_info[0] + "��");																								// ���簡
					labels[0].setFont(head);
					labels[1] = new JLabel("���ϴ�� " + today_info[3] + " " + today_info[4] + " | " + today_info[7] + today_info[8] + today_info[9]);	// ���� ���
					labels[1].setFont(sub);
				}
				// ������ �ڽ�
				String body = info.select("tbody").text();
				String[] body_info = body.split(" ");
				
				labels[2] = new JLabel(body_info[0] + " " + body_info[1]);										// ����
				body_info[12] = body_info[12].substring(0, body_info[12].length() / 2);
				labels[3] = new JLabel(body_info[11] + " " + body_info[12]);									// �ð�
				body_info[4] = body_info[4].substring(0, body_info[4].length() / 2);
				body_info[6] = body_info[6].substring(0, body_info[6].length() / 2);
				labels[4] = new JLabel(body_info[3] + " " + body_info[4] + " (���Ѱ�" + body_info[6] + ")");		// ��
				body_info[14] = body_info[14].substring(0, body_info[14].length() / 2);
				labels[5] = new JLabel(body_info[13] + " " + body_info[14] + " (���Ѱ�" + body_info[16] + ")");	// ����
				labels[6] = new JLabel(body_info[8] + " " + body_info[9]);										// �ŷ���
				labels[7] = new JLabel(body_info[18] + " " + body_info[19] + body_info[21]);					// �ŷ����
				
				labels[2].setFont(etc);
				labels[3].setFont(etc);
				labels[4].setFont(etc);
				labels[5].setFont(etc);
				labels[6].setFont(etc);
				labels[7].setFont(etc);
				
				String temp = item.select("img").attr("src");
				
				int i = temp.indexOf("day");
				faceURL = temp.substring(0, i - 5);												// �պκ� ���� (�߰��� area or candle + day week �� ��)
				tailURL = temp.substring(i + 3, temp.length());									// �޺κ� ����
				imageURL = new URL(temp);														// String������ ���� URL�� URL �������� �����
				image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);	// �ش� URL�� ���� Image ���Ŀ� �־� ������ ���� �� �ε�
				chartImage.setIcon(new ImageIcon(image));
			}
			
			for(JButton b : btn) {
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String divide = e.getActionCommand();
						try {
							switch(divide) {
								case "1��":
									imageURL = new URL(faceURL + "area/day" + tailURL);
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "1����":
									imageURL = new URL(faceURL + "area/week" + tailURL);		
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "3����":
									imageURL = new URL(faceURL + "area/month3" + tailURL);	
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "1��":
									imageURL = new URL(faceURL + "area/year" + tailURL);	
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "3��":
									imageURL = new URL(faceURL + "area/year3" + tailURL);	
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "5��":
									imageURL = new URL(faceURL + "area/year5" + tailURL);		
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "10��":
									imageURL = new URL(faceURL + "area/year10" + tailURL);		
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "�Ϻ�":
									imageURL = new URL(faceURL + "candle/day" + tailURL);		
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "�ֺ�":
									imageURL = new URL(faceURL + "candle/week" + tailURL);			
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								case "����":
									imageURL = new URL(faceURL + "candle/month" + tailURL);	
									image = ImageIO.read(imageURL).getScaledInstance(550, 231, Image.SCALE_SMOOTH);
									chartImage.setIcon(new ImageIcon(image));
									break;
								default:
									break;
							}
						} catch (Exception e3) { e3.printStackTrace(); }
					}
				});
			}
			
		} catch (Exception e2) { e2.printStackTrace();}
		
		chartImage.setBounds(15, 150, 550, 231);
		btn[0].setBounds(15, 440, 70, 20);
		btn[1].setBounds(95, 440, 70, 20);
		btn[2].setBounds(175, 440, 70, 20);
		btn[3].setBounds(255, 440, 70, 20);
		btn[4].setBounds(335, 440, 70, 20);
		btn[5].setBounds(415, 440, 70, 20);
		btn[6].setBounds(495, 440, 70, 20);
		btn[7].setBounds(15, 470, 70, 20);
		btn[8].setBounds(95, 470, 70, 20);
		btn[9].setBounds(175, 470, 70, 20);
		labels[0].setBounds(20, 10, 560, 50);
		labels[1].setBounds(20, 60, 300, 20);
		labels[2].setBounds(20, 90, 100, 20);
		labels[3].setBounds(20, 110, 100, 20);
		labels[4].setBounds(130, 90, 200, 20);
		labels[5].setBounds(130, 110, 200, 20);
		labels[6].setBounds(390, 90, 150, 20);
		labels[7].setBounds(390, 110, 150, 20);
		
		
		tmpFrame.add(chartImage);
		for(int cnt = 0; cnt < btn.length; cnt++)
			tmpFrame.add(btn[cnt]);
		for(int cnt = 0; cnt < labels.length; cnt++)
			tmpFrame.add(labels[cnt]);
		
		tmpFrame.setLayout(null);
		tmpFrame.setBounds(300, 100, 600, 550);
		tmpFrame.setResizable(false);
		tmpFrame.setVisible(true);
	}

	@Override
	public void run() {
		while(true) {
			try {
				lines = 0;
				Thread.sleep(3000);
				Init(KOSPI_URL + "1");						// �ڽ��� ���� 1�� ~ 50��
				Init(KOSPI_URL + "2");						// �ڽ��� ���� 51�� ~ 100��
				dtm.setDataVector(contents, header);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}	
		}
	}
}

class KOSDAQ extends JPanel implements Runnable
{
	private JTextField searchField;
	private JButton searchButton;
	private final String KOSDAQ_URL = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=1&";
	private final String BASE_URL = "https://finance.naver.com/";
	private String[] TITLE = new String[13];
	
	public String[] KOSDOQ_URL;
	public String[][] Image_URL = new String[10][100];
	public LinkedList<String> Kos_url = new LinkedList<String>();
	
	public String url = "";
	public String clicked = "";
	
	public LinkedList<String[]> data = new LinkedList<String[]>();
	public String[][] data_ar = new String[100][5];
	public URL img;
	
	public JFrame info = new JFrame("Information");
	public DefaultTableModel model;
	public JTable table;

	
	public KOSDAQ() 
	{
		Create_Frame();
	}
	
	public void Create_Frame()
	{
		setLayout(null);
		searchField = new JTextField();
		searchField.setBounds(10, 20, 460, 20);
		
		searchButton = new JButton("�˻�");
		searchButton.setBounds(480, 20, 70, 20);
		
		add(searchField);
		add(searchButton);
		
		Load_Title();
		
		Load_Kosdaq(1, data);
		Load_Kosdaq(2, data);
		
		Load_URL(1, Kos_url);
		Load_URL(2, Kos_url);
		
		KOSDOQ_URL = Kos_url.toArray(new String[Kos_url.size()]);
		
		//Print_Kosdaq(data);
		
		
		Build_Table();
	}
	
	public void Print_Kosdaq(LinkedList<String[]> data)
	{
		ListIterator<String[]> data_itor = data.listIterator();
		
		for(String s : TITLE)
		{
			System.out.print(s + "\t");
		}
		System.out.println();
		while(data_itor.hasNext())
		{
			for(String s : data_itor.next())
				System.out.print(s + "\t");
			
			System.out.println();
		}
		Init_Itor(data_itor);
	}
	
	public LinkedList<String[]> Load_Kosdaq(int page, LinkedList<String[]> kosdaq_list)
	{
		int cnt = 0;
		try
		{
			Document doc = Jsoup.connect(KOSDAQ_URL + "page=" + page).get();
			String css_dir = ".box_type_l table tbody tr";
			Elements elements = doc.select(css_dir);
			
			
			
	        for(Element element : elements)
	        {
	        	if(!element.text().equals("") && cnt != 50)
	        	{
	        		cnt++;
	        		String[] tmp = new String[12];
	        		tmp[0] = element.select(".no").text();
	        		tmp[1] = element.select(".tltle").text();
	        		
	        		for(int i = 2; i < 11; i++)
	        			tmp[i] = element.select(".number").text().split(" ")[i-2];
	        		kosdaq_list.add(tmp);
	        	}
	        }
	        	
	        

		} catch(Exception e) {e.printStackTrace();}
		return kosdaq_list;
	}
	
	public LinkedList<String> Load_URL(int page, LinkedList<String> result)
	{
		try
		{
			Document doc = Jsoup.connect(KOSDAQ_URL + "page=" + page).get();
			Elements elements = doc.select("table tbody tr td a");
			
			int cnt = 0;
	        for(Element element : elements)
	        {
	        	if(cnt%2==0 && cnt < 100)
	        		result.add(BASE_URL + element.attr("href"));
	        	cnt++;
	        }
	        	
		} catch(Exception e) {e.printStackTrace();}

		return result;
	}
	
	public void Load_Title()
	{
		try
		{
			Document doc = Jsoup.connect(KOSDAQ_URL + "page=1").get();
			Elements elements = doc.select("table thead tr th");

	        int count = 0;
	        for(Element element : elements)
	        	TITLE[count++] = element.text();
	        
		} catch(Exception e) {e.printStackTrace();}

	}
	
	public void Save_Kosdaq()
	{
		
	}
	
	public void View_Info()
	{
		String URL = KOSDOQ_URL[Integer.parseInt(clicked)-1];
		
		info.getContentPane().removeAll();
		info.repaint();
		
		
		info.setSize(700, 1000);
		info.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		info.setVisible(true);
		info.setLayout(new GridLayout(3, 1));
		
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(2, 1));
		top.setSize(700, 400);

		String[][] Info = new String[7][2];
		String[] Info_Title = {"����", "��", "���Ѱ�", "�ŷ���", "�ð�", "����", "�ŷ����"};
		
		try
		{		
			Document doc = Jsoup.connect(URL).get();
			Elements elements = doc.select(".rate_info tbody tr td em .blind");
			
			int cnt = 0;
			for(Element el : elements)
			{
				Info[cnt++][1] = el.text();
			}
			
			for(int i=0; i<Info_Title.length; i++)
				Info[i][0] = Info_Title[i];
			
		} catch (IOException e) {e.printStackTrace();}
		
		
		String[][] result = new String[11][2];
		ListIterator<String[]> L = data.listIterator();
		
		for(int j = 1; j<Integer.parseInt(clicked); j++)
			L.next();
		String[] information = L.next();
		
		for(int i = 1; i < TITLE.length-1; i++)
		{
			result[i-1][0] = TITLE[i];
			result[i-1][1] = information[i];
		}
		
		DefaultTableModel model = new DefaultTableModel(result, new Object[]{"TITLE", "INFORMATION"});
		
		for(int i = 0; i< Info.length; i++)
			model.addRow( Info[i]);
		
		JTable tb = new JTable(model) 
		{
			public boolean isCellEditable(int row, int column){return false;}
		};
		tb.setSize(700, 300);
		JScrollPane sp = new JScrollPane(tb);
		info.add(sp);
		
		
		try 
		{
			Document doc = Jsoup.connect(URL).get();
			Elements elements = doc.select(".chart img");
			String inner_url = elements.attr("src");
			
			if(url.equals(""))
				url = inner_url;
				
			img = new URL(url);
			BufferedImage image = ImageIO.read(img);
			JLabel stock_img = new JLabel(new ImageIcon(image));
			info.add(stock_img);
			info.pack();
			
		} catch (IOException e) {e.printStackTrace();}
		
		JPanel bottom = new JPanel();
		bottom.setLayout(new FlowLayout());
		String[] button_name = {"1��", "1����", "3����", "1��", "3��", "5��", "10��", "�Ϻ�", "�ֺ�", "����"};
		
		for(String s : button_name)
		{
			JButton tmp = new JButton(s);
			tmp.addActionListener(new ActionListener() 
					{
						public String change_link(String[] in, String change, boolean IsArea)
						{
							in[7] = change;
							if(!IsArea)
								in[6] = "candle";
							String out = "";
							
							for(String s : in)
								out += "/" + s;
							return out.replace("/https", "https");
						}
						
						
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							String[] url_div = url.split("/");
							
							switch(e.getActionCommand())
							{
								case "1��":
									url = change_link(url_div, "day", true);
									break;
								
								case "1����":
									url = change_link(url_div, "week", true);
									break;
								
								case "3����":
									url = change_link(url_div, "month3", true);
									break;
								
								case "1��":
									url = change_link(url_div, "year", true);
									break;
								
								case "3��":
									url = change_link(url_div, "year3", true);
									break;
								
								case "5��":
									url = change_link(url_div, "year5", true);
									break;
								
								case "10��":
									url = change_link(url_div, "year10", true);
									break;
								
								case "�Ϻ�":
									url = change_link(url_div, "day", false);
									break;
								
								case "�ֺ�":
									url = change_link(url_div, "week", false);
									break;
								
								case "����":
									url = change_link(url_div, "month", false);
									break;
								
							}
							
							View_Info();
						}
				
					});
			bottom.add(tmp);
		}

		info.add(bottom);
		
		
	}
	
	public void Build_Table()
	{
		ListIterator<String[]> data_L = data.listIterator();
		
		String[] in_title = new String[5];
		
		for(int i=0; i<5; i++)
			in_title[i] = TITLE[i];

		int cnt = 0;
		while(data_L.hasNext())
		{
			String[] tmp = data_L.next();
			for(int j=0; j<5; j++)
				data_ar[cnt][j] = tmp[j];
			cnt++;
		}
		Init_Itor(data_L);
		
		model = new DefaultTableModel(data_ar, in_title);
		table = new JTable(model) {
									public boolean isCellEditable(int row, int column)
									{return false;}
									
									
										@Override
									    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) 
									    {
									        Component c = super.prepareRenderer(renderer, row, col);
									        if(col == 1 || col == 2 || col == 3|| col == 4) 
									        {
										        if(this.getValueAt(row,  4).toString().contains("-")) 
										        	c.setForeground(Color.blue);
										        
										        else if (this.getValueAt(row,  4).toString().contains("+"))
										            c.setForeground(Color.red);
									        }
									        else
									        	c.setForeground(Color.black);
									        return c;
									    }
		};


		
		TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(table.getModel());
		table.setRowSorter(rowSorter);
		searchField.getDocument().addDocumentListener(new DocumentListener()
				{

					@Override
					public void insertUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
		                String text = searchField.getText();

		                if (text.trim().length() == 0) {
		                    rowSorter.setRowFilter(null);
		                } else {
		                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
		                }
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						String text = searchField.getText();

		                if (text.trim().length() == 0) {
		                    rowSorter.setRowFilter(null);
		                } else {
		                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
		                }
					}

					@Override
					public void changedUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
						  throw new UnsupportedOperationException("Not supported yet.");
					}
			
				});
		
		
		JScrollPane sp = new JScrollPane(table)	;
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
														 	{
																public void valueChanged(ListSelectionEvent e) 
																{
																	if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1)
																	{
																		clicked = table.getValueAt(table.getSelectedRow(), 0).toString();
																		
																		try
																		{
																			Document doc = Jsoup.connect(KOSDOQ_URL[Integer.parseInt(clicked)-1]).get();
																			Elements elements = doc.select(".chart img");
																			url = elements.attr("src");
																		} catch(Exception ex) {ex.printStackTrace();}
																		View_Info();
																		
																	}
																		
																}
														    });
		table.getTableHeader().setReorderingAllowed(false);
		sp.setBounds(0, 100, 598, 600);
		add(sp);
	}
	
	public ListIterator<?> Init_Itor(ListIterator<?> L)
	{
		while(L.hasNext())
			L.previous();
		return L;
	}
	
	public void Refresh_Char()
	{
		ListIterator<String[]> data_L = data.listIterator();
		
		String[] in_title = new String[5];
		
		for(int i=0; i<5; i++)
			in_title[i] = TITLE[i];

		int cnt = 0;
		while(data_L.hasNext())
		{
			String[] tmp = data_L.next();
			for(int j=0; j<5; j++)
				data_ar[cnt][j] = tmp[j];
			cnt++;
		}
		Init_Itor(data_L);
		
		model.setDataVector(data_ar, in_title);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(230);
		table.getColumnModel().getColumn(2).setPreferredWidth(110);
		table.getColumnModel().getColumn(3).setPreferredWidth(110);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
	}

	@Override
	public void run() 
	{
		while(true)
		{
			try 
			{
				Thread.sleep(1000);
				data.clear();
				Load_Kosdaq(1, data);
				Load_Kosdaq(2, data);
				Refresh_Char();
			} catch (InterruptedException e) 
			{
				e.printStackTrace();
			}

		}
		
	}
}

class Stock_Market_Status extends JPanel {
	URL imageURL;
	Image image;
	String kospi_url = "https://finance.naver.com/sise/sise_index.nhn?code=KOSPI";
	String kosdaq_url = "https://finance.naver.com/sise/sise_index.nhn?code=KOSDAQ";
	
	void draw(String URL, int type) {

		try {
			JLabel Title_Label = new JLabel();
			if(type == 0) 
				Title_Label.setText("�ڽ���");
			else
				Title_Label.setText("�ڽ���");
				
			JLabel [] Labels = new JLabel[28];										// JLabel ����
			JButton [] Buttons = new JButton[4];	
			JLabel upDown = new JLabel("���/����");
				
			Document doc = Jsoup.connect(URL).get();
			Elements graph = doc.select("ul.term");
			Elements subtop = doc.select("div.subtop_sise_detail");
			Elements kos = doc.select("dl.lst_kos_info");
			Iterator<Element> value = null;
			
			String char_url = doc.select("div.graph").select("img").attr("src").toString();
			
			String kType1 = subtop.select("div.quotient").toString();		// default
			String kType2 = subtop.select("div.quotient.dn").toString();	// �϶���
			String kType3 = subtop.select("div.quotient.up").toString();	// �����
		
			// value�� ������ ��Ȳ(����Ʈ��, �϶���, �����)�� �´� html�� �Ľ��ϱ� ����
			if(!(kType1 == null))
				value = subtop.select("div.quotient").select("em").iterator();
			else if(!(kType2 == null))
				value = subtop.select("div.quotient.dn").select("em").iterator();
			else if(!(kType3 == null))
				value = subtop.select("div.quotient.up").select("em").iterator();
			
			Iterator<Element> value_rate = subtop.select("span.fluc").iterator();
			Iterator<Element> info1 = subtop.select("td.td").iterator();	// �ŷ���, �����ְ�, 52���ְ�
			Iterator<Element> info2 = subtop.select("td.td2").iterator();	// �ŷ����, ��������, 52������
			Iterator<Element> info3 = subtop.select("td.td3").iterator();	// ��� / ���� 
			Iterator<Element> info4 = kos.select("dl.lst_kos_info").iterator();
			
			imageURL = new URL(char_url);														// String������ ���� URL�� URL �������� �����
			image = ImageIO.read(imageURL).getScaledInstance(201, 140, Image.SCALE_SMOOTH);	// �ش� URL�� ���� Image ���Ŀ� �־� ������ ���� �� �ε�
			JLabel chartImage = new JLabel(new ImageIcon(image));
			LineBorder border = new LineBorder(Color.BLACK, 1, true);	// �׵θ�
			chartImage.setBorder(border);								// �׵θ�
			String frontURL = char_url.substring(0, char_url.indexOf("day"));
			String backURL = char_url.substring(char_url.indexOf("day") + 3, char_url.length());
			
			Font head = new Font("Dialog", Font.BOLD, 30);         
			Font sub = new Font("Dialog", Font.BOLD, 20);         
			Font etc = new Font("Dialog", Font.BOLD, 13);
			Font bnt = new Font("Dialog", Font.PLAIN , 10);
			
			while(value.hasNext()) {
				String volume = info1.next().text();		// �ŷ���
				String high_value = info1.next().text();	// �����ְ�
				String high52 = info1.next().text();		// 52���ְ�
				String trade_cash = info2.next().text();	// �ŷ����
				String low_value = info2.next().text();		// ��������
				String low52 = info2.next().text();			// 52������
				String td3 = info3.next().text();
				String dl = info4.next().text();
				
				

				String[] dlArr = dl.split(" ");	// " " �������� �߶� dlArr �� ����
								
				
				String[] td3Arr = td3.split(" ");	// " " �������� �߶� td3Arr �� ����
				
				for(int i = 0; i < td3Arr.length; i ++) {
					td3Arr[i] = td3Arr[i].substring(td3Arr[i].indexOf("��") + 1, td3Arr[i].length());
				}
				
				String values = value.next().text();
				
				String rate = value_rate.next().text();
				rate = rate.substring(0, rate.indexOf("���"));
				
				String[] rateArr = rate.split(" ");
				if(rateArr[1].substring(0,1).equals("+")) {
					Labels[0] = new JLabel(values + " ��" + rateArr[0] + " " + rateArr[1]);
					Labels[0].setForeground(Color.red);
				}
				else if(rateArr[1].substring(0,1).equals("-")) {
					Labels[0] = new JLabel(values + " ��" + rateArr[0] + " " + rateArr[1]);
					Labels[0].setForeground(Color.blue);
				}
				Labels[1] = new JLabel("�ŷ���(õ��)" + "  " + volume);
				Labels[2] = new JLabel("�����ְ�" + "  " + high_value);
				Labels[3] = new JLabel("52���ְ�" + "  " + high52);
				Labels[4] = new JLabel("�ŷ����(�鸸)" + "  " + trade_cash);
				Labels[5] = new JLabel("��������" + "  " + low_value);
				Labels[6] = new JLabel("52������" + "  " + low52);
				Labels[7] = new JLabel("��" + td3Arr[0]);
				Labels[7].setForeground(Color.red);
				Labels[8] = new JLabel("��" + td3Arr[1]);
				Labels[8].setForeground(Color.red);
				Labels[9] = new JLabel("��" + td3Arr[2]);
				Labels[10] = new JLabel("��" + td3Arr[3]);
				Labels[10].setForeground(Color.blue);
				Labels[11] = new JLabel("��" + td3Arr[4]);	
				Labels[11].setForeground(Color.blue);
				Labels[12] = new JLabel(dlArr[0]);
				Labels[13] = new JLabel(dlArr[1]);
				Labels[14] = new JLabel(dlArr[2]);
				Labels[15] = new JLabel(dlArr[3]);
				Labels[16] = new JLabel(dlArr[4]);
				Labels[17] = new JLabel(dlArr[5]);
				Labels[18] = new JLabel(dlArr[6]);
				Labels[19] = new JLabel(dlArr[7]);			
				Labels[20] = new JLabel(dlArr[8]);
				Labels[21] = new JLabel(dlArr[9]);
				Labels[22] = new JLabel(dlArr[10]);
				Labels[23] = new JLabel(dlArr[11]);
				Labels[24] = new JLabel(dlArr[12]);
				Labels[25] = new JLabel(dlArr[13]);
				Labels[26] = new JLabel(dlArr[14]);
				Labels[27] = new JLabel(dlArr[15]);
					
					
				for(int i = 0; i < dlArr.length; i++) {		
					if(dlArr[i].substring(0,1).equals("+")) 
						Labels[i+12].setForeground(Color.red);
					else if(dlArr[i].substring(0,1).equals("-")) 
						Labels[i+12].setForeground(Color.blue);
				}
				
				
				Buttons[0] = new JButton("1��");
				Buttons[1] = new JButton("3��");
				Buttons[2] = new JButton("1��");
				Buttons[3] = new JButton("3��");
				
				
				
				
			
				if(type == 0) {
					Title_Label.setBounds(10,20,200,30);
					chartImage.setBounds(10,70,201,140);
					upDown.setBounds(221,190,60,20);
					Labels[0].setBounds(221,70,350,20);
					Labels[1].setBounds(221,100,170,20);
					Labels[2].setBounds(221,130,170,20);
					Labels[3].setBounds(221,160,170,20);
					Labels[4].setBounds(411,100,170,20);
					Labels[5].setBounds(411,130,170,20);
					Labels[6].setBounds(411,160,170,20);
					Labels[7].setBounds(281,190,60,20);
					Labels[7].setForeground(Color.red);
					Labels[8] = new JLabel("��" + td3Arr[1]);
					Labels[8].setForeground(Color.red);
					Labels[9] = new JLabel("��" + td3Arr[2]);
					Labels[10] = new JLabel("��" + td3Arr[3]);
					Labels[10].setForeground(Color.blue);
					Labels[11] = new JLabel("��" + td3Arr[4]);	
					Labels[11].setForeground(Color.blue);
					Labels[8].setBounds(341,190,60,20);
					Labels[9].setBounds(401,190,60,20);
					Labels[10].setBounds(461,190,60,20);
					Labels[11].setBounds(521,190,60,20);
					Labels[12].setBounds(10,250,70,20);
					Labels[13].setBounds(10,270,70,20);
					Labels[14].setBounds(80,250,70,20);
					Labels[15].setBounds(80,270,70,20);
					Labels[16].setBounds(150,250,70,20);
					Labels[17].setBounds(150,270,70,20);
					Labels[18].setBounds(220,250,70,20);
					Labels[19].setBounds(220,270,70,20);
					Labels[20].setBounds(290,250,70,20);
					Labels[21].setBounds(290,270,70,20);
					Labels[22].setBounds(360,250,70,20);
					Labels[23].setBounds(360,270,70,20);
					Labels[24].setBounds(430,250,70,20);
					Labels[25].setBounds(430,270,70,20);
					Labels[26].setBounds(500,250,70,20);
					Labels[27].setBounds(500,270,70,20);

					
					Buttons[0].setBounds(10,220,50,20);
					Buttons[1].setBounds(60,220,50,20);
					Buttons[2].setBounds(110,220,50,20);
					Buttons[3].setBounds(160,220,50,20);
				}
				
				else {
					Title_Label.setBounds(10,320,200,30);
					chartImage.setBounds(10,370,201,140);
					upDown.setBounds(221,490,60,20);
					Labels[0].setBounds(221,370,350,20);
					Labels[1].setBounds(221,400,170,20);
					Labels[2].setBounds(221,430,170,20);
					Labels[3].setBounds(221,460,170,20);
					Labels[4].setBounds(411,400,170,20);
					Labels[5].setBounds(411,430,170,20);
					Labels[6].setBounds(411,460,170,20);
					Labels[7].setBounds(281,490,60,20);
					Labels[8].setBounds(341,490,60,20);
					Labels[9].setBounds(401,490,60,20);
					Labels[10].setBounds(461,490,60,20);
					Labels[11].setBounds(521,490,60,20);		
					Labels[12].setBounds(10,550,70,20);
					Labels[13].setBounds(10,570,70,20);
					Labels[14].setBounds(80,550,70,20);
					Labels[15].setBounds(80,570,70,20);
					Labels[16].setBounds(150,550,70,20);
					Labels[17].setBounds(150,570,70,20);
					Labels[18].setBounds(220,550,70,20);
					Labels[19].setBounds(220,570,70,20);
					Labels[20].setBounds(290,550,70,20);
					Labels[21].setBounds(290,570,70,20);
					Labels[22].setBounds(360,550,70,20);
					Labels[23].setBounds(360,570,70,20);
					Labels[24].setBounds(430,550,70,20);
					Labels[25].setBounds(430,570,70,20);
					Labels[26].setBounds(500,550,70,20);
					Labels[27].setBounds(500,570,70,20);
		
					
					Buttons[0].setBounds(10,520,50,20);
					Buttons[1].setBounds(60,520,50,20);
					Buttons[2].setBounds(110,520,50,20);
					Buttons[3].setBounds(160,520,50,20);
				}
					
				Title_Label.setFont(head);
				Labels[0].setFont(sub);

				for(JButton b : Buttons) {
					b.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String divide = e.getActionCommand();
							try {
								switch(divide) {
									case "1��":
										imageURL = new URL(frontURL + "day" + backURL);
										image = ImageIO.read(imageURL).getScaledInstance(201, 140, Image.SCALE_SMOOTH);
										chartImage.setIcon(new ImageIcon(image));
										break;
									case "3��":
										imageURL = new URL(frontURL + "day90" + backURL);		
										image = ImageIO.read(imageURL).getScaledInstance(201, 140, Image.SCALE_SMOOTH);
										chartImage.setIcon(new ImageIcon(image));
										break;
									case "1��":
										imageURL = new URL(frontURL + "day365" + backURL);	
										image = ImageIO.read(imageURL).getScaledInstance(201, 140, Image.SCALE_SMOOTH);
										chartImage.setIcon(new ImageIcon(image));
										break;
									case "3��":
										imageURL = new URL(frontURL + "day1095" + backURL);	
										image = ImageIO.read(imageURL).getScaledInstance(201, 140, Image.SCALE_SMOOTH);
										chartImage.setIcon(new ImageIcon(image));
										break;
									default:
										break;
								}
							} catch (Exception e3) { e3.printStackTrace(); }
						}
					});
				}
				
				add(Title_Label);
				for(int i = 0; i < Labels.length; i++) {
					add(Labels[i]);
				}				
				add(chartImage);		
				for(int i = 0; i < Buttons.length; i++) { 
					Buttons[i].setFont(bnt);
					add(Buttons[i]);
				}
				add(upDown);
				

			}
		}catch(IOException e){
		}
	}
	
	
	public Stock_Market_Status(){
		setLayout(null);
		
		draw(kospi_url, 0);
		draw(kosdaq_url, 1);
		
	}
}

class Buying extends JPanel {
	private JTextField searchField = new JTextField();
	private JButton searchButton = new JButton("�ż�");
	private JTable table;
	private JScrollPane scroll;
	private DefaultTableModel dtm;
	private String[] now_title = new String[200];
	private int[] now_price = new int[200];
	
	final String SELL_PATH = "./database/sell_log.txt";						// �ŵ� �α� ���� ���
	final String BUY_PATH = "./database/buy_log.txt";						// �ż� �α� ���� ���
	final String STOCK_PATH = "./database/now_stock.txt";					// ���� �ֽ� ���� ���
	final String header[] = {"�����", "�ż���(��)", "����(��)", "�Ѿ�"};
	
	// �ִ� �뷮�� 1000���� ���� ����
	static String[][] sell_list = null;
	static String[][] buy_list = null;
	static String[][] now_stock = null;
	
	public Buying() {
		Init();
	}
	
	public void Init() {
		setLayout(null);
		
		sell_list = load(SELL_PATH);
		buy_list = load(BUY_PATH);
		now_stock = load(STOCK_PATH);
		
		dtm = new DefaultTableModel(buy_list, header) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;	// �ֽ� ���̺� ����ڰ� ���� �Ұ�
			}
		};
		table = new JTable(dtm);
		table.getTableHeader().setReorderingAllowed(false);					// ���� ���� �Ұ�
		table.getTableHeader().setResizingAllowed(false);					// ������ ���� �Ұ�
		table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);	// �ڵ� Ű�Է� ����
		
		scroll = new JScrollPane(table);
		
		searchField.addActionListener(new ActionListener() {				// ���͸� ���� �ŵ�
			public void actionPerformed(ActionEvent e) {
				String searchTitle = searchField.getText();
				savePrice();
		    	for(int i = 0; i < 200; i++) {								// ���� �ڽ��� �ڽ��ڿ� �ִ� �������� Ȯ��
		    		if(searchTitle.equals(now_title[i])) {
		    			floatFrame(now_title[i], now_stock, now_price[i]);	// �ش� ������ ������Ȳ�� �Լ��� �Ѱ���
		    			break;
		    		}
		    		else if(i == 199){
		    			JOptionPane.showMessageDialog(null, "�ش�Ǵ� ������ �����ϴ�.","�ȳ�", JOptionPane.ERROR_MESSAGE);
		    		}
		    	}
			}
		});
		
		searchButton.addActionListener(new ActionListener() {				// ��ư�� ���콺�� Ŭ������ �ŵ�
			public void actionPerformed(ActionEvent e) {
		    	String searchTitle = searchField.getText();
		    	savePrice();
		    	for(int i = 0; i < 200; i++) {								// ���� �ڽ��� �ڽ��ڿ� �ִ� �������� Ȯ��
		    		if(searchTitle.equals(now_title[i])) {
		    			floatFrame(now_title[i], now_stock, now_price[i]);	// �ش� ������ ������Ȳ�� �Լ��� �Ѱ���
		    			break;
		    		}
		    		else if(i == 199){
		    			JOptionPane.showMessageDialog(null, "�ش�Ǵ� ������ �����ϴ�.","�ȳ�", JOptionPane.ERROR_MESSAGE);
		    		}
		    	}
		    }
		});
		
		scroll.setBounds(10, 60, 560, 800);
		searchField.setBounds(10, 20, 450, 20);
		searchButton.setBounds(470, 20, 100, 20);
		
		add(scroll);
		add(searchField);
		add(searchButton);
	}
	
	void savePrice() {
		sell_list = load(SELL_PATH);															// �ż� �� ����
		buy_list = load(BUY_PATH);																// �ż� �� ����
		now_stock = load(STOCK_PATH);															// �ż� �� ����
		String KOSPI50 = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=0&page=1";	// �ڽ��� 50������
		String KOSPI100 = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=0&page=2";	// �ڽ��� 100������
		String KOSDAQ50 = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=1&page=1";	// �ڽ��� 50������
		String KOSDAQ100 = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=1&page=2";	// �ڽ��� 100������
		
		int cnt = 0;	// 200�������� ������ ����
		try {
			Document doc = Jsoup.connect(KOSPI50).get();
			Document doc2 = Jsoup.connect(KOSPI100).get();
			Document doc3 = Jsoup.connect(KOSDAQ50).get();
			Document doc4 = Jsoup.connect(KOSDAQ100).get();
			
			// �ڽ��� 1�� ~ 50�� ���
			Elements standard = new Elements(doc.select("a.tltle"));	// ������ ��(���� ��)
			Elements number = new Elements(doc.select("td.number"));	// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			Iterator<Element> title_list = standard.listIterator();
			Iterator<Element> number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				now_title[cnt] = tmp.text();							// �����
				now_price[cnt] = Integer.parseInt(number_list.next().text().replace(",", ""));// ���簡
				number_list.next();										// ���Ϻ�
				number_list.next();										// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				number_list.next().text();								// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				cnt++;
			}
			
			// �ڽ��� 51�� ~ 100�� ���
			standard = new Elements(doc2.select("a.tltle"));				// ������ ��(���� ��)
			number = new Elements(doc2.select("td.number"));				// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			title_list = standard.listIterator();
			number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				now_title[cnt] = tmp.text();							// �����
				now_price[cnt] = Integer.parseInt(number_list.next().text().replace(",", ""));// ���簡
				number_list.next();										// ���Ϻ�
				number_list.next();										// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				number_list.next().text();								// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				cnt++;
			}
			
			// �ڽ��� 1�� ~ 50�� ���
			standard = new Elements(doc3.select("a.tltle"));				// ������ ��(���� ��)
			number = new Elements(doc3.select("td.number"));				// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			title_list = standard.listIterator();
			number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				now_title[cnt] = tmp.text();							// �����
				now_price[cnt] = Integer.parseInt(number_list.next().text().replace(",", ""));// ���簡
				number_list.next();										// ���Ϻ�
				number_list.next();										// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				number_list.next().text();								// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				cnt++;
			}
			
			// �ڽ��� 51�� ~ 100�� ���
			standard = new Elements(doc4.select("a.tltle"));				// ������ ��(���� ��)
			number = new Elements(doc4.select("td.number"));				// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			title_list = standard.listIterator();
			number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				now_title[cnt] = tmp.text();							// �����
				now_price[cnt] = Integer.parseInt(number_list.next().text().replace(",", ""));// ���簡
				number_list.next();										// ���Ϻ�
				number_list.next();										// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				number_list.next().text();								// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				cnt++;
			}
			
		} catch (IOException e) {e.printStackTrace();}
	}
	
	void floatFrame(String title, String[][] list, int price) {
		JFrame tmpFrame = new JFrame(title);
		JButton[] btn = new JButton[2];			// �ż�, ��ҹ�ư
		JTextField txt;							// �����ֽ� ��
		JLabel[] lab = new JLabel[4];			// ���� ���� �ؽ�Ʈ + ���� �ֽ� ����
		int counts = 0;							// ���� �ֽ� ����
		
		for(int i = 0; i < list.length; i++) {	// ���� �ֽ��� ������ �����ϴ� �ݺ���
			if(!(list[i][0] == null)) {
				if(list[i][0].equals(title))
					counts = Integer.parseInt(list[i][1]);
			}
		}
		lab[0] = new JLabel("���� �ֽ� : " + counts + "��");
		lab[1] = new JLabel("���ż���");
		lab[2] = new JLabel("���űݾ�");
		lab[3] = new JLabel(Integer.toString(price));
		txt = new JTextField();
		btn[0] = new JButton("�ż�");
		btn[1] = new JButton("���");

		lab[0].setBounds(10, 10, 280, 20);
		lab[1].setBounds(10, 30, 50, 20);
		lab[2].setBounds(10, 50, 50, 20);
		lab[3].setBounds(70, 50, 180, 20);
		txt.setBounds(70, 30, 180, 20);
		btn[0].setBounds(10, 80, 115, 20);
		btn[1].setBounds(133, 80, 115, 20);
		
		btn[0].addActionListener(new ActionListener() {	// �ż� ��ư
			public void actionPerformed(ActionEvent e) {
		    	int mount = Integer.parseInt(txt.getText());// ����
		    	int total = 0;							// ���� �ֽ� ������ �ѷ�
		    	String s = "";							// ���� �ֽ� ��Ȳ�� �� ���� �о� ������ �ӽú���
		    	boolean isEnter = false;				// �ŵ� �αװ� ��������� ù���� ���͸� �Է����� ����
		    	String dummy = "";						// ���� ���� ���� ���� (������ �κ� ����)
		    	String original = "";					// ���� ���� ���� ���� (������ �κ� ����)
		    	
		    	try {
		    		// ���� ��Ȳ ���Ͽ� ������ �κ��� �� �������� ���̿� �ְ� ������ ���� �� ���� ����� ���̰��� �ٽ� ���Ͼȿ� �־��ְ� ���� ������ �κ��� �߰���
		    		File buyFile = new File(BUY_PATH);
		    		BufferedReader buyReader = new BufferedReader(new InputStreamReader(new FileInputStream(buyFile), "utf-8"));
		    		BufferedWriter buyWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(buyFile, true), StandardCharsets.UTF_8));
		    		File stockFile = new File(STOCK_PATH);
		    		BufferedReader stockReader = new BufferedReader(new InputStreamReader(new FileInputStream(stockFile), "utf-8"));
		    		BufferedWriter stockWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stockFile, true), StandardCharsets.UTF_8));
		    		
		    		if(buyReader.readLine() != null)
		    			isEnter = true;
		    		buyReader.close();
		    		
		    		// ���� �ֽ� ��Ȳ���� ������ �κ��� ������ ���� (������ ���� �߰�)
		    		while((s = stockReader.readLine()) != null) {
		    			original += s + "\n";
		    			String[] check = s.split("\\|");
		    			if(check[0].equals(title))
		    				total = Integer.parseInt(check[1]) + mount;
		    			else
		    				dummy += s + "\n";
		    		}
		    		if(total == 0)	// ���� �ֽ� ��Ȳ�� ���� ���
		    			total = mount;
		    		
		    		stockWriter.close();
		    		stockReader.close();
		    		
		    		stockFile.delete();
		    		stockFile = new File(STOCK_PATH);
		    		stockWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stockFile, true), StandardCharsets.UTF_8));
		    		if(total >= 1) {
		    			// �ش� ���� �ܿ� �ֽ��� 1�� �̻��� ���
		    			// �ŵ� �α׿� ���� �� ���� ����� ����
		    			if(isEnter)
		    				buyWriter.newLine();
		    			buyWriter.append(title + "|" + price + "|" + mount);
		    			buyWriter.flush();
		    			buyWriter.close();
			    		stockWriter.append(title + "|" + total + "|����");
			    		stockWriter.newLine();
			    		stockWriter.flush();
		    			stockWriter.write(dummy);
		    			stockWriter.flush();
		    			stockWriter.close();
		    		}
		    		else if(total == 0) {
		    			// �ش� ���� �ܿ� �ֽ��� ���� ��� (0��)
		    			// �ŵ� �α׿� ���� �� ���� ����� ����
		    			if(isEnter)
		    				buyWriter.newLine();
		    			buyWriter.append(title + "|" + price + "|" + mount);
		    			buyWriter.flush();
		    			buyWriter.close();
		    			stockWriter.write(dummy);
		    			stockWriter.flush();
		    			stockWriter.close();
		    		}
		    		
		    	} catch (Exception e2) { e2.printStackTrace();
		    								tmpFrame.dispose();
		    								Init();}
		    	tmpFrame.dispose();
		    	
		    	// ������ �ٽ� �ҷ����� ���̺��� �ٽ� ä������
		    	sell_list = load(SELL_PATH);
				buy_list = load(BUY_PATH);
				now_stock = load(STOCK_PATH);
				
				dtm.setDataVector(buy_list, header);
				table.setModel(dtm);
		    }
		});
		
		btn[1].addActionListener(new ActionListener() {	// ��� ��ư
			public void actionPerformed(ActionEvent e) {
		    	tmpFrame.dispose();
		    }
		});
		
		tmpFrame.add(txt);
		for(JLabel l : lab)
			tmpFrame.add(l);
		for(JButton b : btn)
			tmpFrame.add(b);
		
		tmpFrame.setLayout(null);
		tmpFrame.setVisible(true);
		tmpFrame.setResizable(false);
		tmpFrame.setBounds(100, 10, 270, 145);
	}
    
	String[][] load(String FILEPATH) 
    {
		String[][] arr = null;
		
		if(FILEPATH.equals("./database/now_stock.txt"))	// �ֽ� ���� ��Ȳ
			arr = new String[1000][3];
		else											// �ŵ����, �ż���� 
			arr = new String[1000][4];
    	try 
    	{
    		File isDir = new File("./database");
    		if(!isDir.exists())
    			isDir.mkdirs();
            File f = new File(FILEPATH);
            if(!f.exists())
            	f.createNewFile();
            
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
            String s;
            int line = 0;
            while((s = buf.readLine()) != null) {
            	String[] tmp = s.split("\\|");
            	if(FILEPATH.equals("./database/now_stock.txt")) {
            		for(int i = 0; i < tmp.length; i++)
	            		arr[line][i] = tmp[i];
            	}
            	else {
            		for(int i = 0; i < tmp.length + 1; i++) {
	            		if(i != 3)
	            			arr[line][i] = tmp[i];
	            		else {	// �ŵ� �ֽ� ���� * �ŵ� �ֽ� �ݾ�
	            			arr[line][i] = Integer.toString(Integer.parseInt(tmp[i-2]) * Integer.parseInt(tmp[i-1]));
	            		}
	            	}
            	}
            	line++;
            }
            buf.close();
            
    	} catch (Exception e) {e.printStackTrace();}
    	return arr;
    }
}

class Selling extends JPanel {
	private JTextField searchField = new JTextField();
	private JButton searchButton = new JButton("�ŵ�");
	private JTable table;
	private JScrollPane scroll;
	private DefaultTableModel dtm;
	private String[] now_title = new String[200];
	private int[] now_price = new int[200];
	
	final String SELL_PATH = "./database/sell_log.txt";						// �ŵ� �α� ���� ���
	final String BUY_PATH = "./database/buy_log.txt";						// �ż� �α� ���� ���
	final String STOCK_PATH = "./database/now_stock.txt";					// ���� �ֽ� ���� ���
	final String header[] = {"�����", "�ŵ���(��)", "����(��)", "�Ѿ�"};
	
	// �ִ� �뷮�� 1000���� ���� ����
	static String[][] sell_list = null;
	static String[][] buy_list = null;
	static String[][] now_stock = null;
	
	public Selling() {
		Init();
	}
	
	public void Init() {
		setLayout(null);
		
		sell_list = load(SELL_PATH);
		buy_list = load(BUY_PATH);
		now_stock = load(STOCK_PATH);
		
		dtm = new DefaultTableModel(sell_list, header) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;	// �ֽ� ���̺� ����ڰ� ���� �Ұ�
			}
		};
		table = new JTable(dtm);
		table.getTableHeader().setReorderingAllowed(false);					// ���� ���� �Ұ�
		table.getTableHeader().setResizingAllowed(false);					// ������ ���� �Ұ�
		table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);	// �ڵ� Ű�Է� ����
		
		scroll = new JScrollPane(table);
		
		searchField.addActionListener(new ActionListener() {				// ���͸� ���� �ŵ�
			public void actionPerformed(ActionEvent e) {
				savePrice();
				String searchTitle = searchField.getText();
		    	for(int i = 0; i < 1000; i++) {
		    		if(searchTitle.equals(now_stock[i][0])) {				// ���� ��Ȳ�� �ִ� �ֽ����� ���� Ȯ��
		    			for(int j = 0; j < 200; j++) {
		    				if(searchTitle.equals(now_title[j]))			// �ش� �ֽ��� ���� ���� ��������
		    					floatFrame(now_title[j], now_stock, now_price[j]);		// �ش� ������ ������Ȳ�� �Լ��� �Ѱ���
		    			}
		    			break;
		    		}
		    		else if(i == 999){
		    			JOptionPane.showMessageDialog(null, "�ش� ������ �����ϰ� ���� �ʽ��ϴ�.","�ȳ�", JOptionPane.ERROR_MESSAGE);
		    		}
		    	}
			}
		});
		
		searchButton.addActionListener(new ActionListener() {				// ��ư�� ���콺�� Ŭ������ �ŵ�
			public void actionPerformed(ActionEvent e) {
				savePrice();
				String searchTitle = searchField.getText();
		    	for(int i = 0; i < 1000; i++) {
		    		if(searchTitle.equals(now_stock[i][0])) {				// ���� ��Ȳ�� �ִ� �ֽ����� ���� Ȯ��
		    			for(int j = 0; j < 200; j++) {
		    				if(searchTitle.equals(now_title[j]))			// �ش� �ֽ��� ���� ���� ��������
		    					floatFrame(now_title[j], now_stock, now_price[j]);		// �ش� ������ ������Ȳ�� �Լ��� �Ѱ���
		    			}
		    			break;
		    		}
		    		else if(i == 999){
		    			JOptionPane.showMessageDialog(null, "�ش� ������ �����ϰ� ���� �ʽ��ϴ�.","�ȳ�", JOptionPane.ERROR_MESSAGE);
		    		}
		    	}
		    }
		});
		
		scroll.setBounds(10, 60, 560, 800);
		searchField.setBounds(10, 20, 450, 20);
		searchButton.setBounds(470, 20, 100, 20);
		
		add(scroll);
		add(searchField);
		add(searchButton);
	}
	
	void savePrice() {
		sell_list = load(SELL_PATH);															// �ŵ� �� ����
		buy_list = load(BUY_PATH);																// �ŵ� �� ����
		now_stock = load(STOCK_PATH);															// �ŵ� �� ����
		String KOSPI50 = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=0&page=1";	// �ڽ��� 50������
		String KOSPI100 = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=0&page=2";	// �ڽ��� 100������
		String KOSDAQ50 = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=1&page=1";	// �ڽ��� 50������
		String KOSDAQ100 = "https://finance.naver.com/sise/sise_market_sum.nhn?sosok=1&page=2";	// �ڽ��� 100������
		int cnt = 0;	// 200�������� ������ ����
		try {
			Document doc = Jsoup.connect(KOSPI50).get();				// �ڽ��� 1�� ~ 50�� ���
			Elements standard = new Elements(doc.select("a.tltle"));	// ������ ��(���� ��)
			Elements number = new Elements(doc.select("td.number"));	// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			Iterator<Element> title_list = standard.listIterator();
			Iterator<Element> number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				now_title[cnt] = tmp.text();							// �����
				now_price[cnt] = Integer.parseInt(number_list.next().text().replace(",", ""));// ���簡
				number_list.next();										// ���Ϻ�
				number_list.next();										// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				number_list.next().text();								// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				cnt++;
			}
			
			doc = Jsoup.connect(KOSPI100).get();						// �ڽ��� 51�� ~ 100�� ���
			standard = new Elements(doc.select("a.tltle"));				// ������ ��(���� ��)
			number = new Elements(doc.select("td.number"));				// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			title_list = standard.listIterator();
			number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				now_title[cnt] = tmp.text();							// �����
				now_price[cnt] = Integer.parseInt(number_list.next().text().replace(",", ""));// ���簡
				number_list.next();										// ���Ϻ�
				number_list.next();										// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				number_list.next().text();								// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				cnt++;
			}
			
			doc = Jsoup.connect(KOSDAQ50).get();						// �ڽ��� 1�� ~ 50�� ���
			standard = new Elements(doc.select("a.tltle"));				// ������ ��(���� ��)
			number = new Elements(doc.select("td.number"));				// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			title_list = standard.listIterator();
			number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				now_title[cnt] = tmp.text();							// �����
				now_price[cnt] = Integer.parseInt(number_list.next().text().replace(",", ""));// ���簡
				number_list.next();										// ���Ϻ�
				number_list.next();										// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				number_list.next().text();								// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				cnt++;
			}
			
			doc = Jsoup.connect(KOSDAQ100).get();						// �ڽ��� 51�� ~ 100�� ���
			standard = new Elements(doc.select("a.tltle"));				// ������ ��(���� ��)
			number = new Elements(doc.select("td.number"));				// �� �� ���簡,���Ϻ�,�����,�׸鰡,�ð��Ѿ�,�����ֽļ�,�ܱ��κ���,�ŷ���,PER,ROE ���� 
			title_list = standard.listIterator();
			number_list = number.listIterator();
			
			while(title_list.hasNext()) {
				Element tmp = title_list.next();						// Ŭ�� �� ������ ��ũ�� ���� �ӽ� Element
				now_title[cnt] = tmp.text();							// �����
				now_price[cnt] = Integer.parseInt(number_list.next().text().replace(",", ""));// ���簡
				number_list.next();										// ���Ϻ�
				number_list.next();										// �����
				number_list.next().text();								// �׸鰡
				number_list.next().text();								// �ð��Ѿ�
				number_list.next().text();								// �����ֽļ�
				number_list.next().text();								// �ܱ��κ���
				number_list.next().text();								// �ŷ���
				number_list.next().text();								// PER
				number_list.next().text();								// ROE
				cnt++;
			}
			
		} catch (IOException e) {e.printStackTrace();}
	}
	
	void floatFrame(String title, String[][] list, int price) {
		JFrame tmpFrame = new JFrame(title);
		JButton[] btn = new JButton[2];			// �ŵ�, ��ҹ�ư
		JTextField txt;							// �Ǹ��ֽ� ��
		JLabel[] lab = new JLabel[4];			// ���� ���� �ؽ�Ʈ + ���� �ֽ� ����
		int counts = 0;							// ���� �ֽ� ����
		
		for(int i = 0; i < list.length; i++) {	// ���� �ֽ��� ������ �����ϴ� �ݺ���
			if(!(list[i][0] == null)) {
				if(list[i][0].equals(title))
					counts = Integer.parseInt(list[i][1]);
			}
		}
		lab[0] = new JLabel("���� �ֽ� : " + counts + "��");
		lab[1] = new JLabel("�Ǹż���");
		lab[2] = new JLabel("�Ǹűݾ�");
		lab[3] = new JLabel(Integer.toString(price));
		txt = new JTextField();
		btn[0] = new JButton("�ŵ�");
		btn[1] = new JButton("���");

		lab[0].setBounds(10, 10, 280, 20);
		lab[1].setBounds(10, 30, 50, 20);
		lab[2].setBounds(10, 50, 50, 20);
		lab[3].setBounds(70, 50, 180, 20);
		txt.setBounds(70, 30, 180, 20);
		btn[0].setBounds(10, 80, 115, 20);
		btn[1].setBounds(133, 80, 115, 20);
		
		btn[0].addActionListener(new ActionListener() {	// �ŵ� ��ư
			public void actionPerformed(ActionEvent e) {
		    	int mount = Integer.parseInt(txt.getText());	// ����
		    	int total = 0;							// ���� �ֽ� ������ �ѷ�
		    	String s = "";							// ���� �ֽ� ��Ȳ�� �� ���� �о� ������ �ӽú���
		    	boolean isEnter = false;				// �ŵ� �αװ� ��������� ù���� ���͸� �Է����� ����
		    	String dummy = "";						// ���� ���� ���� ���� (������ �κ� ����)
		    	String original = "";					// ���� ���� ���� ���� (������ �κ� ����)
		    	
		    	try {
		    		// ���� ��Ȳ ���Ͽ� ������ �κ��� �� �������� ���̿� �ְ� ������ ���� �� ���� ����� ���̰��� �ٽ� ���Ͼȿ� �־��ְ� ���� ������ �κ��� �߰���
		    		File sellFile = new File(SELL_PATH);
		    		BufferedReader sellReader = new BufferedReader(new InputStreamReader(new FileInputStream(sellFile), "utf-8"));
		    		BufferedWriter sellWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sellFile, true), StandardCharsets.UTF_8));
		    		File stockFile = new File(STOCK_PATH);
		    		BufferedReader stockReader = new BufferedReader(new InputStreamReader(new FileInputStream(stockFile), "utf-8"));
		    		BufferedWriter stockWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stockFile, true), StandardCharsets.UTF_8));
		    		
		    		if(sellReader.readLine() != null)
		    			isEnter = true;
		    		sellReader.close();
		    		
		    		// ���� �ֽ� ��Ȳ���� ������ �κ��� ������ ����
		    		while((s = stockReader.readLine()) != null) {
		    			original += s + "\n";
		    			String[] check = s.split("\\|");
		    			if(check[0].equals(title))
		    				total = Integer.parseInt(check[1]) - mount;
		    			else
		    				dummy += s + "\n";
		    		}
		    		stockWriter.close();
		    		stockReader.close();
		    		
		    		stockFile.delete();
		    		stockFile = new File(STOCK_PATH);
		    		stockWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(stockFile, true), StandardCharsets.UTF_8));
		    		if(total >= 1) {
		    			// �ش� ���� �ܿ� �ֽ��� 1�� �̻��� ���
		    			// �ŵ� �α׿� ���� �� ���� ����� ����
		    			if(isEnter)
		    				sellWriter.newLine();
			    		sellWriter.append(title + "|" + price + "|" + mount);
			    		sellWriter.flush();
			    		sellWriter.close();
			    		stockWriter.append(title + "|" + total + "|����");
			    		stockWriter.newLine();
			    		stockWriter.flush();
		    			stockWriter.write(dummy);
		    			stockWriter.flush();
		    			stockWriter.close();
		    		}
		    		else if(total == 0) {
		    			// �ش� ���� �ܿ� �ֽ��� ���� ��� (0��)
		    			// �ŵ� �α׿� ���� �� ���� ����� ����
		    			if(isEnter)
		    				sellWriter.newLine();
			    		sellWriter.append(title + "|" + price + "|" + mount);
			    		sellWriter.flush();
			    		sellWriter.close();
		    			stockWriter.write(dummy);
		    			stockWriter.flush();
		    			stockWriter.close();
		    		}
		    		else {
		    			// �ش� ���� �ܿ� �ֽ��� -1�̻� �Ǵ� ��� (����)
		    			// �ƹ� �α׸� ������ �ʰ� ����
		    			sellWriter.close();
		    			JOptionPane.showMessageDialog(null, "���� �ֽĺ��� ���� �ֽ��� �ŵ��� �� �����ϴ�.","�ȳ�", JOptionPane.ERROR_MESSAGE);
		    			stockWriter.write(original);
		    			stockWriter.flush();
		    			stockWriter.close();
		    		}
		    		
		    	} catch (Exception e2) { e2.printStackTrace();
		    								tmpFrame.dispose();
		    								Init();}
		    	tmpFrame.dispose();
		    	
		    	// ������ �ٽ� �ҷ����� ���̺��� �ٽ� ä������
		    	sell_list = load(SELL_PATH);
				buy_list = load(BUY_PATH);
				now_stock = load(STOCK_PATH);
				
				dtm.setDataVector(sell_list, header);
				table.setModel(dtm);
		    }
		});
		
		btn[1].addActionListener(new ActionListener() {	// ��� ��ư
			public void actionPerformed(ActionEvent e) {
		    	tmpFrame.dispose();
		    }
		});
		
		tmpFrame.add(txt);
		for(JLabel l : lab)
			tmpFrame.add(l);
		for(JButton b : btn)
			tmpFrame.add(b);
		
		tmpFrame.setLayout(null);
		tmpFrame.setVisible(true);
		tmpFrame.setResizable(false);
		tmpFrame.setBounds(100, 10, 270, 145);
	}
    
	String[][] load(String FILEPATH) 
    {
		String[][] arr = null;
		
		if(FILEPATH.equals("./database/now_stock.txt"))	// �ֽ� ���� ��Ȳ
			arr = new String[1000][3];
		else											// �ŵ����, �ż���� 
			arr = new String[1000][4];
    	try 
    	{
    		File isDir = new File("./database");
    		if(!isDir.exists())
    			isDir.mkdirs();
            File f = new File(FILEPATH);
            if(!f.exists())
            	f.createNewFile();
            
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
            String s;
            int line = 0;
            while((s = buf.readLine()) != null) {
            	String[] tmp = s.split("\\|");
            	if(FILEPATH.equals("./database/now_stock.txt")) {
            		for(int i = 0; i < tmp.length; i++)
	            		arr[line][i] = tmp[i];
            	}
            	else {
            		for(int i = 0; i < tmp.length + 1; i++) {
	            		if(i != 3)
	            			arr[line][i] = tmp[i];
	            		else {	// �ŵ� �ֽ� ���� * �ŵ� �ֽ� �ݾ�
	            			arr[line][i] = Integer.toString(Integer.parseInt(tmp[i-2]) * Integer.parseInt(tmp[i-1]));
	            		}
	            	}
            	}
            	line++;
            }
            buf.close();
            
    	} catch (Exception e) {e.printStackTrace();}
    	return arr;
    }
}
	
class My_Stock extends JPanel implements Runnable
{
	
	private DefaultTableModel model;
	private JTable table;
	private JPanel top = new JPanel();
	private JLabel l_buy = new JLabel();
	private JLabel l_sell = new JLabel();
	private JLabel l_gap = new JLabel();
	
	private int buy;
	private int sell;
	private int gap;
	
	My_Stock()
	{
		Build_Panel();
	}

	public void Build_Panel()
	{	
		setLayout(new GridLayout(3, 1));
		top.setLayout(new GridLayout(3, 1));
		
		l_buy.setText("�� ���űݾ� : " + buy);
		top.add(l_buy);
		
		l_sell.setText("�� �Ǹűݾ� : " + sell);
		top.add(l_sell);
		
		l_gap.setText("���� : " + gap);
		top.add(l_gap);
		
		add(top);
		
	}
	
	public void Load_BuyData()
	{
		buy = 0;
		String BUY_PATH = "./database/buy_log.txt";

		
		LinkedList<String[]> buy_data = new LinkedList<String[]>();
		
		File isDir = new File("./database");
		if(!isDir.exists())
			isDir.mkdirs();
        File f = new File(BUY_PATH);
        
        try
        {
            if(!f.exists())
            	f.createNewFile();
            
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
            String s;
            int line = 0;
            while((s = buf.readLine()) != null)
            	buy_data.add(s.split("\\|"));
            
        }catch(Exception e) {e.printStackTrace();}
        ListIterator<String[]> L_buy = buy_data.listIterator();
        while(L_buy.hasNext())
        {
        	String[] tmp = L_buy.next();
        	buy += Integer.parseInt(tmp[1]) * Integer.parseInt(tmp[2]);
        }
        
        System.out.println(buy);
        while(L_buy.hasPrevious())
        	L_buy.previous();
	}
	
	public void Load_SellData()
	{
		sell = 0;
		String SELL_PATH = "./database/sell_log.txt";

		
		LinkedList<String[]> buy_data = new LinkedList<String[]>();
		
		File isDir = new File("./database");
		if(!isDir.exists())
			isDir.mkdirs();
        File f = new File(SELL_PATH);
        
        try
        {
            if(!f.exists())
            	f.createNewFile();
            
            BufferedReader buf = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"));
            String s;
            while((s = buf.readLine()) != null)
            	buy_data.add(s.split("\\|"));
            
        }catch(Exception e) {e.printStackTrace();}
        ListIterator<String[]> L_buy = buy_data.listIterator();
        while(L_buy.hasNext())
        {
        	String[] tmp = L_buy.next();
        	sell += Integer.parseInt(tmp[1]) * Integer.parseInt(tmp[2]);
        }
        
        System.out.println(sell);
        while(L_buy.hasPrevious())
        	L_buy.previous();
	}

	@Override
	public void run() 
	{
		while(true)
		{
			try 
			{
				Thread.sleep(1000);
				Load_BuyData();
				Load_SellData();
				gap = sell - buy;
				l_buy.setText("�� ���űݾ� : " + buy);
				l_sell.setText("�� �Ǹűݾ� : " + sell);
				l_gap.setText("���� : " + gap);
				repaint();
			} catch (InterruptedException e) {e.printStackTrace();}
		}


	}
	
	
	
}
public class Stock_Status extends JFrame
{
	KOSPI K1 = null;
	KOSDAQ K2 = null;
	Stock_Market_Status S1 = null;
	Buying B1 = null;
	Selling S2 = null;
	My_Stock M1 = null;

	public static void main(String[] args) {
		Stock_Status S = new Stock_Status();
		
		S.setTitle("Stock_Status");
		
		S.K1 = new KOSPI();
		Thread t1 = new Thread(S.K1);
		t1.start();
		
		S.K2 = new KOSDAQ();
		Thread t2 = new Thread(S.K2);
		t2.start();

		
		S.S1 = new Stock_Market_Status();
		S.B1 = new Buying();
		S.S2 = new Selling();
		
		S.M1 = new My_Stock();
		Thread t3 = new Thread(S.M1);
		t3.start();
		
		JTabbedPane tab = new JTabbedPane();
		tab.addTab("KOSPI", S.K1);
		tab.addTab("KOSDAQ", S.K2);
		tab.addTab("�ֽ� ��Ȳ", S.S1);
		tab.addTab("�ż�", S.B1);
		tab.addTab("�ŵ�", S.S2);
		tab.addTab("���� �ֽ�", S.M1);
		S.add(tab);
		
		S.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		S.setSize(615, 800);
		S.setVisible(true);
	}
}

