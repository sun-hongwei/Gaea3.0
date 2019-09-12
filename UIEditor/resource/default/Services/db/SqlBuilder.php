<?php

require_once 'StringEncode.php';

class NotSupportDBType extends Exception{}

abstract class DBDefine{

    public static $ARRAY = "ARRAY";
    public static $TINYINT = "TINYINT";
    public static $SMALLINT = "SMALLINT";
    public static $INTEGER = "INTEGER";
    public static $BIGINT = "BIGINT";
    public static $BIT = "BIT";
    public static $BOOLEAN = "BOOLEAN";
    public static $FLOAT = "FLOAT";
    public static $REAL = "REAL";
    public static $DOUBLE = "DOUBLE";
    public static $NUMERIC = "NUMERIC";
    public static $DECIMAL = "DECIMAL";
    public static $CHAR = "CHAR";
    public static $VARCHAR = "VARCHAR";
    public static $LONGVARCHAR = "LONGVARCHAR";
    public static $DATE = "DATE";
    public static $TIME = "TIME";
    public static $TIMESTAMP = "TIMESTAMP";
    public static $BINARY = "BINARY";
    public static $LONGVARBINARY = "LONGVARBINARY";
    public static $VARBINARY = "VARBINARY";
    public static $BLOB = "BLOB";
    public static $CLOB = "CLOB";
    public static $ROWID = "ROWID";
    public static $NCHAR = "NCHAR";
    public static $NVARCHAR = "NVARCHAR";
    public static $LONGNVARCHAR = "LONGNVARCHAR";
    public static $NCLOB = "NCLOB";

    protected abstract function getFieldDefine($column);

    /*
    $tbSchema格式
    {
    tablename:"表名",
    column:[{
        name:"字段名称";
        type:"ARRAY,TINYINT,SMALLINT,INTEGER,BIGINT,BIT,BOOLEAN,FLOAT,REAL,DOUBLE,NUMERIC,DECIMAL,
            CHAR,VARCHAR,LONGVARCHAR,DATE,TIME,TIMESTAMP,BINARY,LONGVARBINARY,VARBINARY,BLOB,CLOB,ROWID,NCHAR,NVARCHAR,LONGNVARCHAR,NCLOB",
        size:20,
        allowNULL:false|true;
    }]
    }
    */
    protected function buildCreateSql($sqlBuilder, $tbSchema){
        $tablename = $sqlBuilder->gettablename();
        $sqlBuilder->sql = "create table " . $tablename . "(";
        $fieldDefs = NULL;
        foreach ($tbSchema["column"] as $column) {
            $fieldDef = $this->getFieldDefine($column);
            
            if ($fieldDefs == NULL)
                $fieldDefs = fieldDef;
            else{
                $fieldDefs .= "," . fieldDef;
            }
        }
        
        $primKeyDefs = NULL;
        foreach ($sqlBuilder->getPrimkeys() as $primKey) {
            if (empty($primKeyDefs))
                $primKeyDefs = $primKey;
            else {
                $primKeyDefs .= " , " . $primKey;
            }
        }
        
        $sqlBuilder->sql .= $fieldDefs . ",PRIMARY KEY (" . $primKeyDefs . "))";
    }
    
    protected function NULLDef($column){
        $value = isset($column["allowNULL"]) && !$column["allowNULL"] ? " not NULL " : "";
        return $value;
    }
}

