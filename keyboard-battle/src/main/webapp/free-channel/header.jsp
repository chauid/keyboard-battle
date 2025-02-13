<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="dto.UserDTO"%>
<%
Object sessionUser = session.getAttribute("user");

if (sessionUser == null) {
	response.getWriter().print("null");
} else {
	response.getWriter().print("exist");
}
%>