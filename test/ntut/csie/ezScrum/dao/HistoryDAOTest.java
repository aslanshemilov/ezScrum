package ntut.csie.ezScrum.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import junit.framework.TestCase;
import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.issue.sql.service.core.IQueryValueSet;
import ntut.csie.ezScrum.issue.sql.service.internal.MySQLQuerySet;
import ntut.csie.ezScrum.issue.sql.service.tool.internal.MySQLControl;
import ntut.csie.ezScrum.test.CreateData.CopyProject;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.ezScrum.web.dataObject.HistoryObject;
import ntut.csie.ezScrum.web.databasEnum.HistoryEnum;
import ntut.csie.ezScrum.web.databasEnum.IssueTypeEnum;

public class HistoryDAOTest extends TestCase {
	private Configuration config;
	private CreateProject CP;
	private int ProjectCount = 1;
	private HistoryDAO historyDao = null;
	private MySQLControl control = null;

	public HistoryDAOTest(String testMethod) {
		super(testMethod);
	}

	protected void setUp() throws Exception {
		config = new Configuration();
		config.setTestMode(true);
		config.store();

		InitialSQL ini = new InitialSQL(config);
		ini.exe();

		CP = new CreateProject(this.ProjectCount);
		CP.exeCreate();

		historyDao = HistoryDAO.getInstance();
		control = new MySQLControl(config);
		control.connection();

		super.setUp();
	}

	protected void tearDown() throws Exception {
		InitialSQL ini = new InitialSQL(config);
		ini.exe(); // 初始化 SQL

		CopyProject copyProject = new CopyProject(this.CP);
		copyProject.exeDelete_Project(); // 刪除測試檔案

		config.setTestMode(false);
		config.store();

		// ============= release ==============
		ini = null;
		copyProject = null;
		CP = null;
		config = null;
		historyDao = null;
		control = null;

		super.tearDown();
	}

	public void testAdd() {
		String oldValue = "1";
		String newValue = "0";
		long modifiedTime = System.currentTimeMillis();

		HistoryObject history = new HistoryObject();
		history.setHistoryType(HistoryObject.TYPE_IMPORTANCE)
				.setNewValue(oldValue).setOldValue(newValue)
				.setModifiedTime(modifiedTime)
				.setIssueType(IssueTypeEnum.TYPE_TASK).setIssueId(1);

		long id = historyDao.add(history);

		IQueryValueSet valueSet = new MySQLQuerySet();
		valueSet.addTableName(HistoryEnum.TABLE_NAME);
		valueSet.addEqualCondition(HistoryEnum.ISSUE_TYPE,
				IssueTypeEnum.TYPE_TASK);
		valueSet.addEqualCondition(HistoryEnum.ISSUE_ID, 1);
		String query = valueSet.getSelectQuery();

		ResultSet result = control.executeQuery(query);
		try {
			if (result.next()) {
				assertEquals(history.getHistoryType(),
						result.getInt(HistoryEnum.HISTORY_TYPE));
				assertEquals(history.getIssueType(),
						result.getInt(HistoryEnum.ISSUE_TYPE));
				assertEquals(history.getIssueId(),
						result.getLong(HistoryEnum.ISSUE_ID));
				assertEquals(history.getNewValue(),
						result.getString(HistoryEnum.NEW_VALUE));
				assertEquals(history.getOldValue(),
						result.getString(HistoryEnum.OLD_VALUE));
				assertEquals(history.getModifiedTime(),
						result.getLong(HistoryEnum.MODIFIED_TIME));
				assertEquals(id, result.getLong(HistoryEnum.ID));
				assertEquals(history.getNewValue(),
						result.getString(HistoryEnum.NEW_VALUE));
			}
		} catch (SQLException e) {
			assertFalse(true);
		}
	}

	public void testGet() throws SQLException {
		long ISSUE_ID = 1;
		ArrayList<Long> idList = new ArrayList<Long>();

		for (int i = 0; i < 5; i++) {
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName(HistoryEnum.TABLE_NAME);
			valueSet.addInsertValue(HistoryEnum.ISSUE_ID, ISSUE_ID);
			valueSet.addInsertValue(HistoryEnum.ISSUE_TYPE,
					IssueTypeEnum.TYPE_STORY);
			valueSet.addInsertValue(HistoryEnum.HISTORY_TYPE,
					HistoryObject.TYPE_ESTIMATE);
			valueSet.addInsertValue(HistoryEnum.OLD_VALUE, i);
			valueSet.addInsertValue(HistoryEnum.NEW_VALUE, (i + 1));
			valueSet.addInsertValue(HistoryEnum.MODIFIED_TIME,
					System.currentTimeMillis());
			String query = valueSet.getInsertQuery();
			control.execute(query, true);

			String[] keys = control.getKeys();
			long id = Long.parseLong(keys[0]);

			idList.add(id);
		}

		for (int i = 0; i < 5; i++) {
			HistoryObject history = historyDao.get(idList.get(i));

			assertEquals(HistoryObject.TYPE_ESTIMATE, history.getHistoryType());
			assertEquals(IssueTypeEnum.TYPE_STORY, history.getIssueType());
			assertEquals(ISSUE_ID, history.getIssueId());
			assertEquals(String.valueOf((i + 1)), history.getNewValue());
			assertEquals(String.valueOf(i), history.getOldValue());
			assertEquals(idList.get(i).longValue(), history.getId());
		}
	}

