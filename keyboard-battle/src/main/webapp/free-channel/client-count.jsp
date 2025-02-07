<%@page import="socket.FreeChat"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
int clientCount = FreeChat.getNumberOfClients();
response.getWriter().print(clientCount);
%>
