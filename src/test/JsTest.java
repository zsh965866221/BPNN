package test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Created by zsh96 on 2016/10/15.
 */
public class JsTest {

    public static void main(String[] args){
        ScriptEngineManager manager=new ScriptEngineManager();
        ScriptEngine engine=manager.getEngineByName("javascript");
        engine.put("a",4);
        engine.put("b",2);
        try {
            engine.eval("import Math");
            double result=(double)engine.eval("sin(a+b)");
            System.out.println(result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }



}