/*
TODO
*/
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
public class SigmaMemo extends JFrame implements ActionListener{

	Vector<Info> infos = new Vector<Info>();

	JMenuBar menubar;
	JMenu file;
	JMenuItem save;
	DetailPanel detailpanel= new DetailPanel();
	Object[][] tableinfo;
	JTable table;
	void getinfos(){
		String dir=".\\Problems";
		String[] folders=( new File(dir) ).list();
		for(int i=0;i<folders.length;i++){
			if(! (new File(dir+"\\"+folders[i])).isDirectory() ) continue;
			File file= new File(dir+"\\"+folders[i]+"\\info.txt");
			infos.add(new Info(file));
			System.out.println("load:   "+file);
		}
	}
	void assignTableInfo(int row,Info info){
		tableinfo[row][0]=info.problemname;
		tableinfo[row][1]=info.difficulity;
		tableinfo[row][2]=(info.used ? "used" : "yet");
	}
	void refreshTableRow(int modelrow,int tablerow){
		table.setValueAt(tableinfo[modelrow][0],tablerow,0);
		table.setValueAt(tableinfo[modelrow][1],tablerow,1);
		table.setValueAt(tableinfo[modelrow][2],tablerow,2);
	}
	public SigmaMemo(){
		super("title");
//		setResizable(false);		forbid to resize
		setLayout(new FlowLayout(FlowLayout.LEFT));
		menubar=new JMenuBar();
		setJMenuBar(menubar);
		file=new JMenu("file");
		menubar.add(file);
		save=new JMenuItem("save");
		save.addActionListener(this);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK));
		file.add(save);

		getinfos();
		setSize(1000,500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		int N=infos.size();
		tableinfo = new Object[N][3];		//name,diff,used
		String[] colnames={"problemname","difficulity","used"};
		int[] colwidth={200,70,50};
		for(int i=0;i<N;i++){
			assignTableInfo(i,infos.get(i));
		}
		TableModel model = new DefaultTableModel(tableinfo,colnames){
			public Class getColumnClass(int col){
				if(col==1) return Integer.class;
				else return String.class;
			}
		};
		TableRowSorter<TableModel> sorter=new TableRowSorter<TableModel>(model);
		table = new JTable(model){
			@Override public boolean isCellEditable(int row,int column){
				return false;
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		table.setAutoCreateRowSorter(true);
		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override public void valueChanged(ListSelectionEvent e){
				if(e.getValueIsAdjusting()) return;
				int selectedrows[] = table.getSelectedRows();
				assert(selectedrows.length==1);
				int selectedrow=table.convertRowIndexToModel(selectedrows[0]);
//				System.out.println("selectedrow="+selectedrow);
				detailpanel.Displayinfo(infos.get(selectedrow));
			}
		});
		for(int i=0;i<colnames.length;i++){
			table.getColumn(colnames[i]).setPreferredWidth(colwidth[i]);
		}

		JScrollPane tablepanel= new JScrollPane(table);
		tablepanel.setPreferredSize(new Dimension(320+20,500));

		JSplitPane splitpane = new JSplitPane();
		splitpane.setLeftComponent(tablepanel);
		splitpane.setRightComponent(detailpanel);
		add(splitpane);
		pack();
	}
	public void actionPerformed(ActionEvent event){
		if(event.getSource().equals(save)){
			try{
				int selectedrows[]=table.getSelectedRows();
				if(selectedrows.length==0) return;
				int tablerow=selectedrows[0];
				int row=table.convertRowIndexToModel(tablerow);

				//write to file
				File file = new File(".\\Problems\\"+infos.get(row).problemname+"\\info.txt");
				FileWriter wr= new FileWriter(file);
				wr.write("problemname="+detailpanel.problemname.getText()+"\n");
				wr.write("statement={\n"+detailpanel.statement.getText()+"\n}\n");
				wr.write("difficulity="+detailpanel.difficulity.getText()+"\n");
				wr.write("used="+(detailpanel.used.isSelected()?"1":"0")+"\n");
				wr.write("comment={\n"+detailpanel.comment.getText()+"\n}");
				wr.close();
				//refresh detailpanel
				Info newinfo=new Info(file);
				infos.set(row,newinfo);
				//refresh tableinfo
				assignTableInfo(row,newinfo);
				//refresh table
				refreshTableRow(row,tablerow);
			}catch(IOException e){
				System.out.println(e);
			}
		}
	}
	public static void main(String args[]){
		new SigmaMemo().setVisible(true);
	}
}
class DetailPanel extends JPanel{
	String convecstr(Vector<String> vs){
		int N=vs.size();
		String ret = new String();
		for(int i=0;i<N;i++){
			ret+=vs.get(i);
			if(i<N-1) ret+="\n";
		}
		return ret;
	}

