package ntut.csie.ezScrum.web.action.report;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.issue.core.IIssue;
import ntut.csie.ezScrum.iteration.support.TranslateSpecialChar;
import ntut.csie.ezScrum.pic.core.IUserSession;
import ntut.csie.ezScrum.web.action.PermissionAction;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.helper.ProductBacklogHelper;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.support.SessionManager;
import ntut.csie.jcis.resource.core.IProject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class ShowCheckOutIssueAction extends PermissionAction {
	private static Log log = LogFactory.getLog(ShowCheckOutIssueAction.class);

	@Override
	public boolean isValidAction() {
		return super.getScrumRole().getAccessSprintBacklog();
	}

	@Override
	public boolean isXML() {
		// html
		return false;
	}

	@Override
	public StringBuilder getResponse(ActionMapping mapping, ActionForm form,
	        HttpServletRequest request, HttpServletResponse response) {

		// get project from session or DB
		IProject project = (IProject) SessionManager.getProject(request);
		IUserSession session = (IUserSession) request.getSession().getAttribute("UserSession");
		StringBuilder result = new StringBuilder("");
		ProductBacklogHelper PBHelper = new ProductBacklogHelper(session, project);
		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(project, session);

		String defaultHandler = session.getAccount().getUsername();
		try {
			long issueId = Long.parseLong(request.getParameter("issueID"));
			String issueType = request.getParameter("issueType");
			if (issueType.equalsIgnoreCase("Story")) {
				IIssue item = PBHelper.getIssue(issueId);
				if (item != null) {
					result.append(getJsonString(item, defaultHandler));
				} else {
					result.append(getJsonString(null, defaultHandler));
				}				
			} else if (issueType.equalsIgnoreCase("Task")) {
				TaskObject task = sprintBacklogHelper.getTask(issueId);
				result.append(getJsonString(task));
			}
		} catch (Exception e) {
			result.append(getJsonString(null, defaultHandler));
			log.debug("class : ShowCheckOutTaskAction, method : execute, exception : " + e.toString());
		}
		return result;
	}

	private StringBuilder getJsonString(IIssue issue, String handler) {
		StringBuilder result = new StringBuilder();
		TranslateSpecialChar translate = new TranslateSpecialChar();
		if (issue != null) {
			result.append("{\"Task\":{")
			        .append("\"Id\":\"").append(issue.getIssueID()).append("\",")
			        .append("\"Name\":\"").append(translate.TranslateJSONChar(issue.getSummary())).append("\",")
			        .append("\"Partners\":\"").append(issue.getPartners()).append("\",")
			        .append("\"Notes\":\"").append(translate.TranslateJSONChar(issue.getNotes())).append("\",")
			        .append("\"Handler\":\"").append(handler).append("\"")
			        .append("},")
			        .append("\"success\":true,")
			        .append("\"Total\":1")
			        .append("}");
		} else {
			result.append("");
		}
		return result;
	}
	
	private StringBuilder getJsonString(TaskObject task) {
		String handlerUsername = task.getHandler() != null ? task.getHandler().getUsername() : "";
		
		StringBuilder result = new StringBuilder();
		TranslateSpecialChar translate = new TranslateSpecialChar();
		if (task != null) {
			result.append("{\"Task\":{")
			        .append("\"Id\":\"").append(task.getId()).append("\",")
			        .append("\"Name\":\"").append(translate.TranslateJSONChar(task.getName())).append("\",")
			        .append("\"Partners\":\"").append(task.getPartners()).append("\",")
			        .append("\"Notes\":\"").append(translate.TranslateJSONChar(task.getNotes())).append("\",")
			        .append("\"Handler\":\"").append(handlerUsername).append("\"")
			        .append("},")
			        .append("\"success\":true,")
			        .append("\"Total\":1")
			        .append("}");
		} else {
			result.append("");
		}
		return result;
	}
}