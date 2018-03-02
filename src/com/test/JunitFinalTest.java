/**
 * 2018-01-17
 * GUXUKAI
 */
package com.test;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.BeforeAll;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;

/**
 * @author GUXUKAI
 *
 */
public class JunitFinalTest {
	protected static ActiveRecordPlugin arp = null;
    protected static C3p0Plugin c3p0Plugin = null;
    @BeforeAll
    static void initAll(){
        if(c3p0Plugin==null){
            PropKit.use("config_develop_e.txt");
            c3p0Plugin = new C3p0Plugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password"));
            c3p0Plugin.start();
        }
        
        if (arp==null) {
            arp = new ActiveRecordPlugin(c3p0Plugin);
            // 打印sql语句
           // arp.setShowSql(true);
            // 数据库映射
            arp.start();
        }
        System.out.println("Begin...");
    }
    @AfterAll
    static void tearDownAll() {
    	//c3p0Plugin.stop();
    	//arp.stop();
    	System.out.println("End...");
    }
}