class AccesDefine extends DBDefine{
    protected function getFieldDefine($column){
        $fieldName = "[". $column["name"] ."]";
        switch ($column.getType()) {
        case self::$ARRAY:
            return $fieldName . " memo " . $this->NULLDef($column);
        case self::$TINYINT:
            return $fieldName . " long " . $this->NULLDef($column);			
        case self::$SMALLINT:
            return $fieldName . " long " . $this->NULLDef($column);
        case self::$INTEGER:
            return $fieldName . " long " . $this->NULLDef($column);
        case self::$BIGINT:
            return $fieldName . " long " . $this->NULLDef($column);
        case self::$BIT:
            return $fieldName . " Bit " . $this->NULLDef($column);
        case self::$BOOLEAN:
            return $fieldName . " Bit " . $this->NULLDef($column);
        case self::$FLOAT:
            return $fieldName . " Float " . $this->NULLDef($column);
        case self::$REAL:
            return $fieldName . " Real " . $this->NULLDef($column);
        case self::$DOUBLE:
            return $fieldName . " Real " . $this->NULLDef($column);
        case self::$NUMERIC:
            return $fieldName . " Money " . $this->NULLDef($column);
        case self::$DECIMAL:
            return $fieldName . " Decimal " . $this->NULLDef($column);
        case self::$CHAR:
            return $fieldName . " Text(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$VARCHAR:
            return $fieldName . " Text(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGVARCHAR:
            return $fieldName . " memo " . $this->NULLDef($column);
        case self::$DATE:
            return $fieldName . " DateTime " . $this->NULLDef($column);
        case self::$TIME:
            return $fieldName . " DateTime " . $this->NULLDef($column);
        case self::$TIMESTAMP:
            return $fieldName . " text(255) " . $this->NULLDef($column);
        case self::$BINARY:
            return $fieldName . " memo " . $this->NULLDef($column);
        case self::$LONGVARBINARY:
            return $fieldName . " memo " . $this->NULLDef($column);
        case self::$VARBINARY:
            return $fieldName . " memo " . $this->NULLDef($column);
        case self::$BLOB:
            return $fieldName . " memo " . $this->NULLDef($column);
        case self::$CLOB:
            return $fieldName . " memo " . $this->NULLDef($column);
        case self::$ROWID:
            return $fieldName . " Text(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NCHAR:
            return $fieldName . " Text(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NVARCHAR:
            return $fieldName . " Text(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGNVARCHAR:
            return $fieldName . " Text(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NCLOB:
            return $fieldName . " memo " . $this->NULLDef($column);
        default:
            throw new NotSupportDBType();
        }
    }
    
}

class SqliteDefine extends DBDefine{ 
    protected function getFieldDefine($column){
        switch ($column.getType()) {
        case self::$ARRAY:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$TINYINT:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$SMALLINT:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$INTEGER:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$BIGINT:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$BIT:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$BOOLEAN:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$FLOAT:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$REAL:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$DOUBLE:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$NUMERIC:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$DECIMAL:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$CHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$VARCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGVARCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$DATE:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$TIME:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$TIMESTAMP:
            return $column["name"] . " TEXT(512) " . $this->NULLDef($column);
        case self::$BINARY:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$LONGVARBINARY:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$VARBINARY:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$BLOB:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$CLOB:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        case self::$ROWID:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        case self::$NCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NVARCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGNVARCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NCLOB:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        default:
            throw new NotSupportDBType();
        }
    }
    
}

class MySQLDefine extends DBDefine{ 
    protected function getFieldDefine($column){
        switch ($column.getType()) {
        case self::$ARRAY:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$TINYINT:
            return $column["name"] . " TINYINT " . $this->NULLDef($column);
        case self::$SMALLINT:
            return $column["name"] . " SMALLINT " . $this->NULLDef($column);
        case self::$INTEGER:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$BIGINT:
            return $column["name"] . " BIGINT " . $this->NULLDef($column);
        case self::$BIT:
            return $column["name"] . " TINYINT " . $this->NULLDef($column);
        case self::$BOOLEAN:
            return $column["name"] . " TINYINT " . $this->NULLDef($column);
        case self::$FLOAT:
            return $column["name"] . " FLOAT " . $this->NULLDef($column);
        case self::$REAL:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$DOUBLE:
            return $column["name"] . " DOUBLE " . $this->NULLDef($column);
        case self::$NUMERIC:
            return $column["name"] . " NUMERIC(" . $column["size"] . "," . $column.getScale() . ") " . $this->NULLDef($column);
        case self::$DECIMAL:
            return $column["name"] . " DECIMAL " . $this->NULLDef($column);
        case self::$CHAR:
            return $column["name"] . " CHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$VARCHAR:
            return $column["name"] . " VARCHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGVARCHAR:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        case self::$DATE:
            return $column["name"] . " DATETIME " . $this->NULLDef($column);
        case self::$TIME:
            return $column["name"] . " TIME " . $this->NULLDef($column);
        case self::$TIMESTAMP:
            return $column["name"] . " TIMESTAMP " . $this->NULLDef($column);
        case self::$BINARY:
            return $column["name"] . " CHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGVARBINARY:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$VARBINARY:
            return $column["name"] . " VARCHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$BLOB:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$CLOB:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        case self::$ROWID:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        case self::$NCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NVARCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGNVARCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NCLOB:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        default:
            throw new NotSupportDBType();
        }
    }
    
}

