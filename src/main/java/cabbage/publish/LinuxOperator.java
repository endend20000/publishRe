package cabbage.publish;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.matches.EofMatch;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import expect4j.matches.TimeoutMatch;

public class LinuxOperator {

	private static Logger logger = Logger.getLogger(LinuxOperator.class);

	private Session session;
	private ChannelShell channelShell;
	private ChannelSftp channelSftp;
	private static Expect4j expect = null;
	private static final long defaultTimeOut = 1000;
	private StringBuffer buffer = new StringBuffer();

	public static final int COMMAND_EXECUTION_SUCCESS_OPCODE = -2;
	public static final String BACKSLASH_R = "\r";
	public static final String BACKSLASH_N = "\n";
	public static final String COLON_CHAR = ":";
	public static String ENTER_CHARACTER = BACKSLASH_R;
	public static final int SSH_PORT = 22;

	// 正则匹配，用于处理服务器返回的结果
	public static String[] linuxPromptRegEx = new String[] { "~]#", "~#", "#",
			":~#", "/$", ">" };

	public static String[] errorMsg = new String[] { "could not acquire the config lock " };

	// ssh服务器的ip地址
	private String ip;
	// ssh服务器的登入端口
	private int port;
	// ssh服务器的登入用户名
	private String user;
	// ssh服务器的登入密码
	private String password;

	public LinuxOperator(Project project) throws Exception {
		this.ip = project.getIp();
		this.port = project.getPort();
		this.user = project.getUser();
		this.password = project.getPassword();
		getExpectAndSftpChannel();
	}

	/**
	 * 关闭SSH远程连接
	 */
	public void disconnect() {
		if (channelShell != null) {
			channelShell.disconnect();
		}
		if (channelSftp != null) {
			channelSftp.disconnect();
		}
		if (session != null) {
			session.disconnect();
		}
	}

	/**
	 * 获取服务器返回的信息
	 * 
	 * @return 服务端的执行结果
	 */
	public String getResponse() {
		return buffer.toString();
	}

	// 获得Expect4j对象，该对用可以往SSH发送命令请求
	private void getExpectAndSftpChannel() {
		try {
			Windows.printLog(String.format("开始登陆linux %s@%s:%s", user,ip, port));
			JSch jsch = new JSch();
			session = jsch.getSession(user, ip, port);
			session.setPassword(password);
			Hashtable<String, String> config = new Hashtable<String, String>();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			localUserInfo ui = new localUserInfo();
			session.setUserInfo(ui);
			session.connect();
			channelShell = (ChannelShell) session.openChannel("shell");
			expect = new Expect4j(channelShell.getInputStream(),
					channelShell.getOutputStream());
			channelShell.connect();
			channelSftp=(ChannelSftp)session.openChannel("sftp");
			channelSftp.connect();
			Windows.printLog(String.format("登陆 %s@%s:%s 成功!", user, ip, port));
		} catch (Exception ex) {
			Windows.printLog("登陆 " + ip + ":" + port+ "失败，请检查用户名和密码!");
			logger.error(ExceptionUtils.getStackTrace(ex));
		}		
	}
	
	public void uploadFile(InputStream src, String dst) throws Exception{
		channelSftp.put(src, dst);
	}
	
	/**
	 * 执行配置命令
	 * 
	 * @param commands
	 *            要执行的命令，为字符数组
	 * @return 执行是否成功
	 */
	public boolean executeCommands(String[] commands) {
		// 如果expect返回为0，说明登入没有成功
		if (expect == null) {
			return false;
		}

		Windows.printLog("----------以下命令将被执行----------");
		for (String command : commands) {
			Windows.printLog(command);
		}
		Windows.printLog("--------------------");

		Closure closure = new Closure() {
			public void run(ExpectState expectState) throws Exception {
				//打印linux输出
				Windows.printLog(expectState.getBuffer());
				
				buffer.append(expectState.getBuffer());
				expectState.exp_continue();

			}
		};
		List<Match> lstPattern = new ArrayList<Match>();
		String[] regEx = linuxPromptRegEx;
		if (regEx != null && regEx.length > 0) {
			synchronized (regEx) {
				for (String regexElement : regEx) {
					try {
						RegExpMatch mat = new RegExpMatch(regexElement, closure);
						lstPattern.add(mat);
					} catch (MalformedPatternException e) {
						Windows.printLog("命令格式有误");
						return false;
					} catch (Exception e) {
						Windows.printLog("命令执行异常");
						return false;
					}
				}
				lstPattern.add(new EofMatch(new Closure() {
							public void run(ExpectState state) {
							}
						}));
				lstPattern.add(new TimeoutMatch(defaultTimeOut, new Closure() {
					public void run(ExpectState state) {
					}
				}));
			}
		}
		try {
			boolean isSuccess = true;
			for (String strCmd : commands) {
				isSuccess = isSuccess(lstPattern, strCmd);
			}
			// 防止最后一个命令执行不了
			isSuccess = !checkResult(expect.expect(lstPattern));

			// 找不到错误信息标示成功
			String response = buffer.toString().toLowerCase();
			//System.out.println(buffer);
			for (String msg : errorMsg) {
				if (response.indexOf(msg) > -1) {
					return false;
				}
			}

			return isSuccess;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	// 检查执行是否成功
	private boolean isSuccess(List<Match> objPattern, String strCommandPattern) {
		try {
			boolean isFailed = checkResult(expect.expect(objPattern));
			if (!isFailed) {
				expect.send(strCommandPattern);
				expect.send("\r");
				return true;
			}
			return false;
		} catch (MalformedPatternException ex) {
			return false;
		} catch (Exception ex) {
			return false;
		}
	}

	// 检查执行返回的状态
	private boolean checkResult(int intRetVal) {
		if (intRetVal == COMMAND_EXECUTION_SUCCESS_OPCODE) {
			return true;
		}
		return false;
	}

	// 登入SSH时的控制信息
	// 设置不提示输入密码、不显示登入信息等
	public static class localUserInfo implements UserInfo {
		String passwd;

		public String getPassword() {
			return passwd;
		}

		public boolean promptYesNo(String str) {
			return true;
		}

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public boolean promptPassword(String message) {
			return true;
		}

		public void showMessage(String message) {

		}
	}

}