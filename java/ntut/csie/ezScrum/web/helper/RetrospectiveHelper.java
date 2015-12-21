package ntut.csie.ezScrum.web.helper;

import java.sql.SQLException;
import java.util.ArrayList;

import ntut.csie.ezScrum.web.dataInfo.RetrospectiveInfo;
import ntut.csie.ezScrum.web.dataObject.ProjectObject;
import ntut.csie.ezScrum.web.dataObject.RetrospectiveObject;
import ntut.csie.ezScrum.web.dataObject.SprintObject;
import ntut.csie.ezScrum.web.mapper.RetrospectiveMapper;
import ntut.csie.ezScrum.web.support.TranslateSpecialChar;

public class RetrospectiveHelper {
	private RetrospectiveMapper mRetrospectiveMapper;

	public RetrospectiveHelper(ProjectObject project) {
		mRetrospectiveMapper = new RetrospectiveMapper(project);
	}

	public RetrospectiveObject getRetrospective(long retrospectiveId) {
		return mRetrospectiveMapper.getRetrospective(retrospectiveId);
	}

	public long addRetrospective(RetrospectiveInfo retrospectiveInfo) {
		return mRetrospectiveMapper.addRetrospective(retrospectiveInfo);
	}

	public void editRetrospective(RetrospectiveInfo retrospectiveInfo) {
		mRetrospectiveMapper.updateRetrospective(retrospectiveInfo);
	}

	public void deleteRetrospective(long retrospectiveId) {
		mRetrospectiveMapper.deleteRetrospective(retrospectiveId);
	}

	// 前端XML格式定義在: Common.js 之 變數 Retrospective, Parser 為 retReader
	public StringBuilder getXML(String actionType, RetrospectiveObject retrospective) {
		String tag = null;

		if (actionType.equals("add"))
			tag = "AddNew";
		else if (actionType.equals("get"))
			tag = "Edit";
		else if (actionType.equals("edit"))
			tag = "Edit";
		else if (actionType.equals("delete"))
			tag = "Delete";
		else
			tag = "";

		StringBuilder result = new StringBuilder("");
		result.append("<" + tag + "Retrospective><Result>true</Result><Retrospective>");
		result.append("<Id>" + retrospective.getId() + "</Id>");

		// get的順序跟別人不一樣,先 Name再SprintID -> 前端認XML tag所以順序沒關係
		if (actionType.equals("add") || actionType.equals("edit") || actionType.equals("get")) {
			result.append("<Link>" + "/ezScrum/showIssueInformation.do?issueID=" + retrospective.getId() + "</Link>");
			result.append("<SprintID>" + retrospective.getSprintId() + "</SprintID>");
			result.append("<Name>" + TranslateSpecialChar.TranslateXMLChar(retrospective.getName()) + "</Name>");
			result.append("<Type>" + retrospective.getType() + "</Type>");
			result.append("<Description>" + TranslateSpecialChar.TranslateXMLChar(retrospective.getDescription()) + "</Description>");
			result.append("<Status>" + retrospective.getStatus() + "</Status>");
		} else if (actionType.equals("delete")) {
			result.append("<SprintID>" + retrospective.getSprintId() + "</SprintID>");
		}

		result.append("</Retrospective></" + tag + "Retrospective>");

		return result;
	}

	// 前端XML格式定義在: ShowRetrospective.jsp 之 變數 retrospectiveStore
	public StringBuilder getListXML(String sprintIdString) throws SQLException {
		ArrayList<RetrospectiveObject> goods = new ArrayList<RetrospectiveObject>();
		ArrayList<RetrospectiveObject> improvements = new ArrayList<RetrospectiveObject>();
		final String specialSprintId = "ALL"; // 這個特殊的sprintID主要是為了抓出所有 good 和
											  // improve(待改善) 的 retrospective 
		if (sprintIdString != null && sprintIdString.equals(specialSprintId)) {
			goods = mRetrospectiveMapper.getAllGoods();
			improvements = mRetrospectiveMapper.getAllImprovements();
		} else {
			long sprintId = -1;
			try {
				sprintId = Long.parseLong(sprintIdString);
			} catch (Exception e) {
				SprintPlanHelper sprintPlanHelper = new SprintPlanHelper(mRetrospectiveMapper.getProjct());
				// if there is not current sprint, then get latest sprint
				SprintObject sprint = sprintPlanHelper.getCurrentSprint();
				// The project has no any sprint
				if (sprint != null) {
					sprintId = sprint.getId();
				}
			}
			goods = mRetrospectiveMapper.getGoodsInSprint(sprintId);
			improvements = mRetrospectiveMapper.getImprovementsInSprint(sprintId);
		}
		
		StringBuilder result = new StringBuilder();
		result.append("<Retrospectives><Sprint><Id>" + sprintIdString + "</Id><Name>Sprint #" + sprintIdString + "</Name></Sprint>");

		// Good Retrospective 封裝成 XML 給 Ext 使用
		for (RetrospectiveObject good : goods) {
			result.append("<Retrospective>");
			result.append("<Id>" + good.getId() + "</Id>");
			result.append("<Link></Link>");
			result.append("<SprintID>" + good.getSprintId() + "</SprintID>");
			result.append("<Name>" + TranslateSpecialChar.TranslateXMLChar(good.getName()) + "</Name>");
			result.append("<Type>" + good.getType() + "</Type>");
			result.append("<Description>" + TranslateSpecialChar.TranslateXMLChar(good.getDescription()) + "</Description>");
			result.append("<Status>" + good.getStatus() + "</Status>");
			result.append("</Retrospective>");
		}
		
		// Improvement Retrospective 封裝成 XML 給 Ext 使用
		for (RetrospectiveObject improvement : improvements) {
			result.append("<Retrospective>");
			result.append("<Id>" + improvement.getId() + "</Id>");
			result.append("<Link></Link>");
			result.append("<SprintID>" + improvement.getSprintId() + "</SprintID>");
			result.append("<Name>" + TranslateSpecialChar.TranslateXMLChar(improvement.getName()) + "</Name>");
			result.append("<Type>" + improvement.getType() + "</Type>");
			result.append("<Description>" + TranslateSpecialChar.TranslateXMLChar(improvement.getDescription()) + "</Description>");
			result.append("<Status>" + improvement.getStatus() + "</Status>");
			result.append("</Retrospective>");
		}
		result.append("</Retrospectives>");
		return result;
	}

}
