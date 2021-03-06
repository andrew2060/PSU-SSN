<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="records" scope="request" type="java.util.Collection<edu.pdx.ssn.application.Record>"/>
<p>Records with loans ending in less than a week</p>
<table>
  <tr>
    <td>ISBN</td>
    <td>Title</td>
    <td>Due Date</td>
    <td>Account</td>
  </tr>
  <c:forEach var="record" items="${records}">
    <tr>
      <td>${record.book.ISBN}</td>
      <td>${record.book.title}</td>
      <td>${record.dueDate}</td>
      <td>${record.checkoutMember.email}</td>
    </tr>
  </c:forEach>
</table>