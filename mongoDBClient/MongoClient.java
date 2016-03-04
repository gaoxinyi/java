package com.rubicware.util;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoClient {
	
	private DB db;
	private Mongo mongo;
	/**
	 * Client 构造器
	 * @param server	服务器IP地址
	 * @param db		数据库名称
	 */
	public MongoClient(String server,String db){
		try {
			this.mongo = new Mongo(server,27021);
			this.db = mongo.getDB(db);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 获取表的sequence
	 * @param coll	表
	 * @return		该数据库表的sequence
	 */
	public Long sequence(String coll){
		DBCollection collection = db.getCollection("coll_seq");
		DBObject update = new BasicDBObject();
		update.put("$inc", new BasicDBObject("seq",1));
		try{
			return (Long)collection.findAndModify(new BasicDBObject("coll",coll), update).get("seq");
		}catch(NullPointerException e){
			DBObject data = new BasicDBObject();
			data.put("coll", coll);
			data.put("seq", 2L);
			collection.insert(data);
			return 1L;
		}
	}
	/**
	 * 解码数据(转化一些Mongodb不支持的类型)
	 * @param data	原始数据
	 * @return		转化后的数据
	 */
	private BasicDBObject decode(Map<String,Object> data){
		BasicDBObject odb = new BasicDBObject();
		for(String key:data.keySet()){
			Object value = data.get(key);
			if(value instanceof BigDecimal){
				value = ((BigDecimal)value).doubleValue();
			}else if(value instanceof Map){
				DBObject sub = new BasicDBObject();
				for(String sub_key:((Map<String,Object>) value).keySet()){
					Object sub_value = ((Map) value).get(sub_key);
					if(sub_value instanceof Map){
						sub.put(sub_key, decode((Map<String, Object>) ((Map) value).get(sub_key)));
					}else{
						sub.put(sub_key, sub_value);
					}
				}
				value = sub;
			}
			odb.put(key, value);
		}
		return odb;
	}
	/**
	 * 插入数据
	 * @param coll	表名
	 * @param data	数据
	 */
	public void insert(String coll,Map<String,Object> data){
		DBCollection collection = db.getCollection(coll);
		BasicDBObject odb = this.decode(data);
		odb.put("_id", this.sequence(coll));
		collection.insert(odb);
	}
	/**
	 * 修改所有符合条件的数据
	 * @param coll	表名
	 * @param query	查询条件
	 * @param data	修改的数据
	 */
	public void update(String coll,Map<String,Object> query,Map<String,Object> data){
		DBCollection collection = db.getCollection(coll);
		collection.updateMulti(this.decode(query), this.decode(data));
	}
	/**
	 * 根据ID修改数据
	 * @param coll	表名
	 * @param data	修改的数据
	 * @param id	修改数据的ID(唯一)
	 */
	public void update(String coll,Map<String,Object> data,Long id){
		DBCollection collection = db.getCollection(coll);
		collection.update(new BasicDBObject("_id",id), this.decode(data));
	}
	/**
	 * 根据查询条件修改数据，如果查询条件没有找到数据则把修改的内容新增到数据表中
	 * @param coll	表名
	 * @param query	查询条件
	 * @param data	修改的数据
	 */
	public void save(String coll,Map<String,Object> query,Map<String,Object> data){
		DBCollection collection = db.getCollection(coll);
		BasicDBObject odb = this.decode(data);
//		odb.put("_id", this.sequence(coll));
		collection.update(this.decode(query), odb, Boolean.TRUE, Boolean.FALSE);
	}
	/**
	 * 删除数据(暂不开放)
	 * @param coll	表名
	 * @param query	查询条件
	 */
//	public void delete(String coll,Map<String,Object> query){
//		DBCollection collection = db.getCollection(coll);
//		collection.remove(this.decode(query));
//	}
	/**
	 * 查询所有数据
	 * @param coll	表名
	 * @param query	查询条件
	 * @return	查询结果集
	 */
	public List<Map> search(String coll,Map<String,Object> query){
		DBCollection collection = db.getCollection(coll);
		DBCursor cursor = collection.find(this.decode(query));
		List<Map> res = new ArrayList();
		while(cursor.hasNext()){
			res.add((Map)cursor.next());
		}
		return res;
	}
	/**
	 * 关闭数据库连接
	 */
	public void close(){
		mongo.close();
	}
}
