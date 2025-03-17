package utilities;

import java.util.HashMap;

public class RuntimeStack {

    private HashMap<String, Object> stack;
    private RuntimeStack parent;

    public RuntimeStack() {
        stack = new HashMap<String,Object>();
        parent = null;
    }

    public RuntimeStack(RuntimeStack parent) {
        this();
        this.parent = parent;
    }

    public void addValue(String name, Object value) { stack.put(name,value); }

    public void setValue(String name, Object value) {
        if(stack.containsKey(name)) { stack.put(name,value); }
        else if(parent != null) { parent.setValue(name,value); }
    }

    public Object getValue(String name) {
        if(stack.containsKey(name)) { return stack.get(name); }
        else if(parent != null) { return parent.getValue(name); }
        else { return null; }
    }

    public RuntimeStack createCallFrame() { return new RuntimeStack(this);}
    public RuntimeStack destroyCallFrame() { return this.parent; }

}
