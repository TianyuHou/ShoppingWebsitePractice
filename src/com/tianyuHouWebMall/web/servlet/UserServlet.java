package com.tianyuHouWebMall.web.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;

import com.tianyuHouWebMall.domain.User;
import com.tianyuHouWebMall.service.UserService;
import com.tianyuHouWebMall.utils.CommonUtils;
import com.tianyuHouWebMall.utils.MailUtils;

/**
 * Servlet implementation class UserServlet
 */
public class UserServlet extends BaseServlet {

	// �û�ע��
	public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		// ��session�н�userɾ��
		session.removeAttribute("user");

		// ���洢�ڿͻ��˵�cookieɾ����
		Cookie cookie_username = new Cookie("cookie_username", "");
		cookie_username.setMaxAge(0);
		// �����洢�����cookie
		Cookie cookie_password = new Cookie("cookie_password", "");
		cookie_password.setMaxAge(0);

		response.addCookie(cookie_username);
		response.addCookie(cookie_password);

		response.sendRedirect(request.getContextPath() + "/login.jsp");

	}

	// �û���¼
	public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();

		// ���������û���������
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		// ���û��������봫�ݸ�service��
		UserService service = new UserService();
		User user = null;
		try {
			user = service.login(username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// �ж��û��Ƿ��¼�ɹ� user�Ƿ���null
		if (user != null) {
			// ��¼�ɹ�
			// ***************�ж��û��Ƿ�ѡ���Զ���¼*****************
			String autoLogin = request.getParameter("autoLogin");
			if ("autoLogin".equals(autoLogin)) {
				// Ҫ�Զ���¼
				// �����洢�û�����cookie
				Cookie cookie_username = new Cookie("cookie_username", user.getUsername());
				cookie_username.setMaxAge(10 * 60);
				// �����洢�����cookie
				Cookie cookie_password = new Cookie("cookie_password", user.getPassword());
				cookie_password.setMaxAge(10 * 60);

				response.addCookie(cookie_username);
				response.addCookie(cookie_password);

			}

			// ***************************************************
			// ��user����浽session��
			session.setAttribute("user", user);

			// �ض�����ҳ
			response.sendRedirect(request.getContextPath() + "/index.jsp");
		} else {
			request.setAttribute("loginError", "Password is wrong");
			request.getRequestDispatcher("/login.jsp").forward(request, response);
		}
	}

	public void register(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, String[]> properties = request.getParameterMap();
		User user = new User();
		try {
			ConvertUtils.register(new Converter() {
				@Override
				public Object convert(Class clazz, Object value) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date parse = null;
					try {
						parse = format.parse(value.toString());
					} catch (ParseException e) {
						e.printStackTrace();
					}
					return parse;
				}
			}, Date.class);

			BeanUtils.populate(user, properties);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		user.setUid(CommonUtils.getUUID());
		user.setTelephone(null);
		user.setState(0);
		String activeCode = CommonUtils.getUUID();
		user.setCode(activeCode);

		// ��user���ݸ�service��
		UserService service = new UserService();
		boolean isRegisterSuccess = service.regist(user);

		if (isRegisterSuccess) {

			String msg = "Congratulations! you have signed up successfully! Click" + " the link to active your account"
					+ "<a href='http://localhost:8080/ShoppingWebsite/user?method=active&activeCode=" + activeCode
					+ "'>" + "http://localhost:8080/ShoppingWebsite/user?method=active&activeCode=" + activeCode
					+ "</a>";
			try {
				MailUtils.sendMail(user.getEmail(), msg);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}

			response.sendRedirect(request.getContextPath() + "/registerSuccess.jsp");
		} else {
			response.sendRedirect(request.getContextPath() + "/registerFail.jsp");
		}
	}

	public void checkUserName(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String username = request.getParameter("username");
		UserService service = new UserService();
		boolean isExist = service.checkUserName(username);

		String json = "{\"isExist\":" + isExist + "}";

		response.getWriter().write(json);
	}

	public void active(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String activecode = request.getParameter("activeCode");
		UserService service = new UserService();
		try {
			service.active(activecode);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		response.sendRedirect(request.getContextPath() + "/login.jsp");

	}

}
