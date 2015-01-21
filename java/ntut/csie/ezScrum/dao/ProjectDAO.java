package ntut.csie.ezScrum.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ntut.csie.ezScrum.issue.sql.service.core.IQueryValueSet;
import ntut.csie.ezScrum.issue.sql.service.internal.MySQLQuerySet;
import ntut.csie.ezScrum.web.dataObject.ProjectObject;
import ntut.csie.ezScrum.web.databasEnum.ProjectEnum;

public class ProjectDAO extends AbstractDAO<ProjectObject, ProjectObject> {

	private static ProjectDAO sInstance = null;

	public static ProjectDAO getInstance() {
		if (sInstance == null) {
			sInstance = new ProjectDAO();
		}
		return sInstance;
	}
	
	@Override
    public long create(ProjectObject project) {
		long createTime = System.currentTimeMillis();
		
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(ProjectEnum.TABLE_NAME);
		valueSet.addInsertValue(ProjectEnum.NAME, project.getName());
		valueSet.addInsertValue(ProjectEnum.DISPLAY_NAME, project.getDisplayName());
		valueSet.addInsertValue(ProjectEnum.COMMENT, project.getComment());
		valueSet.addInsertValue(ProjectEnum.PRODUCT_OWNER, project.getManager());
		valueSet.addInsertValue(ProjectEnum.ATTATCH_MAX_SIZE, project.getAttachFileSize());
		valueSet.addInsertValue(ProjectEnum.CREATE_TIME, createTime);
		valueSet.addInsertValue(ProjectEnum.UPDATE_TIME, createTime);
		String query = valueSet.getInsertQuery();
		return mControl.executeInsert(query);
    }

	@Override
    public ProjectObject get(long id) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(ProjectEnum.TABLE_NAME);
		valueSet.addEqualCondition(ProjectEnum.ID, id);
		String query = valueSet.getSelectQuery();
		
		ResultSet result = mControl.executeQuery(query);

		ProjectObject project = null;
		try {
			if (result.next()) {
				project = convert(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return project;
    }

	@Override
    public boolean update(ProjectObject project) {
		long currentTime = System.currentTimeMillis();
		
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(ProjectEnum.TABLE_NAME);
		valueSet.addInsertValue(ProjectEnum.DISPLAY_NAME, project.getDisplayName());
		valueSet.addInsertValue(ProjectEnum.COMMENT, project.getComment());
		valueSet.addInsertValue(ProjectEnum.PRODUCT_OWNER, project.getManager());
		valueSet.addInsertValue(ProjectEnum.ATTATCH_MAX_SIZE, project.getAttachFileSize());
		valueSet.addInsertValue(ProjectEnum.UPDATE_TIME, currentTime);
		valueSet.addEqualCondition(ProjectEnum.ID, project.getId());
		String query = valueSet.getUpdateQuery();
		
		return mControl.executeUpdate(query);
    }

	@Override
    public boolean delete(long id) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(ProjectEnum.TABLE_NAME);
		valueSet.addEqualCondition(ProjectEnum.ID, id);
		String query = valueSet.getDeleteQuery();
		return mControl.executeUpdate(query);
    }
	
	public ProjectObject getProjectByName(String name) {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(ProjectEnum.TABLE_NAME);
		valueSet.addTextFieldEqualCondition(ProjectEnum.NAME, name);
		String query = valueSet.getSelectQuery();
		
		ResultSet result = mControl.executeQuery(query);

		ProjectObject project = null;
		try {
			if (result.next()) {
				project = convert(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return project;
	}
	
	public ArrayList<ProjectObject> getProjects() {
		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(ProjectEnum.TABLE_NAME);
		String query = valueSet.getSelectQuery();
		
		ResultSet result = mControl.executeQuery(query);
		
		ArrayList<ProjectObject> projects = new ArrayList<ProjectObject>();
		try {
	        while (result.next()) {
	        	projects.add(convert(result));
	        }
        } catch (SQLException e) {
	        e.printStackTrace();
        }
		return projects;
	}

	private ProjectObject convert(ResultSet result) throws SQLException {
		ProjectObject project = new ProjectObject(result.getLong(ProjectEnum.ID),
				result.getString(ProjectEnum.NAME));
		project
			.setDisplayName(result.getString(ProjectEnum.DISPLAY_NAME))
			.setComment(result.getString(ProjectEnum.COMMENT))
			.setManager(result.getString(ProjectEnum.PRODUCT_OWNER))
			.setAttachFileSize(result.getLong(ProjectEnum.ATTATCH_MAX_SIZE))
			.setCreateTime(result.getLong(ProjectEnum.CREATE_TIME))
			.setUpdateTime(result.getLong(ProjectEnum.UPDATE_TIME));
		return project;
	}
}
