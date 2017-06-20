package ntut.csie.ezScrum.web.action.rbac;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScru.web.microservice.CallAccountMicroservice;
import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.dataObject.AccountObject;
import ntut.csie.ezScrum.web.databaseEnum.RoleEnum;
import ntut.csie.ezScrum.web.helper.AccountHelper;
import ntut.csie.ezScrum.web.support.SessionManager;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RemoveUserAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		long accountId, projectId;
		
		try {
			accountId = Long.parseLong(request.getParameter("id"));
			projectId = Long.parseLong(request.getParameter("resource"));
		} catch (NumberFormatException e) {
			if (request.getParameter("resource").equals("system")) {
				accountId = Long.parseLong(request.getParameter("id"));
				projectId = 0;
			} else {
				accountId = 0;
				projectId = -1;
			}
		}
		String role = request.getParameter("operation");
		
		IUserSession session = (IUserSession) request.getSession().getAttribute("UserSession");
		String token = session.getAccount().getToken();
		CallAccountMicroservice accountService = new CallAccountMicroservice(token);
		AccountObject account = null;
		AccountHelper accountHelper = new AccountHelper();
		
		if (accountId > 0 && projectId > -1 && role != null) {
			try {
				if(role.equals("admin")){
					String responseString = accountService.updateAccountSystemRole(accountId, false);
					try {
						JSONObject accountJSON = new JSONObject(responseString);
						account = new AccountObject(Long.valueOf(accountJSON.getString("id")), accountJSON.getString("username"));
						account.setEmail(accountJSON.getString("email"));
						account.setEnable(Boolean.valueOf(accountJSON.getString("enabled")));
						account.setNickName(accountJSON.getString("nickname"));	
						account.setAdmin(Boolean.valueOf(accountJSON.getString("systemrole")));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					account = accountService.getAccountById(accountId);
					account.deleteProjectRole(projectId, RoleEnum.valueOf(role));
				}
//				AccountObject account = accountHelper.removeAssignRole(accountId, projectId, role);
				
				// 刪除 Session 中關於該使用者的所有專案權限。
				SessionManager.removeScrumRolesMap(request, account);
				response.setContentType("text/xml; charset=utf-8");
				response.getWriter().write(accountHelper.getAccountXML(account));				
				response.getWriter().close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
