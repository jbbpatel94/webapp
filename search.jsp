<%-- 
    Document   : search
    Created on : Jan 30, 2017, 8:17:09 PM
    Author     : JP
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" import="java.sql.*"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Patient Page</title>
    </head>
    <body>
       <%
       String s=request.getParameter("v");
        if(s==null){
            response.sendRedirect("index.jsp");
        }
        else{
       try{  
           Class.forName("com.mysql.jdbc.Driver");
           Connection con = DriverManager.getConnection("jdbc:mysql://localhost/webapp","root",""); 
           Statement st= con.createStatement();
           ResultSet rs =st.executeQuery("select * from patient where first_name LIKE'%"+s+"%'");
            %>
            <table width="100%">
             <tr>
               <th>S.No.</th>
               <th>First Name</th>
               <th>Last Name</th>
               <th>Age</th>
               <th>Date Of Birth</th>
               <th>Gender</th>
               <th>Mobile No.</th>
               <th>Patient Info</th>
           </tr>
            <%
            while(rs.next()){
                %>
              <tr>
               <td align="center"> <%= rs.getInt(1) %> </td>
               <td align="center"> <%= rs.getString(2) %> </td>
               <td align="center"> <%= rs.getString(3) %> </td>
               <td align="center"> <%= rs.getString(4) %> </td>
               <td align="center"> <%= rs.getString(5) %> </td>
               <td align="center"> <%= rs.getString(6) %> </td>
               <td align="center"> <%= rs.getString(7) %> </td>
               <td align="center"> <%= rs.getString(8) %> </td>
           </tr>      
               
               <% 
            }
            %>
            
             </table>
            <%
            con.close();
}
catch(Exception w){
    out.println(w.getMessage()); 
}
}
       %>
    </body>
</html>