class MSSQLDefine extends DBDefine{ 
    protected function getFieldDefine($column){
        switch ($column.getType()) {
        case self::$ARRAY:
            return $column["name"] . " image " . $this->NULLDef($column);
        case self::$TINYINT:
            return $column["name"] . " TINYINT " . $this->NULLDef($column);
        case self::$SMALLINT:
            return $column["name"] . " SMALLINT " . $this->NULLDef($column);
        case self::$INTEGER:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$BIGINT:
            return $column["name"] . " BIGINT " . $this->NULLDef($column);
        case self::$BIT:
            return $column["name"] . " bit " . $this->NULLDef($column);
        case self::$BOOLEAN:
            return $column["name"] . " bit " . $this->NULLDef($column);
        case self::$FLOAT:
            return $column["name"] . " FLOAT " . $this->NULLDef($column);
        case self::$REAL:
            return $column["name"] . " real " . $this->NULLDef($column);
        case self::$DOUBLE:
            return $column["name"] . " DOUBLE " . $this->NULLDef($column);
        case self::$NUMERIC:
            return $column["name"] . " NUMERIC(" . $column["size"] . "," . $column.getScale() . ") " . $this->NULLDef($column);
        case self::$DECIMAL:
            return $column["name"] . " decimal(" . $column["size"] . "," . $column.getScale() . ") " . $this->NULLDef($column);
        case self::$CHAR:
            return $column["name"] . " CHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$VARCHAR:
            return $column["name"] . " VARCHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGVARCHAR:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        case self::$DATE:
            return $column["name"] . " DATETIME " . $this->NULLDef($column);
        case self::$TIME:
            return $column["name"] . " DATETIME " . $this->NULLDef($column);
        case self::$TIMESTAMP:
            return $column["name"] . " timestamp " . $this->NULLDef($column);
        case self::$BINARY:
            return $column["name"] . " binary(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGVARBINARY:
            return $column["name"] . " image " . $this->NULLDef($column);
        case self::$VARBINARY:
            return $column["name"] . " varbinary(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$BLOB:
            return $column["name"] . " image " . $this->NULLDef($column);
        case self::$CLOB:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        case self::$ROWID:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        case self::$NCHAR:
            return $column["name"] . " NCHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NVARCHAR:
            return $column["name"] . " NVARCHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGNVARCHAR:
            return $column["name"] . " TEXT(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NCLOB:
            return $column["name"] . " TEXT " . $this->NULLDef($column);
        default:
            throw new NotSupportDBType();
        }
    }
    
    protected function buildCreateSql($sqlBuilder, $tableSchema){
        $tablename = $tableSchema["tablename"];
        $this->sql = "create table " . $tablename . "(";
        $fieldDefs = NULL;
        foreach ($tableSchema["column"] as $column) {
            $fieldDef = getFieldDefine($column);
            
            if ($fieldDefs == NULL)
                $fieldDefs = $fieldDef;
            else{
                $fieldDefs .= "," . $fieldDef;
            }
        }
        
        $primKeyDefs = NULL;
        foreach ($sqlBuilder->getPrimkeys() as $primKey) {
            if (empty($primKeyDefs))
                $primKeyDefs = $primKey;
            else {
                $primKeyDefs .= " , " . $primKey;
            }
        }
        
        $sqlBuilder->sql .= $fieldDefs . ",constraint PK_" . $tablename . " primary key (" . $primKeyDefs . "))";
    }
}

