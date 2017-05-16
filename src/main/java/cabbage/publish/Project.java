package cabbage.publish;

import javax.swing.JButton;
import javax.xml.bind.annotation.XmlTransient;

public class Project {
	private String name;
	private String ip;
	private int port=22;
	private String user;
	private String password;
	private String source;
	private String temp;
	private String target;
	private String shells;
	
	private JButton button;
	
	@XmlTransient
	public JButton getButton() {
		return button;
	}
	public void setButton(JButton button) {
		this.button = button;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public String getShells() {
		return shells;
	}

	public void setShells(String shells) {
		this.shells = shells;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTemp() {
		return temp;
	}
	public void setTemp(String temp) {
		this.temp = temp;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
}
