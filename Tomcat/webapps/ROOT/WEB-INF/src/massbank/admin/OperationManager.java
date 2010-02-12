/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *******************************************************************************
 *
 * ユーザ操作管理クラス
 *
 * ver 1.0.1 2010.02.05
 *
 ******************************************************************************/
package massbank.admin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * ユーザ操作の管理を行うSingletonクラス
 * ページに対する操作を行えるユーザの制限を行う
 */
public class OperationManager {

    /** 管理対象ページ（Database Manager） */
    public final String P_MANAGER = "Manager";
    
    /** 管理対象ページ（Instrument Editor） */
    public final String P_INSTRUMENT = "Instrument";
    
    /** 管理対象ページ（Record List） */
    public final String P_RECORD = "Record";
    
    /** 管理対象ページ（Structure List） */
    public final String P_STRUCTURE = "Structure";
    
    /** 操作種別（更新系） */
    public final String TP_UPDATE = "Update";
    
    /** 操作種別（表示系） */
    public final String TP_VIEW = "View";
    
    /** 唯一のインスタンス */
    private static final OperationManager instance = new OperationManager();
    
    /** 操作管理用コレクション */
    private Set<String> oSet = Collections.synchronizedSet(new HashSet<String>());
    
    /**
     * コンストラクタ
     */
    private OperationManager() {
    }

    /**
     * このクラスの唯一のインスタンスを返す
     */
    public static OperationManager getInstance() {
        return instance;
    }
    
    /**
     * 操作開始
     * 対象ページの対象DBに対する更新系操作を1ユーザのみに制限する
     * この関数を呼び出した場合は操作終了後に必ずendOparation関数を呼び出すこと
     * @param page 対象ページ
     * @param type 操作種別
     * @param db 対象DB名
     * @return 操作開始結果
     */
    public boolean startOparation(String page, String type, String db) {
    	boolean isOperation = false;
    	String key = page + type;
    	if ( db != null ) {
    		key += db;
    	}
    	
    	// データベース管理関連ページのユーザ制限処理
    	//  ->データベースの更新に関する処理（データベース管理、装置情報、レコード、構造式）を
    	//    1つでも行っている場合は操作不可
    	if ( page.equals(P_MANAGER) ) {
    		isOperation = true;
    		Iterator<String> i = oSet.iterator();
    		while ( i.hasNext() ) { 
                String useKey = (String)i.next();
                if ( useKey.equals(P_MANAGER + TP_UPDATE) ||
                		useKey.indexOf(P_INSTRUMENT + TP_UPDATE) != -1 ||
                		useKey.indexOf(P_RECORD + TP_UPDATE) != -1 ||
                		useKey.indexOf(P_STRUCTURE + TP_UPDATE) != -1 ) {
                	
                	isOperation = false;
                	break;
                }
    		}
     		if ( isOperation && !type.equals(TP_VIEW)) {
        		// 制限したい操作の場合はキーを保持
     			oSet.add(key);
    		}    		
    	}
    	// 装置情報関連ページのユーザ制限処理
    	//  ->データベース管理、該当DBの装置情報、該当DBのレコードの
    	//    処理を行っている場合は操作不可
		else if ( page.equals(P_INSTRUMENT) ) {
     		if ( !oSet.contains(P_MANAGER + TP_UPDATE) &&
     				!oSet.contains(P_INSTRUMENT + TP_UPDATE + db) &&
    				!oSet.contains(P_RECORD + TP_UPDATE + db) ) {
    			isOperation = true;
    		}
     		if ( isOperation && !type.equals(TP_VIEW)) {
        		// 制限したい操作の場合はキーを保持
     			oSet.add(key);
    		}
    	}
    	// レコード関連ページのユーザ制限処理
    	//  ->データベース管理、該当DBの装置情報、該当DBのレコードの
    	//    処理を行っている場合は操作不可
    	else if ( page.equals(P_RECORD) ) {
     		if ( !oSet.contains(P_MANAGER + TP_UPDATE) &&
    				!oSet.contains(P_INSTRUMENT + TP_UPDATE + db) &&
    				!oSet.contains(P_RECORD + TP_UPDATE + db) ) {
    			isOperation = true;
    		}
     		if ( isOperation && !type.equals(TP_VIEW) ) {
        		// 制限したい操作の場合はキーを保持
     			oSet.add(key);
    		}
    	}
    	// 構造式関連ページのユーザ制限処理
    	//  ->データベース管理、該当DBの構造式の処理を行っている場合は操作不可
    	else if ( page.equals(P_STRUCTURE) ) {
    		if ( !oSet.contains(P_MANAGER + TP_UPDATE) &&
    				!oSet.contains(P_STRUCTURE + TP_UPDATE + db) ) {
    			isOperation = true;
    		}
    		if ( isOperation && !type.equals(TP_VIEW) ) {
        		// 制限したい操作の場合はキーを保持
    			oSet.add(key);
    		}
    	}
    	
    	return isOperation;
    }
    
    /**
     * 操作終了
     * 対象ページの対象DBに対する操作の制限を解除する
     * @param page 対象ページ
     * @param type 操作種別
     * @param db 対象DB名
     * @return 操作終了結果
     */
    public boolean endOparation(String page, String type, String db) {
    	String key = page + type;
    	if ( db != null ) {
    		key += db;
    	}

    	return oSet.remove(key);
    }
}
