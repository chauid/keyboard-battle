<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String roomId = request.getParameter("id");
String inputPassword = request.getParameter("password");

System.out.println("roomId: " + roomId);
System.out.println("inputPassword: " + inputPassword);

%>