class OracleDefine extends MSSQLDefine{ 
    protected function getFieldDefine($column){
        switch ($column.getType()) {
        case self::$ARRAY:
            return $column["name"] . " image " . $this->NULLDef($column);
        case self::$TINYINT:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$SMALLINT:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$INTEGER:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$BIGINT:
            return $column["name"] . " NUMERIC(20) " . $this->NULLDef($column);
        case self::$BIT:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$BOOLEAN:
            return $column["name"] . " INTEGER " . $this->NULLDef($column);
        case self::$FLOAT:
            return $column["name"] . " FLOAT " . $this->NULLDef($column);
        case self::$REAL:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$DOUBLE:
            return $column["name"] . " REAL " . $this->NULLDef($column);
        case self::$NUMERIC:
            return $column["name"] . " NUMERIC(" . $column["size"] . "," . $column.getScale() . ") " . $this->NULLDef($column);
        case self::$DECIMAL:
            return $column["name"] . " decimal(" . $column["size"] . "," . $column.getScale() . ") " . $this->NULLDef($column);
        case self::$CHAR:
            return $column["name"] . " CHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$VARCHAR:
            return $column["name"] . " VARCHAR2(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGVARCHAR:
            return $column["name"] . " LONG " . $this->NULLDef($column);
        case self::$DATE:
            return $column["name"] . " DATE " . $this->NULLDef($column);
        case self::$TIME:
            return $column["name"] . " timestamp " . $this->NULLDef($column);
        case self::$TIMESTAMP:
            return $column["name"] . " timestamp " . $this->NULLDef($column);
        case self::$BINARY:
            return $column["name"] . " RAW(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGVARBINARY:
            return $column["name"] . " LONG " . $this->NULLDef($column);
        case self::$VARBINARY:
            return $column["name"] . " VARCHAR2(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$BLOB:
            return $column["name"] . " BLOB " . $this->NULLDef($column);
        case self::$CLOB:
            return $column["name"] . " CLOB " . $this->NULLDef($column);
        case self::$ROWID:
            return $column["name"] . " ROWID " . $this->NULLDef($column);
        case self::$NCHAR:
            return $column["name"] . " NCHAR(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$NVARCHAR:
            return $column["name"] . " NVARCHAR2(" . $column["size"] . ") " . $this->NULLDef($column);
        case self::$LONGNVARCHAR:
            return $column["name"] . " LONG " . $this->NULLDef($column);
        case self::$NCLOB:
            return $column["name"] . " NCLOB " . $this->NULLDef($column);
        default:
            throw new NotSupportDBType();
        }
    }
    
}

class SQLBuilder{

	public static $Default_Date_Format = "Y-m-d h:i:s";

    public static $dbAccessFile = "dbAccessFile";
    public static $dbAccessSource = "dbAccessSource";
    public static $dbMSSQL = "dbMSSQL";
    public static $dbSqLite = "dbSqLite";
    public static $dbSybase = "dbSybase";
    public static $dbMySQL = "dbMySQL";
    public static $dbInformix = "dbInformix";
    public static $dbPostSQL = "dbPostSQL";
    public static $dbOracle = "dbOracle";
    public static $dbDB2 = "dbDB2";

    public static $otAnd = "otAnd";
    public static $otOr = "otOr";
    public static $otNot = "otNot";
    public static $otLeftPair = "otLeftPair";
    public static $otRightPair = "otRightPair";

    public static $otIn = "otIn";
    public static $otEqual = "otEqual";
    public static $otGreater = "otGreater";
    public static $otLess = "otLess";
    public static $otGreaterAndEqual = "otGreaterAndEqual";
    public static $otLessAndEqual = "otLessAndEqual";
    public static $otBetween = "otBetween";
    public static $otLike = "otLike";
    public static $otUnequal = "otUnequal";

    public static $stQuery = "stQuery";
    public static $stDelete = "stDelete";
    public static $stInsert = "stInsert";
    public static $stPageQuery = "stPageQuery";
    public static $stUpdate = "stUpdate";
    public static $stDrop = "stDrop";
    public static $stCreate = "stCreate";
   
	protected $version = 12;
	protected $dbType = NULL;
	protected $sqlType = NULL;
	protected $tablename = NULL;
	protected $where = NULL;
	protected $having = NULL;
	protected $group = NULL;
	protected $order = NULL;
	protected $set = NULL;
	protected $values = NULL;
	
	protected $pageOderKeys = array();
	protected $pagePos = -1;
	protected $pageSize = -1;
	
	protected $tables = array();
	protected $fields = array();
	
    protected $sql = NULL;

    public function __construct($dbType) 
    {
		$this->setDBType($dbType);
		$this->setSqlType(self::$stQuery);
    }

	public function setDBType($dbType){
		$this->dbType = $dbType;
	}

	public function setSqlType($sqlType){
		$this->sqlType = $sqlType;
	}

	public function getDBType(){
		return $this->dbType;
	}

	public function getSqlType(){
		return $this->sqlType;
	}

    protected function formatDate($dt, $format){
		return date_format($dt, $format);
	}
	
	public $encodeName = "UTF-8";

	public function convertStringCode($data){
        return convertStringCode($data, $this->encodeName);
    }

	protected function getPageorderString(){
		if (empty($this->pageOderKeys))
			throw new Exception("未提供有效的分页关键字！");
		
		$tmp = NULL;
		foreach ($this->pageOderKeys as $this->order) {
			if (empty($tmp))
				$tmp = $this->convertStringCode($this->order);
			else
				$tmp .= "," . $this->convertStringCode($this->order);
		}
		
		return $tmp;
	}
	
