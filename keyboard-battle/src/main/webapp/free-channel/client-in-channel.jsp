<%@page import="java.util.Set"%>
<%@page import="listener.SessionTracker"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
Set<HttpSession> currentSession = SessionTracker.getActiveSessions();
int userCount = 0;

for (HttpSession s : currentSession) {
	Object sessionName = s.getAttribute("user");
	if (sessionName != null) {
		userCount++;
	}
}
response.getWriter().print(userCount);
%>