	public void testGetHistoriesByIssue() throws SQLException {
		long ISSUE_ID = 1;

		for (int i = 0; i < 5; i++) {
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName(HistoryEnum.TABLE_NAME);
			valueSet.addInsertValue(HistoryEnum.ISSUE_ID, ISSUE_ID);
			valueSet.addInsertValue(HistoryEnum.ISSUE_TYPE,
					IssueTypeEnum.TYPE_STORY);
			valueSet.addInsertValue(HistoryEnum.HISTORY_TYPE,
					HistoryObject.TYPE_ESTIMATE);
			valueSet.addInsertValue(HistoryEnum.OLD_VALUE, i);
			valueSet.addInsertValue(HistoryEnum.NEW_VALUE, (i + 1));
			valueSet.addInsertValue(HistoryEnum.MODIFIED_TIME,
					System.currentTimeMillis());
			String query = valueSet.getInsertQuery();
			control.execute(query, true);
		}

		ArrayList<HistoryObject> histories = historyDao.getHistoriesByIssue(
				ISSUE_ID, IssueTypeEnum.TYPE_STORY);
		
		assertEquals(5, histories.size());

		for (int i = 0; i < 5; i++) {
			HistoryObject history = histories.get(i);
			assertEquals(HistoryObject.TYPE_ESTIMATE, history.getHistoryType());
			assertEquals(IssueTypeEnum.TYPE_STORY, history.getIssueType());
			assertEquals(ISSUE_ID, history.getIssueId());
			assertEquals(String.valueOf((i + 1)), history.getNewValue());
			assertEquals(String.valueOf(i), history.getOldValue());
		}

		// assert 不存在的 history
		histories = historyDao.getHistoriesByIssue(ISSUE_ID,
				IssueTypeEnum.TYPE_TASK);
		assertEquals(0, histories.size());
	}

	public void testDelete() throws SQLException {
		long ISSUE_ID = 1;
		ArrayList<Long> idList = new ArrayList<Long>();

		for (int i = 0; i < 5; i++) {
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName(HistoryEnum.TABLE_NAME);
			valueSet.addInsertValue(HistoryEnum.ISSUE_ID, ISSUE_ID);
			valueSet.addInsertValue(HistoryEnum.ISSUE_TYPE,
					IssueTypeEnum.TYPE_STORY);
			valueSet.addInsertValue(HistoryEnum.HISTORY_TYPE,
					HistoryObject.TYPE_ESTIMATE);
			valueSet.addInsertValue(HistoryEnum.OLD_VALUE, i);
			valueSet.addInsertValue(HistoryEnum.NEW_VALUE, (i + 1));
			valueSet.addInsertValue(HistoryEnum.MODIFIED_TIME,
					System.currentTimeMillis());
			String query = valueSet.getInsertQuery();
			control.execute(query, true);

			String[] keys = control.getKeys();
			long id = Long.parseLong(keys[0]);

			idList.add(id);
		}

		assertEquals(
				5,
				historyDao.getHistoriesByIssue(ISSUE_ID,
						IssueTypeEnum.TYPE_STORY).size());

		for (long id : idList) {
			historyDao.delete(id);
		}

		assertEquals(
				0,
				historyDao.getHistoriesByIssue(ISSUE_ID,
						IssueTypeEnum.TYPE_STORY).size());
	}

	public void testDeleteByIssue() throws SQLException {
		long ISSUE_ID = 1;
		ArrayList<Long> idList = new ArrayList<Long>();

		for (int i = 0; i < 5; i++) {
			IQueryValueSet valueSet = new MySQLQuerySet();
			valueSet.addTableName(HistoryEnum.TABLE_NAME);
			valueSet.addInsertValue(HistoryEnum.ISSUE_ID, ISSUE_ID);
			valueSet.addInsertValue(HistoryEnum.ISSUE_TYPE,
					IssueTypeEnum.TYPE_STORY);
			valueSet.addInsertValue(HistoryEnum.HISTORY_TYPE,
					HistoryObject.TYPE_ESTIMATE);
			valueSet.addInsertValue(HistoryEnum.OLD_VALUE, i);
			valueSet.addInsertValue(HistoryEnum.NEW_VALUE, (i + 1));
			valueSet.addInsertValue(HistoryEnum.MODIFIED_TIME,
					System.currentTimeMillis());
			String query = valueSet.getInsertQuery();
			control.execute(query, true);

			String[] keys = control.getKeys();
			long id = Long.parseLong(keys[0]);

			idList.add(id);
		}

		assertEquals(
				5,
				historyDao.getHistoriesByIssue(ISSUE_ID,
						IssueTypeEnum.TYPE_STORY).size());

		historyDao
				.deleteByIssue(ISSUE_ID, IssueTypeEnum.TYPE_STORY);

		assertEquals(
				0,
				historyDao.getHistoriesByIssue(ISSUE_ID,
						IssueTypeEnum.TYPE_STORY).size());
	}
}