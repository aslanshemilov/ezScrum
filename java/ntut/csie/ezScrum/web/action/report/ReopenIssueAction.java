package ntut.csie.ezScrum.web.action.report;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntut.csie.ezScrum.web.action.PermissionAction;
import ntut.csie.ezScrum.web.dataObject.ProjectObject;
import ntut.csie.ezScrum.web.dataObject.StoryObject;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.helper.SprintBacklogHelper;
import ntut.csie.ezScrum.web.support.SessionManager;
import ntut.csie.ezScrum.web.support.Translation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public class ReopenIssueAction extends PermissionAction {
	private static Log log = LogFactory.getLog(ReopenIssueAction.class);

	@Override
	public boolean isValidAction() {
		return (super.getScrumRole().getAccessProductBacklog() && (!super
				.getScrumRole().isGuest()));
	}

	@Override
	public boolean isXML() {
		return false;
	}

	@Override
	public StringBuilder getResponse(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		log.info("Reopen Issue in ReopenIssueAction.");

		// get project from session or DB
		ProjectObject project = SessionManager.getProject(request);

		// get parameter info
		long serialNumber = Long.parseLong(request.getParameter("Id"));
		String name = request.getParameter("Name");
		String notes = request.getParameter("Notes");
		String changeDate = request.getParameter("ChangeDate");
		String issueType = request.getParameter("IssueType");

		SprintBacklogHelper sprintBacklogHelper = new SprintBacklogHelper(
				project);
		StringBuilder result = new StringBuilder("");

		if (issueType.equals("Story")) {
			sprintBacklogHelper.reopenStory(serialNumber, name, notes, changeDate);
			// return re open 的 issue的相關資訊
			StoryObject story = sprintBacklogHelper.getStory(serialNumber);
			result.append(Translation.translateTaskboardStoryToJson(story));
		} else if (issueType.equals("Task")) {
			sprintBacklogHelper.reopenTask(project.getId(),serialNumber, name, notes, changeDate);
			TaskObject task = sprintBacklogHelper.getTask(project.getId(), serialNumber);
			result.append(Translation.translateTaskboardTaskToJson(task));
		}

		return result;
	}
}
