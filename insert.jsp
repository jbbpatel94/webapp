<%-- 
    Document   : insert
    Created on : Jan 30, 2017, 8:16:06 PM
    Author     : JP
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" import="java.sql.*"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
       <%
       try{
            Class.forName("com.mysql.jdbc.Driver"); 
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/webapp","root",""); 
            int n=0;
               PreparedStatement st1=con.prepareStatement("select MAX(sn) from patient");
                ResultSet rs=st1.executeQuery();  
       
                while(rs.next()){
                    n=rs.getInt(1);
                }
                n=n+1;
            PreparedStatement preparedStmt = con.prepareStatement("insert into patient values(?,?, ?, ?, ?, ?, ?, ?)"); 
            
            preparedStmt.setInt(1, n);
            preparedStmt.setString (2, request.getParameter("fname"));
            preparedStmt.setString (3, request.getParameter("lname"));
            preparedStmt.setString (4, request.getParameter("age"));
            preparedStmt.setString (5, request.getParameter("dob"));
            preparedStmt.setString (6, request.getParameter("gender"));
            preparedStmt.setString (7, request.getParameter("mobile"));
            preparedStmt.setString (8, request.getParameter("pinfo"));
            preparedStmt.execute();
            con.close();
            response.sendRedirect("index.jsp");
}
catch(Exception w){
    out.println(w.getMessage()); 
}
       %>