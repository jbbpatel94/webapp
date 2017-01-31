<%-- 
    Document   : index.jsp
    Created on : Jan 30, 2017, 6:45:31 PM
    Author     : JP
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" import="java.sql.*"%>
<!DOCTYPE html>
<html>
   <head>
      <title>Patient Information</title>
      <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
      <script type="text/javascript">
        $(document).ready(function(){
                $("#search").keyup(function(){
                      var v= $(this).val();
                   //  alert(v);
                     $.post("search.jsp",{v:v},function(data){
                         $("#show").html(data);
                         
                     });
                });
            });
            </script>
      <script type="text/javascript">
    function validate(){
         
         if( document.myForm.fname.value == "" )
         {
            alert( "Please provide your FirstName!" );
            document.myForm.fname.focus() ;
            return false;
         }
         
         if( document.myForm.lname.value == "" )
         {
            alert( "Please provide your LastName!" );
            document.myForm.lname.focus() ;
            return false;
         }
         if (isNaN(document.myForm.age.value) || document.myForm.age.value < 1 || document.myForm.age.value > 75)
         {
            alert( "The age must be a number between 1 and 75");
            return false;
         }
         
         if( document.myForm.dob.value == "" )
         {
            alert( "Please provide your Date Of Birth!" );
            return false;
         }
         
         if( document.myForm.gender.value == "-1" )
         {
            alert( "Please provide your gender!" );
            return false;
         }
           if( document.myForm.mobile.value.length<10)
         {
            alert( "Mobile No must be 10 digit!" );
            return false;
         }
         if( document.myForm.pinfo.value == "" )
         {
            alert( "Please provide Patient information!" );
            return false;
         }
         return( true );
      }
      </script>
      
   </head>
   
   <body>
   <h4 style="color:red">Patient Entry </h4>
   <form method="post" action="insert.jsp" name="myForm" onsubmit="return(validate());">
         <table cellspacing="2" cellpadding="2" >
            
            <tr>
               <td align="right">First Name</td>
               <td><input type="text" name="fname" placeholder="Enter First Name" size="30" /></td>
            </tr>
            
            <tr>
               <td align="right">Last Name</td>
               <td><input type="text" name="lname" size="30"  placeholder="Enter Last Name"/></td>
            </tr>
            
            <tr>
               <td align="right">Age</td>
               <td><input type="text" name="age" size="30" placeholder="Enter Patient Age" /></td>
            </tr>
             <tr>
               <td align="right">Date Of Birth</td>
               <td><input type="date" name="dob" /></td>
            </tr>
            
            <tr>
               <td align="right" >Gender</td>
               <td>
                  <select name="gender" >
                     <option value="-1" selected>choose</option>
                     <option  size="30">Male</option>
                     <option >Female</option>
                  </select>
               </td>
            </tr>
            <tr>
               <td align="right">Mobile No</td>
               <td><input type="text" name="mobile" size="30" placeholder="Enter Patient Mobile No"/></td>
            </tr>
            <tr>
               <td align="right">Patient Info</td>
               <td><textarea name="pinfo" placeholder="Enter patient information" rows="5" cols="33"></textarea></td>
            </tr>
            
            <tr>
               <td align="right"></td>
               <td><input type="submit" value="Submit" /></td>
            </tr>
            
         </table>
      </form>

   <h4 style="color:red">Patient Directory</h4>
   
       <table cellspacing="2" cellpadding="2">
           <tr>
               <td align="right">Search</td>
               <td align="left" style=" padding-left:1px"><input id="search" type="text" name="search" class="form-control" placeholder="Search by First name" size="30"></td>

           </tr>
       </table>
   <br/>
   <div id="show">
   <%
       try{
            Class.forName("com.mysql.jdbc.Driver"); 
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/webapp","root",""); 
             PreparedStatement st1=con.prepareStatement("select * from patient order  by  sn desc ");
                ResultSet rs=st1.executeQuery();
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
       
       %>
   </div>
   </body>
</html>
