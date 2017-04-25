<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>


<div class="box">

	<form method="POST" id="uploadUpdateForm" enctype="multipart/form-data">

		Option to Import: <select id="optionImport" name="optionImport">
			<option value="">--- Select ---</option>
			<c:forEach var="option" items="${optionsToImport}"
				varStatus="varStatus">
				<option value="${option}">
					<spring:message code="${option.description}" />
				</option>
			</c:forEach>
		</select> <input type="file" name="xlsFile" size="40" /> <input type="hidden"
			name="action" value="upload" /> <input type="hidden" name="update"
			value="true" /> <input type="submit" value="Carregar" />
	</form>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>