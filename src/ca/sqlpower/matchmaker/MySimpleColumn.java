package ca.sqlpower.matchmaker;

public class MySimpleColumn {

    String name;
    int type;
    String typeName;
    
    MySimpleTable parent;
    
    public MySimpleColumn(String name) {
        super();
        this.name = name;
    }

    public MySimpleColumn(String name, int type) {
        this(name);
        this.type = type;
    }

    public MySimpleColumn(String name, MySimpleTable parent) {
        this(name);
        this.parent = parent;
    }

    public MySimpleColumn(String name, int type, String typeName, MySimpleTable parent) {
        this(name,parent);
        this.type = type;
        this.typeName = typeName;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    
}
