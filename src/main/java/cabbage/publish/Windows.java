package cabbage.publish;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

public class Windows  extends JFrame implements MouseListener{

	private static final long serialVersionUID = 1L;

	private static JTextArea logTextArea;
	private JPanel buttonPanel;
	private JScrollPane scrollPane;
	
	public static Project clickedProject;
	private List<Project> projects;
	
	private static Logger logger = Logger.getLogger(Windows.class);
	
	public static void main(String[] args) {
		try {				
			@SuppressWarnings("unused")
			Windows windows=new Windows();
		} catch (Exception ex) {
			logger.error(ExceptionUtils.getStackTrace(ex));
		}
	}
	
	public Windows() throws Exception{		
		generateTextArea();	
		generateButtons();
		setParamerer();
	}

	private void generateButtons() throws Exception{
		buttonPanel=new JPanel(new FlowLayout());
		projects = XMLReader.getProjects();		
		for(Project project:projects){
				JButton button=new JButton(project.getName());
				button.addMouseListener(this);		
				buttonPanel.add(button);
				project.setButton(button);	
			}
		this.add(buttonPanel,BorderLayout.NORTH);	
	}
	
	private void generateTextArea() throws Exception{
		scrollPane=new JScrollPane();
		logTextArea=new JTextArea();		
		scrollPane.setViewportView(logTextArea);		
		this.add(scrollPane,BorderLayout.CENTER);		
	}
	
	private void setParamerer() {
		this.setTitle("发布小工具");
		this.setSize(1000, 800);
		this.setLocation(500, 200);
		this.setVisible(true);	
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		try {
			for (Project project : projects) {
				if (e.getSource() == project.getButton()) {					
					printLog("开始部署"+project.getName());
					clickedProject = project;
					
					String tempFileName=project.getTemp()+"/"+project.getName();
					
					File temp=new File(project.getTemp());
					if(!temp.isDirectory()){
						temp.mkdir();
					}
					
					Compress.generateTarGzFile(project.getSource(),tempFileName);					
					printLog("压缩完成");

					File srcFile = new File(tempFileName+Variable.TAR_GZ);

					LinuxOperator shell = new LinuxOperator(project);
					shell.uploadFile(new FileInputStream(srcFile), project.getName()+Variable.TAR_GZ);
					
					printLog("上传完成");
					
					shell.executeCommands(project.getShells().split(","));					
					printLog("命令执行完成");
				}
			}
		} catch (Exception ex) {
			logger.error(ExceptionUtils.getStackTrace(ex));
		}
	}

	public static void printLog(String logContent){
		logTextArea.append(logContent+Variable.LINE_FEED);		
		logTextArea.paintImmediately(logTextArea.getBounds());
	}
	

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {

	}
	

}
