package org.quil.server.Tasks;
import java.io.Serializable;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class ResultItem implements Serializable{
		
		
		public ResultItem(String _taskName, String _taskTag, int _index, String _id,
				String _field, String _stringValue, double _doubleValue,
				int _intValue) {
			
			this._taskName = _taskName;
			this._taskTag = _taskTag;
			this._index = _index;
			this._id = _id;
			this._field = _field;
			this._stringValue = _stringValue;
			this._doubleValue = _doubleValue;
			this._intValue = _intValue;
		}

		@QuerySqlField(index=true)
		protected String _taskName = "";
		
		public String getTaskName() {
			return _taskName;
		}
		
		@QuerySqlField
		protected String _taskTag = "";

		@QuerySqlField(index=true)
		protected String _id = "";

		@QuerySqlField
		protected int _index = 0;
		
		@QuerySqlField
		protected String _field = "";
		
		@QuerySqlField
		protected String _stringValue = "";
		
		@QuerySqlField
		protected double _doubleValue = 0.0;
		
		@QuerySqlField
		protected int _intValue = 0;
		
	}