	JTextField problemname;
	JTextArea statement;
	JTextField difficulity;
	JCheckBox used;
	JTextArea comment;

	DetailPanel(){
		setPreferredSize(new Dimension(500,500));
		setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel label0=new JLabel("problemname:");
		JLabel label1=new JLabel("statement:");
		JLabel label2=new JLabel("difficulity:");
		JLabel label3=new JLabel("used:");
		JLabel label4=new JLabel("comment:");
/*		problemname=new JTextField(info.problemname,20);
		statement=new JTextArea(convecstr(info.statement),10,40);
		difficulity=new JTextField(Integer.toString(info.difficulity),4);
		used=new JCheckBox("",info.used);
		comment=new JTextArea(convecstr(info.comment),10,40);*/
		problemname=new JTextField(20);
		statement=new JTextArea(10,40);
		difficulity=new JTextField(4);
		used=new JCheckBox("");
		comment=new JTextArea(10,40);
		add(label0);
		add(problemname);
		add(label1);
		add(statement);
		add(label2);
		add(difficulity);
		add(label3);
		add(used);
		add(label4);
		add(comment);
//		pack();
	}
	void Displayinfo(Info info){
		problemname.setText(info.problemname);
		statement.setText(convecstr(info.statement));
		difficulity.setText(Integer.toString(info.difficulity));
		used.setSelected(info.used);
		comment.setText(convecstr(info.comment));
	}
}

class Info{
	boolean ispfx(String s,String p){
		if(s.length()<p.length()) return false;
		if(p.equals(s.substring(0,p.length()))) return true;
		return false;
	}
	String problemname;
	int difficulity;
	boolean used;
	Vector<String> statement = new Vector<String>();
	Vector<String> comment = new Vector<String>();
	Info(File file){
		try{
//			File file = new File(".\\Problems\\"+Integer.toString(id)+"\\info.txt");
//			File file = new File(".\\Problems\\"+foldername+"\\info.txt");
//			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"SJIS"));
			//UTF-8
			String st;
			while((st=br.readLine())!=null){
				if(ispfx(st,"problemname")){
					problemname=st.substring(12);
				}
				if(ispfx(st,"difficulity")){
					difficulity=Integer.valueOf(st.substring(12));
				}
				if(ispfx(st,"used")){
					used= ( Integer.valueOf(st.substring(5)) == 1 );
				}
				if(ispfx(st,"statement")){
					while( ! (st=br.readLine()).equals("}") ){
						statement.add(st);
					}
				}
				if(ispfx(st,"comment")){
					while( ! (st=br.readLine()).equals("}") ){
						comment.add(st);
					}
				}
			}
			br.close();
		}catch(FileNotFoundException e){
			System.out.println("No such file.   "+file+"\n");
		}catch(IOException e){
			System.out.println(e);
		}
	}
	/*
	public class comp_problemname implements Comparator<Info> {
		public int compare(Info a,Info b){
			return (a.problemname).compareTo(b.problemname);
		}
	}
	public class comp_difficulity implements Comparator<Info> {
		public int compare(Info a,Info b){
			return Integer.valueOf(a.difficulity).compareTo(b.difficulity);
		}
	}
	public class comp_used implements Comparator<Info> {
		public int compare(Info a,Info b){
			return Boolean.valueOf(a.used).compareTo(b.used);
		}
	}
	*/
}