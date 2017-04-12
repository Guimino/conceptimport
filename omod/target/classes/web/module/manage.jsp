<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

TESTING...
<div class="box">

	<form method="POST" id="uploadUpdateForm" enctype="multipart/form-data">
		<input type="file" name="xlsFile" size="40" /> <input type="hidden"
			name="action" value="upload" /> <input type="hidden" name="update"
			value="true" /> <input type="submit" value="Carregar" />
	</form>
</div>
<%@ include file="/WEB-INF/template/footer.jsp"%>