	protected function getTableString(){
		if (empty($this->tables))
			throw new Exception("未提供有效的表名！");
		
		$tmp = NULL;
		foreach ($this->tables as $table) {
			if (empty($tmp))
				$tmp = $this->convertStringCode($table);
			else
				$tmp .= "," . $this->convertStringCode($table);
		}
		
		return $tmp;
	}
	
	public function getFieldsString(){
		if (empty($this->fields))
			return "*";
		
		$tmp = NULL;
		foreach ($this->fields as $field) {
			if (empty($tmp))
				$tmp = $this->convertStringCode($field);
			else
				$tmp .= "," . $this->convertStringCode($field);
		}
		
		return $tmp;
	}
	
	protected function addwhereString() {
		if (!empty($this->where))
			$this->sql .= " where " . $this->convertStringCode($this->where);
	}
	
	protected function addhavingString() {
		if (!empty($this->having))
			$this->sql .= " having " . $this->convertStringCode($this->having);
	}
	
	protected function addgrouptString() {
		if (!empty($this->group))
			$this->sql .=" group by " . $this->convertStringCode($this->group);
	}
	
	protected function addSortString() {
		if (!empty($this->order))
			$this->sql .=" order by " . $this->convertStringCode($this->order);
	}
	
	protected function addsetString() {
		if (!empty($this->set))
			$this->sql .=" set " . $this->convertStringCode($this->set);
	}
	
