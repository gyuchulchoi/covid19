<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<title>Home</title>
</head>
<body>
    <h1>Hello world!</h1>
 
    <table>
        <thead>
            <tr>
                <th>UPDATE_TIME</th>
                <th>Nation</th>
                <th>TotalCase</th>
                <th>NewCase</th>
                <th>TOTAL_DEATH</th>
                <th>NEW_DEATH</th>
                <th>TOTAL_RECOVERED</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${worldList}" var="world">
                <tr>
                    <td>${world.update_time}</td>
                    <td>${world.nation_name}</td>
                    <td>${world.total_case}</td>
                    <td>${world.new_case}</td>
                    <td>${world.total_death}</td>
                    <td>${world.new_death}</td>
                    <td>${world.total_recovered}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
 
 
</body>
</html>
