package ca.sqlpower.matchmaker;

public class MySimpleIndex {
    String name;
    MySimpleTable parent;

    public MySimpleIndex(String name, MySimpleTable parent) {
        super();
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MySimpleTable getParent() {
        return parent;
    }

    public void setParent(MySimpleTable parent) {
        this.parent = parent;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