	protected function getDateExpr($dt){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
			return "#" . $this->formatDate($dt, self::$Default_Date_Format) . "#";
		case self::$dbMSSQL:
		case self::$dbSqLite:
		case self::$dbSybase:
		case self::$dbMySQL:
		case self::$dbInformix:
		case self::$dbPostSQL:
			return "'" . $this->formatDate($dt, self::$Default_Date_Format) . "'";
		case self::$dbOracle:
			return "to_date('" . $this->formatDate($dt, self::$Default_Date_Format) . "','yyyy-mm-dd hh24:mi:ss')";
		case self::$dbDB2:
			return "'" . $this->formatDate($dt, "Y-m-d h.i.s") . "'";
		default:
			throw new NotSupportDBType();
		}
	}
	
	protected function getSplitString(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
			return "\"";
		case self::$dbMSSQL:
		case self::$dbSqLite:
		case self::$dbSybase:
		case self::$dbOracle:
		case self::$dbDB2:
		case self::$dbMySQL:
		case self::$dbInformix:
		case self::$dbPostSQL:
			return "'";
		default:
			throw new NotSupportDBType();
		}
	}
	
	protected function buildQueryPageSql(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbPostSQL:
			$this->sql = "select " . $this->getFieldsString() . " from " . $this->getTableString();
			$this->addwhereString();
			$this->addgrouptString();
			$this->addhavingString();
			$this->addSortString();
			break;
		case self::$dbSqLite:
			$this->sql = "select " . $this->getFieldsString() . " from " . $this->getTableString();
			$this->addwhereString();
			$this->addgrouptString();
			$this->addhavingString();
			$this->addSortString();
			$this->sql .= " limit " . $this->pageSize . " offset " . ($this->pagePos * $this->pageSize);
			break;
		case self::$dbMySQL:
			$this->sql = "select " . $this->getFieldsString() . " from " . $this->getTableString();
			$this->addwhereString();
			$this->addgrouptString();
			$this->addhavingString();
			$this->addSortString();
			$this->sql .= " limit " . ($this->pagePos * $this->pageSize) . "," . ($this->pageSize);
			break;
		case self::$dbMSSQL:
			if ($this->version < 12){
				$this->sql = "select " . $this->getFieldsString() . ",ROW_NUMBER() over(order by " . $this->getPageorderString() . ") as 'rowNumber' from " . $this->getTableString();
				$this->addwhereString();
				$this->addgrouptString();
				$this->addhavingString();
				//$this->addSortString();
				$this->sql = "select * from (" . $this->sql . ") as temp where rowNumber between " . ($this->pagePos * $this->pageSize + 1) . " and " . (($this->pagePos + 1) * $this->pageSize);	
			}else{
				$this->sql = "select " . $this->getFieldsString() . " from " . $this->getTableString();
				$this->addwhereString();
				$this->addgrouptString();
				$this->addhavingString();
				//$this->addSortString();
				$this->sql .= " order by " . $this->getPageorderString() . " offset " . ($this->pagePos * $this->pageSize) . " row fetch next " . $this->pageSize . " row only";
			}
			break;
		case self::$dbDB2: 
		case self::$dbSybase:
			$this->sql = "select " . $this->getFieldsString() . ",ROW_NUMBER() over(order by " . $this->getPageorderString() . ") as 'rowNumber' from " . $this->getTableString();
			$this->addwhereString();
			$this->addgrouptString();
			$this->addhavingString();
			$this->addSortString();
			$this->sql = "select * from (" . $this->sql . ") as temp where rowNumber between " . ($this->pagePos * $this->pageSize + 1) . " and " . (($this->pagePos + 1) * $this->pageSize);
			break;
		case self::$dbInformix:
			$this->sql = "select SKIP " . ($this->pagePos * $this->pageSize) . " FIRST " . ($this->pageSize) . " " . $this->getFieldsString() . " from " . $this->getTableString();
			$this->addwhereString();
			$this->addgrouptString();
			$this->addhavingString();
			$this->addSortString();
			break;
		case self::$dbOracle:
			$this->sql = "select " . $this->getFieldsString() . ",ROWNUM AS rowNumber from " . $this->getTableString();
			$this->addwhereString();
			$this->addgrouptString();
			$this->addhavingString();
			$this->addSortString();
			$this->sql = "select * from (" . $this->sql . ") temp where temp.rowNumber between " . ($this->pagePos * $this->pageSize + 1) . 
					" and " . (($this->pagePos + 1) * $this->pageSize);
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected function buildQueryCountSql(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbPostSQL:
		case self::$dbSqLite:
		case self::$dbMySQL:
		case self::$dbDB2:
		case self::$dbMSSQL:
		case self::$dbSybase:
		case self::$dbInformix:
		case self::$dbOracle:
			$this->sql = "select count(*) from " . $this->getTableString();
			$this->addwhereString();
			$this->addgrouptString();
			$this->addhavingString();
			$this->addSortString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected function buildQuerySql(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbPostSQL:
		case self::$dbSqLite:
		case self::$dbMySQL:
		case self::$dbDB2:
		case self::$dbMSSQL:
		case self::$dbSybase:
		case self::$dbInformix:
		case self::$dbOracle:
			$this->sql = "select " . $this->getFieldsString() . " from " . $this->getTableString();
			$this->addwhereString();
			$this->addgrouptString();
			$this->addhavingString();
			$this->addSortString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected function buildUpdateSql(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbPostSQL:
		case self::$dbSqLite:
		case self::$dbMySQL:
		case self::$dbDB2:
		case self::$dbMSSQL:
		case self::$dbSybase:
		case self::$dbInformix:
		case self::$dbOracle:
			$this->sql = "update " . $this->tables[0];
			$this->addsetString();
			$this->addwhereString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected function buildInsertSql(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbPostSQL:
		case self::$dbSqLite:
		case self::$dbMySQL:
		case self::$dbDB2:
		case self::$dbMSSQL:
		case self::$dbSybase:
		case self::$dbInformix:
		case self::$dbOracle:
			$fields = $this->getFieldsString();
			$fields = $fields == "*" ? "" : "(" . $fields . ")";
			$this->sql = "insert into " . $this->tables[0] . $fields . " values(" . $this->convertStringCode($this->values) . ")";
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected function buildDeleteSql(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbPostSQL:
		case self::$dbSqLite:
		case self::$dbMySQL:
		case self::$dbDB2:
		case self::$dbMSSQL:
		case self::$dbSybase:
		case self::$dbInformix:
		case self::$dbOracle:
			$this->sql = "delete from " . $this->tables[0];
			$this->addwhereString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 

	public function getLikeSymbolString(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbMSSQL:
		case self::$dbSqLite:
		case self::$dbSybase:
		case self::$dbOracle:
		case self::$dbDB2:
		case self::$dbMySQL:
		case self::$dbInformix:
		case self::$dbPostSQL:
			return "%";
		default:
			throw new NotSupportDBType();
		}
	}
	
	public function getUnEqualSymbolString(){
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbMySQL:
		case self::$dbMSSQL:
		case self::$dbSqLite:
		case self::$dbSybase:
		case self::$dbOracle:
		case self::$dbDB2:
		case self::$dbInformix:
		case self::$dbPostSQL:
			return "<>";
		default:
			throw new NotSupportDBType();
		}
	}
    
	public function getFieldValue($value){
		if (is_string($value)){
            $dt = date_create_from_format("Y-m-d h:i:s",$value);
            if ($dt !== false) { 
                return $this->getDateExpr($dt);
            }
			switch ($this->dbType) {
			case self::$dbAccessFile:
			case self::$dbAccessSource:
				return (empty($value) || strtoupper($value) == "NULL" || strtolower($value) == "undefined") ? "" : ("'" . $this->convertStringCode($value) . "'");
			default:
				return (empty($value) || strtoupper($value) == "NULL" || strtolower($value) == "undefined") ? "null" : ("'" . $this->convertStringCode($value) . "'");
			}
		}else if (is_bool($value)){
			switch ($this->dbType) {
			case self::$dbMySQL:
			case self::$dbSqLite:
			case self::$dbMSSQL:
			case self::$dbAccessFile:
			case self::$dbAccessSource:
			case self::$dbSybase:
			case self::$dbOracle:
			case self::$dbDB2:
			case self::$dbInformix:
			case self::$dbPostSQL:
				return ($value)?"1":"0";
			default:
				throw new NotSupportDBType();
			}
		}
		
		return empty($value) ? "''" : $value;
	}
	
	public function setVersion($version){
		$this->version = $version;
	}

	public function addLogicalOperation($logicalOperation){
		switch ($logicalOperation) {
		case self::$otAnd:
			$this->where .= " and ";
			break;
		case self::$otOr:
			$this->where .= " or ";
			break;
		case self::$otNot:
			$this->where .= " not ";
			break;
		case self::$otLeftPair:
			$this->where .= " ( ";
			break;
		case self::$otRightPair:
			$this->where .= " ) ";
			break;
		default:
			break;
		}
	}
	
	public function addTable($tablename){
		$this->tables[] = $tablename;
	}
	
	public function addField($fieldName){
		$this->fields[] = $fieldName;
	}
	
	public function addwhere($field, $operation, $values){
		if (is_string($values))
			$values = array($values);

		switch ($operation) {
		case self::$otIn:
			$this->where .= " " . $field . " in (" . $values[0] . ")";
			break;
		case self::$otEqual:
			$this->where .= " " . $field . " = " . $this->getFieldValue($values[0]);
			break;
		case self::$otGreater:
			$this->where .= " " . $field . " > " . $this->getFieldValue($values[0]);
			break;
		case self::$otLess:
			$this->where .= " " . $field . " < " . $this->getFieldValue($values[0]);
			break;
		case self::$otGreaterAndEqual:
			$this->where .= " " . $field . " >= " . $this->getFieldValue($values[0]);
			break;
		case self::$otLessAndEqual:
			$this->where .= " " . $field . " <= " . $this->getFieldValue($values[0]);
			break;
		case self::$otBetween:
			$this->where .= " " . $field . " between " . $this->getFieldValue($values[0]) . " and " . $this->getFieldValue($values[1]);
			break;
		case self::$otLike:
			$this->where .= " " . $field . " like " . $this->getSplitString() . $this->getFieldValue($values[0]) . $this->getSplitString();
			break;
		case self::$otUnequal:
			$this->where .= " " . $field . " " . $this->getUnEqualSymbolString() . " " . $this->getFieldValue($values[0]);
			break;
		default:
			throw new NotSupportDBType();
		}
	}

	public function addgroup($field){
		if (empty($this->group))
			$this->group = $field;
		else
			$this->group .= "," . $field;
	}

	public function addSort($field){
		if (empty($this->order))
			$this->order = $field;
		else
			$this->order .= "," . $field;
	}

	public function addhaving($field){
		if (empty($this->having))
			$this->having = $field;
		else
			$this->having .= "," . $field;
	}

	public function addset($field, $value){
		if (empty($this->set))
			$this->set = " " . $field . " = " . $this->getFieldValue($value);
		else
			$this->set .= " , " . $field . " = " . $this->getFieldValue($value);
	}

	public function addValue($value){
		if (empty($value)){
			$value = "NULL";
		}
		
		$newValue = $this->getFieldValue($value);
		if (empty($this->values))
			$this->values = $newValue;
		else
			$this->values .= "," . $newValue;
	}

	public function setValue($values){
		$this->values = NULL;
		if (empty($values)){
			$value = "NULL";
			$this->addValue($value);
		}else if (is_array($values)){
			foreach($values as $value){
				$this->addValue($value);
			}			
		}
	}

    public function getSql($tbSchema = NULL){
		switch ($this->sqlType) {
		case self::$stQuery:
			$this->buildQuerySql();
			return $this->sql;
		case self::$stDelete:
            $this->buildDeleteSql();
			return $this->sql;
		case self::$stInsert:
            $this->buildInsertSql();
			return $this->sql;
		case self::$stPageQuery:
            $this->buildQueryPageSql();
			return $this->sql;
		case self::$stUpdate:
            $this->buildUpdateSql();
			return $this->sql;
		case self::$stDrop:
            $this->buildDropSql();
			return $this->sql;
		case self::$stCreate:
			$this->buildCreateSql();
			return $this->sql;
		default:
			throw new NotSupportDBType();
		}
	}

	private function buildDropSql() {
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbPostSQL:
		case self::$dbSqLite:
		case self::$dbMySQL:
		case self::$dbDB2: 
		case self::$dbMSSQL:
		case self::$dbSybase:
		case self::$dbInformix:
		case self::$dbOracle:
			$this->sql = "drop table " . $this->gettablename();
			break;
		default:
			throw new NotSupportDBType();
		}
	}
	
	private function buildCreateSql($tbSchema) {
		$this->sql = "";
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
			new AccesDefine().buildCreateSql($this, $tbSchema);
			break;
		case self::$dbSqLite:
			new SqliteDefine().buildCreateSql($this, $tbSchema);
			break;
		case self::$dbPostSQL:
		case self::$dbMySQL:
			new MySQLDefine().buildCreateSql($this, $tbSchema);
			break;
		case self::$dbDB2: 
		case self::$dbMSSQL:
		case self::$dbSybase:
		case self::$dbInformix:
			new MSSQLDefine().buildCreateSql($this, $tbSchema);
			break;
		case self::$dbOracle:
			new OracleDefine().buildCreateSql($this, $tbSchema);
			break;
		default:
			throw new NotSupportDBType();
		}		
	}

	public function getwhere() {
		return $this->where;
	}

	public function getgroup() {
		return $this->group;
	}

	public function getSort() {
		return $this->order;
	}

	public function gethaving() {
		return $this->having;
	}

	public function getvalues() {
		return $this->values;
	}

	public function getFields() {
		return $this->fields;
	}

	public function getsets() {
		return $this->set;
	}

	public function getPageorders() {
		return $this->pageOderKeys;
	}

	public function getCatalog() {
		return db.getCatalog();
	}

	public function getSchema() {
		return db.getSchema();
	}

	public function gettablename() {
		return count($this->tables) == 0 ? "" : $this->tables[0];
	}

	public function getpagePos(){
		return $this->pagePos;
	}
	
	public function getpageSize(){
		return $this->pageSize;
	}

	public function setRawwhere($value){
		$this->where = $value;
	}
	
	public function setRawhaving($value){
		$this->having = $value;
	}
	
	public function setRawgroup($value){
		$this->group = $value;
	}
	
	public function setRaworder($value){
		$this->order = $value;
	}
	
	public function setRawset($value){
		$this->set = $value;
	}
	
	public function setRawvalues($value){
		$this->values = $value;
	}
	
	public function setpagePos($value){
		$this->pagePos = $value;
	}

    public function setpageSize($value){
		$this->pageSize = $value;
	}
	
	public function setRawFields($value){
		if (is_array($value))
			$this->fields = $value;
		else
			$this->fields = array($value);
	}

	public function addPageorderField($field) {
		$this->pageOderKeys[] = ($field);
	}

	public function getBlobInsertPlaceholder() {
		switch ($this->dbType) {
		case self::$dbOracle:
			return "empty_clob()";
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbMSSQL:
		case self::$dbSqLite:
		case self::$dbSybase:
		case self::$dbDB2:
		case self::$dbMySQL:
		case self::$dbInformix:
		case self::$dbPostSQL:
			return "NULL";
		default:
			throw new NotSupportDBType();
		}
	}

	public function getPreparedUpdateSql($fields) {
		switch ($this->dbType) {
		case self::$dbAccessFile:
		case self::$dbAccessSource:
		case self::$dbPostSQL:
		case self::$dbSqLite:
		case self::$dbMySQL:
		case self::$dbDB2:
		case self::$dbMSSQL:
		case self::$dbSybase:
		case self::$dbInformix:
		case self::$dbOracle:
			$this->sql = "update " . $this->tables[0] ;
			$this->setValue = NULL;
			for ($i = 0; $i < count($fields); $i++) {
				if (empty($this->setValue))
					$this->setValue = $fields[i] . " = ?";
				else
					$this->setValue .= " , " . $fields[i] . " = ?";
			}
			$this->addwhereString();
			break;
		default:
			throw new NotSupportDBType();
		}
		return $this->sql;
	}
		
}

?>